package it.femco.textual;

import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class PanelWindowsTest {
    public static class ByteArrayDelayedInputStream extends InputStream {
        List<byte[]> maindata = new ArrayList<>();
        int currentPos = 0;
        boolean stopStepMode = false;

        public ByteArrayDelayedInputStream(byte[] data) {
            int pos=0;
            while (pos < data.length) {
                int len = 0;
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
            }
            return ch;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            skipOneRow();
            stopStepMode = true;
            int totalBytes = super.read(b, off, len);
            stopStepMode = false;
            return totalBytes;
        }
    }

    @Test
    public void configuration_ok() throws Exception {
        String preemptedinput = "\nn\nnnn\ny20\nny\n";
        InputStream input = new ByteArrayDelayedInputStream(preemptedinput.getBytes());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream output = new PrintStream(new BufferedOutputStream(baos));

        Configuration conf = new Configuration(input, output);

        Panel tp = new PanelWindows(conf);

        Panel opened = tp.open(10, 10);

        assertTrue(baos.toString().contains("This will be your textual panel (press "));
        assertSame(tp, opened);
        assertTrue(tp.isOpened());
    }

    @Test
    public void open_ok() throws Exception {
        String preemptedinput = "";
        InputStream input = new ByteArrayInputStream(preemptedinput.getBytes());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream output = new PrintStream(new BufferedOutputStream(baos));
        // put the "ok" configuration
        Configuration conf = new Configuration(input, output,
                20, 20, 0, false, 0);

        Panel tp = new PanelWindows(conf);

        Panel opened = tp.open(10, 10);

        assertEquals("", baos.toString());
        assertSame(tp, opened);
        assertTrue(tp.isOpened());
    }

    @Test
    public void open_reduced() throws Exception {
        String preemptedinput = "n\n15\nn\n10\n\n";
        InputStream input = new ByteArrayDelayedInputStream(preemptedinput.getBytes());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream output = new PrintStream(new BufferedOutputStream(baos));

        Configuration conf = new Configuration(input, output,
                15, 15, 0, false, 0);

        Panel tp = new PanelWindows(conf);

        Panel opened = tp.open(20, 20);

        assertEquals("", baos.toString());
        assertSame(tp, opened);
        assertTrue(tp.isOpened());
        assertEquals("Opened undersized", tp.getLastError());
        assertEquals(15, tp.maxColumns());
        assertEquals(15, tp.maxRows());
    }

    @Test
    public void configuration_known_dims() throws Exception {
        String preemptedinput = "\ny15,10\n\nnnn\ny10\nny\n";
        InputStream input = new ByteArrayDelayedInputStream(preemptedinput.getBytes());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream output = new PrintStream(new BufferedOutputStream(baos));

        Configuration conf = new Configuration(input, output);

        Panel tp = new PanelWindows(conf);

        assertTrue(tp.open(2, 2).isOpened());

        assertTrue(baos.toString().contains("This will be your textual panel (press "));
        assertFalse(tp.isOpened());
        assertEquals(0, tp.maxColumns());
        assertEquals(0, tp.maxRows());
        assertSame(conf, tp.getConfiguration());
        assertEquals(15, tp.maxColumns());
        assertEquals(10, tp.maxRows());
    }

    @Test
    public void configuration_with_void_file() throws Exception {
        String preemptedinput = "";
        InputStream input = new ByteArrayDelayedInputStream(preemptedinput.getBytes());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream output = new PrintStream(new BufferedOutputStream(baos));

        Configuration conf = new Configuration(input, output);

        Panel tp = new PanelWindows(conf);

        assertTrue(tp.configure());

        assertTrue(baos.toString().indexOf("This will be your textual panel (press ")>-1);
        assertFalse(tp.isOpened());
        assertEquals(0, tp.maxColumns());
        assertEquals(0, tp.maxRows());
        assertSame(conf, tp.getConfiguration());
        assertEquals(15, tp.maxColumns());
        assertEquals(10, tp.maxRows());
    }

}