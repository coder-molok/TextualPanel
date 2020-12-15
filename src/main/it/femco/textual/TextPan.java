package it.femco.textual;

public class TextPan {

    public static void main(String[] args) {
	    // identifico il systema operativo
        String OS = System.getProperty("os.name");
        if (OS.toLowerCase().indexOf("win") >= 0) {
            System.out.print("Run on Windows...");

            TextualPanel tp = new TextualPanelWindows();

            runDemo(tp);
        } else {
            System.out.printf("Operative System '%s' not supported.", OS);
        }
    }

    private static void runDemo(TextualPanel tp) {
        tp.open(140, 40);
    }
}
