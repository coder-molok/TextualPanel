import it.femco.textual.*;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class TextualPanel {

    static Logger log = Logger.getLogger("TextualPanel");
    static {
        String logfile = System.getProperty("java.io.tmpdir")+ "textualpanel.log";
        try {
            log.addHandler(new FileHandler(logfile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        TextualPanelConfiguration conf = new TextualPanelConfiguration(System.in, System.out);
        Panel tp = TextualPanel.getPanel(conf);

        runDemo(tp);
    }

    public static Panel getPanel(TextualPanelConfiguration config) {
        // identify the operating system
        String OS = System.getProperty("os.name");
        if (OS.toLowerCase().indexOf("win") >= 0) {
            log.info("Run on Windows...");

            return new PanelWindows(config);

        } else if (OS.toLowerCase().indexOf("linux") >= 0) {
            log.info("Run on Linux...");

            return new PanelLinux(config);

        } else {
            log.info("Operating System '"+OS+"' not supported, basic TP available.");

            return new PanelBasic(config);
        }
    }

    private static void runDemo(Panel tp) {
        tp.open(140, 40);
    }
}
