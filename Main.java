import java.io.*;
import java.math.BigInteger;
import java.util.*;

public class PolynomialSolver {

    static BigInteger convertToBigInteger(String numString, int base) {
        return new BigInteger(numString, base);
    }

    public static void main(String[] args) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder inputBuffer = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            inputBuffer.append(line.trim());
        }

        String jsonText = inputBuffer.toString();
        int kValue = extractIntValue(jsonText, "\"k\"");
        Map<Integer, RootData> rootsMap = parseRoots(jsonText);

        List<Integer> keyList = new ArrayList<>(rootsMap.keySet());
        Collections.sort(keyList);

        BigInteger[] decodedValues = new BigInteger[kValue];
        for (int i = 0; i < kValue; i++) {
            RootData rootData = rootsMap.get(keyList.get(i));
            decodedValues[i] = convertToBigInteger(rootData.valueStr, rootData.radix);
        }

        List<BigInteger> coefficients = new ArrayList<>();
        coefficients.add(BigInteger.ONE);

        for (BigInteger rootVal : decodedValues) {
            List<BigInteger> nextCoeffs =
                    new ArrayList<>(Collections.nCopies(coefficients.size() + 1, BigInteger.ZERO));

            for (int i = 0; i < coefficients.size(); i++) {
                // coefficient for x^(i+1)
                nextCoeffs.set(i + 1, nextCoeffs.get(i + 1).add(coefficients.get(i)));
                // coefficient for x^i
                nextCoeffs.set(i, nextCoeffs.get(i).add(coefficients.get(i).multiply(rootVal).negate()));
            }
            coefficients = nextCoeffs;
        }

        System.out.print("Decoded roots: [");
        for (int i = 0; i < decodedValues.length; i++) {
            System.out.print(decodedValues[i]);
            if (i < decodedValues.length - 1) {
                System.out.print(", ");
            }
        }
        System.out.println("]");

        System.out.println("Polynomial degree = " + (kValue - 1));

        System.out.print("Output coefficients: [");
        for (int i = 0; i < coefficients.size(); i++) {
            System.out.print(coefficients.get(i));
            if (i < coefficients.size() - 1) {
                System.out.print(", ");
            }
        }
        System.out.println("]");
    }

    static class RootData {
        int radix;
        String valueStr;
    }

    static int extractIntValue(String json, String keyLabel) {
        int pos = json.indexOf(keyLabel);
        pos = json.indexOf(":", pos);
        int idx = pos + 1;

        while (!Character.isDigit(json.charAt(idx))) {
            idx++;
        }

        int start = idx;
        while (Character.isDigit(json.charAt(idx))) {
            idx++;
        }

        return Integer.parseInt(json.substring(start, idx));
    }

    static Map<Integer, RootData> parseRoots(String json) {
        Map<Integer, RootData> result = new HashMap<>();

        for (int i = 0; i < json.length(); i++) {
            if (json.charAt(i) == '"') {
                int endQuote = json.indexOf('"', i + 1);
                String token = json.substring(i + 1, endQuote);

                if ("keys".equals(token)) {
                    i = endQuote;
                    continue;
                }

                boolean isNumericKey = token.chars().allMatch(Character::isDigit);
                if (isNumericKey) {
                    int index = Integer.parseInt(token);
                    int objStart = json.indexOf("{", endQuote);
                    int objEnd = json.indexOf("}", objStart);
                    String segment = json.substring(objStart, objEnd + 1);

                    RootData data = new RootData();
                    data.radix = extractIntValue(segment, "\"base\"");
                    data.valueStr = extractStringValue(segment, "\"value\"");

                    result.put(index, data);
                    i = objEnd;
                }
            }
        }

        return result;
    }

    static String extractStringValue(String json, String keyLabel) {
        int pos = json.indexOf(keyLabel);
        pos = json.indexOf("\"", pos + keyLabel.length());
        int end = json.indexOf("\"", pos + 1);
        return json.substring(pos + 1, end);
    }
}
