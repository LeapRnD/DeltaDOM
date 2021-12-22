package com.leaprnd.deltadom.matching;

import org.w3c.dom.Node;

import java.util.Collection;
import java.util.HashMap;

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

	private final HashMap<Node, NodeMatch> forwards = new HashMap<>();
	private final HashMap<Node, NodeMatch> backwards = new HashMap<>();

	public void add(Node x, Node y, float similarity) {
		if (similarity == IMPOSSIBLE_MATCH) {
			return;
		}
		final var oldY = forwards.put(x, new NodeMatch(y, similarity));
		final var oldX = backwards.put(y, new NodeMatch(x, similarity));
		if (oldY != null || oldX != null) {
			throw new IllegalStateException("Node was already present!");
		}
	}

	public NodeMatch getAfterOf(Node before) {
		return forwards.get(before);
	}

	public NodeMatch getBeforeOf(Node after) {
		return backwards.get(after);
	}

	public Node getAfterNodeOf(Node before) {
		final var match = getAfterOf(before);
		if (match == null) {
			return null;
		}
		return match.node();
	}

	public Node getBeforeNodeOf(Node after) {
		final var match = getBeforeOf(after);
		if (match == null) {
			return null;
		}
		return match.node();
	}

	public Node[] getAfterNodesOf(Collection<Node> befores) {
		var index = 0;
		final var afters = new Node[befores.size()];
		for (final var before : befores) {
			final var after = getAfterNodeOf(before);
			afters[index ++] = after;
		}
		return afters;
	}

	public boolean isPerfectMatch(Node before, Node after) {
		final var match = forwards.get(before);
		if (match == null) {
			return false;
		}
		final var similarity = match.similarity();
		final var matchOfBefore = match.node();
		return similarity == PERFECT_MATCH && matchOfBefore.isSameNode(after);
	}

}
