import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import org.json.JSONObject;

public class Main {

    public static void main(String[] args) throws IOException {
        String[] testFiles = {"testcase1.json", "testcase2.json"};

        for (String file : testFiles) {
            try {
                String content = new String(Files.readAllBytes(Paths.get(file)), StandardCharsets.UTF_8);
                JSONObject json = new JSONObject(content);

                JSONObject keysObject = json.getJSONObject("keys");
                int n = keysObject.getInt("n");
                int k = keysObject.getInt("k");

                Map<Integer, BigInteger> xList = new LinkedHashMap<>();
                Map<Integer, BigInteger> yList = new LinkedHashMap<>();

                for (String key : json.keySet()) {
                    if (key.equals("keys")) continue;
                    int x = Integer.parseInt(key);
                    JSONObject point = json.getJSONObject(key);
                    int base = Integer.parseInt(point.getString("base"));
                    String val = point.getString("value");
                    BigInteger y = new BigInteger(val, base);
                    xList.put(x, BigInteger.valueOf(x));
                    yList.put(x, y);
                }

                List<BigInteger> secrets = new ArrayList<>();
                List<Integer> keys = new ArrayList<>(xList.keySet());

                generateCombinations(keys, k, 0, new ArrayList<>(), combination -> {
                    List<BigInteger> xSubset = new ArrayList<>();
                    List<BigInteger> ySubset = new ArrayList<>();
                    for (int i : combination) {
                        xSubset.add(xList.get(i));
                        ySubset.add(yList.get(i));
                    }
                    BigInteger s = lagrangeInterpolation(BigInteger.ZERO, xSubset, ySubset);
                    secrets.add(s);
                });

                Map<BigInteger, Integer> freq = new HashMap<>();
                for (BigInteger s : secrets) {
                    freq.put(s, freq.getOrDefault(s, 0) + 1);
                }

                BigInteger bestSecret = null;
                int maxCount = -1;
                for (Map.Entry<BigInteger, Integer> entry : freq.entrySet()) {
                    if (entry.getValue() > maxCount) {
                        bestSecret = entry.getKey();
                        maxCount = entry.getValue();
                    }
                }

                System.out.println("Secret from " + file + " = " + bestSecret);
            } catch (Exception e) {
                System.err.println("Error processing file " + file + ": " + e.getMessage());
            }
        }
    }

    public static void generateCombinations(List<Integer> keys, int k, int start, List<Integer> temp, java.util.function.Consumer<List<Integer>> callback) {
        if (temp.size() == k) {
            callback.accept(new ArrayList<>(temp));
            return;
        }
        for (int i = start; i < keys.size(); i++) {
            temp.add(keys.get(i));
            generateCombinations(keys, k, i + 1, temp, callback);
            temp.remove(temp.size() - 1);
        }
    }

    public static BigInteger lagrangeInterpolation(BigInteger x, List<BigInteger> xi, List<BigInteger> yi) {
        BigInteger resultNumerator = BigInteger.ZERO;
        BigInteger resultDenominator = BigInteger.ONE;
        int k = xi.size();

        for (int i = 0; i < k; i++) {
            BigInteger termNumerator = yi.get(i);
            BigInteger termDenominator = BigInteger.ONE;

            for (int j = 0; j < k; j++) {
                if (i != j) {
                    termNumerator = termNumerator.multiply(x.subtract(xi.get(j)));
                    termDenominator = termDenominator.multiply(xi.get(i).subtract(xi.get(j)));
                }
            }

            
            BigInteger commonDenominator = resultDenominator.multiply(termDenominator);
            BigInteger newNumerator = resultNumerator.multiply(termDenominator).add(termNumerator.multiply(resultDenominator));
            resultNumerator = newNumerator;
            resultDenominator = commonDenominator;
        }

        
        return resultNumerator.divide(resultDenominator);
    }

}
