package com.leaprnd.deltadom.matching;

import org.w3c.dom.Node;

@FunctionalInterface
interface MatcherFactory<T extends Node> {
	Matcher<T> newMatcher();
}
