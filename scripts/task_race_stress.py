#!/usr/bin/env python3
"""
HTTP concurrency stress script for delayed-task execute vs cancel race.

Prerequisites:
  1. Start leadnews-schedule (default http://localhost:51701)
  2. MySQL + Redis available

What it does:
  1. Creates N immediately-ready tasks via POST /api/v1/task/add
  2. Concurrently runs cancel + poll workers
  3. Records per-task cancel and poll outcomes in a CSV file
  4. Checks that each task is finalized exactly once at API layer
     (cancel_success + poll_success == N)

For DB-level assertions (status must be EXECUTED or CANCELLED), also run:
  mvn -pl leadnews-service/leadnews-schedule -Dtest=TaskRaceConditionStressTest test

Usage:
  python scripts/task_race_stress.py
  python scripts/task_race_stress.py --base-url http://localhost:51701 --tasks 200 --workers 8
  python scripts/task_race_stress.py --output results/task_race_results.csv
"""

from __future__ import annotations

import argparse
import csv
import json
import threading
import time
import urllib.error
import urllib.request
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path
from typing import Any, Dict, List, Optional


def http_json(method: str, url: str, body: Optional[dict] = None, timeout: float = 10.0) -> Dict[str, Any]:
    data = None
    headers = {"Accept": "application/json"}
    if body is not None:
        data = json.dumps(body).encode("utf-8")
        headers["Content-Type"] = "application/json"
    req = urllib.request.Request(url=url, data=data, headers=headers, method=method)
    with urllib.request.urlopen(req, timeout=timeout) as resp:
        raw = resp.read().decode("utf-8")
        return json.loads(raw) if raw else {}


def add_task(base_url: str, task_type: int, priority: int) -> Optional[int]:
    payload = {
        "taskType": task_type,
        "priority": priority,
        "executeTime": int(time.time() * 1000),
        "parameters": None,
    }
    result = http_json("POST", f"{base_url}/api/v1/task/add", payload)
    if result.get("code") != 200 or result.get("data") is None:
        print(f"[add] failed result={result}")
        return None
    return int(result["data"])


def cancel_task(base_url: str, task_id: int) -> bool:
    result = http_json("GET", f"{base_url}/api/v1/task/{task_id}")
    return result.get("code") == 200 and bool(result.get("data"))


def poll_task(base_url: str, task_type: int, priority: int) -> Optional[int]:
    result = http_json("GET", f"{base_url}/api/v1/task/{task_type}/{priority}")
    data = result.get("data")
    if result.get("code") != 200 or not isinstance(data, dict) or data.get("taskId") is None:
        return None
    return int(data["taskId"])


def main() -> None:
    parser = argparse.ArgumentParser(description="Delayed task execute/cancel race stress test")
    parser.add_argument("--base-url", default="http://localhost:51701")
    parser.add_argument("--tasks", type=int, default=200)
    parser.add_argument("--workers", type=int, default=8, help="cancel workers and poll workers each")
    parser.add_argument("--task-type", type=int, default=9001)
    parser.add_argument("--priority", type=int, default=1)
    parser.add_argument(
        "--output",
        type=Path,
        default=Path("task_race_results.csv"),
        help="per-task result CSV (default: task_race_results.csv)",
    )
    args = parser.parse_args()

    print(f"Adding {args.tasks} ready tasks to {args.base_url} ...")
    task_ids: List[int] = []
    for _ in range(args.tasks):
        task_id = add_task(args.base_url, args.task_type, args.priority)
        if task_id is not None:
            task_ids.append(task_id)

    if not task_ids:
        raise SystemExit("No tasks created. Is leadnews-schedule running?")

    print(f"Created {len(task_ids)} tasks")
    print(f"Racing: {args.workers} cancel workers + {args.workers} poll workers")

    start_gate = threading.Event()
    result_lock = threading.Lock()
    cancelled_task_ids = set()
    polled_task_ids = set()

    def cancel_worker() -> int:
        start_gate.wait()
        ok = 0
        for task_id in task_ids:
            try:
                if cancel_task(args.base_url, task_id):
                    ok += 1
                    with result_lock:
                        cancelled_task_ids.add(task_id)
            except Exception as e:
                print(f"[cancel] error taskId={task_id}: {e}")
        return ok

    def poll_worker() -> int:
        start_gate.wait()
        ok = 0
        # Extra loops help drain the list under contention
        for _ in range(len(task_ids) * 2):
            try:
                task_id = poll_task(args.base_url, args.task_type, args.priority)
                if task_id is not None:
                    ok += 1
                    with result_lock:
                        polled_task_ids.add(task_id)
            except Exception as e:
                print(f"[poll] error: {e}")
        return ok

    t0 = time.time()
    with ThreadPoolExecutor(max_workers=args.workers * 2) as pool:
        cancel_futs = [pool.submit(cancel_worker) for _ in range(args.workers)]
        poll_futs = [pool.submit(poll_worker) for _ in range(args.workers)]
        start_gate.set()
        cancel_ok = sum(f.result() for f in cancel_futs)
        poll_ok = sum(f.result() for f in poll_futs)
    elapsed = time.time() - t0

    args.output.parent.mkdir(parents=True, exist_ok=True)
    with args.output.open("w", newline="", encoding="utf-8") as output_file:
        writer = csv.writer(output_file)
        writer.writerow(["task_id", "cancel_succeeded", "returned_by_poll", "outcome"])
        for task_id in task_ids:
            cancel_succeeded = task_id in cancelled_task_ids
            returned_by_poll = task_id in polled_task_ids
            if cancel_succeeded and returned_by_poll:
                outcome = "cancelled_but_polled"
            elif cancel_succeeded:
                outcome = "cancelled"
            elif returned_by_poll:
                outcome = "polled"
            else:
                outcome = "no_successful_terminal_operation"
            writer.writerow([task_id, int(cancel_succeeded), int(returned_by_poll), outcome])

    total_success = cancel_ok + poll_ok
    conflicting_task_ids = cancelled_task_ids & polled_task_ids
    print("===== Race Stress Result =====")
    print(f"tasks={len(task_ids)}")
    print(f"cancel_success={cancel_ok}")
    print(f"poll_success={poll_ok}")
    print(f"success_sum={total_success}")
    print(f"cancelled_but_polled={len(conflicting_task_ids)}")
    print(f"elapsed_sec={elapsed:.2f}")
    print(f"task_results={args.output.resolve()}")
    print()
    print("Expectation with state-machine conditional update:")
    print("  each task finalized exactly once => success_sum == tasks")

    if total_success == len(task_ids) and not conflicting_task_ids:
        print("PASS")
    else:
        print("CHECK: inconsistent task outcomes detected")
        if conflicting_task_ids:
            print(f"  cancelled_but_polled_task_ids={sorted(conflicting_task_ids)}")
        print("  Inspect DB: SELECT status, COUNT(*) FROM taskinfo_logs WHERE task_type=%d GROUP BY status;" % args.task_type)
        print("  Or run JUnit: TaskRaceConditionStressTest")


if __name__ == "__main__":
    try:
        main()
    except urllib.error.URLError as e:
        raise SystemExit(f"Cannot reach schedule service: {e}")
