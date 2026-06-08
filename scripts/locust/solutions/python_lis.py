# Your code here
import sys
import bisect

def solve():
    # Use fast I/O for competitive programming
    input_data = sys.stdin.read().split()
    if not input_data:
        return
    
    # First value is n, the rest are the array elements
    n = int(input_data[0])
    nums = list(map(int, input_data[1:]))

    # 'tails' will store the smallest tail for each possible length of LIS
    tails = []

    for x in nums:
        # bisect_left finds the first index where tails[i] >= x
        # This is the equivalent of std::lower_bound in C++
        idx = bisect.bisect_left(tails, x)

        if idx == len(tails):
            # x is larger than any current tail, so we extend the LIS
            tails.append(x)
        else:
            # Replace the existing tail with x to keep it as small as possible
            tails[idx] = x

    # The length of the tails array is the length of the LIS
    print(len(tails))

if __name__ == "__main__":
    solve()
