package it.femco.textual;

import it.femco.textual.panel.Pos;
import it.femco.textual.panel.Size;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Properties;
import java.util.logging.Logger;

public class ConfigurationTool {
    private static final boolean FOR_CONFIGURATION = false;
    private static final boolean END_CONFIGURATION = true;

    public static String DEFAULT_CONFIG_FILE = "textualpanel.properties";

    protected static final String MINIMODE_FLAG = "--minimode--";
    public static int MINIMODE_MAX_WIDTH = 49;
    // Properties props
    public static final String PROP_MAX_WIDTH = "dimensions.max_width";
    public static final String PROP_MAX_HEIGHT = "dimensions.max_height";
    public static final String PROP_OVERHEIGHT = "dimensions.overheight";
    public static final String PROP_LINE_INTR = "behaviour.line_interruption";
    public static final String PROP_ENTER_REQ = "behaviour.enter_required";
    public static final String PROP_CONFIGURED = "status.configured";

    protected static Logger log = Logger.getLogger(ConfigurationTool.class.getCanonicalName());

    Panel panel;
    Configuration configuration, old;
    /*
        Mini-mode is a way to configure very little display.
     */
    boolean minimode;
    public static Properties messages = setupMessages(false);

    private ConfigurationTool(Panel panel, Configuration old) {
        this.panel = panel;
        this.old = old;
        this.configuration = panel.getConfiguration();
        this.minimode = this.checkMiniMode();
    }

    public static Configuration doConfiguration(Panel panel, Configuration old) {
        ConfigurationTool me = new ConfigurationTool(panel, old);
        return me.doConfigurationWizard().getConfiguration();
    }

    private Configuration getConfiguration() {
        return this.configuration;
    }

    private static Properties setupMessages(boolean minimode) {
        Properties setup = new Properties();

        // set this property as a flag
        if (minimode) setup.setProperty(MINIMODE_FLAG, "mini");

        // pressAKeyMessage
        setup.setProperty("enter", minimode? "ENTER"  :"press ENTER");
        setup.setProperty("a key", minimode? "a KEY"  :"press a key");

        // WIZARD
        setup.setProperty("intro.1", "  It seems that your panel is not configured, yet.");
        setup.setProperty("intro.2", "  Now start the configuration procedure.");
        setup.setProperty("intro.3", "  Press a key to start (may be needed the ENTER, if nothing happen)");

        setup.setProperty("intro.4+e", "This is a very little term. press %s when you see: '"+Panel.VLT_CONTINUE_CHAR+"'");

        setup.setProperty("dimension.survey.1", "  Do you know the approximate dimension of this textual window? [y/n]");
        setup.setProperty("dimension.survey.2", "  please, insert the approximate dimension in the form:");
        setup.setProperty("dimension.survey.3", "  columns,rows (integer,integer)-> ");
        setup.setProperty("dimension.survey.4", "  A default value will be used to configure the panel.");

        setup.setProperty("config.start+w+h+e", minimode? "Check for %dx%d, %s to start":
                        "Now a %d x %d panel will be checked. %s to start");
        setup.setProperty("config.check", "Check the line below:");
        setup.setProperty("config.restart", "Check it again ...");
        setup.setProperty("config.line.overflow", minimode? "Is it overflowed?":
                "Is it overflowed on several lines?");
        setup.setProperty("config.line.max", minimode? "Insert the first line max:":
                "Insert the maximum abscissa on the first line of the ruler: ");
        setup.setProperty("config.line.more+w", minimode? "Room after %s?":
                "Is there room on the right of %d ?");
        setup.setProperty("config.line.room_width", minimode? "How much missing?":
                "Insert the number of characters that may fill the gap "+
                        "(use this ruler as reference): ");
        setup.setProperty("config.line.wrapping", minimode? "See 2 equals lines?":
                "Do you see two equal lines below?");
        setup.setProperty("config.line.end", minimode? "Here the width:":
                "this is the horizontal dimension of your panel:");
        setup.setProperty("config.column.start", minimode? "Now the height!":
                "Now let's get the vertical one!");
        setup.setProperty("config.column.check", minimode? "Look at the ruler":
                "Check the next numbered column");
        setup.setProperty("config.column.fit", minimode? "if fill the height":
                "Does it fill the whole height? ...");
        setup.setProperty("config.column.max", minimode? "Insert the max":
                "Now, insert the maximum ordinate you see at the top:");
        setup.setProperty("config.column.too+h+n", minimode? "%d is too much, try %d.":
                "It seems that %d was too much, set %d.");

        // messages of attention about input errors
        setup.setProperty("input.error.yn", minimode? "[y/n]:": "Only y or n:");
        setup.setProperty("input.error.uint", minimode? "[0..N]:":
                "Please, insert a positive integer:");
        setup.setProperty("input.error.dims", minimode? "wrong format, cols,rows :" :
                "it seems that your answer isn't correct, retry please ( cols,rows ): ");
        setup.setProperty("input.error.cols", minimode? "wrong number, cols!,rows :" :
                "it seems that columns number isn't correct, retry please ( cols,rows ): ");
        setup.setProperty("input.error.rows", minimode? "wrong number, cols,rows! :" :
                "it seems that rows number isn't correct, retry please ( cols,rows ): ");
        return setup;
    }

    private static String mesmat(String message, Object... args) {
        return String.format(messages.getProperty(message, "<"+message+"...>"), args);
    }

    private static String messag(String message) {
        return messages.getProperty(message, "<"+message+">");
    }

    private boolean checkMiniMode() {
        int maxColumns = this.configuration.maxColumns();
        if (maxColumns <= MINIMODE_MAX_WIDTH) {
            if (!messages.containsKey(MINIMODE_FLAG)) {
                messages = setupMessages(true);
                printAware(mesmat("intro.4+e", pressAKeyMessage()));
            }
            this.minimode = true;
        } else {
            if (messages.containsKey(MINIMODE_FLAG)) {
                messages = setupMessages(false);
            }
            this.minimode = true;
        }
        return this.minimode;
    }

    /*
    Print a text with careful about the width of the screen.
    If VLT, then activate a sort of pagination.
     */
    private void printAware(String text) {
        if (this.minimode) {
            int maxWidth = this.configuration.maxColumns();
            int rowsPerPage = this.configuration.maxRows();
            while (text.length()>0) {
                int rowsInPage = 0, nextRoom = 0;
                String rowText;
                while (rowsInPage < rowsPerPage) {
                    rowsInPage++;
                    if (rowsInPage==rowsPerPage) nextRoom = 1;
                    if (maxWidth-nextRoom > text.length()) {
                        rowText = text;
                        text = "";
                    } else {
                        rowText = text.substring(0, maxWidth-nextRoom);
                        text = text.substring(maxWidth-nextRoom);
                    }
                    if (nextRoom==1) rowText = rowText.concat(
                            String.valueOf(Panel.VLT_CONTINUE_CHAR));
                    this.panel.rawprint(rowText);
                }
                if (text.length()>0) panel.waitAChar();
            }
        } else {
            this.panel.rawprint(text);
        }
    }
    private String pressAKeyMessage() {
        if (this.configuration.isRequiredEnter()) {
            return messag("enter");
        }
        return messag("a key");
    }

    private ConfigurationTool doConfigurationWizard() {
        int width=80, height=20;
        boolean miniMode = false;
        // wizard messages may differ in mini-mode
        // if panel.configuration bring a number of columns < 40, setup minimode
        miniMode = checkMiniMode();

        // sondaggio iniziale
        log.fine("Start configuration wizard");
        printAware(Panel.newline+messag("intro.1")
                +Panel.newline+messag("intro.2")
                +Panel.newline+messag("intro.3")
        );
        panel.waitAChar();
        printAware(Panel.newline+messag("dimension.survey.1"));
        if ('y' == panel.inputYN(character -> {printAware(messag("input.error.yn"));return 0;})) {
            log.fine("User knows dimensions");
            printAware(Panel.newline+messag("dimension.survey.2")
                    +Panel.newline+messag("dimension.survey.3"));
            do {
                String response = panel.inputString();
                if (response.length()==0
                || !response.contains(",")
                || response.split(",").length != 2) {
                    printAware(Panel.newline+messag("input.error.dims"));
                    continue;
                }
                if (response.split(",")[0].length()==0
                        || !response.split(",")[0].matches("\\d+")) {
                    printAware(Panel.newline+messag("input.error.cols"));
                    continue;
                }
                if (response.split(",")[1].length()==0
                        || !response.split(",")[1].matches("\\d+")) {
                    printAware(Panel.newline+messag("input.error.rows"));
                    continue;
                }
                width = Integer.parseInt(response.split(",")[0]);
                height = Integer.parseInt(response.split(",")[1]);
                this.configuration = this.configuration.configureSize(new Size(width, height));
                break;
            } while(true);
        } else {
            printAware(Panel.newline+messag("dimension.survey.4"));
        }
        miniMode = checkMiniMode();
        log.fine(String.format("check conf for %d x %d panel", width, height));
        printAware(Panel.newline+mesmat("config.start+w+h+e",
                width, height, pressAKeyMessage()));
        panel.waitAChar();

        do {
            // try to get max H and W
            printAware(Panel.newline+messag("config.check"));
            do {
                printAware(Panel.newline +messag("config.line.overflow"));
                if (miniMode) {
                    panel.rawprint(Panel.newline+Panel.VLT_CONTINUE_CHAR);
                    panel.waitAChar();
                }
                showHorizontalLine(width);
                if ('y' == panel.inputYN(c -> {printAware(messag("input.error.yn"));return 0;})) {
                    printAware(Panel.newline+messag("config.line.max"));
                    if (miniMode) {
                        panel.rawprint(String.valueOf(Panel.VLT_CONTINUE_CHAR));
                        panel.waitAChar();
                    }
                    showHorizontalLineRuler(width);
                    width = panel.inputUInteger(c -> {printAware(messag("input.error.uint"));return 0;});
                    this.configuration = this.configuration.cloneWith(null, null,
                            width,null, null, null, null, null);
                    miniMode = checkMiniMode();
                    printAware(Panel.newline+messag("config.restart"));
                } else {
                    printAware(Panel.newline+
                            mesmat("config.line.more+w", width));
                    if (miniMode) {
                        panel.rawprint(String.valueOf(Panel.VLT_CONTINUE_CHAR));
                        panel.waitAChar();
                    }
                    showHorizontalLineRuler(width);
                    if ('y' == panel.inputYN(c -> {printAware(messag("input.error.yn"));return 0;})) {
                        printAware(Panel.newline+messag("config.line.room_width"));
                        showHorizontalLineRuler(width, true, false);
                        showHorizontalLineRuler(width, false, true);
                        panel.rawprint(Panel.newline);
                        width += panel.inputUInteger(c -> {printAware(messag("input.error.uint"));return 0;});
                        printAware(Panel.newline+messag("config.restart"));
                    } else {
                        // check for non-wrapping terminal
                        printAware(Panel.newline+messag("config.line.wrapping"));
                        showHorizontalLine(width, true, false);
                        showHorizontalLine(width, false, true);
                        if ('n' == panel.inputYN(c -> {printAware(messag("input.error.yn"));return 0;})) {
                            this.configuration = this.configuration.configureInterruption(true);
                        }
                        break;
                    }
                }
            } while(true);
            printAware(Panel.newline+messag("config.line.end")+Panel.newline);
            showHorizontalRuler(width, configuration.isInterrupted());
            if (miniMode) {
                panel.rawprint(String.valueOf(Panel.VLT_CONTINUE_CHAR));
                panel.waitAChar();
            }

            printAware(Panel.newline+Panel.newline+messag("config.column.start"));
            printAware(Panel.newline+messag("config.column.check"));
            if (miniMode) {
                panel.rawprint(String.valueOf(Panel.VLT_CONTINUE_CHAR));
                panel.waitAChar();
            }
            do {
                printAware(messag("config.column.fit"));
                if (miniMode) {
                    panel.rawprint(String.valueOf(Panel.VLT_CONTINUE_CHAR));
                    panel.waitAChar();
                }
                showVerticalRuler(height, 1);
                if ('y' == panel.inputYN(c -> {printAware(messag("input.error.yn"));return 0;})) {
                    printAware(messag("config.column.max"));
                    if (miniMode) {
                        panel.rawprint(String.valueOf(Panel.VLT_CONTINUE_CHAR));
                        panel.waitAChar();
                    }
                    showVerticalRuler(height, 1);
                    int newheight = panel.inputUInteger(c -> {printAware(messag("input.error.uint"));return 0;});
                    if (newheight == height) {
                        break;
                    }
                    printAware(Panel.newline+mesmat("config.column.too+h+n",height,newheight));
                    height = newheight;
                    printAware(messag("config.restart"));
                    panel.inputChar();
                } else {
                    panel.rawprint(Panel.newline+"Insert the number of lines that may fill the gap"+
                            Panel.newline+"(exaggerate if you are unsure): ");
                    height += panel.inputUInteger(c -> panel.rawprint(
                            "Please, insert a positive integer:")>0?0:1);
                    panel.rawprint(Panel.newline+"Check it again ..."+Panel.newline);
                }
            } while(true);
            this.configuration = this.configuration.configureSize(new Size(width, height));

            if (this.configuration.isInterrupted()) {
                showConfiguration(this.configuration, FOR_CONFIGURATION,
                        "Is the grid interrupted by blank lines? [y/n]");
                if ('y' == panel.inputYN(c -> panel.rawprint(
                        "Only y or n:")>0?0:1)) {
                    this.configuration = this.configuration.configureInterruption(false);
                }
            }
            showConfiguration(this.configuration, FOR_CONFIGURATION,
                    String.format("Can you see the grid %d x %d ? [y/n]", width, height));
        } while ('n' == panel.inputYN(c -> panel.rawprint("Only y or n:")>0?0:1));

        log.fine(String.format("configured with %d%s x %d panel%s",
                configuration.maxColumns(), (configuration.isInterrupted()?" [intr]":""),
                configuration.maxRows(), (configuration.isRequiredEnterChecked() && configuration.isRequiredEnter()
                ?", ENTER required":"")));
        showConfiguration(this.configuration, END_CONFIGURATION,
                "This will be your textual panel ("+pressAKeyMessage()+"):");
        panel.waitAChar();
        this.configuration.validate(true);
        return this;
    }

    private void showConfiguration(Configuration conf, boolean endConfiguration, String message) {
        Size dim = conf.getSize();
        // draw space over the panel
        panel.rawprint(Panel.newline);
        for (int i = (Integer.max(0, conf.overHeight())); i > 0; i--) {
            if (!endConfiguration) {
                panel.rawprint( String.format("%3d", i));
                if (i % 10 == 0) {
                    panel.rawprint(" ==");
                } else if (i % 5 == 0) {
                    panel.rawprint(" -");
                }
            }
            panel.rawprint(Panel.newline);
        }
        Pos messagePosition = new Pos(
                Integer.min(dim.w()-message.length(), 7),
                Integer.max(dim.h()-4, 0));
        if (messagePosition.x()<0) {
            // I have a very short console
            panel.rawprint(Panel.newline+message);
            panel.waitAChar();
        }
        for (int y = 0; y < dim.h(); y++) {
            for (int x = 0; x < dim.w(); x++) {
                if (x==messagePosition.x() && y==messagePosition.y()) {
                    x+=panel.rawprint(message)-1;
                } else {
                    char ch = ' ';
                    if (x % 3 == 0 && y % 2 == 0) {
                        ch = '+';
                    } else if (x % 3 == 0) {
                        ch = '|';
                    } else if (y % 2 == 0) {
                        ch = '-';
                    }
                    if (endConfiguration) {
                        ch = ' ';
                        if (x==0 || y==0 || x==dim.w()-1 || y==dim.h()-1) ch='#';
                    } else {
                        ch = showConfRuler(x + 1, dim.w(), y + 1, dim.h(), ch);
                    }
                    panel.rawprint(String.valueOf(ch));
                }
                // interrompo prima all'ultima riga per evitare che la risposta sposti la maschera
                if (y==dim.h()-1 && x+3>dim.w()) break;
            }
            if (y < dim.h()-1 && conf.isInterrupted()) panel.rawprint(Panel.newline);
        }
    }

    private char showConfRuler(int x, int maxx, int y, int maxy, char ch) {
        if (y == maxy-1) {
            ch = '.';
            if (x % 10 == 5) ch = ':';
            if (x % 10 == 0) ch = '0';
            if (x % 10 == 9) ch = (char) ('0'+(int)((x+1)/10));
            if (x > 97) {
                if (x % 10 == 8) ch = (char) ('0'+(int)((x+2)/100));
                if (x % 10 == 9) ch = (char) ('0'+((int)((x+1)/10))% 10);
            }
        }
        if (x == 2) ch = ' ';
        if (x == 3) ch = '.';
        int yPrint = maxy - y +1;
        if ((y-1) % 3 == 0 && y != maxy) {
            if (x == 3) {
                ch = (char) ('0'+ yPrint % 10);
            } else if (yPrint > 9 && x == 2) {
                ch = (char) ('0'+((int)(yPrint/10))% 10);
            } else if (yPrint > 99 && x == 1) {
                ch = (char) ('0'+(int)(yPrint/100));
            }
        }
        return ch;
    }

    private void showVerticalRuler(int dim) {
        showVerticalRuler(dim, 0);
    }

    /**
     * Write a vertical ruler from bottom to top.
     * @param dim max number to start with, at the top.
     * @param stop (optional) min number to stop at, at the bottom.
     *             if stop is not provide, it stop at 1.
     */
    private void showVerticalRuler(int dim, int stop) {
        for (int i = dim; i > 1 && i > stop; i--) {
            panel.rawprint(String.format("%3d", i));
            if (i % 10 == 0) {
                panel.rawprint(" ==");
            } else if (i % 5 == 0) {
                panel.rawprint(" -");
            } else {
                panel.rawprint(" |");
            }
            panel.rawprint(Panel.newline);
        }
        panel.rawprint(String.format("%3d", 1));
    }

    /**
     * Write an horizontal line of a specific length in a new line.
     *
     * This function write a NL character at the end of the line
     * to ensure the line is on an own line.
     * Use the full form to control start and ending NLs.
     * @param dim the length to reach.
     */
    private void showHorizontalLine(int dim) {
        showHorizontalLine(dim, true, true);
    }
    private void showHorizontalLine(int dim, boolean startingNL, boolean endingNL) {
        if (startingNL) panel.rawprint(Panel.newline);
        for (int i=0; i<dim; i++) {
            panel.rawprint("-");
        }
        if (endingNL) panel.rawprint(Panel.newline);
    }

    /**
     * Write an horizontal ruler of a specific length in a new line.
     *
     * Use the full form to control starting NL and the presence of numbers.
     * @param dim the length to reach.
     */
    private void showHorizontalLineRuler(int dim) {
        showHorizontalLineRuler(dim, true, true);
    }
    private void showHorizontalLineRuler(int dim, boolean startingNL, boolean numbered) {
        if (startingNL)
            panel.rawprint(Panel.newline);
        for (int i=1; i<=dim; i++) {
            if (i % 5 == 0) {
                if (i % 10 == 0)
                    panel.rawprint("|");
                else
                    panel.rawprint("+");
            } else if (numbered) {
                if (i > 995 && i % 10 == 6) {
                    panel.rawprint(String.valueOf((i+4) % 10000).substring(0,1));
                } else if (i > 96 && i % 10 == 7) {
                    panel.rawprint(String.valueOf((i+3) % 1000).substring(0,1));
                } else if (i % 10 == 8) {
                    panel.rawprint(String.valueOf((i+2) % 100).substring(0,1));
                } else if (i % 10 == 9) {
                    panel.rawprint("0");
                } else
                    panel.rawprint("-");
            } else
                panel.rawprint("-");
        }
    }

    /**
     * Write an horizontal ruler from left to right.
     *
     * There is two types of ruler: one-liner and wide,
     * see {@link ConfigurationTool#showHorizontalLineRuler(int)} for one-liner.
     * The wide ruler take up at least two rows: one for the tens and the line,
     * second for the units.
     * If dim is grater then 99, lines will be 3: hundreds, tens and units,
     * and 4 with thousands; ruler don't manages correct dim over 9999.
     * @param dim max number to end with to the right.
     * @param needLineBreak true if ruler will not reach the end of the
     *                      screen line or may be will exceeds it.
     */
    private void showHorizontalRuler(int dim, boolean needLineBreak) {
        // thousands
        if (dim > 999) {
            for (int i = 1; i <= dim; i++) {
                if (i % 10 == 0 && i>999) {
                    i+=panel.rawprint(String.valueOf((int)Math.floor(i/1000.0)))-1;
                } else {
                    panel.rawprint("_");
                }
            }
            if (needLineBreak) panel.rawprint(Panel.newline);
        }
        // hundreds
        if (dim > 99) {
            for (int i = 1; i <= dim; i++) {
                if (i % 10 == 0 && i>99) {
                    panel.rawprint(String.valueOf((int)Math.floor((i % 1000)/100.0)));
                } else {
                    panel.rawprint(" ");
                }
            }
            if (needLineBreak) panel.rawprint(Panel.newline);
        }
        // tens
        for (int i = 1; i <= dim; i++) {
            if (i % 10 == 0) {
                panel.rawprint(String.valueOf((int)Math.floor((i % 100)/10.0)));
            } else {
                panel.rawprint("-");
            }
        }
        if (needLineBreak) panel.rawprint(Panel.newline);
        // units
        for (int i = 1; i <= dim; i++) {
            panel.rawprint(String.valueOf(i % 10));
        }
    }

    /**
     * Version with explicit parameters for streams.
     * @see ConfigurationTool#getFromProperties(InputStream, Configuration)
     * @param propertiesSource data for replicate the configuration
     * @param inps the input stream to use
     * @param oups the print stream to use
     * @return the configuration as in {@link ConfigurationTool#getFromProperties(InputStream, Configuration)}
     */
    public static Configuration getFromProperties(InputStream propertiesSource, InputStream inps, PrintStream oups) {
        return getFromProperties(propertiesSource, new Configuration(inps, oups));
    }

    /**
     * This method parses a character stream and produces a configuration.
     * Because the input and print streams are not serializable or re-obtainable
     * from a char description,
     * this method get two optional parameters for in/print streams or a
     * basic configuration with these two properties.
     * If the description of the configuration reports stdin/out, then a null
     * value is accepted.
     * Other options are saved in properties files.
     *
     * @param propertiesSource data for replicate the configuration
     * @param conf object Configuration with the needed streams.
     * @return the configuration in accordance with the read stream as a clone of conf object.
     */
    public static Configuration getFromProperties(InputStream propertiesSource, Configuration conf) {
        Properties confProp = new Properties();
        if (conf == null) {
            conf = new Configuration(System.in, System.out);
        }
        try {
            confProp.load(propertiesSource);

            Integer maxwidth = Integer.parseInt(confProp.getProperty(PROP_MAX_WIDTH,"80"));
            Integer maxheight = Integer.parseInt(confProp.getProperty(PROP_MAX_HEIGHT,"25"));
            Integer overheight = Integer.parseInt(confProp.getProperty(PROP_OVERHEIGHT,"0"));
            Boolean lineinterrupted = Boolean.getBoolean(confProp.getProperty(PROP_LINE_INTR,"true"));
            Configuration.EnterRequired enterIsRequired = Configuration.EnterRequired.valueOf(
                    confProp.getProperty(PROP_ENTER_REQ, Configuration.EnterRequired.NOT_CHECKED.name()));
            Boolean configured = Boolean.getBoolean(confProp.getProperty(PROP_CONFIGURED,"false"));

            conf = conf.cloneWith(null, null,
                    maxwidth, maxheight, overheight, lineinterrupted,
                    enterIsRequired, configured);

            return conf;
        } catch (IOException e) {
            log.severe("getFromProperties can't get properties from input-stream.");
            e.printStackTrace();
        } catch (ClassCastException e) {
            log.severe("getFromProperties, properties file contain 'integer' property with wrong value.");
            e.printStackTrace();
        }
        return null;
    }
    public static void putInProperties(Configuration config, OutputStream output) {
        Properties confProp = new Properties();

        confProp.setProperty(PROP_CONFIGURED, Boolean.toString(config.isConfigured()));
        confProp.setProperty(PROP_MAX_WIDTH, Integer.toString(config.maxColumns()));
        confProp.setProperty(PROP_MAX_HEIGHT, Integer.toString(config.maxRows()));
        confProp.setProperty(PROP_OVERHEIGHT, Integer.toString(config.overHeight()));
        confProp.setProperty(PROP_LINE_INTR, Boolean.toString(config.isInterrupted()));
        confProp.setProperty(PROP_ENTER_REQ, config.getEnterIsRequired().name());

        try {
            confProp.store(output, "Textual Panel Configuration");
        } catch (IOException e) {
            log.severe("putInProperties can't put properties in output stream.");
            e.printStackTrace();
        }
    }
}

