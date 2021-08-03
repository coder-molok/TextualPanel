package it.femco.textual;

import junit.framework.TestCase;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Queue;

/**
 * @author Molok
 * @version 15/05/21 10:45
 */
public class PanelBasicTest extends TestCase {

    Panel test_panel;
    // define a virtual terminal:
    // terminal_keyb simulate the keyboard inputs, test_keyb allow to write on the keyboard.
    // terminal_disp simulate the display output, test_disp allow to read from the display.
    PipedInputStream terminal_keyb;
    PipedOutputStream test_keyb;
    PipedOutputStream terminal_disp;
    PipedInputStream test_disp;

    /**
     * Allow to append characters to the keyboard input, only after the last was read.
     */
    public static class AsynKeyboard implements Runnable {
        Exception iGotError = null;
        PipedOutputStream stream_to_keybord;
        PipedInputStream stream_from_test;
        long waitAfterRun = 1000; // ms to wait
        boolean finish = false;
        // this class manage list of byte as the user input more charachter rapidly ;-)
        Queue<byte[]> charsToWrite = new LinkedList<byte[]>();

        public AsynKeyboard(PipedOutputStream to_keyboard, PipedInputStream from_test) {
            this.stream_from_test = from_test;
            this.stream_to_keybord = to_keyboard;
        }

        public synchronized void typeOnKeyboard(byte[] charactersList) {
            synchronized (charsToWrite) {
                this.charsToWrite.add(charactersList);
                this.charsToWrite.notify();
            }
        }

        public synchronized void stop() { finish=true; }

        public Exception getError() { return iGotError; }

        @Override
        public void run() {
            while (!finish) {
                try {
                    synchronized (charsToWrite) {
                        if (charsToWrite.size()==0) {
                            charsToWrite.wait();
                        }
                    }

                    // write on the "keyboard" when there are no more chars waiting.
                    while(stream_from_test.available() >0) {
                        Thread.sleep(waitAfterRun);
                    }
                    synchronized (charsToWrite) {
                        stream_to_keybord.write(charsToWrite.poll());
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                    iGotError = e;
                }
            }
        }
    }
    public void setUp() throws Exception {
        super.setUp();
        // test_keyb allow to write on terminal_keyb.
        test_keyb = new PipedOutputStream();
        terminal_keyb = new PipedInputStream(test_keyb);
        // test_disp allow to read from terminal_disp.
        terminal_disp = new PipedOutputStream();
        test_disp = new PipedInputStream(terminal_disp);

        InputStream sin = new BufferedInputStream(terminal_keyb);
        PrintStream sout = new PrintStream(terminal_disp);

        Configuration config = new Configuration(sin, sout);
        test_panel = new PanelBasic(config);
    }


    public void testInputChar() {
        try {
            test_keyb.write('p');
        } catch (IOException e) {
            e.printStackTrace();
            fail("something is wrong in the test.");
        }
        assertEquals("single character not read", 'p', test_panel.inputChar());

        try {
            test_keyb.write("testo lungo di test.".getBytes(StandardCharsets.UTF_8));
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
        AsynKeyboard asynKeyboard = new AsynKeyboard(test_keyb, terminal_keyb);
        new Thread(asynKeyboard, "keyboard").start();

        try {
            test_keyb.write('p');
        } catch (IOException e) {
            e.printStackTrace();
            fail("something is wrong in the test.");
        }
        // asincrono test_input.write('a');
        asynKeyboard.typeOnKeyboard(new byte[] {'a'});
        assertEquals("single character not read", 'a', test_panel.waitAChar());
        assertNull(asynKeyboard.getError());

        try {
            test_keyb.write("lungo testo di test.".getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
            fail("something is wrong in the test.");
        }
        // asincrono test_input.write('p');
        asynKeyboard.typeOnKeyboard(new byte[] {'p','a','r','e','c','c','h','i'});
        assertEquals("multiple characters read", 'p', test_panel.waitAChar());
        assertNull(asynKeyboard.getError());
        try {
            assertEquals("multiple characters read", 7, test_panel.getConfiguration().getInput().available());
        } catch (IOException e) {
            e.printStackTrace();
            fail("something is wrong in the test.");
        }

        asynKeyboard.stop();
    }

    public void testWatchOutForEnterRequired() {
        // the function is immersed in the class and is not directed testable,
        // the logic is that in some terminal a single character is not send
        // without a ENTER press, in some cases (inputChar, waitAChar) the 'new line'
        // sequence is not expected and has to be removed.
        // The test must verify that the next input don't carry the unwanted newline.
        String next_input = "Next sentence.";

        try {
            // correct single char and subsequent text
            test_keyb.write(("p"+next_input+System.lineSeparator())
                    .getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
            fail("something is wrong in the test.");
        }
        assertEquals("single character not read", 'p', test_panel.inputChar());
        assertEquals("next input was wrong", next_input, test_panel.inputString());

        String first_sentence = "Casual sentence.";
        try {
            // correct string and subsequent string
            test_keyb.write((first_sentence+System.lineSeparator()
                            +next_input+System.lineSeparator())
                    .getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
            fail("something is wrong in the test.");
        }
        assertEquals("first sentence is wrong", first_sentence, test_panel.inputString());
        assertEquals("next input was wrong", next_input, test_panel.inputString());

        // this will be trigger on the wofer
        assertFalse(test_panel.getConfiguration().isRequiredEnter());
        AsynKeyboard asynKeyboard = new AsynKeyboard(test_keyb, terminal_keyb);
        new Thread(asynKeyboard, "keyboard").start();
        try {
            test_keyb.write(new byte[] {'p','a','r','o','l','a'});
        } catch (IOException e) {
            e.printStackTrace();
            fail("something is wrong in the test.");
        }
        if (System.lineSeparator().length()==2) {
            asynKeyboard.typeOnKeyboard(new byte[]{'x', '\n', '\r'});
        } else {
            asynKeyboard.typeOnKeyboard(new byte[]{'x',
                    System.lineSeparator().getBytes(StandardCharsets.UTF_8)[0]});
        }
        assertEquals("multiple characters read", 'x', test_panel.waitAChar());
        assertNull(asynKeyboard.getError());
        assertTrue(test_panel.getConfiguration().isRequiredEnter());
        asynKeyboard.stop();

        String char_accompanied = "A"+System.lineSeparator();
        try {
            // correct string and subsequent string
            test_keyb.write((char_accompanied+next_input+System.lineSeparator())
                    .getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
            fail("something is wrong in the test.");
        }
        assertEquals("first char is wrong", 'A',
                test_panel.inputChar());
        assertEquals("wofer isn't intervened", next_input,
                test_panel.inputString());
    }
}