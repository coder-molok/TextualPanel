package it.femco.textual;

import java.util.function.Function;

/**
 * TextualPanel represent the canvas of character where a software can write a complex screen.
 *
 * It is intended to provide a complete output solution for a non-CLI textual software.
 */
public interface Panel {
    public final static char VOID_CHAR = '\0';
    public final static char NEWLINE_CHAR = '\n';
    public final static char RETURN_CHAR = '\r';
    public final static char CANCEL_CHAR = '\u0018';

    public final static String VOID_STRING = String.valueOf(VOID_CHAR);

    /**
     * facility for do new lines.
     * this allow to personalize the "new line" sequence.
     */
    public static String newline = System.lineSeparator();

    Panel open(int columns, int rows);

    boolean isOpened();

    String getLastError();

    int maxColumns();

    int maxRows();

    boolean configure(CharSequence s);

    Configuration getConfiguration();

    /**
     * Update and draw recent/new modifications to the screen.
     */
    void render();

    /*
     * Since this want to be a complete output solution, it provide also e simple raw I/O functions.
     */
    /**
     * Just write on the output.
     */
    int rawprint(CharSequence text);
    /**
     * Read only one character and return.
     *
     * @return the character pressed, in case of errors or no inputs, return VOID_CHAR;
     */
    char inputChar();
    /**
     * Read the input until a line-break is pressed.
     *
     * @return the string inserted before the line-break, in case of errors return VOID_STRING;
     */
    String inputString();
    /**
     * Wait for a Y/N response.
     *
     * Loop until the user inputs a Y or a N (case insensitive).
     * Any other input cause a 'wrongInput' call, with the input passed as parameter;
     * 'wrongInput' can return an integer > 0 for stop iteration.
     *
     * @return a character 'y' or 'n' if a valid answer was given,
     * VOID_CHAR in case of errors, CANCEL_CHAR in case of 'wrongInput' stop.
     */
    char inputYN(Function<Character, Integer> wrongInput);
    /**
     * Wait for an unsigned integer number.
     *
     * Loop until the user inputs a valid positive integer.
     * Any other input cause a 'wrongInput' call, with the input passed as parameter;
     * 'wrongInput' can return an integer > 0 for stop iteration.
     *
     * @return an unsigned integer if a valid sequence of digits was given,
     * -2 or below in case of errors, -1 in case of 'wrongInput' stop.
     */
    int inputUInteger(Function<CharSequence, Integer> wrongInput);

    /**
     * Flush the current input buffer, then wait a single char.
     *
     * Pay attention! if the input stream is a file, this method
     * will flush all the stream and then it will wait forever.
     * @return the last char read.
     */
    char waitAChar();
}
