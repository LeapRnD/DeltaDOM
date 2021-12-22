package com.leaprnd.deltadom.matching;

import org.w3c.dom.Node;

interface Matcher<T extends Node> extends Calculator<T> {

	default void addXs(Iterable<? extends T> xNodes) {
		for (final var xNode : xNodes) {
			addX(xNode);
		}
	}

	void addX(T node);

	MatcherRemovalResult removeX(T node);

	default void addYs(Iterable<? extends T> yNodes) {
		for (final var yNode : yNodes) {
			addY(yNode);
		}
	}

	void addY(T node);

	MatcherRemovalResult removeY(T node);

	void findMatches(Calculator<Node> calculator, NodeMatches matches);

}