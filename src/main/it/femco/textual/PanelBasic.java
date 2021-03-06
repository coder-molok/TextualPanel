package it.femco.textual;

import it.femco.textual.panel.Actor;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
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
    public boolean configure(CharSequence s) {
        return false;
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

    @Override
    public char inputChar() {
        try {
            return (char) this.sin.read();
        } catch (IOException e) {
            e.printStackTrace();
            return '\0';
        }
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
        try {
            if (!configuration.isRequiredEnterChecked()) {
                int available = sin.available();
                if (available==1 || available==2) {
                    if (sin.markSupported()) sin.mark(3);
                    char checkWithLoss = (char)sin.read();
                    if (checkWithLoss=='\n' || checkWithLoss=='\r') {
                        configuration.setEnterRequired(available);
                        // also remove a second char
                        if (available==2) {
                            checkWithLoss = (char)sin.read();
                            if (checkWithLoss!='\n' && checkWithLoss!='\r')
                                // hops... second character doesn't matter
                                configuration.setEnterRequired(1);
                        }
                    }
                    else if (sin.markSupported()) sin.reset();
                }
            } else {
                // the 'enter' required is already proved, remove the line terminator
                sin.skip(sin.available());
            }
        } catch (IOException e) {
            e.printStackTrace();
            // check ended
        }
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
            try {
                response = Character.toLowerCase((char) this.sin.read());
            } catch (IOException e) {
                e.printStackTrace();
                response = VOID_CHAR;
                break;
            }
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
                input = this.sinreader.nextLine();
                response = Integer.valueOf(input);
                if (response < 0) {
                    // not needed, but get in evindence the user error.
                    response = -5;
                }
            } catch (ClassCastException cce) {
                response = -4;
            } catch (Exception e) {
                e.printStackTrace();
                response = -3;
                break;
            }
        }
        return response;
    }
}
