package it.femco.textual;

import it.femco.textual.panel.Size;

import java.io.*;

/**
 * Configuration for a {@link Panel} to work with.
 *
 */
public class Configuration {
    private final InputStream sin;
    private final PrintStream sout;
    private boolean configured = false;
    private int maxWidth = 0;
    private int maxHeight= 0;
    private int overHeight = -1;
    private boolean interrupted = true;


    public Configuration(InputStream streamin, PrintStream streamout) {
        this.sin = streamin;
        this.sout= streamout;
    }

    public Configuration(InputStream streamin, PrintStream streamout,
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
    public static Configuration getConfiguration(InputStream configurationStream) {
        // TODO read stream and set a properties object then get values
        return getUnconfigured();
    }

    /**
     * This static method allows to postpone the configuration loading.
     */
    static public Configuration getUnconfigured() {
        byte[] buffer = new byte[10];
        InputStream bsi = new ByteArrayInputStream(buffer);
        OutputStream bso = new ByteArrayOutputStream(10);
        return new Configuration(bsi, new PrintStream(bso));
    }

    public Configuration configureSize(Size newSize) {
        if (this.getSize().equals(newSize)) {
            return this;
        } else {
            return new Configuration(this.sin, this.sout,
                    newSize.w(), newSize.h(),
                    this.overHeight, this.interrupted);
        }
    }

    public Configuration configureInterruption(boolean b) {
        if (this.isInterrupted() == b) {
            return this;
        } else {
            return new Configuration(this.sin, this.sout,
                    this.maxWidth, this.maxHeight,
                    this.overHeight, b);
        }
    }

    public Configuration configureOverheight(int space) {
        if (this.overHeight == space) {
            return this;
        } else {
            return new Configuration(this.sin, this.sout,
                    this.maxWidth, this.maxHeight,
                    space, this.interrupted);
        }
    }

    public OutputStream save() {
        // TODO create a stream with all the serializable values
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

    public Size getSize() { return new Size(this.maxWidth, this.maxHeight); }
}
