package com.leaprnd.deltadom.matching;

import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.HashSet;

import static com.leaprnd.deltadom.Similarity.IMPOSSIBLE_MATCH;
import static com.leaprnd.deltadom.Similarity.PERFECT_MATCH;
import static com.leaprnd.deltadom.matching.AlwaysGoodMatch.ALWAYS_GOOD_MATCH;
import static com.leaprnd.deltadom.matching.CommentMatcherFactory.DEFAULT_COMMENT_MATCHER_FACTORY;
import static com.leaprnd.deltadom.matching.DocumentTypeMatcherFactory.DOCUMENT_TYPE_MATCHER_FACTORY;
import static com.leaprnd.deltadom.matching.TextMatcherFactory.TEXT_MATCHER_FACTORY;

public class NodeMatches {

	public static NodeMatches between(Node xRoot, Node yRoot) {
		final var multiplexNodesByType = new MultiplexingByTypeMatcherFactory(
			DEFAULT_COMMENT_MATCHER_FACTORY,
			ALWAYS_GOOD_MATCH,
			ALWAYS_GOOD_MATCH,
			DOCUMENT_TYPE_MATCHER_FACTORY,
			new ElementMatcherFactory(),
			TEXT_MATCHER_FACTORY
		);
		return new MultiplexBySubtreeChecksumMatcher(xRoot, yRoot, multiplexNodesByType).findMatches();
	}

	private final HashMap<Node, Node> beforeToAfter = new HashMap<>();
	private final HashMap<Node, Node> afterToBefore = new HashMap<>();
	private final HashSet<Node> perfect = new HashSet<>();

	public void add(Node before, Node after, float similarity) {
		if (similarity == IMPOSSIBLE_MATCH) {
			return;
		}
		final var oldY = beforeToAfter.put(before, after);
		final var oldX = afterToBefore.put(after, before);
		if (oldY != null || oldX != null) {
			throw new IllegalStateException("Node was already present!");
		}
		if (similarity == PERFECT_MATCH) {
			perfect.add(before);
		}
	}

	public Node getAfterOf(Node before) {
		return beforeToAfter.get(before);
	}

	public Node getBeforeOf(Node after) {
		return afterToBefore.get(after);
	}

	public boolean isPerfectMatch(Node before, Node after) {
		return perfect.contains(before) && beforeToAfter.get(before).isSameNode(after);
	}

}
