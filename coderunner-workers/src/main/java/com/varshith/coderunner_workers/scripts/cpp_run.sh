#!/bin/bash

USER_CODE="user_code.cpp"
JUDGE_CODE="judge.cpp"

USER_EXEC="user_program"
JUDGE_EXEC="judge_program"

TESTCASE_DIR="./testcases/input"

USER_OUTPUT="user_output.txt"
USER_ERROR="runtime_error.txt"

echo "Compiling user code..."

g++ "$USER_CODE" -O2 -std=c++17 -o "$USER_EXEC"

if [ $? -ne 0 ]; then
    echo "USER_COMPILATION_ERROR"
    exit 1
fi

echo "Compiling judge..."

g++ "$JUDGE_CODE" -O2 -std=c++17 -o "$JUDGE_EXEC"

if [ $? -ne 0 ]; then
    echo "JUDGE_COMPILATION_ERROR"
    exit 1
fi

echo "Running testcases..."

for testcase in "$TESTCASE_DIR"/*.txt
do
    echo "Running $testcase"


    ./"$USER_EXEC" < "$testcase" > "$USER_OUTPUT" 2> "$USER_ERROR"

    STATUS=$?


    if [ $STATUS -ne 0 ]; then
        echo "RUNTIME_ERROR on $testcase"
        exit 1
    fi

    ./"$JUDGE_EXEC" "$testcase" < "$USER_OUTPUT"

    JUDGE_STATUS=$?

    if [ $JUDGE_STATUS -ne 0 ]; then
        echo "WRONG_ANSWER on $testcase"
        exit 1
    fi

done

echo "ACCEPTED"
exit 0