# Schedule peak rehearsal (Path A)

Mixed **schedule + random cancel** load against `leadnews-schedule`, then **reconcile** `taskinfo_logs` to find tasks that were **cancelled in the test but ended as `EXECUTED` (status=1)**.

This matches the staging drill narrative: business-like traffic first, anomalies found in reconciliation—not a dedicated “1 second before execute” race script.

## Prerequisites

1. **MySQL** `leadnews_schedule` with tables `taskinfo`, `taskinfo_logs`
2. **Redis** (same as `leadnews-schedule` `application.yml`)
3. **leadnews-schedule** running, e.g. port `51701`
4. Optional: **leadnews-wemedia** running if you also want articles to publish (E2E). Reconciliation in this tool uses **schedule DB status only**.

## Setup

```bash
cd tools/schedule_peak_test
pip install -r requirements.txt
copy config.example.yaml config.yaml
# Edit config.yaml: base_url, MySQL password
```

## Quick smoke test (~3 minutes)

Edit `config.yaml`:

```yaml
run:
  duration_sec: 120
  schedule_workers: 5
  cancel_workers: 3
  cancel_fraction: 0.25
  execute_delay_sec_min: 130
  execute_delay_sec_max: 200
  cancel_before_execute_sec_min: 3
  cancel_before_execute_sec_max: 90
  drain_sec: 180
```

## Full rehearsal (~45 min)

Use defaults in `config.example.yaml` (`duration_sec: 1800`, etc.).

## Run

```bash
python run_peak_rehearsal.py
```

Outputs under `output/run_YYYYMMDD_HHMMSS/`:

| File | Purpose |
|------|---------|
| `tasks.csv` | Per-task schedule/cancel intent |
| `meta.json` | Run stats |
| `reconcile_report.json` | Summary + anomaly task IDs |
| `anomalies.csv` | Rows with `cancelled_but_executed` |

Exit code `2` means at least one **cancelled_but_executed** anomaly (useful to confirm the race before a fix).

## Reconcile only

```bash
python run_peak_rehearsal.py --reconcile-only output/run_20260101_120000
```

## Multi-instance schedule

Start **two** `leadnews-schedule` processes (different ports) behind the same MySQL/Redis only if you know how your deployment shares data; for a simple test, one instance is enough. For HA drill, point a load balancer at two instances and keep one `base_url` if both share the same DB.

## Anomaly meaning

| Condition | Meaning |
|-----------|---------|
| `intended_cancel=1`, `cancel_ok=1`, DB `EXECUTED` | **Bug signal**: cancel won in API/DB path but task was still executed (poll/refresh race) |
| `intended_cancel=1`, DB `CANCELLED` | Expected |
| `intended_cancel=0`, DB `EXECUTED` | Normal publish path |

## Note on API

`cancelTask` is exposed as `GET /api/v1/task/{taskId}` in `ScheduleClient.java` (same as the Java project).
