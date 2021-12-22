package com.leaprnd.deltadom.matching;

import org.w3c.dom.Node;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.singleton;

class NodeChecksums {

	private final HashMap<Node, NodeChecksum> checksumsByNode = new HashMap<>();
	private final HashMap<NodeChecksum, Node> nodesByUniqueChecksum = new HashMap<>();
	private final HashMap<NodeChecksum, Set<Node>> nodesByDuplicateChecksum = new HashMap<>();

	public void put(Node node, NodeChecksum checksum) {
		checksumsByNode.put(node, checksum);
		final var oldNodes = nodesByDuplicateChecksum.get(checksum);
		if (oldNodes != null) {
			oldNodes.add(node);
			return;
		}
		final var collision = nodesByUniqueChecksum.putIfAbsent(checksum, node);
		if (collision == null) {
			return;
		}
		verify(nodesByUniqueChecksum.remove(checksum, collision));
		final var newNodes = new HashSet<Node>();
		newNodes.add(node);
		newNodes.add(collision);
		verify(nodesByDuplicateChecksum.putIfAbsent(checksum, newNodes) == null);
	}

	public Node removeNodeIfUnique(NodeChecksum checksum) {
		final var node = nodesByUniqueChecksum.remove(checksum);
		if (node == null) {
			return null;
		}
		verify(checksumsByNode.remove(node, checksum));
		return node;
	}

	public Set<Node> removeNodesBy(NodeChecksum checksum) {
		final var unique = removeNodeIfUnique(checksum);
		if (unique == null) {
			return removeNodesIfDuplicate(checksum);
		}
		return singleton(unique);
	}

	public Set<Node> removeNodesIfDuplicate(NodeChecksum checksum) {
		final var nodes = nodesByDuplicateChecksum.remove(checksum);
		if (nodes == null) {
			return null;
		}
		for (final var node : nodes) {
			verify(checksumsByNode.remove(node, checksum));
		}
		return nodes;
	}

	public int getTotalSize() {
		return checksumsByNode.size();
	}

	public int getUniqueSize() {
		return nodesByUniqueChecksum.size();
	}

	public NodeChecksum getChecksumOf(Node node) {
		return checksumsByNode.get(node);
	}

	public Set<Node> getNodes() {
		return checksumsByNode.keySet();
	}

	public boolean remove(Node root) {
		final var checksum = checksumsByNode.remove(root);
		if (checksum == null) {
			return false;
		}
		if (nodesByUniqueChecksum.remove(checksum, root)) {
			return true;
		}
		final var oldNodes = nodesByDuplicateChecksum.get(checksum);
		switch (oldNodes.size()) {
			case 0, 1 -> throw new ConcurrentModificationException();
			case 2 -> {
				verify(nodesByDuplicateChecksum.remove(checksum, oldNodes));
				for (final var oldNode : oldNodes) {
					if (oldNode == root) {
						continue;
					}
					verify(nodesByUniqueChecksum.putIfAbsent(checksum, oldNode) == null);
				}
			}
			default -> verify(oldNodes.remove(root));
		}
		return true;
	}

	public Set<NodeChecksum> getUniqueChecksums() {
		return nodesByUniqueChecksum.keySet();
	}

	public Set<NodeChecksum> getDuplicateChecksums() {
		return nodesByDuplicateChecksum.keySet();
	}

	private static void verify(boolean test) {
		if (test) {
			return;
		}
		throw new ConcurrentModificationException();
	}

}
