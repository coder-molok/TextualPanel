package it.femco.textual;

import java.io.InputStream;
import java.io.PrintStream;

public class PanelLinux extends PanelBasic implements Panel {
    static public Panel getPanel(InputStream streamin, PrintStream streamout) {
        TextualPanelConfiguration linuxconf = new TextualPanelConfiguration(streamin, streamout);

        return new PanelLinux(linuxconf);
    }

    public PanelLinux(TextualPanelConfiguration conf) { super(conf);}
    public PanelLinux() {
        super();
    }

}
