package com.leaprnd.deltadom.selectors;

import org.w3c.dom.Element;

import java.io.IOException;

import static org.w3c.dom.Node.ELEMENT_NODE;

public sealed interface Selector permits ChildSelector,FirstOfTypeSelector,IdSelector,LastOfTypeSelector,NthOfTypeSelector,TagSelector {

	static Selector toSelector(Element element) {
		if (element == null) {
			throw new IllegalArgumentException();
		}
		var part = toSelectorPart(element);
		if (part instanceof IdSelector) {
			return part;
		}
		while (true) {
			final var parentNode = element.getParentNode();
			if (parentNode instanceof final Element parentElement) {
				element = parentElement;
			} else {
				return part;
			}
			final var parentPart = toSelectorPart(element);
			part = new ChildSelector(parentPart, part);
			if (parentPart instanceof IdSelector) {
				return part;
			}
		}
	}

	private static Selector toSelectorPart(Element element) {
		if (element.hasAttribute("id")) {
			return new IdSelector(element.getAttribute("id"));
		}
		final var tagName = element.getTagName();
		Selector part = new TagSelector(tagName);
		var nthOfType = 1;
		var childrenOfType = 1;
		for (var sibling = element.getPreviousSibling(); sibling != null; sibling = sibling.getPreviousSibling()) {
			if (sibling.getNodeType() == ELEMENT_NODE && sibling.getNodeName().equals(tagName)) {
				childrenOfType ++;
				nthOfType ++;
			}
		}
		for (var sibling = element.getNextSibling(); sibling != null; sibling = sibling.getNextSibling()) {
			if (sibling.getNodeType() == ELEMENT_NODE && sibling.getNodeName().equals(tagName)) {
				childrenOfType ++;
			}
		}
		if (childrenOfType > 1) {
			if (nthOfType == 1) {
				part = new FirstOfTypeSelector(part);
			} else if (nthOfType == childrenOfType) {
				part = new LastOfTypeSelector(part);
			} else {
				part = new NthOfTypeSelector(part, nthOfType);
			}
		}
		return part;
	}

	void appendTo(Appendable writer) throws IOException;

}
