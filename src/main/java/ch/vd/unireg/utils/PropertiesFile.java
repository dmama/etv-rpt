package ch.vd.unireg.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class PropertiesFile extends Properties {
	/**
	 * A table of hex digits
	 */
	private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

	/**
	 * Some characters that must be save specially
	 */
	private static final String SPECIAL_SAVE_CHARS = "=: \t\r\n\f#!";

	/**
	 * White space characters
	 */
	private static final String WHITE_SPACE_CHARS = " \t\r\n\f";

	/**
	 * Key-value separators
	 */
	private static final String KEY_VALUE_SEPARATORS = "=: \t\r\n\f";

	/**
	 * Strict key-value separators
	 */
	private static final String STRICT_KEY_VALUE_SEPARATORS = "=:";

	/**
	 * The lines
	 */
	private List lines;

	/**
	 * A map between the key and the corresponding line number
	 */
	private Map lineNumberToKey;

	/**
	 * Set or replace a property and its value.
	 *
	 * @param key   the property key
	 * @param value the property value
	 * @return the previous value of the property
	 */
	public synchronized Object setProperty(String key, String value) {
		return put(key, value);
	}

	/**
	 * Replace a key with another, if the old key exists in the configuration, it will be removed.
	 *
	 * @param oldKey   the old key
	 * @param newKey   the new key
	 * @param newValue the new value
	 */
	public void replaceProperty(String oldKey, String newKey, String newValue) {
		remove(oldKey);
		setProperty(newKey, newValue);

		// checking if the old key has an associated line number
		for (Iterator iterator = lineNumberToKey.entrySet().iterator(); iterator.hasNext(); ) {
			Map.Entry entry = (Map.Entry) iterator.next();
			Integer lineNumber = (Integer) entry.getKey();
			String key = (String) entry.getValue();
			if (key.equals(oldKey)) {
				iterator.remove();
				lineNumberToKey.put(lineNumber, newKey);
				break;
			}
		}
	}

	/**
	 * Reads a property list (key and element pairs) from the input stream. The stream is assumed to be using the ISO 8859-1 character encoding; that is each byte is one Latin1 character. Characters not in Latin1, and certain special characters, can
	 * be represented in keys and elements using escape sequences similar to those used for character and string literals (see <a href="http://java.sun.com/docs/books/jls/second_edition/html/lexical.doc.html#100850">&sect;3.3</a> and <a
	 * href="http://java.sun.com/docs/books/jls/second_edition/html/lexical.doc.html#101089">&sect;3.10.6</a> of the <i>Java Language Specification</i>). The differences from the character escape sequences used for characters and strings are:
	 * <ul>
	 * <li> Octal escapes are not recognized.
	 * <li> The character sequence <code>\b</code> does <i>not</i> represent a backspace character.
	 * <li> The method does not treat a backslash character, <code>\</code>, before a non-valid escape character as an error; the
	 * backslash is silently dropped. For example, in a Java string the sequence <code>"\z"</code> would cause a compile time error. In contrast, this method silently drops the backslash. Therefore, this method treats the two character sequence
	 * <code>"\b"</code> as equivalent to the single character <code>'b'</code>.
	 * <li> Escapes are not necessary for single and double quotes; however, by the rule above, single and double quote characters preceded
	 * by a backslash still yield single and double quote characters, respectively.
	 * </ul>
	 * An <code>IllegalArgumentException</code> is thrown if a malformed Unicode escape appears in the input.
	 * <p>
	 * This method processes input in terms of lines. A natural line of input is terminated either by a set of line terminator characters (<code>\n</code> or <code>\r</code> or <code>\r\n</code>) or by the end of the file. A natural line may be
	 * either a blank line, a comment line, or hold some part of a key-element pair. The logical line holding all the data for a key-element pair may be spread out across several adjacent natural lines by escaping the line terminator sequence with a
	 * backslash character, <code>\</code>. Note that a comment line cannot be extended in this manner; every natural line that is a comment must have its own comment indicator, as described below. If a logical line is continued over several natural
	 * lines, the continuation lines receive further processing, also described below. Lines are read from the input stream until end of file is reached.
	 * <p>
	 * A natural line that contains only white space characters is considered blank and is ignored. A comment line has an ASCII
	 * <code>'#'</code> or <code>'!'</code> as its first non-white space character; comment lines are also ignored and do not encode
	 * key-element information. In addition to line terminators, this method considers the characters space (<code>' '</code>,
	 * <code>'&#92;u0020'</code>), tab (<code>'\t'</code>, <code>'&#92;u0009'</code>), and form feed (<code>'\f'</code>,
	 * <code>'&#92;u000C'</code>) to be white space.
	 * <p>
	 * If a logical line is spread across several natural lines, the backslash escaping the line terminator sequence, the line terminator sequence, and any white space at the start the following line have no affect on the key or element values. The
	 * remainder of the discussion of key and element parsing will assume all the characters constituting the key and element appear on a single natural line after line continuation characters have been removed. Note that it is <i>not</i> sufficient
	 * to only examine the character preceding a line terminator sequence to to see if the line terminator is escaped; there must be an odd number of contiguous backslashes for the line terminator to be escaped. Since the input is processed from left
	 * to right, a non-zero even number of 2<i>n</i> contiguous backslashes before a line terminator (or elsewhere) encodes <i>n</i> backslashes after escape processing.
	 * <p>
	 * The key contains all of the characters in the line starting with the first non-white space character and up to, but not including, the first unescaped <code>'='</code>, <code>':'</code>, or white space character other than a line terminator.
	 * All of these key termination characters may be included in the key by escaping them with a preceding backslash character; for example,
	 * <p>
	 * <code>\:\=</code>
	 * <p>
	 * would be the two-character key <code>":="</code>. Line terminator characters can be included using <code>\r</code> and
	 * <code>\n</code> escape sequences. Any white space after the key is skipped; if the first non-white space character after the key is
	 * <code>'='</code> or <code>':'</code>, then it is ignored and any white space characters after it are also skipped. All remaining
	 * characters on the line become part of the associated element string; if there are no remaining characters, the element is the empty string <code>&quot;&quot;</code>. Once the raw character sequences constituting the key and element are
	 * identified, escape processing is performed as described above.
	 * <p>
	 * As an example, each of the following three lines specifies the key <code>"Truth"</code> and the associated element value
	 * <code>"Beauty"</code>:
	 * <p>
	 *
	 * <pre>
	 *   Truth = Beauty
	 *    Truth:Beauty
	 *   Truth            :Beauty
	 * </pre>
	 * <p>
	 * As another example, the following three lines specify a single property:
	 * <p>
	 *
	 * <pre>
	 *   fruits                           apple, banana, pear, \
	 *                                    cantaloupe, watermelon, \
	 *                                    kiwi, mango
	 * </pre>
	 * <p>
	 * The key is <code>"fruits"</code> and the associated element is:
	 * <p>
	 *
	 * <pre>
	 * &quot;apple, banana, pear, cantaloupe, watermelon, kiwi, mango&quot;
	 * </pre>
	 * <p>
	 * Note that a space appears before each <code>\</code> so that a space will appear after each comma in the final result; the
	 * <code>\</code>, line terminator, and leading white space on the continuation line are merely discarded and are <i>not</i>
	 * replaced by one or more other characters.
	 * <p>
	 * As a third example, the line:
	 * <p>
	 *
	 * <pre>
	 * cheeses
	 * </pre>
	 * <p>
	 * specifies that the key is <code>"cheeses"</code> and the associated element is the empty string <code>""</code>.
	 * <p>
	 *
	 * @param inStream the input stream.
	 * @throws IOException if an error occurred when reading from the input stream. sequence.
	 */
	public synchronized void load(InputStream inStream) throws IOException {
		lines = new ArrayList();
		lineNumberToKey = new HashMap();

		BufferedReader in = new BufferedReader(new InputStreamReader(inStream, "8859_1"));
		while (true) {
			// Get next line
			String line = in.readLine();
			if (line == null) {
				return;
			}
			lines.add(line);
			if (line.length() > 0) {
				// Find start of key
				int len = line.length();
				int keyStart;
				for (keyStart = 0; keyStart < len; keyStart++) {
					if (WHITE_SPACE_CHARS.indexOf(line.charAt(keyStart)) == -1) {
						break;
					}
				}
				// Blank lines are ignored
				if (keyStart == len) {
					continue;
				}
				// Continue lines that end in slashes if they are not comments
				char firstChar = line.charAt(keyStart);
				if ((firstChar != '#') && (firstChar != '!')) {
					while (continueLine(line)) {
						String nextLine = in.readLine();
						if (nextLine == null) {
							nextLine = "";
						}
						String loppedLine = line.substring(0, len - 1);
						// Advance beyond whitespace on new line
						int startIndex;
						for (startIndex = 0; startIndex < nextLine.length(); startIndex++) {
							if (WHITE_SPACE_CHARS.indexOf(nextLine.charAt(startIndex)) == -1) {
								break;
							}
						}
						nextLine = nextLine.substring(startIndex, nextLine.length());
						line = loppedLine + nextLine;
						len = line.length();
					}
					// Find separation between key and value
					int separatorIndex;
					for (separatorIndex = keyStart; separatorIndex < len; separatorIndex++) {
						char currentChar = line.charAt(separatorIndex);
						if (currentChar == '\\') {
							separatorIndex++;
						}
						else if (KEY_VALUE_SEPARATORS.indexOf(currentChar) != -1) {
							break;
						}
					}
					// Skip over whitespace after key if any
					int valueIndex;
					for (valueIndex = separatorIndex; valueIndex < len; valueIndex++) {
						if (WHITE_SPACE_CHARS.indexOf(line.charAt(valueIndex)) == -1) {
							break;
						}
					}
					// Skip over one non whitespace key value separators if any
					if (valueIndex < len) {
						if (STRICT_KEY_VALUE_SEPARATORS.indexOf(line.charAt(valueIndex)) != -1) {
							valueIndex++;
						}
					}
					// Skip over white space after other separators if any
					while (valueIndex < len) {
						if (WHITE_SPACE_CHARS.indexOf(line.charAt(valueIndex)) == -1) {
							break;
						}
						valueIndex++;
					}
					String key = line.substring(keyStart, separatorIndex);
					String value = "";
					if (separatorIndex < len) {
						value = line.substring(valueIndex, len);
					}
					// Convert then store key and value
					key = loadConvert(key);
					value = loadConvert(value);
					put(key, value);
					lineNumberToKey.put(lines.size() - 1, key);
				}
			}
		}
	}

	/**
	 * Converts encoded &#92;uxxxx to unicode chars and changes special saved chars to their original forms
	 *
	 * @param string the string to convert
	 * @return the converted string
	 */
	private String loadConvert(String string) {
		char aChar;
		int len = string.length();
		StringBuffer outBuffer = new StringBuffer(len);
		for (int x = 0; x < len; ) {
			aChar = string.charAt(x++);
			if (aChar == '\\') {
				aChar = string.charAt(x++);
				if (aChar == 'u') {
					// Read the xxxx
					int value = 0;
					for (int i = 0; i < 4; i++) {
						aChar = string.charAt(x++);
						switch (aChar) {
						case '0':
						case '1':
						case '2':
						case '3':
						case '4':
						case '5':
						case '6':
						case '7':
						case '8':
						case '9':
							value = (value << 4) + aChar - '0';
							break;
						case 'a':
						case 'b':
						case 'c':
						case 'd':
						case 'e':
						case 'f':
							value = (value << 4) + 10 + aChar - 'a';
							break;
						case 'A':
						case 'B':
						case 'C':
						case 'D':
						case 'E':
						case 'F':
							value = (value << 4) + 10 + aChar - 'A';
							break;
						default:
							throw new IllegalArgumentException("Malformed \\uxxxx encoding.");
						}
					}
					outBuffer.append((char) value);
				}
				else {
					if (aChar == 't') {
						aChar = '\t';
					}
					else if (aChar == 'r') {
						aChar = '\r';
					}
					else if (aChar == 'n') {
						aChar = '\n';
					}
					else if (aChar == 'f') {
						aChar = '\f';
					}
					outBuffer.append(aChar);
				}
			}
			else {
				outBuffer.append(aChar);
			}
		}
		return outBuffer.toString();
	}

	/**
	 * @param line the line to check
	 * @return <code>true</code> if the given line is a line that must be appended to the next line
	 */
	private boolean continueLine(String line) {
		int slashCount = 0;
		int index = line.length() - 1;
		while ((index >= 0) && (line.charAt(index--) == '\\')) {
			slashCount++;
		}
		return (slashCount % 2 == 1);
	}

	/**
	 * Writes this property list (key and element pairs) in this <code>Properties</code> table to the output stream in a format suitable for loading into a <code>Properties</code> table using the {@link #load(InputStream) load} method. The stream is
	 * written using the ISO 8859-1 character encoding.
	 * <p>
	 * Properties from the defaults table of this <code>Properties</code> table (if any) are <i>not</i> written out by this method.
	 * <p>
	 * If the header argument is not null, then an ASCII <code>#</code> character, the header string, and a line separator are first written to the output stream. Thus, the <code>header</code> can serve as an identifying comment.
	 * <p>
	 * Next, a comment line is always written, consisting of an ASCII <code>#</code> character, the current date and time (as if produced by the <code>toString</code> method of <code>Date</code> for the current time), and a line separator as
	 * generated by the Writer.
	 * <p>
	 * Then every entry in this <code>Properties</code> table is written out, one per line. For each entry the key string is written, then an ASCII <code>=</code>, then the associated element string. Each character of the key and element strings is
	 * examined to see whether it should be rendered as an escape sequence. The ASCII characters <code>\</code>, tab, form feed, newline, and carriage return are written as <code>\\</code>, <code>\t</code>, <code>\f</code> <code>\n</code>, and
	 * <code>\r</code>, respectively. Characters less than <code>&#92;u0020</code> and characters greater than <code>&#92;u007E</code> are written as
	 * <code>&#92;u</code><i>xxxx</i> for the appropriate hexadecimal value <i>xxxx</i>. For the key, all space characters are written
	 * with a preceding <code>\</code> character. For the element, leading space characters, but not embedded or trailing space characters, are written with a preceding <code>\</code> character. The key and element characters <code>#</code>,
	 * <code>!</code>,
	 * <code>=</code>, and <code>:</code> are written with a preceding backslash to ensure that they are properly loaded.
	 * <p>
	 * After the entries have been written, the output stream is flushed. The output stream remains open after this method returns.
	 *
	 * @param out    an output stream.
	 * @param header a description of the property list.
	 * @throws IOException if writing this property list to the specified output stream throws an <tt>IOException</tt>. values that are not <code>Strings</code>.
	 */
	public synchronized void store(OutputStream out, String header) throws IOException {
		BufferedWriter writer;
		writer = new BufferedWriter(new OutputStreamWriter(out, "8859_1"));
		if (header != null) {
			writeln(writer, "#" + header);
		}
		writeln(writer, "#" + new Date().toString());

		// write the existing file content
		Set writtenKeys = new HashSet();
		for (int i = 0; i < lines.size(); i++) {
			String key = (String) lineNumberToKey.get(i);
			if (key == null) {
				writeln(writer, (String) lines.get(i));
			}
			else if (!writtenKeys.contains(key)) {
				String val = getProperty(key);
				writeProperty(writer, key, val);
				writtenKeys.add(key);
			}
		}

		for (Enumeration e = keys(); e.hasMoreElements(); ) {
			String key = (String) e.nextElement();

			if (!writtenKeys.contains(key)) {
				String val = (String) get(key);
				writeProperty(writer, key, val);
			}
		}

		writer.flush();
	}

	/**
	 * Write a property and its key with a new line
	 *
	 * @param bw  the writer
	 * @param key the key
	 * @param val the value
	 * @throws IOException if an error occurs during the write
	 */
	private void writeProperty(BufferedWriter bw, String key, String val) throws IOException {
		key = saveConvert(key, true);
		// No need to escape embedded and trailing spaces for value, hence pass false to flag.
		val = saveConvert(val, false);
		writeln(bw, key + " = " + val);
	}

	/**
	 * Write a string and a new line in the provided writer.
	 *
	 * @param bw the writer
	 * @param s  the string to write
	 * @throws IOException if an error occurs during the write
	 */
	private void writeln(BufferedWriter bw, String s) throws IOException {
		bw.write(s);
		bw.newLine();
	}

	/**
	 * Converts unicodes to encoded &#92;uxxxx and writes out any of the characters in specialSaveChars with a preceding slash.
	 *
	 * @param string      the string to convert
	 * @param escapeSpace when <code>true</code> escapes every space character
	 * @return the converted string
	 */
	private String saveConvert(String string, boolean escapeSpace) {
		int len = string.length();
		StringBuffer outBuffer = new StringBuffer(len * 2);
		for (int x = 0; x < len; x++) {
			char aChar = string.charAt(x);
			switch (aChar) {
			case ' ':
				if (x == 0 || escapeSpace) {
					outBuffer.append('\\');
				}
				outBuffer.append(' ');
				break;
			case '\\':
				outBuffer.append('\\');
				outBuffer.append('\\');
				break;
			case '\t':
				outBuffer.append('\\');
				outBuffer.append('t');
				break;
			case '\n':
				outBuffer.append('\\');
				outBuffer.append('n');
				break;
			case '\r':
				outBuffer.append('\\');
				outBuffer.append('r');
				break;
			case '\f':
				outBuffer.append('\\');
				outBuffer.append('f');
				break;
			default:
				if ((aChar < 0x0020) || (aChar > 0x007e)) {
					outBuffer.append('\\');
					outBuffer.append('u');
					outBuffer.append(toHex((aChar >> 12) & 0xF));
					outBuffer.append(toHex((aChar >> 8) & 0xF));
					outBuffer.append(toHex((aChar >> 4) & 0xF));
					outBuffer.append(toHex(aChar & 0xF));
				}
				else {
					if (SPECIAL_SAVE_CHARS.indexOf(aChar) != -1) {
						outBuffer.append('\\');
					}
					outBuffer.append(aChar);
				}
			}
		}
		return outBuffer.toString();
	}

	/**
	 * Convert a nibble to a hex character
	 *
	 * @param nibble the nibble to convert.
	 * @return the hexadecimal character
	 */
	private char toHex(int nibble) {
		return HEX_DIGITS[(nibble & 0xF)];
	}
}