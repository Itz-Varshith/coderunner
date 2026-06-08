#!/usr/bin/env bash
# Run all three Locust scenarios in headless mode and dump CSV/HTML reports
# into ``scripts/results/raw``. After the run, ``summarize_results.py`` is
# invoked to refresh ``scripts/results/summary.md``.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOCUST_DIR="${SCRIPT_DIR}/locust"
RESULTS_DIR="${SCRIPT_DIR}/results/raw"
VENV_PY="${SCRIPT_DIR}/.venv/bin/python"
LOCUST_BIN="${SCRIPT_DIR}/.venv/bin/locust"

API_BASE="${API_BASE:-http://localhost:8081}"
HEALTH_PATH="${HEALTH_PATH:-/question/get-all}"

mkdir -p "${RESULTS_DIR}"

# Quick sanity check: refuse to start if the API is not reachable.
if ! curl -s -o /dev/null -w "%{http_code}" "${API_BASE}${HEALTH_PATH}" | grep -qE "^(200|2[0-9][0-9])$"; then
    echo "ERROR: API at ${API_BASE}${HEALTH_PATH} is not responding with 2xx." >&2
    exit 1
fi

run_scenario() {
    local name="$1"
    local user_class="$2"
    local users="$3"
    local spawn_rate="$4"
    local duration="$5"

    echo "=============================================="
    echo "  scenario : ${name}"
    echo "  class    : ${user_class}"
    echo "  users    : ${users}"
    echo "  spawn/s  : ${spawn_rate}"
    echo "  duration : ${duration}"
    echo "=============================================="

    API_BASE="${API_BASE}" "${LOCUST_BIN}" \
        -f "${LOCUST_DIR}/locustfile.py" \
        --headless \
        --users "${users}" \
        --spawn-rate "${spawn_rate}" \
        --run-time "${duration}" \
        --host "${API_BASE}" \
        --csv "${RESULTS_DIR}/${name}" \
        --html "${RESULTS_DIR}/${name}.html" \
        --only-summary \
        "${user_class}"
}

run_scenario "read_heavy"   "ReadHeavyUser"     50 10 60s
run_scenario "submit_burst" "SubmitBurstUser"   20 5  60s
run_scenario "mixed_flow"   "MixedJourneyUser"  30 5  90s

"${VENV_PY}" "${SCRIPT_DIR}/summarize_results.py"

echo
echo "Done. Report: ${SCRIPT_DIR}/results/summary.md"
