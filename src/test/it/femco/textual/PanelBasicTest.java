package it.femco.textual;

import junit.framework.TestCase;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * @author Molok
 * @version 15/05/21 10:45
 */
public class PanelBasicTest extends TestCase {

    Panel test_panel;
    PipedOutputStream test_input = new PipedOutputStream();
    PipedInputStream test_output = null;

    public void setUp() throws Exception {
        super.setUp();
        PipedInputStream sin = new PipedInputStream(test_input);
        PipedOutputStream pout = new PipedOutputStream();
        test_output = new PipedInputStream(pout);
        PrintStream sout = new PrintStream(pout);

        Configuration config = new Configuration(sin, sout);
        test_panel = new PanelBasic(config);
    }


    public void testInputChar() {
        try {
            test_input.write('p');
        } catch (IOException e) {
            e.printStackTrace();
            fail("something is wrong in the test.");
        }
        assertEquals("single character not read", 'p', test_panel.inputChar());

        try {
            test_input.write("testo lungo di test.".getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
            fail("something is wrong in the test.");
        }
        StringBuffer res= new StringBuffer();
        res.append(test_panel.inputChar())
                .append(test_panel.inputChar())
                .append(test_panel.inputChar())
                .append(test_panel.inputChar())
                .append(test_panel.inputChar());
        assertEquals("multiple characters not read", "testo", res.toString());
    }

    public void testWaitAChar() {
        try {
            test_input.write('p');
        } catch (IOException e) {
            e.printStackTrace();
            fail("something is wrong in the test.");
        }
        assertEquals("single character not read", 'a', test_panel.waitAChar());
        // asincrono test_input.write('a');

        try {
            test_input.write("lungo testo di test.".getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
            fail("something is wrong in the test.");
        }
        StringBuffer res= new StringBuffer();
        assertEquals("multiple characters not read", "p", test_panel.waitAChar());
        // asincrono test_input.write('p');
    }
}