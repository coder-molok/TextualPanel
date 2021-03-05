package it.femco.textual;

import java.io.InputStream;
import java.io.PrintStream;

public class TextualPanelLinux extends TextualPanelBasic implements TextualPanel {
    public TextualPanelLinux(InputStream streamin, PrintStream streamout) {
        super(streamin, streamout);
    }

    public TextualPanelLinux() {
        super();
    }

}
