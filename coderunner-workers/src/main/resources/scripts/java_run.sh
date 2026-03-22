#!/bin/bash

# Force standard naming for Java submissions
USER_CODE={{user_classname}}.java
USER_EXEC={{user_classname}}

JUDGE_CODE="testcase/judge.cpp"
JUDGE_EXEC="testcase/judge_program"
TESTCASE_DIR="./testcase/input"
USER_OUTPUT="user_output.txt"
USER_ERROR="runtime_error.txt"

TIME_LIMIT={{TIME_LIMIT}}
MEMORY_LIMIT={{MEMORY_LIMIT}} # Assume this is in MB

# 1. Compile User Code
javac "$USER_CODE"
if [ $? -ne 0 ]; then
    echo "COMPILATION_ERROR"
    exit 1
fi

# 2. Execute Against Test Cases
for testcase in "$TESTCASE_DIR"/*.txt
do
    # Run the sandboxed execution
    # -Xmx sets the max heap memory.
    # -XX:+UseSerialGC uses less base memory, ideal for small CP tasks.
    timeout "$TIME_LIMIT" java -Xmx${MEMORY_LIMIT}M -XX:+UseSerialGC "$USER_EXEC" \
        < "$testcase" \
        > "$USER_OUTPUT" \
        2> "$USER_ERROR"

    STATUS=$?


    if [ $STATUS -eq 124 ]; then
        echo "TIME_LIMIT_EXCEEDED"
        exit 1
    elif [ $STATUS -ne 0 ]; then

        if grep -q "java.lang.OutOfMemoryError" "$USER_ERROR"; then
            echo "MEMORY_LIMIT_EXCEEDED"
        else
            echo "RUNTIME_ERROR"
        fi
        exit 1
    fi


    ./"$JUDGE_EXEC" "$testcase" < "$USER_OUTPUT"
    JUDGE_STATUS=$?

    if [ $JUDGE_STATUS -ne 0 ]; then
        echo "WRONG_ANSWER"
        exit 1
    fi
done


echo "ACCEPTED"
exit 0