package it.femco.textual;

public class TextPan {

    public static void main(String[] args) {
        TextualPanelConfiguration conf = new TextualPanelConfiguration(System.in, System.out);
        TextualPanel tp = new TextualPanelBasic(conf);

        // identifico il systema operativo
        String OS = System.getProperty("os.name");
        if (OS.toLowerCase().indexOf("win") >= 0) {
            System.out.print("Run on Windows...");

            tp = new TextualPanelWindows(conf);

        } else if (OS.toLowerCase().indexOf("linux") >= 0) {
            System.out.print("Run on Linux...");

            tp = new TextualPanelLinux(conf);

        } else {
            System.out.printf("Operating System '%s' not supported.\n\n", OS);
        }

        runDemoConfiguration(tp);
    }

    private static void runDemoConfiguration(TextualPanel tp) {
        tp.open(140, 40);
    }
}
