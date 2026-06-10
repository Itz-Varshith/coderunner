# CodeRunner - Load Test Report

_Generated: **2026-06-10 21:15:39**_ - all numbers are produced by [Locust](https://locust.io/) in headless mode from a single host against `http://localhost:8081` with the workers consuming the `submission-stream` Redis stream live.

> **Test environment caveat:** the IP-based rate limiter interceptor was disabled for the duration of these runs so that one host could simulate many concurrent users. In production it is left enabled and prevents a single client from spamming `POST /submit`.

## Headline numbers

* **Read-heavy browsing** - 2,910 reqs, 0 fails, **49.3 RPS**, p95 **10 ms**.
* **Submit burst** - 5,361 reqs, 0 fails, **90.7 RPS**, p95 **22 ms**.
* **Mixed realistic flow** - 2,797 reqs, 0 fails, **31.4 RPS**, p95 **14 ms**.

## Scenario rollup

| Scenario | Reqs | Fails | RPS | p50 | p95 | Max |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| Read-heavy browsing | 2,910 | 0 | 49.29 | 4 ms | 10 ms | 404 ms |
| Submit burst | 5,361 | 0 | 90.74 | 13 ms | 22 ms | 145 ms |
| Mixed realistic flow | 2,797 | 0 | 31.40 | 5 ms | 14 ms | 49 ms |

## Read-heavy browsing

50 users, 10/s spawn, 60s. GETs against `/question/get-all`, `/question/get/[id]`, `/submit/details/[id]`, `/submit/all-submissions/[userId]`.

### Per-endpoint metrics

| Endpoint | Reqs | Fails | RPS | p50 (ms) | p95 (ms) | p99 (ms) | Max (ms) |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| `GET /question/get-all` | 1,157 | 0 | 19.60 | 4 | 8 | 11 | 22 |
| `GET /question/get/[id]` | 861 | 0 | 14.58 | 4 | 8 | 16 | 27 |
| `GET /submit/all-submissions/[userId]` | 313 | 0 | 5.30 | 8 | 20 | 380 | 404 |
| `GET /submit/details/[id]` | 579 | 0 | 9.81 | 4 | 8 | 15 | 21 |

### Failures

No failures recorded.

## Submit burst

20 users, 5/s spawn, 60s. Sustained `POST /submit` traffic that exercises validation, the JPA write, and the Redis stream enqueue.

### Per-endpoint metrics

| Endpoint | Reqs | Fails | RPS | p50 (ms) | p95 (ms) | p99 (ms) | Max (ms) |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| `POST /submit` | 5,361 | 0 | 90.74 | 13 | 22 | 38 | 145 |

### Failures

No failures recorded.

## Mixed realistic flow

30 users, 5/s spawn, 90s. End-to-end journey: browse problems -> open one -> submit -> poll `/submit/details/[id]` until terminal -> occasional history refresh.

### Per-endpoint metrics

| Endpoint | Reqs | Fails | RPS | p50 (ms) | p95 (ms) | p99 (ms) | Max (ms) |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| `GET /question/get-all` | 125 | 0 | 1.40 | 5 | 12 | 17 | 25 |
| `GET /question/get/[id]` | 125 | 0 | 1.40 | 4 | 10 | 14 | 15 |
| `POST /submit` | 125 | 0 | 1.40 | 11 | 20 | 25 | 46 |
| `GET /submit/all-submissions/[userId]` | 29 | 0 | 0.33 | 29 | 35 | 36 | 35 |
| `GET /submit/details/[id]` | 2,393 | 0 | 26.87 | 5 | 12 | 17 | 49 |

### Failures

No failures recorded.

## Methodology

* All three scenarios are defined in `scripts/locust/locustfile.py` as independent `HttpUser` classes (`ReadHeavyUser`, `SubmitBurstUser`, `MixedJourneyUser`).
* Submission payloads come from real `ACCEPTED` submissions pulled from the API, stored under `scripts/locust/solutions/`, so the worker pipeline executes representative code each run.
* Locust was run headless via `scripts/run_load_tests.sh` and the raw `_stats.csv` / `_failures.csv` outputs in `scripts/results/raw/` are parsed by `summarize_results.py` to regenerate this file.
