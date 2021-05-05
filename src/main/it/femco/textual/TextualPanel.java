package it.femco.textual;

public interface TextualPanel {
    TextualPanel open(int columns, int rows);

    boolean isOpened();

    String getLastError();

    int maxColumns();

    int maxRows();

    boolean configure(CharSequence s);

    TextualPanelConfiguration getConfiguration();

    /**
     * Update and draw recent/new modifications to the screen.
     */
    void render();
}
