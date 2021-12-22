package com.leaprnd.deltadom.selectors;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public record Position(Selector parent, int offset) {

	public static Position toRemovePosition(Node node) {
		final var parent = (Element) node.getParentNode();
		var previousSibling = node.getPreviousSibling();
		int offset = 0;
		while (previousSibling != null) {
			offset ++;
			previousSibling = previousSibling.getPreviousSibling();
		}
		return new Position(Selector.toSelector(parent), offset);
	}

	public static Position toInsertPosition(Node parent, int offset) {
		return new Position(Selector.toSelector((Element) parent), offset);
	}

}
