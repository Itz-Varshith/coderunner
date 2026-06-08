import java.io.*;
import java.util.*;

public class Letsgooo {
    public static void main(String[] args) throws IOException {
        // Fast I/O is MANDATORY for 50,000 integers in Java
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out)));

        String line = br.readLine();
        if (line == null) return;
        int n = Integer.parseInt(line.trim());

        int[] nums = new int[n];
        int count = 0;
        
        // Efficiently parse space-separated integers
        while (count < n) {
            StringTokenizer st = new StringTokenizer(br.readLine());
            while (st.hasMoreTokens()) {
                nums[count++] = Integer.parseInt(st.nextToken());
            }
        }

        // Shuffle the array to prevent O(N^2) worst-case on sorted data
        shuffle(nums);
        quickSort(nums, 0, n - 1);

        // Fast printing using PrintWriter
        for (int i = 0; i < n; i++) {
            out.print(nums[i] + (i == n - 1 ? "" : " "));
        }
        out.println();
        out.flush();
        out.close();
    }

    private static void quickSort(int[] arr, int low, int high) {
        if (low < high) {
            int p = partition(arr, low, high);
            quickSort(arr, low, p);
            quickSort(arr, p + 1, high);
        }
    }

    private static int partition(int[] arr, int low, int high) {
        int pivot = arr[low];
        int i = low - 1;
        int j = high + 1;
        while (true) {
            do { i++; } while (arr[i] < pivot);
            do { j--; } while (arr[j] > pivot);
            if (i >= j) return j;
            int temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
        }
    }

    // Fisher-Yates shuffle to guarantee O(N log N) on any input
    private static void shuffle(int[] arr) {
        Random rnd = new Random();
        for (int i = arr.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            int a = arr[index];
            arr[index] = arr[i];
            arr[i] = a;
        }
    }
}
