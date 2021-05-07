package it.femco.textual.panel;

import java.util.Observable;

/**
 * @author Molok
 * @version 30/04/21 01:27
 */
public class Pos {
    private int x, y;

    public Pos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
