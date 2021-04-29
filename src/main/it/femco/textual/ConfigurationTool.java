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

        // sondaggio iniziale??

        showConfiguration(FOR_CONFIGURATION, String.format(
                "Can you see the grid %d x %d ? [y/n]", width, height));
        char risposta = '\0';
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
        if (risposta=='n') {
            // try to get max H and W
            dimensioniConfermate=false;
            showConfiguration(FOR_CONFIGURATION,"Check the maximum ascissa in the last line ruler [1..number]");
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

}
