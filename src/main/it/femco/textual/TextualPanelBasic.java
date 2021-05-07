package it.femco.textual;

import it.femco.textual.panel.TextualPanelActor;

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
 * The hierarchy of implementations of {@link TextualPanel} see this, the primary, as the
 * simpler and lean in the display managing (for each modification, it redraw all the screen)
 * for make it working on the simpler character device, even if it was a printer or similar.
 * The classes that extends this to apply to a specific device/system can implements
 * optimizations in the updates of the screen (the {@link TextualPanel#render()} method).
 */
public class TextualPanelBasic implements TextualPanel {
    private final InputStream sin;
    private final PrintStream sout;
    protected Scanner sinreader;
    protected boolean openresized = false;
    protected TextualPanelConfiguration configuration = null;
    protected int width = 0, height = 0;
    protected TextualPanelActor paneltree;


    public TextualPanelBasic(TextualPanelConfiguration config) {
        this.configuration = config;
        // bring back streams here for convenience
        this.sin = config.getInput();
        this.sout= config.getOutput();
    }

    public TextualPanelBasic() {
        this(TextualPanelConfiguration.getUnconfigured());
    }

    public TextualPanel open(int columns, int rows) {
        width = columns;
        height = rows;
        sinreader = new Scanner(sin);

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

    private char showConfRuler(int x, int maxx, int y, int maxy, char ch) {
        if (y == maxy) {
            ch = '.';
            if (x % 10 == 5) ch = ':';
            if (x % 10 == 0) ch = '0';
            if (x % 10 == 9) ch = (char) ('0'+(int)((x+1)/10));
            if (x > 97) {
                if (x % 10 == 8) ch = (char) ('0'+(int)((x+2)/100));
                if (x % 10 == 9) ch = (char) ('0'+((int)((x+1)/10))% 10);
            }
        }
        if (x == 3) ch = ' ';
        if (x == 4) ch = '.';
        int yPrint = height - y +1;
        if ((y-1) % 3 == 0 && y != maxy) {
            if (x == 4) {
                ch = (char) ('0'+ yPrint % 10);
            } else if (yPrint > 9 && x == 3) {
                ch = (char) ('0'+((int)(yPrint/10))% 10);
            } else if (yPrint > 99 && x == 2) {
                ch = (char) ('0'+(int)(yPrint/100));
            }
        }
        return ch;
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
    public TextualPanelConfiguration getConfiguration() {
        return null;
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
            return Character.toLowerCase((char) this.sin.read());
        } catch (IOException e) {
            e.printStackTrace();
            return '\0';
        }
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
