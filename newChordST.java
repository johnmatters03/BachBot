import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class newChordST {

    private final int kLen; // Length of K-Gram.
    private final ST<String, ST<String, Integer>> freq; // Frequency of succeeding chord table.

    private static final int VOICES = 4;

    public newChordST(int len) {
        kLen = len;
        freq = new ST<>();
    }

    // Private helper method, transforms a 2D int array into a String sequence by iterating through each array and
    // concatenating the integers as strings.
    private static String transform(int[][] distSeq, double[][] timeSeq) {
        StringBuilder res = new StringBuilder();
        for (int[] intArray : distSeq) {
            for (int anInt : intArray) {
                res.append(anInt);
            }
        }
        for (double[] doubleArray : timeSeq) {
            for (double aDouble : doubleArray) {
                res.append(aDouble);
            }
        }
        return res.toString();
    }

    // Private helper method, overrides method to transform 1D int arrays using the same method.
    private static String transform(int[] next, double[] time) {
        if (next.length != VOICES) throw new IllegalArgumentException("Incorrect transform dimensions");
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < VOICES; i++) res.append(next[i]).append(",");
        for (int i = 0; i < VOICES; i++) res.append(time[i]).append(",");
        return res.toString();
    }

    // Private helper method, Distance calculation between four integers.
    private static int[] dist(int a, int b, int c, int d) {
        return new int[]{b - a, c - b, d - c};
    }

    // Private helper method, generates a random string based on input ST weights, does not mutate input.
    private static String rand(ST<String, Integer> freqTable) {
        int totalOcc = 0;
        for (String k : freqTable) {
            totalOcc += freqTable.get(k);
        }
        int randInt = (int) (Math.random() * totalOcc);
        for (String k : freqTable) {
            for (int i = 0; i < freqTable.get(k); i++) {
                if (randInt == 0) return k;
                randInt--;
            }
        }
        throw new RuntimeException("Incorrect random generation");
    }

    public class StupidAssTuple {
        public final int[] dPitch;
        public final double[] time;

        public StupidAssTuple(int[] dDa, double[] timea) {
            dPitch = dDa;
            this.time = timea;
        }
    }

    // Private helper method, functions as an opposite transform(int[]), parses a String into array.
    private StupidAssTuple unpack(String dD) {
        List<String> items = Arrays.asList(dD.split("\\s*,\\s*"));
        int[] pitchRes = new int[VOICES];
        double[] timeRes = new double[VOICES];

        for (int i = 0; i < VOICES; i++) {
            pitchRes[i] = Integer.parseInt(items.get(i));
            timeRes[i] = Double.parseDouble(items.get(i + VOICES));
        }
        return new StupidAssTuple(pitchRes, timeRes);
    }

    // Generates a succeeding array based on input K-Gram.
    public StupidAssTuple gen(int[][] rawKey, double[][] rawTime) {
        int[][] key = new int[rawKey.length][VOICES - 1];
        for (int i = 0; i < rawKey.length; i++) {
            key[i] = dist(rawKey[i][0], rawKey[i][1], rawKey[i][2], rawKey[i][3]);
        }
        if (!freq.contains(transform(key, rawTime))) throw new IllegalArgumentException("K-Gram Does Not Appear");

        return unpack(Objects.requireNonNull(rand(freq.get(transform(key, rawTime)))));
    }

    // Analogous to the ST put() method, works with integer arrays.
    public void put(int[][] rawKey, double[][] timeSeq, int[] val, double[] valTime) {
        assert rawKey.length == kLen : "Incorrect K-Gram Length";
        assert val.length == VOICES : "Incorrect Value Length";
        int[][] key = new int[rawKey.length][VOICES - 1];
        for (int i = 0; i < rawKey.length; i++) {
            key[i] = dist(rawKey[i][0], rawKey[i][1], rawKey[i][2], rawKey[i][3]);
        }

        String tempKey = transform(key, timeSeq);
        String tempVal = transform(val, valTime);

        if (!freq.contains(tempKey)) {
            freq.put(tempKey, new ST<>());
            freq.get(tempKey).put(tempVal, 1);
        } else if (freq.get(tempKey).contains(tempVal)) {
            freq.get(tempKey).put(tempVal, freq.get(tempKey).get(tempVal) + 1);
        } else {
            freq.get(tempKey).put(tempVal, 1);
        }
    }

    // Tests class methods.
    public static void main(String[] args) {
        int[] c1 = new int[]{43, 59, 62, 67};
        int[] c2 = new int[]{55, 59, 62, 67};
        int[] c3 = new int[]{52, 60, 64, 67};
        int[] val = new int[]{0, -1, 0, 0};
        int[] val2 = new int[]{1, 0, 0, 0};

        int[][] distSeq = new int[][]{c1, c2, c3};

        ChordST testST = new ChordST(3);
        testST.put(distSeq, val);
        testST.put(distSeq, val2);

        // Expect to see about an even distribution of 0 and 1.
        StdOut.println(testST.gen(distSeq)[0]);
        StdOut.println(testST.gen(distSeq)[0]);
        StdOut.println(testST.gen(distSeq)[0]);
        StdOut.println(testST.gen(distSeq)[0]);
        StdOut.println(testST.gen(distSeq)[0]);
        StdOut.println(testST.gen(distSeq)[0]);
        StdOut.println(testST.gen(distSeq)[0]);
    }
}
