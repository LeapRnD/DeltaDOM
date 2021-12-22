package com.leaprnd.deltadom.json;

import com.leaprnd.deltadom.DifferenceHandler;
import com.leaprnd.deltadom.selectors.Position;
import com.leaprnd.deltadom.selectors.Selector;
import org.w3c.dom.NamedNodeMap;

import java.io.Closeable;
import java.io.IOException;

public class JSONDifferenceHandler implements DifferenceHandler<IOException>, Closeable {

	private final JSONWriter.ArrayWriter writer;

	public JSONDifferenceHandler(Appendable writer) throws IOException {
		this.writer = new JSONWriter(writer).array();
	}

	@Override
	public void onDeleteNode(Position position) throws IOException {
		try (final var arrayWriter = writer.array()) {
			arrayWriter.value(JSONEventType.DELETE_NODE);
			try (final var valueWriter = arrayWriter.value()) {
				position.parent().appendTo(valueWriter);
			}
			arrayWriter.value(position.offset());
		}
	}

	@Override
	public void onDeleteElement(Selector element) throws IOException {
		try (final var arrayWriter = writer.array()) {
			arrayWriter.value(JSONEventType.DELETE_ELEMENT);
			try (final var valueWriter = arrayWriter.value()) {
				element.appendTo(valueWriter);
			}
		}
	}

	@Override
	public void onInsertComment(Position parent, String content) throws IOException {
		try (final var arrayWriter = writer.array()) {
			arrayWriter.value(JSONEventType.INSERT_COMMENT);
			try (final var valueWriter = arrayWriter.value()) {
				parent.parent().appendTo(valueWriter);
			}
			arrayWriter.value(parent.offset());
			arrayWriter.value(content);
		}
	}

	@Override
	public void onInsertElement(Position parent, String tagName, NamedNodeMap attributes) throws IOException {
		try (final var arrayWriter = writer.array()) {
			arrayWriter.value(JSONEventType.INSERT_ELEMENT);
			try (final var valueWriter = arrayWriter.value()) {
				parent.parent().appendTo(valueWriter);
			}
			arrayWriter.value(parent.offset());
			arrayWriter.value(tagName);
			final var length = attributes.getLength();
			for (var index = 0; index < length; index ++) {
				final var attribute = attributes.item(index);
				final var name = attribute.getNodeName();
				final var value = attribute.getNodeValue();
				arrayWriter.value(name);
				arrayWriter.value(value);
			}
		}
	}

	@Override
	public void onInsertText(Position position, String text) throws IOException {
		try (final var arrayWriter = writer.array()) {
			arrayWriter.value(JSONEventType.INSERT_TEXT);
			try (final var valueWriter = arrayWriter.value()) {
				position.parent().appendTo(valueWriter);
			}
			arrayWriter.value(position.offset());
			arrayWriter.value(text);
		}
	}

	@Override
	public void onMoveNode(Position oldPosition, Position newPosition) throws IOException {
		try (final var arrayWriter = writer.array()) {
			arrayWriter.value(JSONEventType.MOVE_NODE);
			try (final var valueWriter = arrayWriter.value()) {
				oldPosition.parent().appendTo(valueWriter);
			}
			arrayWriter.value(oldPosition.offset());
			try (final var valueWriter = arrayWriter.value()) {
				newPosition.parent().appendTo(valueWriter);
			}
			arrayWriter.value(newPosition.offset());
		}
	}

	@Override
	public void onMoveElement(Selector element, Position newPosition) throws IOException {
		try (final var arrayWriter = writer.array()) {
			arrayWriter.value(JSONEventType.MOVE_ELEMENT);
			try (final var valueWriter = arrayWriter.value()) {
				element.appendTo(valueWriter);
			}
			try (final var valueWriter = arrayWriter.value()) {
				newPosition.parent().appendTo(valueWriter);
			}
			arrayWriter.value(newPosition.offset());
		}
	}

	@Override
	public void onRemoveAttribute(Selector element, String name) throws IOException {
		try (final var arrayWriter = writer.array()) {
			arrayWriter.value(JSONEventType.REMOVE_ATTRIBUTE);
			try (final var valueWriter = arrayWriter.value()) {
				element.appendTo(valueWriter);
			}
			arrayWriter.value(name);
		}
	}

	@Override
	public void onSetAttribute(Selector element, String name, String value) throws IOException {
		try (final var arrayWriter = writer.array()) {
			arrayWriter.value(JSONEventType.SET_ATTRIBUTE);
			try (final var valueWriter = arrayWriter.value()) {
				element.appendTo(valueWriter);
			}
			arrayWriter.value(name);
			arrayWriter.value(value);
		}
	}

	@Override
	public void onSetValue(Position position, String newValue) throws IOException {
		try (final var arrayWriter = writer.array()) {
			arrayWriter.value(JSONEventType.SET_VALUE);
			try (final var valueWriter = arrayWriter.value()) {
				position.parent().appendTo(valueWriter);
			}
			arrayWriter.value(position.offset());
			arrayWriter.value(newValue);
		}
	}

	@Override
	public void close() throws IOException {
		writer.close();
	}

}
