package com.leaprnd.deltadom.matching;

import org.w3c.dom.Node;

record SiblingCalculator<T extends Node> (
	Calculator<? super T> calculator,
	Calculator<Node> siblingCalculator
) implements Calculator<T> {

	private static final float FACTOR = 0.1f;
	private static final float MAX_BONUS = 0.25f;
	private static final float MIN_BONUS = -0.1f;

	@Override
	public float getSimilarityOf(T a, T b) {
		return calculator.getSimilarityOf(a, b) +
			getBonusSimilarityOf(a.getPreviousSibling(), b.getPreviousSibling()) +
			getBonusSimilarityOf(a.getNextSibling(), b.getNextSibling());
	}

	private float getBonusSimilarityOf(Node a, Node b) {
		if (a == null || b == null) {
			return 0;
		}
		final var bonus = siblingCalculator.getSimilarityOf(a, b) * FACTOR;
		if (bonus < MIN_BONUS) {
			return MIN_BONUS;
		}
		if (bonus > MAX_BONUS) {
			return MAX_BONUS;
		}
		return bonus;
	}

}
