package it.femco.textual;

import it.femco.textual.panel.Size;

import java.io.*;

/**
 * Configuration for a {@link Panel} to work with.
 *
 */
public class Configuration {
    private InputStream sin;
    private PrintStream sout;
    private boolean configured = false;
    private int maxWidth = 0;
    private int maxHeight= 0;
    private int overHeight = -1;
    private boolean interrupted = true;

    // input configurations
    protected enum EnterRequired {
        NOT_CHECKED, // needs check
        NOT_REQUIRED, // already checked, not required
        ENTER_REQUIRED, // inputs are followed by one character to skip
        ENTER_REQUIRED_DOUBLE // inputs are followed by two characters.
    }
    EnterRequired enterIsRequired = EnterRequired.NOT_CHECKED;
    // some terminals send input only on 'enter' pressed.
    // this signal trace the need for remove lineseparator from single char input
    // it may be  triggered by a waitAChar call, watch out,
    // when a strictly 1 or 2 char following: a line separator (an enter).
    // To optimize and check this mode only once, this flag may by in different status.



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

        this.configured = true;
    }

    /**
     * Parse a charachter stream and fill this configuration.
     * @see ConfigurationTool#getConfiguration(InputStream, Configuration)
     * @param configurationStream data for replicate the configuration
     * @param instream the input stream to use instead the current ins - optional
     * @param printStream the print stream to use instead the current outs- optional
     * @return the configuration as in {@link ConfigurationTool#getConfiguration(InputStream, Configuration)}
     */
    public void getConfiguration(InputStream configurationStream,
                                 InputStream instream, PrintStream printStream) {
        if (instream != null) {
            this.sin = instream;
        }
        if (printStream != null) {
            this.sout = printStream;
        }
        this.getConfiguration(configurationStream);
    }
    public void getConfiguration(InputStream configurationStream) {
        ConfigurationTool.getFromProperties(configurationStream, this);
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

    protected Configuration cloneWith(InputStream streaminNew, PrintStream streamoutNew,
                                 Integer maxwidthNew, Integer maxheightNew,
                                 Integer overheightNew, Boolean lineinterruptedNew,
                                 EnterRequired enterIsRequiredNew, Boolean configuredNew
                                ) {
        if (streaminNew==null) streaminNew = this.sin;
        if (streamoutNew==null) streamoutNew = this.sout;
        if (maxwidthNew==null) maxwidthNew = this.maxWidth;
        if (maxheightNew==null) maxheightNew = this.maxHeight;
        if (overheightNew==null) overheightNew = this.overHeight;
        if (lineinterruptedNew==null) lineinterruptedNew = this.interrupted;
        if (enterIsRequiredNew==null) enterIsRequiredNew = this.enterIsRequired;
        if (configuredNew==null) configuredNew = this.configured;
        Configuration newConf = new Configuration(
                streaminNew, streamoutNew,
                maxwidthNew, maxheightNew,
                overheightNew, lineinterruptedNew);
        newConf.enterIsRequired = enterIsRequiredNew;
        newConf.configured = configuredNew;
        return newConf;
    }

    public Configuration configureSize(Size newSize) {
        if (this.getSize().equals(newSize)) {
            return this;
        } else {
            return this.cloneWith(null, null,
                    newSize.w(), newSize.h(), null, null,
                    null, null);
        }
    }

    public Configuration configureInterruption(boolean b) {
        if (this.isInterrupted() == b) {
            return this;
        } else {
            return this.cloneWith(null,null,
                    null,null,
                    null,b,
                    null,null);
        }
    }

    public Configuration configureOverheight(int space) {
        if (this.overHeight == space) {
            return this;
        } else {
            return this.cloneWith(null,null,
                    null,null,
                    space,null,
                    null,null);
        }
    }

    public void save(OutputStream output) {
        // create a stream with all the serializable values
        ConfigurationTool.putInProperties(this, output);
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

    public boolean isRequiredEnter() {
        return enterIsRequired==EnterRequired.ENTER_REQUIRED
            || enterIsRequired==EnterRequired.ENTER_REQUIRED_DOUBLE;
    }
    public boolean isRequiredEnterChecked() {
        return enterIsRequired!=EnterRequired.NOT_CHECKED;
    }
    public int howMuchSkipEnterReqired() {
        return (enterIsRequired==EnterRequired.ENTER_REQUIRED_DOUBLE)
                ?2
                :(enterIsRequired==EnterRequired.ENTER_REQUIRED
                ?1
                :0);
    }
    /*
     * This is an irregular setter: this flag can be only activate.
     */
    public void setEnterRequired(int howMuchCharactersToSkip) {
        enterIsRequired =
                (howMuchCharactersToSkip==0)
                ? EnterRequired.NOT_REQUIRED
                :((howMuchCharactersToSkip==1)
                ? EnterRequired.ENTER_REQUIRED
                : EnterRequired.ENTER_REQUIRED_DOUBLE);
    }
}
