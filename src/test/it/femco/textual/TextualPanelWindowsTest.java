package it.femco.textual;

import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class TextualPanelWindowsTest {
    public static class ByteArrayDelayedInputStream extends InputStream {
        List<byte[]> maindata = new ArrayList<>();
        int currentPos = 0;
        boolean stopStepMode = false;

        public ByteArrayDelayedInputStream(byte[] data) {
            int pos=0;
            while (pos < data.length) {
                int len = 1;
                while (pos+len < data.length) {
                    if (data[pos+len]=='\n') {
                        len++;
                        break;
                    }
                    len++;
                }
                byte[] subdata = Arrays.copyOfRange(data, pos, pos+len);
                pos+=len;
                if (pos < data.length && data[pos]=='\r') pos++;
                this.maindata.add(subdata);
            }
        }

        void skipOneRow() {
            if (!stopStepMode && maindata.size()>0 && currentPos>=maindata.get(0).length) {
                currentPos = 0;
                maindata.remove(0);
            }
        }

        @Override
        public int read() throws IOException {
            byte ch = -1;
            skipOneRow();
            if (maindata.size()>0 && currentPos<maindata.get(0).length) {
                ch = maindata.get(0)[currentPos++];
                skipOneRow();
            }
            return ch;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            stopStepMode = true;
            int totalBytes = super.read(b, off, len);
            stopStepMode = false;
            skipOneRow();
            return totalBytes;
        }
    }

    @Test
    public void open_ok() throws Exception {
        String preemptedinput = "y\n0\n\n";
        InputStream input = new ByteArrayInputStream(preemptedinput.getBytes());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream output = new PrintStream(new BufferedOutputStream(baos));

        TextualPanelConfiguration conf = new TextualPanelConfiguration(input, output);

        TextualPanel tp = new TextualPanelWindows(conf);

        TextualPanel opened = tp.open(10, 10);

        assertTrue(baos.toString().contains("This will be your textual panel (press any key):"));
        assertSame(tp, opened);
        assertTrue(tp.isOpened());
    }

    @Test
    public void open_reduced() throws Exception {
        String preemptedinput = "n\n15\nn\n10\n\n";
        InputStream input = new ByteArrayDelayedInputStream(preemptedinput.getBytes());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream output = new PrintStream(new BufferedOutputStream(baos));

        TextualPanelConfiguration conf = new TextualPanelConfiguration(input, output);

        TextualPanel tp = new TextualPanelWindows(conf);

        TextualPanel opened = tp.open(20, 20);

        assertTrue(baos.toString().contains("This will be your textual panel (press any key):"));
        assertSame(tp, opened);
        assertTrue(tp.isOpened());
        assertEquals("Opened undersized", tp.getLastError());
        assertEquals(15, tp.maxColumns());
        assertEquals(10, tp.maxRows());
    }

    @Test
    public void configuration() throws Exception {
        String preemptedinput = "n\n15\nn\n10\n\n";
        InputStream input = new ByteArrayDelayedInputStream(preemptedinput.getBytes());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream output = new PrintStream(new BufferedOutputStream(baos));

        TextualPanelConfiguration conf = new TextualPanelConfiguration(input, output);

        TextualPanel tp = new TextualPanelWindows(conf);

        assertTrue(tp.configure("."));

        assertTrue(baos.toString().indexOf("This will be your textual panel (press ENTER):")>-1);
        assertFalse(tp.isOpened());
        assertEquals(0, tp.maxColumns());
        assertEquals(0, tp.maxRows());
        assertSame(conf, tp.getConfiguration());
        assertEquals(15, tp.maxColumns());
        assertEquals(10, tp.maxRows());
    }

}