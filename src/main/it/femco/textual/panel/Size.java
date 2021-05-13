package it.femco.textual.panel;

/**
 * @author Molok
 * @version 30/04/21 01:17
 */
public class Size {
    private int width, height;

    public Size(int w, int h) {
        this.width = w;
        this.height = h;
    }

    public int getWidth() {
        return width;
    }

    public int w() { return this.getWidth(); }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public int h() { return this.getHeight(); }

    public void setHeight(int height) {
        this.height = height;
    }
}
