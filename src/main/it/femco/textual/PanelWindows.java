package it.femco.textual;

import java.io.InputStream;
import java.io.PrintStream;

public class PanelWindows extends PanelBasic implements Panel {
    static public Panel getPanel(InputStream streamin, PrintStream streamout) {
        TextualPanelConfiguration winconf = new TextualPanelConfiguration(streamin, streamout);

        return new PanelWindows(winconf);
    }

    static public Panel getPanel() {
        TextualPanelConfiguration winconf = new TextualPanelConfiguration(System.in, System.out);

        return new PanelWindows(winconf);
    }

    public PanelWindows(TextualPanelConfiguration conf) {
        super(conf);
    }

    public PanelWindows() {
        super();
    }

}
