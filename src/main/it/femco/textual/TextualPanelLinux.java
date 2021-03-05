package it.femco.textual;

import java.io.InputStream;
import java.io.PrintStream;

public class TextualPanelLinux extends TextualPanelBasic implements TextualPanel {
    static public TextualPanel getPanel(InputStream streamin, PrintStream streamout) {
        TextualPanelConfiguration linuxconf = new TextualPanelConfiguration(streamin, streamout);

        return new TextualPanelLinux(linuxconf);
    }

    public TextualPanelLinux(TextualPanelConfiguration conf) { super(conf);}
    public TextualPanelLinux() {
        super();
    }

}
