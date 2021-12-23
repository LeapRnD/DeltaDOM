package com.leaprnd.deltadom;

import com.leaprnd.deltadom.matching.NodeMatches;
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

import static com.leaprnd.deltadom.Position.toInsertPosition;
import static com.leaprnd.deltadom.Position.toRemovePosition;
import static com.leaprnd.deltadom.Similarity.GOOD_MATCH;
import static com.leaprnd.deltadom.selectors.Selector.toSelector;
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
	private final HashSet<Node> outOfOrder = new HashSet<>();

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
			final var matchOfAfterParent = matches.getBeforeOf(afterParent);
			final var before = matches.getBeforeOf(after);
			final Node result;
			if (before == null) {
				result = handleInsert(after, matchOfAfterParent);
			} else {
				final var beforeParent = before.getParentNode();
				if (matchOfAfterParent.isSameNode(beforeParent)) {
					result = before;
				} else {
					result = handleMove(before, after, matchOfAfterParent);
				}
				if (matches.isPerfectMatch(before, after)) {
					continue;
				}
				handleUpdate(result, after);
			}
			for (var child = after.getFirstChild(); child != null; child = child.getNextSibling()) {
				fifo.add(child);
			}
			alignChildren(result, after);
		}
		handleDeletes(beforeRoot);
	}

	private void alignChildren(Node x, Node y) throws E {
		markChildrenAsOutOfOrder(x);
		markChildrenAsOutOfOrder(y);
		final var xOverlap = getSequence(x, y, matches::getAfterOf);
		final var yOverlap = getSequence(y, x, matches::getBeforeOf);
		final var longestSubsequence = findLongestCommonSubsequence(xOverlap, yOverlap);
		markNodesAndMatchesInOrder(longestSubsequence);
		moveMisalignedNodes(x, xOverlap, longestSubsequence);
		markChildrenAsInOrder(x);
		markChildrenAsInOrder(y);
	}

	private void markChildrenAsOutOfOrder(Node node) {
		for (var child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			outOfOrder.add(child);
		}
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

		var index = 0;
		final var matchesOfX = new Node[x.size()];
		for (final var before : x) {
			final var after = matches.getAfterOf(before);
			matchesOfX[index ++] = after;
		}

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

	private void markNodesAndMatchesInOrder(Iterable<Node> nodes) {
		for (final var node : nodes) {
			outOfOrder.remove(node);
			final var match = matches.getAfterOf(node);
			outOfOrder.remove(match);
		}
	}

	private void moveMisalignedNodes(Node parent, Iterable<Node> children, Set<Node> longestSubsequence) throws E {
		for (final var child : children) {
			if (longestSubsequence.contains(child)) {
				continue;
			}
			final var match = matches.getAfterOf(child);
			final var insertPosition = findInsertPositionOf(match);
			onMove(child, parent, insertPosition);
			insertAsChild(insertPosition, parent, child);
			outOfOrder.remove(child);
			outOfOrder.remove(match);
		}
	}

	private void markChildrenAsInOrder(Node node) {
		for (var child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			outOfOrder.remove(child);
		}
	}

	private Node handleInsert(Node newNode, Node newParent) throws E {
		final var insertPosition = findInsertPositionOf(newNode);
		final var imported = beforeDocument.importNode(newNode, false);
		outOfOrder.remove(imported);
		outOfOrder.remove(newNode);
		onInsert(imported, newParent, insertPosition);
		insertAsChild(insertPosition, newParent, imported);
		matches.add(imported, newNode, GOOD_MATCH);
		return imported;
	}

	private Node handleMove(Node oldNode, Node newNode, Node newParent) throws E {
		final var insertPosition = findInsertPositionOf(newNode);
		outOfOrder.remove(oldNode);
		outOfOrder.remove(newNode);
		onMove(oldNode, newParent, insertPosition);
		insertAsChild(insertPosition, newParent, oldNode);
		return oldNode;
	}

	private void handleUpdate(Node before, Node after) throws E {
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
					listener.onSetValue(toRemovePosition(before), newValue);
				}
			}
			default -> throw new UnexpectedNodeTypeException(nodeType);
		}
	}

	private void handleDeletes(Node before) throws E {
		final var after = matches.getAfterOf(before);
		if (after == null) {
			onDelete(before);
			final var parent = before.getParentNode();
			parent.removeChild(before);
			return;
		}
		if (matches.isPerfectMatch(before, after)) {
			return;
		}
		var child = before.getLastChild();
		while (child != null) {
			final var previous = child.getPreviousSibling();
			handleDeletes(child);
			child = previous;
		}
	}

	private int findInsertPositionOf(Node newNode) {
		final var previousNode = getInOrderLeftSibling(newNode);
		if (previousNode == null) {
			return 0;
		} else {
			var matchOfPreviousNode = matches.getBeforeOf(previousNode);
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
		} while (node != null && outOfOrder.contains(node));
		return node;
	}

	private static void insertAsChild(int position, Node parent, Node node) {
		final var children = parent.getChildNodes();
		final var insertBefore = children.item(position);
		parent.insertBefore(node, insertBefore);
	}

	private void onInsert(Node node, Node parent, int newPosition) throws E {
		final var nodeType = node.getNodeType();
		switch (nodeType) {
			case ATTRIBUTE_NODE -> {
				listener.onSetAttribute(toSelector((Element) parent), node.getNodeName(), node.getNodeValue());
			}
			case COMMENT_NODE -> {
				listener.onInsertComment(toInsertPosition(parent, newPosition), node.getNodeValue());
			}
			case ELEMENT_NODE -> {
				final var position = toInsertPosition(parent, newPosition);
				listener.onInsertElement(position, node.getNodeName(), node.getAttributes());
			}
			case TEXT_NODE -> {
				listener.onInsertText(toInsertPosition(parent, newPosition), node.getNodeValue());
			}
			default -> throw new UnexpectedNodeTypeException(nodeType);
		}
	}

	private void onMove(Node node, Node newParent, int newPosition) throws E {
		if (node instanceof final Element element) {
			listener.onMoveElement(toSelector(element), toInsertPosition(newParent, newPosition));
		} else {
			listener.onMoveNode(toRemovePosition(node), toInsertPosition(newParent, newPosition));
		}
	}

	private void onUpdate(Attr before, Attr after) throws E {
		final var element = before.getOwnerElement();
		final var name = after.getNodeName();
		final var value = after.getNodeValue();
		listener.onSetAttribute(toSelector(element), name, value);
	}

	private void onDelete(Node node) throws E {
		if (node instanceof final Attr attribute) {
			final var nameOfAttribute = node.getNodeName();
			final var element = attribute.getOwnerElement();
			listener.onRemoveAttribute(toSelector(element), nameOfAttribute);
		} else if (node instanceof final Element element) {
			listener.onDeleteElement(toSelector(element));
		} else {
			listener.onDeleteNode(toRemovePosition(node));
		}
	}

}
