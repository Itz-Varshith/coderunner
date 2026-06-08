# Your code here
import sys

# Increase recursion depth for deep Merge Sort trees
sys.setrecursionlimit(200000)

def merge_sort(arr):
    if len(arr) <= 1:
        return arr
    
    mid = len(arr) // 2
    left = merge_sort(arr[:mid])
    right = merge_sort(arr[mid:])
    
    return merge(left, right)

def merge(left, right):
    result = []
    i = j = 0
    
    while i < len(left) and j < len(right):
        if left[i] <= right[j]:
            result.append(left[i])
            i += 1
        else:
            result.append(right[j])
            j += 1
            
    # Append remaining elements
    result.extend(left[i:])
    result.extend(right[j:])
    return result

def solve():
    # Fast I/O is critical for 50,000 integers
    input_data = sys.stdin.read().split()
    if not input_data:
        return
    
    n = int(input_data[0])
    nums = list(map(int, input_data[1:]))

    sorted_nums = merge_sort(nums)
    
    # Efficiently print 50k numbers joined by spaces
    sys.stdout.write(" ".join(map(str, sorted_nums)) + "\n")

if __name__ == "__main__":
    solve()
