#!/usr/bin/env python3
"""
LeadNews schedule service — staging peak rehearsal (Path A).

Mixed schedule + random cancel traffic, then reconcile taskinfo_logs against
intended cancel flags from the run log.

Prerequisites:
  - leadnews-schedule running (default http://127.0.0.1:51701)
  - MySQL leadnews_schedule + Redis as in application.yml
  - Optional: leadnews-wemedia for end-to-end article publish (not required
    for schedule-layer reconciliation)

Usage:
  pip install -r requirements.txt
  copy config.example.yaml config.yaml   # edit credentials
  python run_peak_rehearsal.py
  python run_peak_rehearsal.py --reconcile-only output/run_20260101_120000
"""

from __future__ import annotations

import argparse
import csv
import json
import random
import sys
import threading
import time
import uuid
from dataclasses import dataclass, field
from datetime import datetime, timezone
from pathlib import Path
from typing import Any, Optional

import requests
import yaml

try:
    import pymysql
except ImportError:  # pragma: no cover
    pymysql = None

# taskinfo_logs.status (ScheduleConstants)
STATUS_SCHEDULED = 0
STATUS_EXECUTED = 1
STATUS_CANCELLED = 2

STATUS_LABEL = {
    STATUS_SCHEDULED: "SCHEDULED",
    STATUS_EXECUTED: "EXECUTED",
    STATUS_CANCELLED: "CANCELLED",
}


@dataclass
class TaskRecord:
    task_id: int
    execute_time_ms: int
    scheduled_at_ms: int
    intended_cancel: bool = False
    cancel_requested: bool = False
    cancel_ok: bool = False
    cancel_at_ms: Optional[int] = None
    add_ok: bool = True
    error: str = ""


@dataclass
class RunState:
    stop_at: float
    lock: threading.Lock = field(default_factory=threading.Lock)
    records: dict[int, TaskRecord] = field(default_factory=dict)
    stats: dict[str, int] = field(default_factory=lambda: {
        "add_ok": 0,
        "add_fail": 0,
        "cancel_ok": 0,
        "cancel_fail": 0,
        "cancel_skip": 0,
    })


def load_config(path: Path) -> dict[str, Any]:
    if not path.is_file():
        print(f"Missing {path}. Copy config.example.yaml to config.yaml.", file=sys.stderr)
        sys.exit(1)
    with path.open(encoding="utf-8") as f:
        return yaml.safe_load(f)


def ms_now() -> int:
    return int(time.time() * 1000)


def make_run_dir(base: Path) -> Path:
    stamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    run_dir = base / f"run_{stamp}"
    run_dir.mkdir(parents=True, exist_ok=True)
    return run_dir


class ScheduleClient:
    def __init__(self, base_url: str, task_type: int, priority: int, timeout: float = 30.0):
        self.base_url = base_url.rstrip("/")
        self.task_type = task_type
        self.priority = priority
        self.session = requests.Session()
        self.timeout = timeout

    def add_task(self, execute_time_ms: int) -> tuple[bool, int, str]:
        # parameters: minimal payload (empty byte array as JSON null)
        body = {
            "taskType": self.task_type,
            "priority": self.priority,
            "executeTime": execute_time_ms,
            "parameters": None,
        }
        url = f"{self.base_url}/api/v1/task/add"
        try:
            r = self.session.post(url, json=body, timeout=self.timeout)
            r.raise_for_status()
            data = r.json()
            if data.get("code") != 200:
                return False, 0, str(data.get("errorMessage") or data)
            task_id = int(data.get("data"))
            return True, task_id, ""
        except Exception as e:  # noqa: BLE001
            return False, 0, repr(e)

    def cancel_task(self, task_id: int) -> tuple[bool, str]:
        url = f"{self.base_url}/api/v1/task/{task_id}"
        try:
            r = self.session.get(url, timeout=self.timeout)
            r.raise_for_status()
            data = r.json()
            if data.get("code") != 200:
                return False, str(data.get("errorMessage") or data)
            # data is boolean from taskService.cancelTask
            ok = bool(data.get("data"))
            return ok, ""
        except Exception as e:  # noqa: BLE001
            return False, repr(e)


def schedule_worker(
    client: ScheduleClient,
    state: RunState,
    cfg: dict[str, Any],
    cancel_fraction: float,
) -> None:
    delay_min = cfg["execute_delay_sec_min"]
    delay_max = cfg["execute_delay_sec_max"]
    while time.time() < state.stop_at:
        delay = random.uniform(delay_min, delay_max)
        execute_time_ms = ms_now() + int(delay * 1000)
        ok, task_id, err = client.add_task(execute_time_ms)
        intended_cancel = random.random() < cancel_fraction
        rec = TaskRecord(
            task_id=task_id if ok else -1,
            execute_time_ms=execute_time_ms,
            scheduled_at_ms=ms_now(),
            intended_cancel=intended_cancel,
            add_ok=ok,
            error=err,
        )
        with state.lock:
            if ok:
                state.records[task_id] = rec
                state.stats["add_ok"] += 1
            else:
                state.stats["add_fail"] += 1
        # Pace slightly to avoid pure CPU spin
        time.sleep(random.uniform(0.02, 0.15))


def cancel_worker(client: ScheduleClient, state: RunState, cfg: dict[str, Any]) -> None:
    """Cancel in a random window [executeTime - before_max, executeTime - before_min]."""
    before_min = int(cfg["cancel_before_execute_sec_min"])
    before_max = int(cfg["cancel_before_execute_sec_max"])
    if before_max < before_min:
        before_max = before_min

    while time.time() < state.stop_at:
        with state.lock:
            candidates = [
                r
                for r in state.records.values()
                if r.add_ok
                and r.intended_cancel
                and not r.cancel_requested
            ]
        if not candidates:
            time.sleep(0.2)
            continue

        rec = random.choice(candidates)
        now = ms_now()
        window_start = rec.execute_time_ms - before_max * 1000
        window_end = rec.execute_time_ms - before_min * 1000

        if now < window_start:
            time.sleep(min((window_start - now) / 1000.0, 2.0))
            continue
        if now > window_end + 5000:
            # Missed window; still attempt cancel (may stress edge timing)
            pass
        elif now < window_end and random.random() < 0.65:
            time.sleep(random.uniform(0.1, 0.8))
            continue

        with state.lock:
            live = state.records.get(rec.task_id)
            if live is None or live.cancel_requested:
                continue
            live.cancel_requested = True

        ok, err = client.cancel_task(rec.task_id)
        with state.lock:
            live = state.records.get(rec.task_id)
            if live is None:
                continue
            live.cancel_ok = ok
            live.cancel_at_ms = ms_now()
            if ok:
                state.stats["cancel_ok"] += 1
            else:
                state.stats["cancel_fail"] += 1
                live.error = err or live.error


def write_csv(run_dir: Path, records: dict[int, TaskRecord]) -> Path:
    path = run_dir / "tasks.csv"
    with path.open("w", newline="", encoding="utf-8") as f:
        w = csv.writer(f)
        w.writerow([
            "task_id",
            "execute_time_ms",
            "scheduled_at_ms",
            "intended_cancel",
            "cancel_requested",
            "cancel_ok",
            "cancel_at_ms",
            "add_ok",
            "error",
        ])
        for tid in sorted(records):
            r = records[tid]
            w.writerow([
                r.task_id,
                r.execute_time_ms,
                r.scheduled_at_ms,
                int(r.intended_cancel),
                int(r.cancel_requested),
                int(r.cancel_ok),
                r.cancel_at_ms or "",
                int(r.add_ok),
                r.error,
            ])
    return path


def fetch_db_status(mysql_cfg: dict[str, Any], task_ids: list[int]) -> dict[int, int]:
    if not mysql_cfg.get("enabled"):
        return {}
    if pymysql is None:
        raise RuntimeError("pymysql not installed; pip install pymysql")

    conn = pymysql.connect(
        host=mysql_cfg["host"],
        port=int(mysql_cfg.get("port", 3306)),
        user=mysql_cfg["user"],
        password=mysql_cfg["password"],
        database=mysql_cfg["database"],
        charset="utf8mb4",
    )
    try:
        status_map: dict[int, int] = {}
        batch = 500
        with conn.cursor() as cur:
            for i in range(0, len(task_ids), batch):
                chunk = task_ids[i : i + batch]
                placeholders = ",".join(["%s"] * len(chunk))
                sql = f"SELECT task_id, status FROM taskinfo_logs WHERE task_id IN ({placeholders})"
                cur.execute(sql, chunk)
                for task_id, status in cur.fetchall():
                    status_map[int(task_id)] = int(status)
        return status_map
    finally:
        conn.close()


def reconcile(run_dir: Path, cfg: dict[str, Any]) -> int:
    csv_path = run_dir / "tasks.csv"
    if not csv_path.is_file():
        print(f"No tasks.csv in {run_dir}", file=sys.stderr)
        return 1

    records: list[dict[str, str]] = []
    with csv_path.open(encoding="utf-8") as f:
        records = list(csv.DictReader(f))

    task_ids = [int(r["task_id"]) for r in records if r.get("add_ok") == "1"]
    status_map = fetch_db_status(cfg["mysql"], task_ids)

    anomalies = []
    summary = {
        "total_logged": len(records),
        "add_ok": sum(1 for r in records if r.get("add_ok") == "1"),
        "intended_cancel": sum(1 for r in records if r.get("intended_cancel") == "1"),
        "cancel_ok": sum(1 for r in records if r.get("cancel_ok") == "1"),
        "missing_in_db": 0,
        "cancelled_still_executed": 0,
        "cancelled_ok": 0,
        "not_cancelled_executed": 0,
        "not_cancelled_scheduled": 0,
    }

    report_rows = []
    for r in records:
        if r.get("add_ok") != "1":
            continue
        tid = int(r["task_id"])
        intended = r.get("intended_cancel") == "1"
        cancel_ok = r.get("cancel_ok") == "1"
        st = status_map.get(tid)
        if st is None:
            summary["missing_in_db"] += 1
            report_rows.append({
                "task_id": tid,
                "intended_cancel": intended,
                "cancel_ok": cancel_ok,
                "db_status": "MISSING",
                "anomaly": "missing_row",
            })
            continue
        label = STATUS_LABEL.get(st, str(st))
        anomaly = ""
        if intended and cancel_ok and st == STATUS_EXECUTED:
            summary["cancelled_still_executed"] += 1
            anomaly = "cancelled_but_executed"
            anomalies.append(tid)
        elif intended and cancel_ok and st == STATUS_CANCELLED:
            summary["cancelled_ok"] += 1
        elif not intended and st == STATUS_EXECUTED:
            summary["not_cancelled_executed"] += 1
        elif not intended and st == STATUS_SCHEDULED:
            summary["not_cancelled_scheduled"] += 1

        report_rows.append({
            "task_id": tid,
            "intended_cancel": intended,
            "cancel_ok": cancel_ok,
            "db_status": label,
            "anomaly": anomaly,
        })

    report_path = run_dir / "reconcile_report.json"
    with report_path.open("w", encoding="utf-8") as f:
        json.dump(
            {
                "run_dir": str(run_dir),
                "generated_at": datetime.now(timezone.utc).isoformat(),
                "summary": summary,
                "anomaly_task_ids": anomalies,
            },
            f,
            indent=2,
        )

    anomaly_csv = run_dir / "anomalies.csv"
    with anomaly_csv.open("w", newline="", encoding="utf-8") as f:
        w = csv.DictWriter(f, fieldnames=["task_id", "intended_cancel", "cancel_ok", "db_status", "anomaly"])
        w.writeheader()
        for row in report_rows:
            if row.get("anomaly"):
                w.writerow(row)

    print(json.dumps(summary, indent=2))
    print(f"Report: {report_path}")
    if anomalies:
        print(f"ANOMALY: {len(anomalies)} task(s) cancelled in test but EXECUTED in DB: {anomalies[:20]}")
        print(f"Details: {anomaly_csv}")
        return 2
    print("No cancelled-but-executed anomalies in this run.")
    return 0


def run_load(cfg: dict[str, Any], run_dir: Path) -> Path:
    svc = cfg["schedule_service"]
    run_cfg = cfg["run"]
    client = ScheduleClient(
        svc["base_url"],
        int(svc["task_type"]),
        int(svc["priority"]),
    )

    state = RunState(stop_at=time.time() + float(run_cfg["duration_sec"]))
    threads: list[threading.Thread] = []

    print(f"Load until {datetime.fromtimestamp(state.stop_at)} -> {run_dir}")
    print(f"Target: {svc['base_url']}")

    for _ in range(int(run_cfg["schedule_workers"])):
        t = threading.Thread(
            target=schedule_worker,
            args=(client, state, run_cfg, float(run_cfg["cancel_fraction"])),
            daemon=True,
        )
        t.start()
        threads.append(t)

    for _ in range(int(run_cfg["cancel_workers"])):
        t = threading.Thread(
            target=cancel_worker,
            args=(client, state, run_cfg),
            daemon=True,
        )
        t.start()
        threads.append(t)

    try:
        while time.time() < state.stop_at:
            time.sleep(5)
            with state.lock:
                s = dict(state.stats)
                n = len(state.records)
            print(f"  stats={s} tasks={n}")
    except KeyboardInterrupt:
        print("Interrupted; stopping injection...")
        state.stop_at = time.time()

    for t in threads:
        t.join(timeout=2)

    csv_path = write_csv(run_dir, state.records)
    meta = {
        "run_id": run_dir.name,
        "config_snapshot": cfg,
        "stats": state.stats,
        "task_count": len(state.records),
    }
    with (run_dir / "meta.json").open("w", encoding="utf-8") as f:
        json.dump(meta, f, indent=2, default=str)

    print(f"Wrote {csv_path}")
    drain = float(run_cfg["drain_sec"])
    print(f"Draining {drain}s for refresh/poll (start wemedia if testing E2E)...")
    time.sleep(drain)
    return run_dir


def main() -> None:
    parser = argparse.ArgumentParser(description="LeadNews schedule peak rehearsal")
    parser.add_argument(
        "--config",
        type=Path,
        default=Path(__file__).resolve().parent / "config.yaml",
    )
    parser.add_argument(
        "--reconcile-only",
        type=Path,
        metavar="RUN_DIR",
        help="Skip load; reconcile an existing output/run_* directory",
    )
    parser.add_argument(
        "--skip-reconcile",
        action="store_true",
        help="Only run load + CSV, do not query MySQL",
    )
    args = parser.parse_args()
    cfg = load_config(args.config)

    if args.reconcile_only:
        code = reconcile(args.reconcile_only, cfg)
        sys.exit(code)

    out_base = Path(cfg["output"]["dir"])
    if not out_base.is_absolute():
        out_base = Path(__file__).resolve().parent / out_base
    run_dir = make_run_dir(out_base)

    run_load(cfg, run_dir)

    if args.skip_reconcile or not cfg.get("mysql", {}).get("enabled"):
        print("Skipping DB reconcile (enable mysql in config.yaml).")
        sys.exit(0)

    code = reconcile(run_dir, cfg)
    sys.exit(code)


if __name__ == "__main__":
    main()
