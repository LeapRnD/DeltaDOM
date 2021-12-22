package com.leaprnd.deltadom.util;

import org.w3c.dom.Node;

public record DepthFirstTreeWalker(Node root) implements Iterable<Node> {
	@Override
	public DepthFirstTreeWalkerIterator iterator() {
		return new DepthFirstTreeWalkerIterator(root);
	}
}
