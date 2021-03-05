package it.femco.textual;

public class TextPan {

    public static void main(String[] args) {
        TextualPanel tp = new TextualPanelBasic();

        // identifico il systema operativo
        String OS = System.getProperty("os.name");
        if (OS.toLowerCase().indexOf("win") >= 0) {
            System.out.print("Run on Windows...");

            tp = new TextualPanelWindows();

        } else if (OS.toLowerCase().indexOf("linux") >= 0) {
            System.out.print("Run on Linux...");

            tp = new TextualPanelLinux();

        } else {
            System.out.printf("Operating System '%s' not supported.\n\n", OS);
        }
        runDemo(tp);
    }

    private static void runDemo(TextualPanel tp) {
        tp.open(140, 40);
    }
}
