package it.femco.textual;

import junit.framework.TestCase;

import java.io.*;

/**
 * @author Molok
 * @version 15/05/21 10:45
 */
public class PanelBasicTest extends TestCase {

    Panel testpanel;
    StringWriter input = new StringWriter();

    public void setUp() throws Exception {
        super.setUp();
        InputStream sin;
        PrintStream sout;

        /*
            PipedOutputStream pos = null;
    PipedInputStream pis = null;

    try {
        DataInputStream dis = new DataInputStream(source);
        String input;

        pos = new PipedOutputStream();
        pis = new PipedInputStream(pos);
        PrintStream ps = new PrintStream(pos);

        new WriteReversedThread(ps, dis).start();

    } catch (Exception e) {
        System.out.println("RhymingWords reverse: " + e);
    }
    return pis;

        */

        sin = new BufferedInputStream(input);
        sout = new BufferedOutputStream();
        Configuration config = new Configuration(sin, sout);
        testpanel = new PanelBasic(config);
    }

    public void testInputChar() {
    }
}