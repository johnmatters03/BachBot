import jm.music.data.Score;
import jm.util.Play;

import java.util.ArrayList;

// BachBot implements the methods needed for markov chain collection and generation based on said chains. Object BachBot
// is initialized with the K-Gram length.
public class BachBot {

    public newChordST chordST; // chordST keeps track of all the K-Grams and the changes succeeding them.
    private final int kLen; // kLen keeps track of the K-Gram length.
    private static final int VOICES = 4; // Number of voices.
    private static final int LOWER_THRESHOLD = 30;
    private static final int UPPER_THRESHOLD = 80;

    public BachBot(int kLen) {
        this.kLen = kLen;
        chordST = new newChordST(kLen);
    }

    // Static helper method, takes in a roller 2D array and mutates it based on the other argument. It "rolls" all
    // values forward by one, appending a new value based on the change array.
    private static void roll(int[][] pRoller, double[][] tRoller, int[] pChange, double[] newT) {

        // Account for when the roller is of length 1
        if (pRoller.length != 1) {

            // Move values forward, removing the value at roller[0]
            for (int i = 0; i < pRoller.length - 1; i++) {
                pRoller[i] = pRoller[i + 1].clone();
                tRoller[i] = tRoller[i + 1].clone();
            }

            tRoller[tRoller.length - 1] = newT.clone();

            // Calculate new value based on change array
            pRoller[pRoller.length - 1] = new int[VOICES];
            for (int i = 0; i < VOICES; i++) {
                pRoller[pRoller.length - 1][i] = pRoller[pRoller.length - 2][i] + pChange[i];
            }
        } else {
            for (int i = 0; i < VOICES; i++) {
                pRoller[0][i] = pRoller[0][i] + pChange[i];
            }
            tRoller[0] = newT.clone();
        }

        for (int i = 0; i < VOICES; i++) {
            if (pRoller[pRoller.length - 1][i] < LOWER_THRESHOLD && pRoller[pRoller.length - 1][i] != -2147483648) {
                for (int j = 0; j < VOICES; j++) {
                    pRoller[pRoller.length - 1][j] += 12;
                }
            } else if (pRoller[pRoller.length - 1][i] > UPPER_THRESHOLD) {
                for (int j = 0; j < VOICES; j++) {
                    pRoller[pRoller.length - 1][j] -= 12;
                }
            }
        }
    }

    // Generates a random chorale String
    public static String genID() {
        // Samples a random chorale for the initial K-Gram
        int id = (int) (Math.random() * 371);
        String name;

        if (id < 10) {
            name = "chor00" + id + ".krn";
        } else if (id < 100) {
            name = "chor0" + id + ".krn";
        } else if (id < 150) {
            name = "chor" + id + ".krn";
        } else {
            name = "chor" + (id + 1) + ".krn";
        }
        return name;
    }

    // Parses the input Kern file, adding to the newChordST instance variable, does not mutate original Kern file.
    public void add(Kern file) {

        ArrayList<ArrayList<Integer>> filePitch = file.getPitch();
        ArrayList<ArrayList<Double>> fileTime = file.getTimes();

        // Create "circular text" by appending chords to the end
        for (int i = 0; i < kLen; i++) {
            for (int j = 0; j < VOICES; j++) {
                filePitch.get(j).add(filePitch.get(j).get(i));
                fileTime.get(j).add(fileTime.get(j).get(i));
            }
        }

        // Loop through 2D array to add K-Grams and succeeding chords
        for (int i = 0; i < filePitch.get(0).size() - kLen; i++) {

            // Transpose data to 2D int array seq
            int[][] pSeq = new int[kLen][VOICES];
            double[][] tSeq = new double[kLen][VOICES];

            for (int j = 0; j < kLen; j++) {
                for (int k = 0; k < VOICES; k++) {
                    pSeq[j][k] = filePitch.get(k).get(i + j);
                    tSeq[j][k] = fileTime.get(k).get(i + j);
                }
            }

            // Calculating succeeding change
            int[] pSucceeding = new int[VOICES];
            double[] tSucceeding = new double[VOICES];

            for (int j = 0; j < VOICES; j++) {
                pSucceeding[j] = filePitch.get(j).get(i + kLen) - filePitch.get(j).get(i + kLen - 1);
                tSucceeding[j] = fileTime.get(j).get(i + kLen);
            }

            chordST.put(pSeq, tSeq, pSucceeding, tSucceeding);
        }
    }

    // Generates chord sentence, length based on input, initial K-Gram based on input.
    public Kern generate(int length, String name) {

        // Sample first K-Gram to roller
        Kern initialKern = new Kern(name);
        int[][] pRoller = new int[kLen][VOICES];
        double[][] tRoller = new double[kLen][VOICES];
        for (int i = 0; i < kLen; i++) {
            for (int j = 0; j < VOICES; j++) {
                pRoller[i][j] = initialKern.getPitch().get(j).get(i);
                tRoller[i][j] = initialKern.getTimes().get(j).get(i);
            }
        }

        // Add initial roller results to results array
        ArrayList<ArrayList<Integer>> pResult = new ArrayList<>();
        ArrayList<ArrayList<Double>> tResult = new ArrayList<>();
        for (int i = 0; i < VOICES; i++) {
            pResult.add(new ArrayList<>());
            tResult.add(new ArrayList<>());
            for (int j = 0; j < kLen; j++) {
                pResult.get(i).add(pRoller[j][i]);
                tResult.get(i).add(tRoller[j][i]);
            }
        }

        // Generate chord, output generation
        for (int i = 0; i < length; i++) {
            //StdOut.println();
            //StdOut.println(i);

            newChordST.StupidAssTuple temp = chordST.gen(pRoller, tRoller);
            roll(pRoller, tRoller, temp.dPitch, temp.time);

            for (int j = 0; j < VOICES; j++) {
                pResult.get(j).add(pRoller[pRoller.length - 1][j]);
                tResult.get(j).add(temp.time[j]);
            }
            //StdOut.println(Arrays.deepToString(pRoller));
            //StdOut.println(Arrays.deepToString(tRoller));
        }
        return new Kern(pResult, tResult);
    }

    // Tests class methods, command line input K-Gram length and generated length
    public static void main(String[] args) {

        // Tests BachBot, loads 5 chorales as example
        BachBot tester = new BachBot(Integer.parseInt(args[0]));
        tester.add(new Kern("chor001.krn"));
        tester.add(new Kern("chor002.krn"));
        tester.add(new Kern("chor003.krn"));
        tester.add(new Kern("chor004.krn"));
        tester.add(new Kern("chor005.krn"));

        // Generates sample
        Kern generated = tester.generate(20, "chor001.krn");
        Score score = generated.getScore(6);
        score.setTempo(100);
        Play.midi(score);
    }
}
