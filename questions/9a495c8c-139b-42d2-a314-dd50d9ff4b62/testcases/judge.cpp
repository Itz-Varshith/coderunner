#include <iostream>
#include <fstream>
#include <vector>
#include <algorithm>

using namespace std;

int main(int argc, char* argv[]) {
    // 1. Validate Arguments
    if (argc < 2) {
        cout << "System Error: Missing testcase input file argument." << endl;
        return 2; 
    }

    // 2. Open and read the original input file (.in)
    ifstream fin(argv[1]);
    if (!fin.is_open()) {
        cout << "System Error: Could not open input file." << endl;
        return 2;
    }

    int n;
    if (!(fin >> n)) {
        cout << "System Error: Malformed input file." << endl;
        return 2;
    }

    vector<long long> original_array(n);
    for (int i = 0; i < n; ++i) {
        fin >> original_array[i];
    }
    fin.close();

    // 3. Read the user's output directly from standard input (cin)
    vector<long long> user_output;
    long long val;
    while (cin >> val) {
        user_output.push_back(val);
    }

    // 4. CHECK 1: Did they output the correct amount of numbers?
    if (user_output.size() != original_array.size()) {
        cout << "Output length mismatch. Expected " << original_array.size() 
             << " numbers, but got " << user_output.size() << "." << endl;
        return 1;
    }

    // 5. CHECK 2: Is the array actually sorted?
    for (size_t i = 1; i < user_output.size(); ++i) {
        if (user_output[i] < user_output[i - 1]) {
            cout << "Array is not sorted! Element at index " << i 
                 << " (" << user_output[i] << ") is smaller than previous element (" 
                 << user_output[i-1] << ")." << endl;
            return 1;
        }
    }

    // 6. CHECK 3: Did they just print fake sorted numbers, or do they match the original?
    // Since we know the user's output is sorted, if we sort the original array, 
    // they must be exactly identical arrays.
    sort(original_array.begin(), original_array.end());
    
    for (size_t i = 0; i < n; ++i) {
        if (original_array[i] != user_output[i]) {
            cout << "Elements do not match the original array. "
                 << "Expected '" << original_array[i] << "' but found '" << user_output[i] << "'." << endl;
            return 1;
        }
    }

    // 7. Everything is perfect
    cout << "OK" << endl;
    return 0;
}
