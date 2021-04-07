package it.femco.textual;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * Motore di gestione del Panel principale.
 *
 * Non gestisce questioni pratiche come leggere informazioni dal sistema (configurazione)
 * o dal terminale (dimensioni correnti) ma può accettare una configurazione pronta da usare.
 *
 * Anche l'input e l'output sono presi dalla configurazione per permettere una gestione specifica
 * più fine, questo per contro rende necessario preparare sempre e comunque una configurazione
 * per usare questa classe.
 */
public class TextualPanelBasic implements TextualPanel {
    private static final boolean FOR_CONFIGURATION = false;
    private static final boolean END_CONFIGURATION = true;
    private final InputStream sin;
    private final PrintStream sout;
    protected Scanner sinreader;
    protected boolean openresized = false;
    protected TextualPanelConfiguration configuration = null;
    protected int width = 0, height = 0;


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
            boolean non_resized = doConfiguration();
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

    private void showRuler() {
        // vert
        for (int i = height; i > 2; i--) {
            sout.printf("%3d", i);
            if (i % 10 == 0) {
                sout.print(" ==");
            } else if (i % 5 == 0) {
                sout.print(" -");
            }
            sout.println();
        }
        // horiz
        for (int i = 1; i <= width; i++) {
            if (i == 3) {
                // print the vertical 2
                sout.print(2);
            }
            if (i > 99 && i % 10 == 9) {
                sout.print((int)Math.floor((i+1)/10.0));
            }
            if (i < 100 && i % 10 == 0) {
                sout.print((int)Math.floor(i/10.0));
            }
        }
        for (int i = 1; i <= width; i++) {
            sout.print(i % 10);
        }
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
    public boolean doConfiguration() {
        boolean dimensioniConfermate=true;
        boolean configurazioneValida=true;
        boolean interrupted=true;
        int overHeight = -1;

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
        return dimensioniConfermate;
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
}
