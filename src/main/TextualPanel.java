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
        it.femco.textual.TextualPanel tp = TextualPanel.getPanel(conf);

        runDemo(tp);
    }

    public static it.femco.textual.TextualPanel getPanel(TextualPanelConfiguration config) {
        // identifico il systema operativo
        String OS = System.getProperty("os.name");
        if (OS.toLowerCase().indexOf("win") >= 0) {
            log.info("Run on Windows...");

            return new TextualPanelWindows(config);

        } else if (OS.toLowerCase().indexOf("linux") >= 0) {
            log.info("Run on Linux...");

            return new TextualPanelLinux(config);

        } else {
            log.info("Operating System '"+OS+"' not supported, basic TP available.");

            return new TextualPanelBasic(config);
        }
    }

    private static void runDemo(it.femco.textual.TextualPanel tp) {
        tp.open(140, 40);
    }
}
