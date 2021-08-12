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

    private ConfigurationTool(Panel panel, Configuration old) {
        this.panel = panel;
        this.old = old;
        this.configuration = panel.getConfiguration();
    }

    public static Configuration doConfiguration(Panel panel, Configuration old) {
        ConfigurationTool me = new ConfigurationTool(panel, old);
        return me.doConfigurationWizard().getConfiguration();
    }

    private Configuration getConfiguration() {
        return this.configuration;
    }

    private String pressAKeyMessage(String queue) {
        if (this.configuration.isRequiredEnter()) {
            return "press ENTER "+queue;
        }
        return "press a key "+queue;
    }
    private ConfigurationTool doConfigurationWizard() {
        int width=50, height=20;

        // sondaggio iniziale
        log.fine("Start configuration wizard");
        panel.rawprint(panel.newline+"  It seems that your panel is not configured, yet."
                +panel.newline+"  Now start the configuration procedure."
                +panel.newline+"  Press a key to start (may be needed the ENTER, if nothing happen)"
        );
        panel.waitAChar();
        panel.rawprint(panel.newline+"  Do you know the approximate dimension of this textual window? [y/n]"
        );
        if ('y' == panel.inputYN(character -> panel.rawprint("Only y or n:")>0?0:1)) {
            panel.rawprint(panel.newline+"  please, insert the approximate dimension in the form:"
                    +panel.newline+"  columns,rows (integer,integer)-> "
            );
            do {
                String response = panel.inputString();
                if (response.length()==0
                || !response.contains(",")
                || response.split(",").length != 2) {
                    panel.rawprint(panel.newline+
                            "it seems that your answer isn't correct, retry please ( cols,rows ): ");
                    continue;
                }
                if (response.split(",")[0].length()==0
                        || !response.split(",")[0].matches("\\d+")) {
                    panel.rawprint(panel.newline+
                            "it seems that columns number isn't correct, retry please ( cols,rows ): ");
                    continue;
                }
                if (response.split(",")[1].length()==0
                        || !response.split(",")[1].matches("\\d+")) {
                    panel.rawprint(panel.newline+
                            "it seems that rows number isn't correct, retry please ( cols,rows ): ");
                    continue;
                }
                width = Integer.parseInt(response.split(",")[0]);
                height = Integer.parseInt(response.split(",")[1]);
                break;
            } while(true);
        } else {
            panel.rawprint(panel.newline+"  A default value will be used to configure the panel.");
        }
        log.fine(String.format("check conf for %d x %d panel", width, height));
        panel.rawprint(panel.newline+
                String.format("Now a %d x %d panel will be checked. "+pressAKeyMessage("to start"),
                        width, height));
        panel.waitAChar();

        do {
            // try to get max H and W
            panel.rawprint(panel.newline+"Check this line ...");
            do {
                showHorizontalLine(width);
                panel.rawprint(panel.newline+"Is it broken on several lines?");
                if ('y' == panel.inputYN(c -> panel.rawprint("Only y or n:")>0?0:1)) {
                    showHorizontalLineRuler(width);
                    panel.rawprint(panel.newline+"Insert the maximum abscissa on the right: ");
                    width = panel.inputUInteger(c -> panel.rawprint("Please, insert a positive integer:")>0?0:1);
                    panel.rawprint(panel.newline+"Check it again ...");
                } else {
                    showHorizontalLineRuler(width);
                    panel.rawprint(panel.newline+
                            String.format("Is there room on the right of %d ?", width));
                    if ('y' == panel.inputYN(c ->
                            panel.rawprint("Only y or n (you can answer Y and then come back):")>0?0:1)) {
                        panel.rawprint(panel.newline+
                                "Insert the number of characters that may fill the gap "+
                                "(use this ruler as reference): ");
                        showHorizontalLineRuler(width, true, false);
                        showHorizontalLineRuler(width, false, true);
                        panel.rawprint(panel.newline);
                        width += panel.inputUInteger(c -> panel.rawprint("Please, insert a positive integer:")>0?0:1);
                        panel.rawprint(panel.newline+"Check it again ...");
                    } else {
                        // check for non-wrapping terminal
                        showHorizontalLine(width, true, false);
                        showHorizontalLine(width, false, true);
                        panel.rawprint("Do you see two equal lines above? ");
                        if ('n' == panel.inputYN(c -> panel.rawprint(
                                "Only y or n:")>0?0:1)) {
                            this.configuration = this.configuration.configureInterruption(true);
                        }
                        break;
                    }
                }
            } while(true);
            panel.rawprint(panel.newline+"this is the horizontal dimension of your panel:"+panel.newline);
            showHorizontalRuler(width, configuration.isInterrupted());

            panel.rawprint(panel.newline+panel.newline+"Now let's get the vertical one!");
            panel.rawprint(panel.newline+"Check the next numbered column ("+
                    pressAKeyMessage(")"));
            panel.inputChar();
            do {
                showVerticalRuler(height, 1);
                panel.rawprint("    Does it fill the whole height?");
                if ('y' == panel.inputYN(c -> panel.rawprint(
                        "Only y or n (answer 'n' and then '0' to repeat):")>0?0:1)) {
                    showVerticalRuler(height, 1);
                    panel.rawprint("    Insert the maximum ordinate you see at the top: ");
                    int newheight = panel.inputUInteger(c -> panel.rawprint(
                            "Please, insert a positive integer:")>0?0:1);
                    if (newheight == height) {
                        break;
                    }
                    panel.rawprint(panel.newline+"It seems that "+height+
                            " was too much, set "+newheight+". Check it again ...");
                    height = newheight;
                    panel.inputChar();
                } else {
                    panel.rawprint(panel.newline+"Insert the number of lines that may fill the gap"+
                            panel.newline+"(exaggerate if you are unsure): ");
                    height += panel.inputUInteger(c -> panel.rawprint(
                            "Please, insert a positive integer:")>0?0:1);
                    panel.rawprint(panel.newline+"Check it again ..."+panel.newline);
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
                "This will be your textual panel ("+pressAKeyMessage("):"));
        panel.waitAChar();
        this.configuration.validate(true);
        return this;
    }

    private void showConfiguration(Configuration conf, boolean endConfiguration, String message) {
        Size dim = conf.getSize();
        // draw space over the panel
        panel.rawprint(panel.newline);
        for (int i = (Integer.max(0, conf.overHeight())); i > 0; i--) {
            if (!endConfiguration) {
                panel.rawprint( String.format("%3d", i));
                if (i % 10 == 0) {
                    panel.rawprint(" ==");
                } else if (i % 5 == 0) {
                    panel.rawprint(" -");
                }
            }
            panel.rawprint(panel.newline);
        }
        Pos messagePosition = new Pos(
                Integer.min(dim.w()-message.length(), 7),
                Integer.max(dim.h()-4, 0));
        if (messagePosition.x()<0) {
            // I have a very short console
            panel.rawprint(panel.newline+message);
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
            if (y < dim.h()-1 && conf.isInterrupted()) panel.rawprint(panel.newline);
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
            panel.rawprint(panel.newline);
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
        if (startingNL) panel.rawprint(panel.newline);
        for (int i=0; i<dim; i++) {
            panel.rawprint("-");
        }
        if (endingNL) panel.rawprint(panel.newline);
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
            panel.rawprint(panel.newline);
        for (int i=0; i<dim; i++) {
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
            if (needLineBreak) panel.rawprint(panel.newline);
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
            if (needLineBreak) panel.rawprint(panel.newline);
        }
        // tens
        for (int i = 1; i <= dim; i++) {
            if (i % 10 == 0) {
                panel.rawprint(String.valueOf((int)Math.floor((i % 100)/10.0)));
            } else {
                panel.rawprint("-");
            }
        }
        if (needLineBreak) panel.rawprint(panel.newline);
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

