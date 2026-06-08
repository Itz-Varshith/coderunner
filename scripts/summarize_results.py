#!/usr/bin/env python3
"""Build ``scripts/results/summary.md`` from the CSVs Locust produced.

Locust ``--csv <prefix>`` produces (among others):

* ``<prefix>_stats.csv``         - per-request-name aggregate metrics
* ``<prefix>_failures.csv``      - failure details
* ``<prefix>_stats_history.csv`` - per-10s time series (unused here)

The aggregate row ("Aggregated") of ``_stats.csv`` is treated as the
scenario-level rollup.
"""

from __future__ import annotations

import csv
import datetime as _dt
from pathlib import Path

SCRIPT_DIR = Path(__file__).resolve().parent
RAW_DIR = SCRIPT_DIR / "results" / "raw"
SUMMARY_PATH = SCRIPT_DIR / "results" / "summary.md"

SCENARIOS = [
    (
        "Read-heavy browsing",
        "read_heavy",
        "50 users, 10/s spawn, 60s. GETs against `/question/get-all`, "
        "`/question/get/[id]`, `/submit/details/[id]`, "
        "`/submit/all-submissions/[userId]`.",
    ),
    (
        "Submit burst",
        "submit_burst",
        "20 users, 5/s spawn, 60s. Sustained `POST /submit` traffic that "
        "exercises validation, the JPA write, and the Redis stream enqueue.",
    ),
    (
        "Mixed realistic flow",
        "mixed_flow",
        "30 users, 5/s spawn, 90s. End-to-end journey: browse problems "
        "-> open one -> submit -> poll `/submit/details/[id]` until terminal "
        "-> occasional history refresh.",
    ),
]


def _read_stats(prefix: str) -> list[dict]:
    csv_path = RAW_DIR / f"{prefix}_stats.csv"
    if not csv_path.exists():
        return []
    with csv_path.open() as f:
        return list(csv.DictReader(f))


def _read_failures(prefix: str) -> list[dict]:
    csv_path = RAW_DIR / f"{prefix}_failures.csv"
    if not csv_path.exists():
        return []
    with csv_path.open() as f:
        return list(csv.DictReader(f))


def _aggregate_row(rows: list[dict]) -> dict | None:
    for row in rows:
        if row.get("Name", "").strip() == "Aggregated":
            return row
    return None


def _fmt_int(value: str | float) -> str:
    try:
        return f"{int(float(value)):,}"
    except (TypeError, ValueError):
        return "-"


def _fmt_float(value: str | float, digits: int = 1) -> str:
    try:
        return f"{float(value):,.{digits}f}"
    except (TypeError, ValueError):
        return "-"


def _scenario_overview_row(label: str, agg: dict | None) -> str:
    if not agg:
        return f"| {label} | _no data_ |  |  |  |  |  |"
    return (
        f"| {label} "
        f"| {_fmt_int(agg.get('Request Count', 0))} "
        f"| {_fmt_int(agg.get('Failure Count', 0))} "
        f"| {_fmt_float(agg.get('Requests/s', 0), 2)} "
        f"| {_fmt_int(agg.get('Median Response Time', 0))} ms "
        f"| {_fmt_int(agg.get('95%', 0))} ms "
        f"| {_fmt_int(agg.get('Max Response Time', 0))} ms |"
    )


def _per_endpoint_table(rows: list[dict]) -> str:
    if not rows:
        return "_no data_"
    header = (
        "| Endpoint | Reqs | Fails | RPS | p50 (ms) | p95 (ms) | p99 (ms) | Max (ms) |\n"
        "| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |"
    )
    lines = [header]
    for row in rows:
        name = row.get("Name", "").strip()
        if name == "Aggregated":
            continue
        method = row.get("Type", "").strip()
        # Avoid `POST POST /submit` if the Name already starts with the method.
        if method and name.upper().startswith(method.upper() + " "):
            display = f"`{name}`"
        elif method:
            display = f"`{method} {name}`"
        else:
            display = f"`{name}`"
        lines.append(
            "| "
            + " | ".join(
                [
                    display,
                    _fmt_int(row.get("Request Count", 0)),
                    _fmt_int(row.get("Failure Count", 0)),
                    _fmt_float(row.get("Requests/s", 0), 2),
                    _fmt_int(row.get("Median Response Time", 0)),
                    _fmt_int(row.get("95%", 0)),
                    _fmt_int(row.get("99%", 0)),
                    _fmt_int(row.get("Max Response Time", 0)),
                ]
            )
            + " |"
        )
    return "\n".join(lines)


def _failures_section(rows: list[dict]) -> str:
    if not rows:
        return "No failures recorded."
    header = (
        "| Method | Name | Error | Count |\n"
        "| --- | --- | --- | ---: |"
    )
    lines = [header]
    for row in rows:
        method = row.get("Method", "").strip()
        name = row.get("Name", "").strip()
        error = row.get("Error", "").strip().replace("|", "\\|")[:120]
        count = _fmt_int(row.get("Occurrences", 0))
        lines.append(f"| `{method}` | `{name}` | {error} | {count} |")
    return "\n".join(lines)


def build_report() -> str:
    generated_at = _dt.datetime.now().strftime("%Y-%m-%d %H:%M:%S %Z").strip()
    sections: list[str] = []

    sections.append("# CodeRunner - Load Test Report\n")
    sections.append(
        f"_Generated: **{generated_at}**_ - all numbers are produced by "
        f"[Locust](https://locust.io/) in headless mode from a single host "
        f"against `http://localhost:8081` with the workers consuming the "
        f"`submission-stream` Redis stream live."
    )
    sections.append(
        "\n> **Test environment caveat:** the IP-based rate limiter "
        "interceptor was disabled for the duration of these runs so that one "
        "host could simulate many concurrent users. In production it is left "
        "enabled and prevents a single client from spamming `POST /submit`."
    )

    # --- Headline numbers: pull a few interesting figures up front ---
    headline_bits: list[str] = []
    for label, prefix, _desc in SCENARIOS:
        rows = _read_stats(prefix)
        agg = _aggregate_row(rows)
        if not agg:
            continue
        reqs = _fmt_int(agg.get("Request Count", 0))
        fails = _fmt_int(agg.get("Failure Count", 0))
        rps = _fmt_float(agg.get("Requests/s", 0), 1)
        p95 = _fmt_int(agg.get("95%", 0))
        headline_bits.append(
            f"* **{label}** - {reqs} reqs, {fails} fails, **{rps} RPS**, p95 **{p95} ms**."
        )
    if headline_bits:
        sections.append("\n## Headline numbers\n")
        sections.append("\n".join(headline_bits))

    sections.append("\n## Scenario rollup\n")
    sections.append(
        "| Scenario | Reqs | Fails | RPS | p50 | p95 | Max |\n"
        "| --- | ---: | ---: | ---: | ---: | ---: | ---: |"
    )
    aggregates: list[tuple[str, str, str, dict | None, list[dict], list[dict]]] = []
    for label, prefix, description in SCENARIOS:
        rows = _read_stats(prefix)
        failures = _read_failures(prefix)
        agg = _aggregate_row(rows)
        aggregates.append((label, prefix, description, agg, rows, failures))
        sections.append(_scenario_overview_row(label, agg))

    for label, prefix, description, agg, rows, failures in aggregates:
        sections.append(f"\n## {label}\n")
        sections.append(description)
        sections.append("")
        sections.append("### Per-endpoint metrics\n")
        sections.append(_per_endpoint_table(rows))
        sections.append("\n### Failures\n")
        sections.append(_failures_section(failures))

    sections.append("\n## Methodology\n")
    sections.append(
        "* All three scenarios are defined in "
        "`scripts/locust/locustfile.py` as independent `HttpUser` classes "
        "(`ReadHeavyUser`, `SubmitBurstUser`, `MixedJourneyUser`).\n"
        "* Submission payloads come from real `ACCEPTED` submissions pulled "
        "from the API, stored under `scripts/locust/solutions/`, so the "
        "worker pipeline executes representative code each run.\n"
        "* Locust was run headless via `scripts/run_load_tests.sh` and the "
        "raw `_stats.csv` / `_failures.csv` outputs in `scripts/results/raw/` "
        "are parsed by `summarize_results.py` to regenerate this file."
    )

    return "\n".join(sections) + "\n"


def main() -> None:
    SUMMARY_PATH.parent.mkdir(parents=True, exist_ok=True)
    SUMMARY_PATH.write_text(build_report())
    print(f"Wrote {SUMMARY_PATH}")


if __name__ == "__main__":
    main()
