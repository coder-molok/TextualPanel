package it.femco.textual;

import java.io.InputStream;
import java.io.PrintStream;

public class PanelWindows extends PanelBasic implements Panel {
    static public Panel getPanel(InputStream streamin, PrintStream streamout) {
        Configuration winconf = new Configuration(streamin, streamout);

        return new PanelWindows(winconf);
    }

    static public Panel getPanel() {
        Configuration winconf = new Configuration(System.in, System.out);

        return new PanelWindows(winconf);
    }

    public PanelWindows(Configuration conf) {
        super(conf);
    }

    public PanelWindows() {
        super();
    }

}
