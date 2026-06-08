#include <iostream>
#include <vector>
#include <algorithm>

using namespace std;

int main() {
    // Fast I/O: crucial for competitive programming platforms to avoid TLE
    ios_base::sync_with_stdio(false);
    cin.tie(NULL);

    int n;
    // Read the size of the array
    if (!(cin >> n)) return 0;

    vector<int> nums(n);
    for (int i = 0; i < n; ++i) {
        cin >> nums[i];
    }

    // 'tails' will store the smallest tail of all increasing subsequences of length i+1.
    // Note: 'tails' itself does NOT store the actual subsequence, just the optimal tails.
    vector<int> tails;

    for (int x : nums) {
        // Binary search to find the first element in 'tails' that is >= x
        auto it = lower_bound(tails.begin(), tails.end(), x);

        // If x is greater than all elements in tails, we can extend the longest subsequence
        if (it == tails.end()) {
            tails.push_back(x);
        } 
        // Otherwise, we replace that element with x to maintain the smallest possible tail,
        // which gives us better chances to extend the subsequence later.
        else {
            *it = x;
        }
    }

    // The length of the tails array is exactly the length of the LIS
    cout << tails.size() << "\n";

    return 0;
}
