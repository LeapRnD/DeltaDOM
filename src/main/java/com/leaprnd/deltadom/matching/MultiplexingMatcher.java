package com.leaprnd.deltadom.matching;

import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.function.Function;

import static com.leaprnd.deltadom.Similarity.IMPOSSIBLE_MATCH;

abstract class MultiplexingMatcher<K, T extends Node> implements Matcher<T>, Function<K, Matcher<T>> {

	private final HashMap<K, Matcher<T>> matchers = new HashMap<>();

	@Override
	public void addX(T node) {
		matchers.computeIfAbsent(getKeyOf(node), this).addX(node);
	}

	@Override
	public MatcherRemovalResult removeX(T node) {
		final var key = getKeyOf(node);
		final var matcher = matchers.get(key);
		if (matcher == null) {
			return MatcherRemovalResult.NotFound.NOT_FOUND;
		}
		final var result = matcher.removeX(node);
		if (result instanceof MatcherRemovalResult.Emptied) {
			matchers.remove(key);
		}
		return result;
	}

	@Override
	public void addY(T node) {
		matchers.computeIfAbsent(getKeyOf(node), this).addY(node);
	}

	public MatcherRemovalResult removeY(T node) {
		final var key = getKeyOf(node);
		final var matcher = matchers.get(key);
		if (matcher == null) {
			return MatcherRemovalResult.NotFound.NOT_FOUND;
		}
		final var result = matcher.removeY(node);
		if (result instanceof MatcherRemovalResult.Emptied) {
			matchers.remove(key);
		}
		return result;
	}

	@Override
	public Matcher<T> apply(K key) {
		return delegate();
	}

	@Override
	public float getSimilarityOf(T a, T b) {
		final var keyOfA = getKeyOf(a);
		final var keyOfB = getKeyOf(b);
		if (keyOfA.equals(keyOfB)) {
			return matchers.get(keyOfA).getSimilarityOf(a, b);
		} else {
			return IMPOSSIBLE_MATCH;
		}
	}

	@Override
	public void findMatches(Calculator<Node> calculator, NodeMatches matches) {
		for (final var matcher : matchers.values()) {
			matcher.findMatches(calculator, matches);
		}
	}

	protected abstract K getKeyOf(T node);
	protected abstract Matcher<T> delegate();

}
