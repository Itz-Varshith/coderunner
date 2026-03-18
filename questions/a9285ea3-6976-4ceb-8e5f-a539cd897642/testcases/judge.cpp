#include <bits/stdc++.h>
using namespace std;

int main(int argc, char* argv[]) {

    if(argc < 2) return 1;

    string testcasePath = argv[1];

    ifstream testcase(testcasePath);

    string expected;
    getline(testcase, expected);

    string userOutput;
    getline(cin, userOutput);

    if(expected == userOutput)
        return 0;

    return 1;
}
