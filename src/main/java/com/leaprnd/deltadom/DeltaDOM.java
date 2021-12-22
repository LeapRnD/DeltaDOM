package com.leaprnd.deltadom;

import com.leaprnd.deltadom.matching.NodeMatches;
import com.leaprnd.deltadom.selectors.Position;
import com.leaprnd.deltadom.selectors.Selector;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static java.lang.Math.max;
import static java.util.Collections.emptySet;
import static org.w3c.dom.Node.ATTRIBUTE_NODE;
import static org.w3c.dom.Node.COMMENT_NODE;
import static org.w3c.dom.Node.ELEMENT_NODE;
import static org.w3c.dom.Node.TEXT_NODE;

public final class DeltaDOM<E extends Throwable> {

	private final Node beforeRoot;
	private final Document beforeDocument;
	private final Node afterRoot;
	private final NodeMatches matches;
	private final DifferenceHandler<E> listener;
	private final HashSet<Node> ordered = new HashSet<>();

	public DeltaDOM(Node beforeRoot, Node afterRoot, NodeMatches matches, DifferenceHandler<E> listener) {
		this.beforeRoot = beforeRoot;
		this.beforeDocument = beforeRoot instanceof final Document document ? document : beforeRoot.getOwnerDocument();
		this.afterRoot = afterRoot;
		this.matches = matches;
		this.listener = listener;
	}

	public void find() throws E {
		if (matches.isPerfectMatch(beforeRoot, afterRoot)) {
			return;
		}
		final var fifo = new LinkedList<Node>();
		for (var child = afterRoot.getFirstChild(); child != null; child = child.getNextSibling()) {
			fifo.add(child);
		}
		alignChildren(beforeRoot, afterRoot);
		while (true) {
			final var after = fifo.poll();
			if (after == null) {
				break;
			}
			final var afterParent = after.getParentNode();
			final var matchOfAfterParent = matches.getBeforeNodeOf(afterParent);
			final var beforeMatch = matches.getBeforeOf(after);
			final Node result;
			if (beforeMatch == null) {
				result = doInsert(after, matchOfAfterParent);
			} else {
				final var beforeNode = beforeMatch.node();
				final var beforeParent = beforeNode.getParentNode();
				if (matchOfAfterParent.isSameNode(beforeParent)) {
					result = beforeNode;
				} else {
					result = doMove(beforeNode, after, matchOfAfterParent);
				}
				final var similarity = beforeMatch.similarity();
				if (similarity < Similarity.PERFECT_MATCH) {
					doUpdate(result, after);
				} else {
					continue;
				}
			}
			for (var child = after.getFirstChild(); child != null; child = child.getNextSibling()) {
				fifo.add(child);
			}
			alignChildren(result, after);
		}
		deletePhase(beforeRoot);
	}

	private void doUpdate(final Node before, final Node after) throws E {
		final var nodeType = before.getNodeType();
		switch (nodeType) {
			case ELEMENT_NODE -> {
				if (before.hasAttributes() || after.hasAttributes()) {
					final var beforeAttributes = before.getAttributes();
					final var afterAttributes = after.getAttributes();
					var index = beforeAttributes.getLength();
					while (--index >= 0) {
						final var beforeAttribute = (Attr) beforeAttributes.item(index);
						final var beforeAttributeName = beforeAttribute.getNodeName();
						final var afterAttribute = (Attr) afterAttributes.getNamedItem(beforeAttributeName);
						if (afterAttribute == null) {
							onDelete(beforeAttribute);
							beforeAttributes.removeNamedItem(beforeAttributeName);
						} else if (!beforeAttribute.getNodeValue().equals(afterAttribute.getNodeValue())) {
							onUpdate(beforeAttribute, afterAttribute);
							beforeAttribute.setNodeValue(afterAttribute.getNodeValue());
						}
					}
					index = afterAttributes.getLength();
					while (--index >= 0) {
						final var afterAttribute = afterAttributes.item(index);
						final var afterAttributeName = afterAttribute.getNodeName();
						if (beforeAttributes.getNamedItem(afterAttributeName) == null) {
							onInsert(afterAttribute, before, 0);
							beforeAttributes.setNamedItem(beforeDocument.importNode(afterAttribute, true));
						}
					}
				}
			}
			case TEXT_NODE, COMMENT_NODE -> {
				final var oldValue = before.getNodeValue();
				final var newValue = after.getNodeValue();
				if (!newValue.equals(oldValue)) {
					listener.onSetValue(Position.toRemovePosition(before), newValue);
				}
			}
			default -> throw new UnexpectedNodeTypeException(nodeType);
		}
	}

	private Node doInsert(Node newNode, Node newParent) throws E {
		final var insertPosition = findInsertPositionOf(newNode);
		final var imported = beforeDocument.importNode(newNode, false);
		markAsInOrder(imported);
		markAsInOrder(newNode);
		onInsert(imported, newParent, insertPosition);
		insertAsChild(insertPosition, newParent, imported);
		matches.add(imported, newNode, Similarity.GOOD_MATCH);
		return imported;
	}

	private Node doMove(Node oldNode, Node newNode, Node newParent) throws E {
		final var insertPosition = findInsertPositionOf(newNode);
		markAsInOrder(oldNode);
		markAsInOrder(newNode);
		onMove(oldNode, newParent, insertPosition);
		insertAsChild(insertPosition, newParent, oldNode);
		return oldNode;
	}

	private void deletePhase(Node node) throws E {
		final var match = matches.getAfterOf(node);
		if (match == null) {
			onDelete(node);
			final var parent = node.getParentNode();
			parent.removeChild(node);
		} else {
			final var similarity = match.similarity();
			if (similarity < Similarity.PERFECT_MATCH) {
				var child = node.getLastChild();
				while (child != null) {
					final var previous = child.getPreviousSibling();
					deletePhase(child);
					child = previous;
				}
			}
		}
	}

	private void markChildrenAsOutOfOrder(final Node node) {
		for (var child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			markAsOutOfOrder(child);
		}
	}

	private void markChildrenAsInOrder(Node node) {
		for (var child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			markAsInOrder(child);
		}
	}

	private void markNodesAndMatchesInOrder(Iterable<Node> nodes) {
		for (final var node : nodes) {
			markAsInOrder(node);
			final var match = matches.getAfterNodeOf(node);
			markAsInOrder(match);
		}
	}

	private void moveMisalignedNodes(Node parent, Iterable<Node> children, Set<Node> longestSubsequence) throws E {
		for (final var child : children) {
			if (longestSubsequence.contains(child)) {
				continue;
			}
			final var match = matches.getAfterNodeOf(child);
			final var insertPosition = findInsertPositionOf(match);
			onMove(child, parent, insertPosition);
			insertAsChild(insertPosition, parent, child);
			markAsInOrder(child);
			markAsInOrder(match);
		}
	}

	private void alignChildren(Node x, Node y) throws E {
		markChildrenAsOutOfOrder(x);
		markChildrenAsOutOfOrder(y);
		final var xOverlap = getSequence(x, y, matches::getAfterNodeOf);
		final var yOverlap = getSequence(y, x, matches::getBeforeNodeOf);
		final var longestSubsequence = findLongestCommonSubsequence(xOverlap, yOverlap);
		markNodesAndMatchesInOrder(longestSubsequence);
		moveMisalignedNodes(x, xOverlap, longestSubsequence);
		markChildrenAsInOrder(x);
		markChildrenAsInOrder(y);
	}

	private static ArrayList<Node> getSequence(Node parent, Node otherParent, Function<Node, Node> getMatch) {
		final var results = new ArrayList<Node>();
		for (var child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
			final var match = getMatch.apply(child);
			if (match == null) {
				continue;
			}
			if (match.getParentNode().isSameNode(otherParent)) {
				results.add(child);
			}
		}
		return results;
	}

	private Set<Node> findLongestCommonSubsequence(List<Node> x, List<Node> y) {

		var lastIndexOfX = x.size() - 1;
		var lastIndexOfY = y.size() - 1;

		if (lastIndexOfX < 0 || lastIndexOfY < 0) {
			return emptySet();
		}

		final var matchesOfX = matches.getAfterNodesOf(x);

		var firstIndex = 0;

		while (firstIndex <= lastIndexOfX && firstIndex <= lastIndexOfY) {
			final var xMatch = matchesOfX[firstIndex];
			final var yNode = y.get(firstIndex);
			if (yNode.isSameNode(xMatch)) {
				firstIndex ++;
			} else {
				break;
			}
		}

		while (firstIndex < lastIndexOfX && firstIndex < lastIndexOfY) {
			final var xMatch = matchesOfX[lastIndexOfX];
			final var yNode = y.get(lastIndexOfY);
			if (yNode.isSameNode(xMatch)) {
				lastIndexOfX --;
				lastIndexOfY --;
			} else {
				break;
			}
		}

		if (firstIndex > lastIndexOfX) {
			return new HashSet<>(x);
		}

		final var xLengthOfMatrix = lastIndexOfX - firstIndex + 2;
		final var yLengthOfMatrix = lastIndexOfY - firstIndex + 2;
		final var memo = new int[xLengthOfMatrix][yLengthOfMatrix];

		{
			for (var xIndex = firstIndex; xIndex <= lastIndexOfX; xIndex ++) {
				final var matchOfX = matchesOfX[xIndex];
				for (var yIndex = firstIndex; yIndex <= lastIndexOfY; yIndex ++) {
					final var yNode = y.get(yIndex);
					final int newLength;
					if (yNode.isSameNode(matchOfX)) {
						newLength = 1 + memo[xIndex - firstIndex][yIndex - firstIndex];
					} else {
						newLength = max(
							memo[xIndex - firstIndex][yIndex - firstIndex + 1],
							memo[xIndex - firstIndex + 1][yIndex - firstIndex]
						);
					}
					memo[xIndex - firstIndex + 1][yIndex - firstIndex + 1] = newLength;
				}
			}
		}

		final var results = new HashSet<Node>();

		for (var xIndex = x.size() - 1; xIndex > lastIndexOfX; xIndex --) {
			final var xNode = x.get(xIndex);
			results.add(xNode);
		}

		{
			var xIndex = xLengthOfMatrix - 1;
			var yIndex = yLengthOfMatrix - 1;

			while (xIndex > 0 && yIndex > 0) {
				final var yNode = y.get(firstIndex + yIndex - 1);
				final var matchOfX = matchesOfX[firstIndex + xIndex - 1];
				if (yNode.isSameNode(matchOfX)) {
					final var xNode = x.get(firstIndex + xIndex - 1);
					results.add(xNode);
					yIndex --;
					xIndex --;
				} else if (memo[xIndex][yIndex - 1] >= memo[xIndex - 1][yIndex]) {
					yIndex --;
				} else {
					xIndex --;
				}
			}
		}

		for (var xIndex = firstIndex - 1; xIndex >= 0; xIndex --) {
			final var xNode = x.get(xIndex);
			results.add(xNode);
		}

		return results;

	}

	public int findInsertPositionOf(Node newNode) {
		final var previousNode = getInOrderLeftSibling(newNode);
		if (previousNode == null) {
			return 0;
		} else {
			var matchOfPreviousNode = matches.getBeforeNodeOf(previousNode);
			var index = 1;
			while (true) {
				matchOfPreviousNode = matchOfPreviousNode.getPreviousSibling();
				if (matchOfPreviousNode == null) {
					return index;
				}
				index ++;
			}
		}
	}

	private Node getInOrderLeftSibling(Node node) {
		do {
			node = node.getPreviousSibling();
		} while (node != null && isOutOfOrder(node));
		return node;
	}

	private void markAsInOrder(Node node) {
		ordered.add(node);
	}

	private void markAsOutOfOrder(Node node) {
		ordered.remove(node);
	}

	private boolean isOutOfOrder(Node node) {
		return !ordered.contains(node);
	}

	public static void insertAsChild(int position, Node parent, Node node) {
		final var children = parent.getChildNodes();
		final var insertBefore = children.item(position);
		parent.insertBefore(node, insertBefore);
	}

	private void onMove(Node node, Node newParent, int newPosition) throws E {
		final var nodeType = node.getNodeType();
		switch (nodeType) {
			case COMMENT_NODE, TEXT_NODE -> {
				listener.onMoveNode(Position.toRemovePosition(node), Position.toInsertPosition(newParent, newPosition));
			}
			case ELEMENT_NODE -> {
				final var element = (Element) node;
				listener.onMoveElement(Selector.toSelector(element), Position.toInsertPosition(newParent, newPosition));
			}
			default -> throw new UnexpectedNodeTypeException(nodeType);
		}
	}

	private void onDelete(Node node) throws E {
		final var nodeType = node.getNodeType();
		switch (nodeType) {
			case ATTRIBUTE_NODE -> {
				final var attribute = (Attr) node;
				final var nameOfAttribute = node.getNodeName();
				final var element = attribute.getOwnerElement();
				listener.onRemoveAttribute(Selector.toSelector(element), nameOfAttribute);
			}
			case COMMENT_NODE, TEXT_NODE -> {
				listener.onDeleteNode(Position.toRemovePosition(node));
			}
			case ELEMENT_NODE -> {
				listener.onDeleteElement(Selector.toSelector((Element) node));
			}
			default -> throw new UnexpectedNodeTypeException(nodeType);
		}
	}

	private void onInsert(Node node, Node parent, int newPosition) throws E {
		final var nodeType = node.getNodeType();
		switch (nodeType) {
			case ATTRIBUTE_NODE -> {
				listener.onSetAttribute(Selector.toSelector((Element) parent), node.getNodeName(), node.getNodeValue());
			}
			case COMMENT_NODE -> {
				listener.onInsertComment(Position.toInsertPosition(parent, newPosition), node.getNodeValue());
			}
			case ELEMENT_NODE -> {
				final var position = Position.toInsertPosition(parent, newPosition);
				listener.onInsertElement(position, node.getNodeName(), node.getAttributes());
			}
			case TEXT_NODE -> {
				listener.onInsertText(Position.toInsertPosition(parent, newPosition), node.getNodeValue());
			}
			default -> throw new UnexpectedNodeTypeException(nodeType);
		}
	}

	private void onUpdate(Node before, Node after) throws E {
		if (before instanceof final Attr beforeAttr) {
			final var parent = beforeAttr.getOwnerElement();
			listener.onSetAttribute(Selector.toSelector(parent), after.getNodeName(), after.getNodeValue());
		} else {
			final var nodeType = before.getNodeType();
			throw new UnexpectedNodeTypeException(nodeType);
		}
	}

}
