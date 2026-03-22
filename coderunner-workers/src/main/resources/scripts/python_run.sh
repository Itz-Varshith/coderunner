#!/bin/bash

USER_CODE="solution.py"
JUDGE_EXEC="./testcase/judge_program"
TESTCASE_DIR="./testcase/input"
USER_OUTPUT="user_output.txt"
USER_ERROR="runtime_error.txt"

TIME_LIMIT={{TIME_LIMIT}}
MEMORY_LIMIT={{MEMORY_LIMIT}}


python3 -m py_compile "$USER_CODE" 2> "$USER_ERROR"
if [ $? -ne 0 ]; then
    echo "COMPILATION_ERROR"
    exit 1
fi

# 2. Execute Against Test Cases
for testcase in "$TESTCASE_DIR"/*.txt
do

    (

        ulimit -v $((MEMORY_LIMIT * 1024))

        timeout "$TIME_LIMIT" python3 "$USER_CODE" \
            < "$testcase" \
            > "$USER_OUTPUT" \
            2> "$USER_ERROR"
    )
    STATUS=$?

    # Evaluate the exact reason for failure
    if [ $STATUS -eq 124 ]; then
        echo "TIME_LIMIT_EXCEEDED"
        exit 1
    elif [ $STATUS -eq 137 ] || [ $STATUS -eq 139 ]; then
        echo "MEMORY_LIMIT_EXCEEDED"
        exit 1
    elif [ $STATUS -ne 0 ]; then

        if grep -q "MemoryError" "$USER_ERROR"; then
            echo "MEMORY_LIMIT_EXCEEDED"
        else
            echo "RUNTIME_ERROR"
        fi
        exit 1
    fi

    # 3. Run the Custom Judge
    ./"$JUDGE_EXEC" "$testcase" < "$USER_OUTPUT"
    JUDGE_STATUS=$?

    if [ $JUDGE_STATUS -ne 0 ]; then
        echo "WRONG_ANSWER"
        exit 1
    fi
done

# If it survives the loop, all test cases passed
echo "ACCEPTED"
exit 0