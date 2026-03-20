#!/bin/bash

USER_CODE="user_code.cpp"
JUDGE_CODE="testcase/judge.cpp"

USER_EXEC="user_program"
JUDGE_EXEC="testcase/judge_program"

TESTCASE_DIR="./testcase/input"

USER_OUTPUT="user_output.txt"
USER_ERROR="runtime_error.txt"


TIME_LIMIT={{TIME_LIMIT}}
MEMORY_LIMIT={{MEMORY_LIMIT}}


# 1. Compile User Code
g++ "$USER_CODE" -O2 -std=c++17 -o "$USER_EXEC"
if [ $? -ne 0 ]; then
    echo "COMPILATION_ERROR"
    exit 1
fi

# 2. Compile Judge Code
#g++ "$JUDGE_CODE" -O2 -std=c++17 -o "$JUDGE_EXEC"
#if [ $? -ne 0 ]; then
#    echo "JUDGE_COMPILATION_ERROR"
#    exit 1
#fi

# 3. Execute Against Test Cases
for testcase in "$TESTCASE_DIR"/*.txt
do
    # Run the sandboxed execution
    (
        ulimit -v $((MEMORY_LIMIT * 1024))

        timeout "$TIME_LIMIT" ./"$USER_EXEC" \
            < "$testcase" \
            > "$USER_OUTPUT" \
            2> "$USER_ERROR"
    )
    STATUS=$?

    # Evaluate the exact reason for failure
    if [ $STATUS -eq 124 ]; then
        echo "TIME_LIMIT_EXCEEDED"
        exit 1
    elif [ $STATUS -eq 139 ] || [ $STATUS -eq 134 ] || [ $STATUS -eq 137 ]; then
        echo "MEMORY_LIMIT_EXCEEDED"
        exit 1
    elif [ $STATUS -ne 0 ]; then
        echo "RUNTIME_ERROR"
        exit 1
    fi

    # 4. Run the Custom Judge
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