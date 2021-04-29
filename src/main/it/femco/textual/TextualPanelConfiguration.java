package it.femco.textual;

import java.io.*;

/**
 * Configurazione su cui viene eseguito il panel.
 *
 */
public class TextualPanelConfiguration {
    private final InputStream sin;
    private final PrintStream sout;
    private boolean configured = false;
    private int maxWidth = 0;
    private int maxHeight= 0;
    private int overHeight = -1;
    private boolean interrupted = true;


    public TextualPanelConfiguration(InputStream streamin, PrintStream streamout) {
        this.sin = streamin;
        this.sout= streamout;
    }

    public TextualPanelConfiguration(InputStream streamin, PrintStream streamout,
                                     int maxwidth, int maxheight, int overheight,
                                     boolean lineinterrupted) {
        this(streamin, streamout);
        
        this.maxWidth = maxwidth;
        this.maxHeight= maxheight;
        this.overHeight = overheight;
        this.interrupted= lineinterrupted;
    }

    /**
     * This method parses a character stream and produces a configuration.
     * @param configurationStream
     * @return the configuration in accordance with the read stream.
     */
    public static TextualPanelConfiguration getConfiguration(InputStream configurationStream) {
        // TODO read stream and set a properties object then get values
        return getUnconfigured();
    }

    /**
     * This static method allows to postpone the configuration loading.
     */
    static public TextualPanelConfiguration getUnconfigured() {
        byte[] buffer = new byte[10];
        InputStream bsi = new ByteArrayInputStream(buffer);
        OutputStream bso = new ByteArrayOutputStream(10);
        return new TextualPanelConfiguration(bsi, new PrintStream(bso));
    }

    public OutputStream save() {
        // TODO create a stream with all the stringifiable values
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

    public boolean isConfigured() {
        return this.configured;
    }

    public int overHeight() {
        return this.overHeight;
    }

    public boolean isInterrupted() {
        return this.interrupted;
    }

    public void validate(boolean configurationIsValid) {
        this.configured = configurationIsValid;
    }

    public boolean sizeFromOriginal() {
        return true;
    }
}
