package it.femco.textual;

import it.femco.textual.panel.TextualPanelActor;

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
 *
 * La gerarchia delle implementazioni di {@link TextualPanel} prevede che la principale,
 * questa, sia la più grezza nella gestione del buffer (ad ogni modifica ridisegna tutto)
 * in modo che possa funzionare su un banale terminale a caratteri (anche una stampante se
 * non consideriamo il consumo di carta e inchiostro un problema).
 * Le estensioni specifiche di questa classe potranno introdurre ottimizzazioni nella gestione
 * dello schermo.
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
