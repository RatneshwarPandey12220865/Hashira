import java.io.*;
import java.util.*;
import org.json.JSONObject;

public class Main {

    // Convert base-n string to decimal
    public static long convertToDecimal(String val, int base) {
        long result = 0;
        for (char ch : val.toCharArray()) {
            int digit = Character.isDigit(ch) ? ch - '0' : Character.toLowerCase(ch) - 'a' + 10;
            result = result * base + digit;
        }
        return result;
    }

    // Lagrange interpolation to find f(0)
    public static double lagrangeInterpolation(List<long[]> points) {
        int k = points.size();
        double secret = 0;

        for (int i = 0; i < k; i++) {
            long xi = points.get(i)[0];
            long yi = points.get(i)[1];

            double term = yi;
            for (int j = 0; j < k; j++) {
                if (i == j) continue;
                long xj = points.get(j)[0];

                // Avoid division by zero
                if (xi == xj) return Double.NaN;

                term *= (0.0 - xj) / (xi - xj);
            }

            secret += term;
        }

        return secret;
    }

    // Generate all combinations of k points from n
    public static void combine(List<long[]> arr, int k, int start, List<long[]> temp, List<List<long[]>> result) {
        if (temp.size() == k) {
            result.add(new ArrayList<>(temp));
            return;
        }

        for (int i = start; i <= arr.size() - (k - temp.size()); i++) {
            temp.add(arr.get(i));
            combine(arr, k, i + 1, temp, result);
            temp.remove(temp.size() - 1);
        }
    }

    public static void processFile(String filename) throws Exception {
        // Step 1: Read file into string
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        br.close();

        JSONObject obj = new JSONObject(sb.toString());

        int k = obj.getJSONObject("keys").getInt("k");

        // Step 2: Collect all (x, y) points
        List<long[]> allPoints = new ArrayList<>();
        for (String key : obj.keySet()) {
            if (key.equals("keys")) continue;
            int x = Integer.parseInt(key);
            JSONObject point = obj.getJSONObject(key);
            int base = Integer.parseInt(point.getString("base"));
            String val = point.getString("value");
            long y = convertToDecimal(val, base);
            allPoints.add(new long[]{x, y});
        }

        // Step 3: Generate all combinations of size k
        List<List<long[]>> combinations = new ArrayList<>();
        combine(allPoints, k, 0, new ArrayList<>(), combinations);

        // Step 4: Try all combinations and track frequency of each secret
        Map<Long, Integer> freq = new HashMap<>();

        for (List<long[]> combo : combinations) {
            // Skip duplicates in x-values
            Set<Long> xSet = new HashSet<>();
            boolean valid = true;
            for (long[] point : combo) {
                if (!xSet.add(point[0])) {
                    valid = false;
                    break;
                }
            }
            if (!valid) continue;

            double val = lagrangeInterpolation(combo);

            if (Double.isNaN(val) || Double.isInfinite(val)) continue;

            long rounded = Math.round(val);
            freq.put(rounded, freq.getOrDefault(rounded, 0) + 1);
        }

        // Step 5: Find the most frequent constant term
        long best = -1;
        int maxCount = -1;
        for (Map.Entry<Long, Integer> e : freq.entrySet()) {
            if (e.getValue() > maxCount) {
                best = e.getKey();
                maxCount = e.getValue();
            }
        }

        if (best == -1) {
            System.out.println("No valid interpolation found for " + filename);
        } else {
            System.out.println("Secret from " + filename + " = " + best);
        }
    }

    public static void main(String[] args) throws Exception {
        processFile("testcase1.json");
        processFile("testcase2.json");
    }
}
