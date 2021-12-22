package com.leaprnd.deltadom.util;

import org.w3c.dom.Node;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class DepthFirstTreeWalkerIterator implements Iterator<Node> {

	private final Node root;
	private Node current;
	private boolean unmoved = false;

	public DepthFirstTreeWalkerIterator(Node root) {
		this.root = root;
		current = getDescendantOf(root);
	}

	@Override
	public boolean hasNext() {
		if (unmoved) {
			moveToNextNode();
			unmoved = false;
		}
		return current != null;
	}

	private void moveToNextNode() {
		if (current == null) {
			return;
		}
		if (current == root) {
			current = null;
			return;
		}
		final var sibling = current.getNextSibling();
		if (sibling == null) {
			current = current.getParentNode();
		} else {
			current = getDescendantOf(sibling);
		}
	}

	private Node getDescendantOf(Node node) {
		var descendant = node.getFirstChild();
		if (descendant == null) {
			return node;
		}
		while (true) {
			var child = descendant.getFirstChild();
			if (child == null) {
				return descendant;
			}
			descendant = child;
		}
	}

	@Override
	public Node next() {
		if (hasNext()) {
			unmoved = true;
			return current;
		} else {
			throw new NoSuchElementException();
		}
	}

}
