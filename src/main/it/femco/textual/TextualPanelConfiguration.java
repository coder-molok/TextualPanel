package it.femco.textual;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Configurazione su cui viene eseguito il panel.
 *
 */
public class TextualPanelConfiguration {
    private final InputStream sin;
    private final PrintStream sout;
    boolean configured = false;
    private int maxWidth = 0;
    private int maxHeight= 0;
    private int overHeight = -1;
    private boolean interrupted = true;
    private boolean openresized = false;


    public TextualPanelConfiguration(InputStream streamin, PrintStream streamout) {
        this.sin = streamin;
        this.sout= streamout;
    }

    public TextualPanelConfiguration(InputStream streamin, PrintStream streamout,
                                    int maxwidth, int maxheight, int overheight,
                                    int lineinterrupted) {
        this(streamin, streamout);
        
        this.maxWidth = maxwidth;
        this.maxHeight= maxheight;
        this.overheight = overheight;
        this.interrupted= lineinterrupted;
    }

    public TextualPanelConfiguration(InputStream configurationStream) {
        // TODO read stream and valorize a properties object then get values
    }

    /**
     * Questo metodo statico permette di rimandare il caricamento della configurazione.
     */
    static public TextualPanelConfiguration getUnconfigured() {
        BufferedInputOutputStream bs = new BufferedInputOutputStream();
        return new TextualPanelConfiguration(new InputStream(bs), new PrintStream(bs));
    }

    public OutputStream save() {
        // TODO create a stream with all the stringifyable values
        return null;
    }
    
    public InputStream getInput() {
        return this.sin;
    }
    public PrintStream getOutput() {
        return this.sout;
    }
    
    public int maxColumns() {
        return maxWidth;
    }

    public int maxRows() {
        return maxHeight;
    }
}
