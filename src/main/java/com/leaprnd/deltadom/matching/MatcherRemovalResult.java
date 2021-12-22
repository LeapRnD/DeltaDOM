package com.leaprnd.deltadom.matching;

import org.w3c.dom.Node;

import java.util.Set;

public sealed interface MatcherRemovalResult {

	enum NotFound implements MatcherRemovalResult {
		NOT_FOUND
	}

	enum Removed implements MatcherRemovalResult {
		REMOVED
	}

	record Emptied(Set<? extends Node> leftovers) implements MatcherRemovalResult {}

}
