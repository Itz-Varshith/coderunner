# CodeRunner load tests

[Locust](https://locust.io/)-driven load tests for the CodeRunner API +
worker pipeline.

```
scripts/
├── README.md                # this file
├── run_load_tests.sh        # entrypoint - runs all scenarios + report
├── summarize_results.py     # rebuilds results/summary.md from raw CSVs
├── locust/
│   ├── locustfile.py        # ReadHeavyUser / SubmitBurstUser / MixedJourneyUser
│   ├── requirements.txt
│   └── solutions/           # real ACCEPTED solutions used as submission payloads
└── results/
    ├── summary.md           # human-readable report (committed)
    └── raw/                 # locust CSV + HTML dumps (gitignored)
```

## One-time setup

```bash
cd scripts
python3 -m venv .venv
.venv/bin/pip install -r locust/requirements.txt
```

## Run

Boot the API on `:8081` and the worker service, then:

```bash
./scripts/run_load_tests.sh
```

This runs three scenarios back-to-back and regenerates
`scripts/results/summary.md`:

| Scenario       | Users | Spawn rate | Duration | What it stresses                       |
| -------------- | ----: | ---------: | -------: | -------------------------------------- |
| `read_heavy`   |    50 |     10 / s |     60 s | Read endpoints, DB read throughput     |
| `submit_burst` |    20 |      5 / s |     60 s | Validation + JPA write + Redis enqueue |
| `mixed_flow`   |    30 |      5 / s |     90 s | Full user journey incl. status polling |

Override the target host or load user with env vars:

```bash
API_BASE=http://192.168.1.10:8081 USER_ID=dummy-user ./scripts/run_load_tests.sh
```

## Interactive UI mode

```bash
.venv/bin/locust -f locust/locustfile.py --host http://localhost:8081
# then open http://localhost:8089
```

## Note on the rate limiter

The IP-based rate limiter interceptor on `POST /submit` is **disabled** while
running these tests so a single host can drive enough concurrent users. In
production it is kept enabled and prevents a single client from spamming the
submit endpoint - the report calls this out explicitly.
