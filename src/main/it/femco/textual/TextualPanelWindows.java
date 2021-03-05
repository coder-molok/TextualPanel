package it.femco.textual;

import java.io.InputStream;
import java.io.PrintStream;

public class TextualPanelWindows extends TextualPanelBasic implements TextualPanel {
    static public TextualPanel getPanel(InputStream streamin, PrintStream streamout) {
        TextualPanelConfiguration winconf = new TextualPanelConfiguration(streamin, streamout);

        return new TextualPanelWindows(winconf);
    }

    static public TextualPanel getPanel() {
        TextualPanelConfiguration winconf = new TextualPanelConfiguration(System.in, System.out);

        return new TextualPanelWindows(winconf);
    }

    public TextualPanelWindows(TextualPanelConfiguration conf) {
        super(conf);
    }

    public TextualPanelWindows() {
        super();
    }

}
