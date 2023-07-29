import jm.music.data.Note;
import jm.music.data.Part;
import jm.music.data.Phrase;
import jm.music.data.Score;
import jm.util.Play;

import java.util.ArrayList;

// Kern class implements the parsing of four-part SATB chorales in .krn format, as well as allowing a generated chord
// sequence to be converted to Score files. If Kern is initialized with a double ArrayList, each chord is given a
// constant duration.
public class Kern {

    // 2D ArrayLists are formatted as follows:
    // [Soprano, Alto, Tenor, Bass]
    // where each item of the list is a sequence of raw strings, time values, and pitches respectively.

    private final ArrayList<ArrayList<String>> raw; // Raw String data input .krn file
    private final ArrayList<ArrayList<Double>> times; // Time value of each item
    private final ArrayList<ArrayList<Integer>> pitch; // Pitch value of each item
    private final String fileName; // File name
    private static final int VOICES = 4;

    // Returns fileName.
    public String getFileName() {
        return fileName;
    }

    // Constructor with file name, the file is open and parsed.
    public Kern(String filename) {
        this.fileName = filename;
        In file = new In(fileName);

        raw = new ArrayList<>();
        times = new ArrayList<>();
        pitch = new ArrayList<>();

        for (int i = 0; i < VOICES; i++) {
            raw.add(new ArrayList<>());
            times.add(new ArrayList<>());
            pitch.add(new ArrayList<>());
        }

        // Read file contents
        while (!file.isEmpty()) {
            String temp = file.readString();
            // Ignore notes (notated with a ! at beginning) and measure numbers (notated with =)
            if (temp.charAt(0) != '!' && (temp.charAt(0) != '=')) {
                raw.get(0).add(temp);
                for (int i = 1; i < VOICES; i++) {
                    raw.get(i).add(file.readString());
                }
            } else {
                file.readLine();
            }
        }

        // Clean data
        for (int i = 0; i < raw.get(0).size(); i++) {

            // Ignore tandem interpretations (notated with * at beginning)
            if (raw.get(0).get(i).charAt(0) != '*') {
                String temp;
                int ind;
                for (int j = 0; j < VOICES; j++) {

                    // Remove irrelevant chars
                    temp = raw.get(j).get(i);
                    if (temp.charAt(0) == '[') temp = temp.substring(1);
                    if (temp.charAt(0) == '(') temp = temp.substring(1);
                    if (temp.charAt(temp.length() - 1) == ']') temp = temp.substring(0, temp.length() - 1);
                    if (temp.charAt(temp.length() - 1) == ';') temp = temp.substring(0, temp.length() - 1);
                    if (temp.charAt(temp.length() - 1) == 'L') temp = temp.substring(0, temp.length() - 1);
                    if (temp.charAt(temp.length() - 1) == 'J') temp = temp.substring(0, temp.length() - 1);

                    // Store index where pitch value data begins
                    ind = splice(temp);

                    // Parse data
                    if (ind > 0) {
                        pitch.get(j).add(pitch(temp.substring(ind)));
                        if (temp.charAt(ind - 1) == '.') {
                            times.get(j).add(dt(Double.parseDouble(temp.substring(0, ind))));
                        } else {
                            times.get(j).add(t(Double.parseDouble(temp.substring(0, ind))));
                        }
                    } else if (temp.equals(".")) {
                        pitch.get(j).add(0);
                        times.get(j).add(0.);
                    }
                }
            }
        }
    }

    // Alternate constructor method, used for playing generated chords. Mutating input ArrayList does not affect
    // object.
    public Kern(ArrayList<ArrayList<Integer>> pitch, ArrayList<ArrayList<Double>> time) {
        fileName = "null";
        this.pitch = pitch;
        this.times = time;
        raw = new ArrayList<>();
    }

    // Returns a formatted version of the pitch array, without rests (replaced with the notes before said rests)
    // changing the return value does not mutate the instance variable
    public ArrayList<ArrayList<Integer>> getPitch() {
        ArrayList<ArrayList<Integer>> result = new ArrayList<>();
        int lastPitch;
        for (int i = 0; i < VOICES; i++) {
            lastPitch = pitch.get(i).get(0);
            result.add(new ArrayList<>());
            for (int j = 0; j < pitch.get(i).size(); j++) {
                if (pitch.get(i).get(j) > 0) {
                    result.get(i).add(pitch.get(i).get(j));
                    lastPitch = pitch.get(i).get(j);
                } else {
                    result.get(i).add(lastPitch);
                }
            }
        }
        return result;
    }

    public ArrayList<ArrayList<Double>> getTimes() {
        return times;
    }

    // Private helper method, checks if input is letter.
    private static boolean isLet(char let) {
        return ((int) let >= 65 && (int) let <= 90) ||
                ((int) let >= 97 && (int) let <= 122);
    }

    // Private helper method, returns index where numbers change to letters (ex. "8D" returns 1, "16C" returns 2, "."
    // returns 0).
    private static int splice(String elem) {
        for (int i = 0; i < elem.length(); i++) {
            if (isLet(elem.charAt(i))) return i;
        }
        return 0;
    }

    // Private helper method, changes .krn time value to jMusic adjusted time value.
    private static double t(double raw) {
        return 4. / raw;
    }

    // Private helper method, dotted time values.
    private static double dt(double raw) {
        return t(raw) * 1.5;
    }

    // Private helper method, changes .krn pitch values to jMusic adjusted pitch values.
    private static int pitch(String name) {
        if (name.charAt(0) == 'r') return -2147483648;
        int temp = 0;
        for (int i = 1; i < name.length(); i++) {
            if (name.charAt(i) == name.charAt(0)) {
                temp++;
            }
        }
        int pitch;
        if (Character.isLowerCase(name.charAt(0))) {
            pitch = 60;
            pitch += temp * 12;
        } else {
            pitch = 48;
            pitch -= temp * 12;
        }
        pitch += note(name.charAt(0));
        if (name.charAt(name.length() - 1) == '#') pitch++;
        if (name.charAt(name.length() - 1) == '-') pitch--;
        return pitch;
    }

    // Private helper method, note name to value correlation.
    private static int note(char note) {
        if (note == 'c' || note == 'C') return 0;
        if (note == 'd' || note == 'D') return 2;
        if (note == 'e' || note == 'E') return 4;
        if (note == 'f' || note == 'F') return 5;
        if (note == 'g' || note == 'G') return 7;
        if (note == 'a' || note == 'A') return 9;
        if (note == 'b' || note == 'B') return 11;
        if (note == 'r') return -2147483648;
        throw new IllegalArgumentException("Not a Note");
    }

    // Returns a jMusic Score object of the Kern file, instrument given by argument.
    public Score getScore(int ins) {
        Phrase[] phrases = new Phrase[VOICES];
        for (int i = 0; i < VOICES; i++) {
            phrases[i] = new Phrase();
            phrases[i].setInstrument(ins);

            for (int j = 0; j < pitch.get(i).size(); j++) {
                if (pitch.get(i).get(j) != 0 && times.get(i).get(j) != 0) {
                    phrases[i].add(new Note(pitch.get(i).get(j), times.get(i).get(j)));
                }
            }
        }

        Score result = new Score();
        for (int i = 0; i < VOICES; i++) result.add(new Part(phrases[i]));

        return result;
    }

    // Tests all class methods. Command line input file name.
    public static void main(String[] args) {
        Kern testChorale = new Kern(args[0]);

        // Test getFileName
        StdOut.println(testChorale.getFileName());
        StdOut.println();

        for (int i = 0; i < VOICES; i++) {
            StdOut.println(testChorale.getTimes().get(i).toString());
        }
        StdOut.println();

        // Test toString
        for (int i = 0; i < VOICES; i++) {
            StdOut.println(testChorale.getPitch().get(i).toString());
        }

        // Test getScore
        Play.midi(testChorale.getScore(6));
    }
}
