package it.femco.textual;

import it.femco.textual.panel.Actor;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.function.Function;

/**
 * Engine for manage the main Panel.
 *
 * This object don't manages low-level operations such as to read information from the system
 * (e.g. the configuration) or from the terminal (e.g. the terminal properties, viewport dimensions)
 * and can get an existent configuration to use.
 *
 * The input stream and the output stream, also, are obtained from configuration, this allow
 * a specific finer manage of the system; however this cause the need to prepare always a
 * configuration to generate a working panel.
 *
 * The hierarchy of implementations of {@link Panel} see this, the primary, as the
 * simpler and lean in the display managing (for each modification, it redraw all the screen)
 * for make it working on the simpler character device, even if it was a printer or similar.
 * The classes that extends this to apply to a specific device/system can implements
 * optimizations in the updates of the screen (the {@link Panel#render()} method).
 */
public class PanelBasic implements Panel {
    private final InputStream sin;
    private final PrintStream sout;
    protected Scanner sinreader;
    protected boolean openresized = false;
    protected Configuration configuration = null;
    protected int width = 0, height = 0;
    protected Actor paneltree;


    public PanelBasic(Configuration config) {
        this.configuration = config;
        // bring back streams here for convenience
        this.sin = config.getInput();
        this.sout= config.getOutput();
        this.sinreader = new Scanner(sin);
    }

    public PanelBasic() {
        this(Configuration.getUnconfigured());
    }

    public Panel open(int columns, int rows) {
        width = columns;
        height = rows;

        if (!configuration.isConfigured()) {
            configuration = ConfigurationTool.doConfiguration(this, configuration);
            boolean non_resized = configuration.isConfigured() && configuration.sizeFromOriginal();
            if (configuration.isConfigured() && !non_resized) {
                openresized = true;
            }
        } else {
            if (width > configuration.maxColumns()) {
                openresized = true;
                width = configuration.maxColumns();
            }
            if (height > configuration.maxRows()) {
                openresized = true;
                height = configuration.maxRows();
            }
        }
        this.sout.flush();

        return this;
    }

    @Override
    public boolean isOpened() {
        return configuration.isConfigured();
    }

    @Override
    public String getLastError() {
        String lastError = "";
        if (openresized) {
            lastError = "Opened undersized";
        } else if (!configuration.isConfigured()) {
            lastError = "Not configured";
        }
        return lastError;
    }

    @Override
    public int maxColumns() {
        return width;
    }

    @Override
    public int maxRows() {
        return height;
    }

    @Override
    public boolean configure(Path s) {
        Path configurationFile = null;
        InputStream configInput = null;
        if (s == null) {
            s = Paths.get(".");
        }
        if (Files.exists(s) && Files.isDirectory(s)) {
            configurationFile = s.resolve(ConfigurationTool.DEFAULT_CONFIG_FILE);
        } else if (Files.isRegularFile(s) || s.getFileName().toString().endsWith(".properties")) {
            configurationFile = s;
        } else {
            // invalid path, use "."
            configurationFile = Paths.get(".").resolve(ConfigurationTool.DEFAULT_CONFIG_FILE);
        }
        if (Files.exists(configurationFile)) {
            try {
                configInput = new FileInputStream(configurationFile.toFile());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                configInput = new ByteArrayInputStream(new byte[] {});
            }
        } else {
            configInput = new ByteArrayInputStream(new byte[] {});
        }
        this.configuration = this.configuration.getConfiguration(configInput);
        if (!configuration.isConfigured()) {
            this.configuration = ConfigurationTool.doConfiguration(this, configuration);
        }
        return this.configuration.isConfigured();
    }

    @Override
    public Configuration getConfiguration() {
        return this.configuration;
    }

    @Override
    public void render() {

    }

    @Override
    public int rawprint(CharSequence text) {
        this.sout.print(text.toString());
        return text.length();
    }

    protected boolean isValidEnterChar(char tested) {
        return (tested=='\n' || tested=='\r');
    }
    protected void wofer() {
        this.wofer(false, null);
    }
    protected void wofer(boolean isConfigurable, Character lastPressed) {
        try {
            if (!configuration.isRequiredEnterChecked() && isConfigurable) {
                int enterLength = 0;
                if (lastPressed!=null && isValidEnterChar(lastPressed.charValue())) {
                    enterLength++;
                }
                int available = sin.available();
                if (available>=1) {
                    if (sin.markSupported()) sin.mark(3);
                    char checkWithLoss = (char)sin.read();
                    if (isValidEnterChar(checkWithLoss)) {
                        enterLength++;
                        // also remove a second char
                        if (available>=2 && enterLength<2) {
                            if (sin.markSupported()) sin.mark(2);
                            checkWithLoss = (char)sin.read();
                            if (isValidEnterChar(checkWithLoss)) {
                                enterLength++;
                            }
                            else if (sin.markSupported()) sin.reset();
                        }
                    }
                    else if (sin.markSupported()) sin.reset();
                }
                if (enterLength>=0) {
                    configuration = configuration.setEnterRequired(enterLength);
                }
            } else if (configuration.isRequiredEnterChecked()) {
                // the 'enter' required is already proved, remove the line terminator
                if (configuration.isRequiredEnter()) {
                    int skippa = Integer.min(sin.available(), configuration.howMuchSkipEnterReqired());
                    sin.skip(skippa);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public char inputChar() {
        char achar;
        try {
            achar = (char) this.sin.read();
            if (achar=='\uFFFF') throw new EOFException("The input stream is ended.");
        } catch (IOException e) {
            e.printStackTrace();
            achar = '\0';
        }
        this.wofer();
        return achar;
    }

    @Override
    public char waitAChar() {
        try {
            while (sin.skip(sin.available())>0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        char achar = inputChar();
        // check for 'enter' pressed
        this.wofer(true, achar);
        return achar;
    }

    @Override
    public String inputString() {
        String input = VOID_STRING;
        try {
            input = this.sinreader.nextLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return input;
    }

    @Override
    public char inputYN(Function<Character, Integer> wrongInput) {
        char response = VOID_CHAR;
        while (response!='y' && response!='n') {
            if (response!=VOID_CHAR && response!=NEWLINE_CHAR && response!=RETURN_CHAR) {
                if (wrongInput!=null) {
                    if (wrongInput.apply(response) > 0) {
                        response = CANCEL_CHAR;
                        break;
                    }
                }
            }
            response = Character.toLowerCase(this.waitAChar());
        }
        return response;
    }

    @Override
    public int inputUInteger(Function<CharSequence, Integer> wrongInput) {
        String input = VOID_STRING;
        int response=-2;
        while (response<0) {
            if (input!=VOID_STRING) {
                if (wrongInput!=null) {
                    if (wrongInput.apply(input) > 0) {
                        response = -1;
                        break;
                    }
                }
            }
            try {
                input = this.inputString();
                // only first run, only if NL is due, a VOID_STRING is skipped
                if (response==-2 && EMPTY_STRING.equals(input)) {
                    // TODO check the NL is due
                    response=-1;
                    input = VOID_STRING;
                } else {
                    response = Integer.valueOf(input);
                    if (response < 0) {
                        // not needed, but get in evidence the user error.
                        response = -5;
                    }
                }
            } catch (ClassCastException cce) {
                response = -4;
            }
        }
        return response;
    }

}
