package com.leaprnd.deltadom.matching;

import com.leaprnd.deltadom.marriage.StableMarriageProblemSolver;
import org.w3c.dom.Node;

import java.util.HashSet;

import static com.leaprnd.deltadom.matching.MatcherRemovalResult.Emptied;
import static com.leaprnd.deltadom.matching.MatcherRemovalResult.Values.NOT_FOUND;
import static com.leaprnd.deltadom.matching.MatcherRemovalResult.Values.REMOVED;

class SimilarityGrid<T extends Node> implements Matcher<T> {

	private final Calculator<? super T> initializer;
	private final HashSet<T> xSet = new HashSet<>();
	private final HashSet<T> ySet = new HashSet<>();

	SimilarityGrid(Calculator<? super T> initializer) {
		this.initializer = initializer;
	}

	@Override
	public void addX(T node) {
		xSet.add(node);
	}

	@Override
	public MatcherRemovalResult removeX(T node) {
		if (xSet.remove(node)) {
			if (xSet.isEmpty()) {
				return new Emptied(ySet);
			}
			return REMOVED;
		}
		return NOT_FOUND;
	}

	@Override
	public void addY(T node) {
		ySet.add(node);
	}

	@Override
	public MatcherRemovalResult removeY(T node) {
		if (ySet.remove(node)) {
			if (ySet.isEmpty()) {
				return new Emptied(xSet);
			}
			return REMOVED;
		}
		return NOT_FOUND;
	}

	@Override
	public float getSimilarityOf(T a, T b) {
		return initializer.getSimilarityOf(a, b);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void findMatches(Calculator<Node> calculator, NodeMatches matches) {
		if (xSet.isEmpty() || ySet.isEmpty()) {
			return;
		}
		final var x = (T[]) xSet.toArray(Node[]::new);
		final var y = (T[]) ySet.toArray(Node[]::new);
		final var siblingCalculator = new SiblingCalculator<T>(initializer, calculator);
		final var xLength = x.length;
		final var yLength = y.length;
		final var similarities = new float[xLength][yLength];
		int xIndex = xLength;
		while (--xIndex >= 0) {
			int yIndex = yLength;
			while (--yIndex >= 0) {
				final var xNode = x[xIndex];
				final var yNode = y[yIndex];
				similarities[xIndex][yIndex] = siblingCalculator.getSimilarityOf(xNode, yNode);
			}
		}
		new StableMarriageProblemSolver() {

			@Override
			protected float getStrengthOfPotentialMarriageBetween(int xIndex, int yIndex) {
				return similarities[xIndex][yIndex];
			}

			@Override
			protected int getNumberOfMen() {
				return xLength;
			}

			@Override
			protected int getNumberOfWomen() {
				return yLength;
			}

			@Override
			protected void onMarriageBetween(int xIndex, int yIndex, float similarity) {
				final var xNode = x[xIndex];
				final var yNode = y[yIndex];
				matches.add(xNode, yNode, similarity);
			}

		}.run();
	}

}
