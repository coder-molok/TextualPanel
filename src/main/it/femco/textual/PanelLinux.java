package it.femco.textual;

import java.io.InputStream;
import java.io.PrintStream;

public class PanelLinux extends PanelBasic implements Panel {
    static public Panel getPanel(InputStream streamin, PrintStream streamout) {
        Configuration linuxconf = new Configuration(streamin, streamout);

        return new PanelLinux(linuxconf);
    }

    public PanelLinux(Configuration conf) { super(conf);}
    public PanelLinux() {
        super();
    }

}
