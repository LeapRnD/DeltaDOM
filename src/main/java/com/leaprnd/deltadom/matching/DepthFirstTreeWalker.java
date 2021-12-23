package com.leaprnd.deltadom.matching;

import org.w3c.dom.Node;

record DepthFirstTreeWalker(Node root) implements Iterable<Node> {
	@Override
	public DepthFirstTreeWalkerIterator iterator() {
		return new DepthFirstTreeWalkerIterator(root);
	}
}
