package it.femco.textual;

public interface TextualPanel {
    TextualPanel open(int columns, int rows);

    boolean isOpened();

    String getLastError();

    int maxColumns();

    int maxRows();

    boolean configure(CharSequence s);

    TextualPanelConfiguration getConfiguration();
}
