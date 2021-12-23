package com.leaprnd.deltadom.matching;

import org.w3c.dom.Node;

import java.util.Set;

sealed interface MatcherRemovalResult {

	enum Values implements MatcherRemovalResult {
		REMOVED,
		NOT_FOUND
	}

	record Emptied(Set<? extends Node> leftovers) implements MatcherRemovalResult {}

}
