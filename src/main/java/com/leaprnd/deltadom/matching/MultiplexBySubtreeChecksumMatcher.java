package com.leaprnd.deltadom.matching;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import static com.leaprnd.deltadom.Similarity.IMPOSSIBLE_MATCH;
import static com.leaprnd.deltadom.Similarity.PERFECT_MATCH;
import static com.leaprnd.deltadom.matching.MatcherRemovalResult.Emptied;
import static com.leaprnd.deltadom.matching.MatcherRemovalResult.Values.NOT_FOUND;
import static com.leaprnd.deltadom.matching.NodeChecksum.computeChecksumsOfDescendantsOf;

class MultiplexBySubtreeChecksumMatcher implements Calculator<Node> {

	private final BiMap<Node, Node> perfect = HashBiMap.create();
	private final HashMap<Node, Matcher<Node>> xMatchers = new HashMap<>();
	private final HashMap<Node, Matcher<Node>> yMatchers = new HashMap<>();
	private final HashSet<Matcher<Node>> matchers = new HashSet<>();
	private final Matcher<Node> fallback;

	MultiplexBySubtreeChecksumMatcher(Node xRoot, Node yRoot, MatcherFactory<Node> factory) {
		final var xChecksums = computeChecksumsOfDescendantsOf(xRoot);
		final var yChecksums = computeChecksumsOfDescendantsOf(yRoot);
		fallback = factory.newMatcher();
		final var buffer = new LinkedList<Node>();
		var xNode = xRoot;
		do {
			final var checksum = xChecksums.getChecksumOf(xNode);
			final boolean visitChildren;
			if (checksum == null) {
				visitChildren = true;
			} else {
				final var duplicateXs = xChecksums.removeNodesIfDuplicate(checksum);
				if (duplicateXs == null) {
					final var uniqueX = xChecksums.removeNodeIfUnique(checksum);
					final var uniqueY = yChecksums.removeNodeIfUnique(checksum);
					if (uniqueY == null) {
						final var duplicateYs = yChecksums.removeNodesIfDuplicate(checksum);
						if (duplicateYs == null) {
							fallback.addX(uniqueX);
						} else {
							final var builder = factory.newMatcher();
							builder.addX(uniqueX);
							builder.addYs(duplicateYs);
							matchers.add(builder);
							xMatchers.put(uniqueX, builder);
							for (final var duplicateY : duplicateYs) {
								yMatchers.put(duplicateY, builder);
							}
						}
						visitChildren = true;
					} else {
						perfect.put(uniqueX, uniqueY);
						for (final var descendantX : new DepthFirstTreeWalker(uniqueX)) {
							if (descendantX == uniqueX) {
								continue;
							}
							if (xChecksums.remove(descendantX)) {
								continue;
							}
							if (fallback.removeX(descendantX) == NOT_FOUND) {
								final var matcher = xMatchers.remove(descendantX);
								final var result = matcher.removeX(descendantX);
								if (result instanceof final Emptied emptied) {
									for (final var leftoverY : emptied.leftovers()) {
										yMatchers.remove(leftoverY, matcher);
										fallback.addY(leftoverY);
									}
									matchers.remove(matcher);
								}
							}
						}
						for (final var descendantY : new DepthFirstTreeWalker(uniqueY)) {
							if (descendantY == uniqueY) {
								continue;
							}
							if (yChecksums.remove(descendantY)) {
								continue;
							}
							if (fallback.removeY(descendantY) == NOT_FOUND) {
								final var matcher = yMatchers.remove(descendantY);
								final var result = matcher.removeY(descendantY);
								if (result instanceof final Emptied emptied) {
									for (final var leftoverX : emptied.leftovers()) {
										xMatchers.remove(leftoverX, matcher);
										fallback.addX(leftoverX);
									}
									matchers.remove(matcher);
								}
							}
						}
						visitChildren = false;
					}
				} else {
					final var yNodes = yChecksums.removeNodesBy(checksum);
					if (yNodes == null) {
						fallback.addXs(duplicateXs);
					} else {
						final var builder = factory.newMatcher();
						builder.addXs(duplicateXs);
						builder.addYs(yNodes);
						matchers.add(builder);
						for (final var duplicateX : duplicateXs) {
							xMatchers.put(duplicateX, builder);
						}
						for (final var yNode : yNodes) {
							yMatchers.put(yNode, builder);
						}
					}
					visitChildren = true;
				}
			}
			if (visitChildren) {
				final var child = xNode.getFirstChild();
				if (child != null) {
					buffer.add(child);
				}
			}
			xNode = xNode.getNextSibling();
			if (xNode == null) {
				xNode = buffer.poll();
			}
		} while (xNode != null);
		fallback.addYs(yChecksums.getNodes());
	}

	@Override
	public float getSimilarityOf(Node x, Node y) {
		final var match = perfect.get(x);
		if (match == y) {
			return PERFECT_MATCH;
		}
		if (match != null) {
			return IMPOSSIBLE_MATCH;
		}
		if (perfect.containsValue(y)) {
			return IMPOSSIBLE_MATCH;
		}
		final var xMatcher = xMatchers.get(x);
		if (xMatcher == null) {
			return fallback.getSimilarityOf(x, y);
		}
		final var yMatcher = yMatchers.get(y);
		if (xMatcher == yMatcher) {
			return xMatcher.getSimilarityOf(x, y);
		}
		return IMPOSSIBLE_MATCH;
	}

	public NodeMatches findMatches() {
		final var pairs = new NodeMatches();
		for (final var entry : perfect.entrySet()) {
			final var x = entry.getKey();
			final var xParent = x.getParentNode();
			if (xParent != null && perfect.containsKey(xParent)) {
				continue;
			}
			final var y = entry.getValue();
			pairs.add(x, y, PERFECT_MATCH);
		}
		for (final var matcher : matchers) {
			matcher.findMatches(this, pairs);
		}
		fallback.findMatches(this, pairs);
		return pairs;
	}

}
