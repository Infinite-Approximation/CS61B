package gh2;

import edu.princeton.cs.algs4.StdAudio;
import edu.princeton.cs.algs4.StdDraw;

public class GuitarHero {
    public GuitarString[] guitarStrings = new GuitarString[37];
    private String keyboard = "q2we4r5ty7u8i9op-[=zxdcfvgbnjmk,.;/' ";
    public GuitarHero() {
        for (int i = 0; i < 37; i++) {
            Double frequency = 440 * Math.pow(2, (i - 24) * 1.0 / 12);
            guitarStrings[i] = new GuitarString(frequency);
        }
    }

    private double sample() {
        double sum = 0.0f;
        for (int i = 0; i < guitarStrings.length; i++) {
            sum += guitarStrings[i].sample();
        }
        return sum;
    }

    private void tic() {
        for (int i = 0; i < guitarStrings.length; i++) {
            guitarStrings[i].tic();
        }
    }

    public static void main(String[] args) {
        GuitarHero guitarHero = new GuitarHero();
        while (true) {
            /* check if the user has typed a key; if so, process it */
            if (StdDraw.hasNextKeyTyped()) {
                char key = StdDraw.nextKeyTyped();
                int indexOfKey = guitarHero.keyboard.indexOf(key);
                System.out.println(indexOfKey);
                if (indexOfKey >= 0) {
                    guitarHero.guitarStrings[indexOfKey].pluck();
                }
            }
            Double sample = guitarHero.sample();
            StdAudio.play(sample);
            guitarHero.tic();
        }
    }
}
