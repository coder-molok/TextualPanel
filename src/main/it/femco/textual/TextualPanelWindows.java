package it.femco.textual;

import java.io.InputStream;
import java.io.PrintStream;

public class TextualPanelWindows extends TextualPanelBasic implements TextualPanel {
    public TextualPanelWindows(InputStream streamin, PrintStream streamout) {
        super(streamin, streamout);
    }

    public TextualPanelWindows() {
        super();
    }

}
