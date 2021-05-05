package it.femco.textual;

import java.io.IOException;
import java.util.InputMismatchException;

public class ConfigurationTool {
    private static final boolean FOR_CONFIGURATION = false;
    private static final boolean END_CONFIGURATION = true;

    TextualPanel panel;
    TextualPanelConfiguration configuration, old;

    private ConfigurationTool(TextualPanel panel, TextualPanelConfiguration old) {
        this.panel = panel;
        this.old = old;
    }

    public static TextualPanelConfiguration doConfiguration(TextualPanel panel, TextualPanelConfiguration old) {
        ConfigurationTool me = new ConfigurationTool(panel, old);
        return me.doConfiguration().getConfiguration();
    }

    private TextualPanelConfiguration getConfiguration() {
        return this.configuration;
    }

    private ConfigurationTool doConfiguration() {
        boolean dimensioniConfermate=true;
        boolean configurazioneValida=true;
        boolean interrupted=true;
        int overHeight = -1;
        int width=50, height=10;

        // sondaggio iniziale
        panel.rawprint(panel.newline+"  It seems that your panel is not configured, yet."
                +panel.newline+"  Now start the configuration procedure:"
                +panel.newline+"  Do you know the approximate dimension of this textual window? [y/n]"
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
                    panel.rawprint("it seems that your answer isn't correct, retry please ( cols,rows ): ");
                    continue;
                }
                if (response.split(",")[0].length()==0
                        || !response.split(",")[0].matches("\\d+")) {
                    panel.rawprint("it seems that columns number isn't correct, retry please ( cols,rows ): ");
                    continue;
                }
                if (response.split(",")[1].length()==0
                        || !response.split(",")[1].matches("\\d+")) {
                    panel.rawprint("it seems that rows number isn't correct, retry please ( cols,rows ): ");
                    continue;
                }
                width = Integer.valueOf(response.split(",")[0]);
                height = Integer.valueOf(response.split(",")[1]);
                break;
            } while(true);
        } else {
            panel.rawprint(panel.newline+"  A default value will be used to configure the panel.");
        }
        panel.rawprint(String.format("Now a %d x %d panel will be configured. press a key to start");
        panel.inputChar();

        showConfiguration(FOR_CONFIGURATION, String.format(
                "Can you see the grid %d x %d ? [y/n]", width, height));
        if ('n' == panel.inputYN(c -> panel.rawprint("Only y or n:")>0?0:1)) {
            // try to get max H and W
            panel.rawprint("Check this line ...");
            showHorizontalLine(width);
            panel.rawprint("Is it broken on several linea?");
            panel.rawprint("Check the maximum ascissa in this ruler ...");



            do {
                try {
                    width = sinreader.nextInt();
                } catch (InputMismatchException e) {
                    width = -1;
                    try {
                        sin.skip(sin.available());
                        sinreader.skip(".*");
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        configurazioneValida = false;
                    }
                    sout.print("Only a valid number:");
                }
            } while (width < 0);
            showConfiguration(FOR_CONFIGURATION,"Is the grid interrupted by blank lines? [y/n]");
            risposta = '\0';
            while (risposta!='y' && risposta!='n') {
                if (risposta!='\0' && risposta!='\n' && risposta!='\r') {
                    sout.printf("Only y or n:");
                }
                try {
                    risposta = Character.toLowerCase((char) sin.read());
                } catch (IOException e) {
                    e.printStackTrace();
                    configurazioneValida = false;
                }
            }
            if (risposta=='y') {
                interrupted = false;
            }
            showConfiguration(FOR_CONFIGURATION,"Check the maximum ordinata on the top of the grid [1..number]");
            do {
                try {
                    height = sinreader.nextInt();
                } catch (InputMismatchException e) {
                    height = -1;
                    try {
                        sin.skip(sin.available());
                        sinreader.skip(".*");
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        configurazioneValida = false;
                    }
                    sout.print("Only a valid number:");
                }
            } while (height < 0);
        } else {
            // get additional height
            showConfiguration(FOR_CONFIGURATION,"How many rows you see over the grid? [0..number]");
            while (overHeight==-1) {
                try {
                    overHeight = sinreader.nextInt();
                } catch (InputMismatchException e) {
                    overHeight = -1;
                    try {
                        sin.skip(sin.available());
                        sinreader.skip(".*");
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        configurazioneValida = false;
                    }
                    sout.print("Only a number:");
                }
            }

        }
        showConfiguration(END_CONFIGURATION, "This will be your textual panel (press any key):");
        try {
            sin.read();
        } catch (IOException e) {
            e.printStackTrace();
            configurazioneValida = false;
        }
        this.configuration = new TextualPanelConfiguration(
                configuration.getInput(), configuration.getOutput(),
                width, height, overHeight, interrupted
        );
        this.configuration.validate(configurazioneValida);
        return this;
    }

    private void showConfiguration(boolean endConfiguration, String message) {
        for (int i = (configuration.overHeight()<0?100:configuration.overHeight()); i > 0; i--) {
            if (!endConfiguration) {
                sout.printf("%3d", i);
                if (i % 10 == 0) {
                    sout.print(" ==");
                } else if (i % 5 == 0) {
                    sout.print(" -");
                }
            }
            sout.println();
        }
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (y==height-4 && x==7) {
                    sout.print(message);
                    x+=(message.length()-1);
                } else {
                    char ch = ' ';
                    if (x % 2 == 0 && y % 2 == 0) {
                        ch = '+';
                    } else if (x % 2 == 0) {
                        ch = '|';
                    } else if (y % 2 == 0) {
                        ch = '-';
                    }
                    if (endConfiguration) {
                        ch = ' ';
                        if (x==0 || y==0 || x==width-1 || y==height-1) ch='#';
                    } else {
                        ch = showConfRuler(x + 1, width, y + 1, height, ch);
                    }
                    sout.print(ch);
                }
            }
            if (y < height -1 && configuration.isInterrupted()) sout.println();
        }
    }

    private void showVerticalRuler(int dim) {
        showVerticalRuler(dim, 0);
    }

    /**
     * Write a vertical ruler from bottom to top.
     * @param dim max number to start with, at the top.
     * @param stop (optional) min number to stop at, at the bottom.
     *             if stop is not provide, it stop at 0.
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
     * @param dim the length to reach.
     */
    private void showHorizontalLine(int dim) {
        panel.rawprint(panel.newline);
        for (int i=0; i<dim; i++) {
            panel.rawprint("-");
        }
        panel.rawprint(panel.newline);
    }

    /**
     * Write an horizontal ruler from left to right.
     *
     * The ruler take up at least two rows: one for the tens and the line,
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
                if (i % 1000 == 0) {
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
                if (i % 100 == 0) {
                    panel.rawprint(String.valueOf((int)Math.floor((i % 1000)/100.0)));
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
}
