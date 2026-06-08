# CodeRunner - Load Test Report

_Generated: **2026-06-08 23:46:56**_ - all numbers are produced by [Locust](https://locust.io/) in headless mode from a single host against `http://localhost:8081` with the workers consuming the `submission-stream` Redis stream live.

> **Test environment caveat:** the IP-based rate limiter interceptor was disabled for the duration of these runs so that one host could simulate many concurrent users. In production it is left enabled and prevents a single client from spamming `POST /submit`.

## Headline numbers

* **Read-heavy browsing** - 2,678 reqs, 0 fails, **45.2 RPS**, p95 **730 ms**.
* **Submit burst** - 4,595 reqs, 0 fails, **77.3 RPS**, p95 **91 ms**.
* **Mixed realistic flow** - 2,612 reqs, 0 fails, **29.5 RPS**, p95 **110 ms**.

## Scenario rollup

| Scenario | Reqs | Fails | RPS | p50 | p95 | Max |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| Read-heavy browsing | 2,678 | 0 | 45.19 | 5 ms | 730 ms | 2,024 ms |
| Submit burst | 4,595 | 0 | 77.33 | 42 ms | 91 ms | 163 ms |
| Mixed realistic flow | 2,612 | 0 | 29.46 | 17 ms | 110 ms | 5,570 ms |

## Read-heavy browsing

50 users, 10/s spawn, 60s. GETs against `/question/get-all`, `/question/get/[id]`, `/submit/details/[id]`, `/submit/all-submissions/[userId]`.

### Per-endpoint metrics

| Endpoint | Reqs | Fails | RPS | p50 (ms) | p95 (ms) | p99 (ms) | Max (ms) |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| `GET /question/get-all` | 1,092 | 0 | 18.43 | 5 | 21 | 58 | 358 |
| `GET /question/get/[id]` | 768 | 0 | 12.96 | 4 | 19 | 56 | 170 |
| `GET /submit/all-submissions/[userId]` | 292 | 0 | 4.93 | 710 | 1,100 | 1,500 | 2,024 |
| `GET /submit/details/[id]` | 526 | 0 | 8.88 | 4 | 20 | 55 | 169 |

### Failures

No failures recorded.

## Submit burst

20 users, 5/s spawn, 60s. Sustained `POST /submit` traffic that exercises validation, the JPA write, and the Redis stream enqueue.

### Per-endpoint metrics

| Endpoint | Reqs | Fails | RPS | p50 (ms) | p95 (ms) | p99 (ms) | Max (ms) |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| `POST /submit` | 4,595 | 0 | 77.33 | 42 | 91 | 110 | 163 |

### Failures

No failures recorded.

## Mixed realistic flow

30 users, 5/s spawn, 90s. End-to-end journey: browse problems -> open one -> submit -> poll `/submit/details/[id]` until terminal -> occasional history refresh.

### Per-endpoint metrics

| Endpoint | Reqs | Fails | RPS | p50 (ms) | p95 (ms) | p99 (ms) | Max (ms) |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| `GET /question/get-all` | 120 | 0 | 1.35 | 24 | 170 | 250 | 258 |
| `GET /question/get/[id]` | 120 | 0 | 1.35 | 21 | 100 | 220 | 291 |
| `POST /submit` | 120 | 0 | 1.35 | 39 | 120 | 210 | 277 |
| `GET /submit/all-submissions/[userId]` | 32 | 0 | 0.36 | 3,300 | 5,400 | 5,600 | 5,570 |
| `GET /submit/details/[id]` | 2,220 | 0 | 25.04 | 16 | 84 | 200 | 358 |

### Failures

No failures recorded.

## Methodology

* All three scenarios are defined in `scripts/locust/locustfile.py` as independent `HttpUser` classes (`ReadHeavyUser`, `SubmitBurstUser`, `MixedJourneyUser`).
* Submission payloads come from real `ACCEPTED` submissions pulled from the API, stored under `scripts/locust/solutions/`, so the worker pipeline executes representative code each run.
* Locust was run headless via `scripts/run_load_tests.sh` and the raw `_stats.csv` / `_failures.csv` outputs in `scripts/results/raw/` are parsed by `summarize_results.py` to regenerate this file.
