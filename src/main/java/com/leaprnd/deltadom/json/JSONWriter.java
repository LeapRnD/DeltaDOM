package com.leaprnd.deltadom.json;

import java.io.Closeable;
import java.io.IOException;

import static java.lang.Integer.toHexString;

public class JSONWriter {

	public class ArrayWriter implements Closeable {

		private boolean comma = false;

		private ArrayWriter() throws IOException {
			writer.append('[');
		}

		public void value(Enum<?> value) throws IOException {
			value(value.ordinal());
		}

		public void value(String value) throws IOException {
			comma();
			writer.append('"');
			valueWriter.append(value);
			writer.append('"');
		}

		public void value(int value) throws IOException {
			comma();
			writer.append(Integer.toString(value));
		}

		public ValueWriter value() throws IOException {
			comma();
			writer.append('"');
			return valueWriter;
		}

		public ArrayWriter array() throws IOException {
			comma();
			return new ArrayWriter();
		}

		private void comma() throws IOException {
			if (comma) {
				writer.append(',');
			} else {
				comma = true;
			}
		}

		@Override
		public void close() throws IOException {
			writer.append(']');
		}

	}

	public class ValueWriter implements Appendable, Closeable {

		@Override
		public Appendable append(CharSequence value) throws IOException {
			return append(value, 0, value.length());
		}

		@Override
		public Appendable append(CharSequence value, int start, int end) throws IOException {
			for (var index = start; index < end; index ++) {
				append(value.charAt(index));
			}
			return this;
		}

		@Override
		public Appendable append(char character) throws IOException {
			switch (character) {
				case '"':
					writer.append("\\\"");
					break;
				case '\\':
					writer.append("\\\\");
					break;
				case '\n':
					writer.append("\\n");
					break;
				case '\r':
					writer.append("\\r");
					break;
				case '\t':
					writer.append("\\t");
					break;
				default:
					if (needsToBeEscaped(character)) {
						final var hex = toHexString(character);
						writer.append("\\u");
						for (var padding = 0; padding < 4 - hex.length(); padding ++) {
							writer.append('0');
						}
						writer.append(hex.toUpperCase());
					} else {
						writer.append(character);
					}
			}
			return this;
		}

		private static boolean needsToBeEscaped(char character) {
			return character <= '\u001F' ||
				character >= '\u007F' && character <= '\u009F' ||
				character >= '\u2000' && character <= '\u20FF';
		}

		@Override
		public void close() throws IOException {
			writer.append('"');
		}

	}

	private final Appendable writer;
	private final ValueWriter valueWriter = new ValueWriter();

	public JSONWriter(Appendable writer) {
		this.writer = writer;
	}

	public ArrayWriter array() throws IOException {
		return new ArrayWriter();
	}

}
