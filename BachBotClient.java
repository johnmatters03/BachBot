import jm.gui.show.ShowScore;
import jm.music.data.Score;
import jm.util.Play;

// BachBotClient is the client class, it takes, from command line input, three integers. The first of which specify
// the BachBot K-Gram length, the second specifies to instrument type (for details refer to jMusic instrument types
// documentation, 6 = harpsichord), the third specifies the number of chords generated.
public class BachBotClient {

    // Loads in all 371 chorales, chor150.krn is missing from database. Plays chor001.krn as sample, then generates
    // based on input.
    public static void main(String[] args) {

        // Initialize BachBot
        if (Integer.parseInt(args[0]) > 40) throw new IllegalArgumentException("Unsupported K-Gram length");
        BachBot bBot = new BachBot(Integer.parseInt(args[0]));

        // Load chorales
        for (int i = 1; i < 372; i++) {
            if (i != 150) {
                if (i < 10) {
                    bBot.add(new Kern("chor00" + i + ".krn"));
                } else if (i < 100) {
                    bBot.add(new Kern("chor0" + i + ".krn"));
                } else {
                    bBot.add(new Kern("chor" + i + ".krn"));
                }
            }
        }

        // Load, play, and display chor001.krn
//        Kern chor001 = new Kern("chor001.krn");
//        Score score = chor001.getScore(6);
//        score.setTempo(100);
//        ShowScore chor = new ShowScore(score);
//        chor.saveMidi();
        // chor.openMidi();
        // Play.midi(score);

        // Generate, play, and display generated chords
        Kern generated = bBot.generate(Integer.parseInt(args[2]), BachBot.genID());
        Score newScore = generated.getScore(Integer.parseInt(args[1]));
        
        ShowScore gen = new ShowScore(newScore);
        gen.saveMidi();
        newScore.setTempo(80);
        Play.midi(newScore);
    }
}
