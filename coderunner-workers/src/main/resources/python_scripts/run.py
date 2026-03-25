"""
Python script we are currently writing, two most important features of this
1) Needs to be lightweight and fast, so that latency is lowered
2) Needs to do all the tasks the bash script does.
3) Avoiding classes for structure in the final saved file is better imo, cause classes add time overhead in running the code.
Steps:
1) Compile user code
2) Capture compilation error
3) Iterate items in the testcases directory,
3.1) Run with a mem limit check and timeout and also maintain the max time taken by any testcase to save as Time limit, and memory taken similarly.
3.2) Check for Runtime errors, if MLE skip other testcases and directly go to part of saving result.
3.3) Run the judge to verify for result and observe shell op to check for wrong answers and skip other testcases.
6) Save with result(JSON) accordingly.
7) Return.

"""
# Also run code with python3 -u (unbuffered mode) script.py to make sure writes are directly visible.
# python3 -u runner.py "<compile_cmd>" "<user_cmd>" "<judge_cmd>" <time_limit_ms> <memory_limit_mb>
import sys
import os
import subprocess
import time
import resource
import shlex
import json

# ------------------ LIMIT SETTER ------------------

def set_limits(memory_limit_mb):
    def limiter():
        mem_bytes = memory_limit_mb * 1024 * 1024
        resource.setrlimit(resource.RLIMIT_AS, (mem_bytes, mem_bytes))
    return limiter

# ------------------ COMPILATION ------------------

def compile_code(cmd):
    print("Compiling Code")
    result = subprocess.run(cmd, capture_output=True, text=True)
    if result.returncode != 0:
        return False, result.stderr[:500]
    return True, ""

# ------------------ SAVING TO JSON -------------------
def writeToJson(var):
    print("writing")
    with open("result.json", "w") as file:
        json.dump(var, file, indent=4)
    return var



# ------------------ MAIN EVALUATION ------------------

def run_evaluation(user_cmd, judge_cmd, testcase_dir, time_limit_ms, memory_limit_mb):
    test_files = sorted([f for f in os.listdir(testcase_dir) if f.endswith(".txt")])

    if not test_files:
        return writeToJson({
            "isSystemError": True,
            "status": "SYSTEM_ERROR",
            "judgeMessage": "No testcases found",
            "timeTakenMs": 0,
            "memoryTakenKb": 0
        })

    max_time = 0
    total_tests = len(test_files)
    cnt=1;
    for idx, tc in enumerate(test_files, 1):
        print(f"Running testcase #{cnt}")
        cnt=cnt+1
        tc_path = os.path.join(testcase_dir, tc)


        with open(tc_path, "r") as stdin_f:

            start = time.perf_counter()

            process = subprocess.Popen(
                user_cmd,
                stdin=stdin_f,
                stdout=subprocess.PIPE,
                stderr=subprocess.PIPE,
                text=True,
                # preexec_fn=set_limits(memory_limit_mb)
            )

            try:
                stdout, stderr = process.communicate(timeout=time_limit_ms / 1000.0)
            except subprocess.TimeoutExpired:
                process.kill()
                process.wait()
                return writeToJson({
                    "isSystemError": False,
                    "status": "TLE",
                    "judgeMessage": f"Time limit exceeded on testcase #{idx}",
                    "timeTakenMs": time_limit_ms,
                    "memoryTakenKb": 0
                })

            exec_time = int((time.perf_counter() - start) * 1000)
            max_time = max(max_time, exec_time)

            # ------------------ RUNTIME / MLE ------------------
            if process.returncode != 0:
                err = stderr[:300]

                if (
                        "OutOfMemoryError" in err or
                        "MemoryError" in err or
                        "bad_alloc" in err or
                        process.returncode in (137, 139)
                ):
                    return writeToJson({
                        "isSystemError": False,
                        "status": "MLE",
                        "judgeMessage": f"Memory limit exceeded on testcase #{idx}",
                        "timeTakenMs": max_time,
                        "memoryTakenKb": memory_limit_mb*1024
                    })

                return writeToJson({
                    "isSystemError": False,
                    "status": "RUNTIME_ERROR",
                    "judgeMessage": f"Runtime error on testcase #{idx}: {err}",
                    "timeTakenMs": max_time,
                    "memoryTakenKb": 0
                })

            # ------------------ JUDGE ------------------
            judge = subprocess.run(
                judge_cmd + [tc_path],
                input=stdout,
                capture_output=True,
                text=True
            )

            judge_msg = (judge.stdout.strip() or judge.stderr.strip())[:200]

            if judge.returncode != 0:
                msg = f"Wrong answer on testcase #{idx}"
                if judge_msg:
                    msg += f" ({judge_msg})"

                return writeToJson({
                    "isSystemError": False,
                    "status": "WRONG_ANSWER",
                    "judgeMessage": msg,
                    "timeTakenMs": max_time,
                    "memoryTakenKb": 0
                })

    # ------------------ ACCEPTED ------------------

    final_msg = f"Accepted ({total_tests} testcases passed)"

    return writeToJson({
        "isSystemError": False,
        "status": "ACCEPTED",
        "judgeMessage": final_msg,
        "timeTakenMs": max_time,
        "memoryTakenKb": 0
    });

# ------------------ ENTRY ------------------

if __name__ == "__main__":
    if len(sys.argv) < 6:
        sys.exit(1)

    compile_cmd = shlex.split(sys.argv[1])
    user_cmd = shlex.split(sys.argv[2])
    judge_cmd = shlex.split(sys.argv[3])
    time_limit_ms = int(sys.argv[4])
    memory_limit_mb = int(sys.argv[5])

    # -------- Compile --------
    ok, err = compile_code(compile_cmd)
    if not ok:
        writeToJson({
            "isSystemError": False,
            "status": "COMPILATION_ERROR",
            "judgeMessage": err,
            "timeTakenMs": 0,
            "memoryTakenKb": 0
        })
        sys.exit(0)

    # -------- Run --------
    result = run_evaluation(
        user_cmd,
        judge_cmd,
        "./testcase/input",
        time_limit_ms,
        memory_limit_mb
    )

    print(result)