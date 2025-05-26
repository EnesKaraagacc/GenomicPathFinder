import java.io.File;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please provide XML file as argument.");
            return;
        }

        File xmlFile = new File(args[0]);
        AlienFlora flora = new AlienFlora(xmlFile);

        flora.readGenomes();
        flora.evaluateEvolutions();
        flora.evaluateAdaptations();
    }
}
