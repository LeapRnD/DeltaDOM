package com.leaprnd.deltadom.matching;

import org.w3c.dom.Node;

import static com.leaprnd.deltadom.Similarity.GOOD_MATCH;

enum AlwaysGoodMatch implements Calculator<Node>, MatcherFactory<Node> {

	ALWAYS_GOOD_MATCH;

	@Override
	public float getSimilarityOf(Node a, Node b) {
		return GOOD_MATCH;
	}

	@Override
	public Matcher<Node> newMatcher() {
		return new SimilarityGrid<>(this);
	}

}
