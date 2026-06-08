"""
Load test suite for the CodeRunner platform.

Three independent user classes are defined so you can target a specific traffic
profile by passing ``--class-picker`` or ``--user-classes`` to ``locust``:

* ``ReadHeavyUser``  - browsing traffic (list questions, open a question,
  fetch submission details, fetch a user's submission history).
* ``SubmitBurstUser`` - hammers ``POST /submit`` to exercise the validation +
  DB write + Redis stream enqueue path, and indirectly the worker semaphore
  pool downstream.
* ``MixedJourneyUser`` - mimics a realistic end-user flow: browse -> open
  problem -> submit -> poll the submission until terminal -> occasionally
  refresh the submission history.

Configuration values can be overridden via environment variables so the
runner script can stay declarative:

* ``API_BASE``       - default ``http://localhost:8081``
* ``USER_ID``        - default ``dummy-user`` (must exist in the API DB)
* ``POLL_TIMEOUT_S`` - default ``20`` seconds (max wall time spent polling
  a single submission before giving up in the mixed scenario)
"""

from __future__ import annotations

import os
import random
import time
from pathlib import Path
from typing import Iterable

from locust import HttpUser, between, constant, task, tag

API_BASE = os.environ.get("API_BASE", "http://localhost:8081")
USER_ID = os.environ.get("USER_ID", "dummy-user")
POLL_TIMEOUT_S = float(os.environ.get("POLL_TIMEOUT_S", "20"))

QUESTION_IDS = [
    "bcbeb05c-fbd7-4a3a-9eba-96ed10170bff",  # Longest Increasing Subsequence
    "8fcf52f4-e59e-4643-97c5-e18584454489",  # Sort
    "7a5d5e36-8496-4f4a-afd6-1e023048c9bf",  # Network Routing Time
]

SOLUTIONS_DIR = Path(__file__).parent / "solutions"

SOLUTION_FILES: dict[tuple[str, str], str] = {
    ("cpp", "bcbeb05c-fbd7-4a3a-9eba-96ed10170bff"): "cpp_lis.cpp",
    ("cpp", "8fcf52f4-e59e-4643-97c5-e18584454489"): "cpp_sort.cpp",
    ("cpp", "7a5d5e36-8496-4f4a-afd6-1e023048c9bf"): "cpp_network.cpp",
    ("java", "8fcf52f4-e59e-4643-97c5-e18584454489"): "java_sort.java",
    ("java", "7a5d5e36-8496-4f4a-afd6-1e023048c9bf"): "java_network.java",
    ("python", "bcbeb05c-fbd7-4a3a-9eba-96ed10170bff"): "python_lis.py",
    ("python", "8fcf52f4-e59e-4643-97c5-e18584454489"): "python_sort.py",
}


def _load_solutions() -> dict[tuple[str, str], str]:
    payloads: dict[tuple[str, str], str] = {}
    for key, fname in SOLUTION_FILES.items():
        path = SOLUTIONS_DIR / fname
        payloads[key] = path.read_text()
    return payloads


SOLUTIONS = _load_solutions()
SOLUTION_KEYS = list(SOLUTIONS.keys())


def _random_submission_payload() -> dict:
    language, question_id = random.choice(SOLUTION_KEYS)
    return {
        "code": SOLUTIONS[(language, question_id)],
        "language": language,
        "userId": USER_ID,
        "questionId": question_id,
    }


def _list_recent_submission_ids(client, limit: int = 25) -> list[int]:
    """Best-effort fetch of recent submission ids for the load user."""
    try:
        with client.get(
            f"/submit/all-submissions/{USER_ID}",
            name="/submit/all-submissions/[userId]",
            catch_response=True,
        ) as resp:
            if resp.status_code != 200:
                resp.failure(f"unexpected status {resp.status_code}")
                return []
            data = resp.json() or []
            ids = [int(item["submissionId"]) for item in data]
            return ids[-limit:]
    except Exception:
        return []


class ReadHeavyUser(HttpUser):
    """Pure read traffic. No submissions are created."""

    host = API_BASE
    wait_time = between(0.5, 1.5)
    weight = 1
    submission_ids: list[int] = []

    def on_start(self):
        if not ReadHeavyUser.submission_ids:
            ReadHeavyUser.submission_ids = _list_recent_submission_ids(self.client) or []

    @tag("list-questions")
    @task(4)
    def list_questions(self):
        self.client.get("/question/get-all", name="/question/get-all")

    @tag("get-question")
    @task(3)
    def get_question(self):
        qid = random.choice(QUESTION_IDS)
        self.client.get(f"/question/get/{qid}", name="/question/get/[id]")

    @tag("get-submission")
    @task(2)
    def get_submission_details(self):
        if not ReadHeavyUser.submission_ids:
            return
        sid = random.choice(ReadHeavyUser.submission_ids)
        self.client.get(
            f"/submit/details/{sid}", name="/submit/details/[id]"
        )

    @tag("list-submissions")
    @task(1)
    def list_user_submissions(self):
        self.client.get(
            f"/submit/all-submissions/{USER_ID}",
            name="/submit/all-submissions/[userId]",
        )


class SubmitBurstUser(HttpUser):
    """Hammer ``POST /submit`` as fast as the wait_time allows."""

    host = API_BASE
    wait_time = constant(0.2)
    weight = 1

    @tag("submit")
    @task
    def submit_code(self):
        payload = _random_submission_payload()
        with self.client.post(
            "/submit",
            json=payload,
            name="/submit",
            catch_response=True,
        ) as resp:
            if resp.status_code != 202:
                resp.failure(f"expected 202, got {resp.status_code}: {resp.text[:200]}")
                return
            try:
                resp.json()
            except Exception as exc:
                resp.failure(f"invalid JSON: {exc}")


class MixedJourneyUser(HttpUser):
    """Browse -> open problem -> submit -> poll until terminal."""

    host = API_BASE
    wait_time = between(1, 3)
    weight = 2

    @task
    def full_journey(self):
        self.client.get("/question/get-all", name="/question/get-all")

        payload = _random_submission_payload()
        qid = payload["questionId"]
        self.client.get(f"/question/get/{qid}", name="/question/get/[id]")

        with self.client.post(
            "/submit",
            json=payload,
            name="/submit",
            catch_response=True,
        ) as submit_resp:
            if submit_resp.status_code != 202:
                submit_resp.failure(
                    f"expected 202, got {submit_resp.status_code}: {submit_resp.text[:200]}"
                )
                return
            body = submit_resp.json()
            submission_id = body.get("data")
            if not submission_id:
                submit_resp.failure(f"no submission id in body: {body}")
                return

        # Poll a few times to mimic the UI's status polling.
        deadline = time.time() + POLL_TIMEOUT_S
        poll_interval = 1.0
        while time.time() < deadline:
            with self.client.get(
                f"/submit/details/{submission_id}",
                name="/submit/details/[id]",
                catch_response=True,
            ) as resp:
                if resp.status_code != 200:
                    resp.failure(f"unexpected status {resp.status_code}")
                    break
                status = (resp.json() or {}).get("status")
            if status in {
                "ACCEPTED",
                "WRONG_ANSWER",
                "TLE",
                "MLE",
                "RUNTIME_ERROR",
                "COMPILATION_ERROR",
            }:
                break
            time.sleep(poll_interval)

        # Refresh history occasionally.
        if random.random() < 0.3:
            self.client.get(
                f"/submit/all-submissions/{USER_ID}",
                name="/submit/all-submissions/[userId]",
            )


# The order in which classes are picked when ``--user-classes`` is omitted.
__all__: Iterable[str] = ("ReadHeavyUser", "SubmitBurstUser", "MixedJourneyUser")
