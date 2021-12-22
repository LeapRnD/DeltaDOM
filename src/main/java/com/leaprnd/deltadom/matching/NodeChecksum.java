package com.leaprnd.deltadom.matching;

import com.leaprnd.deltadom.util.DepthFirstTreeWalker;
import org.w3c.dom.Node;

import static java.lang.Long.rotateLeft;
import static java.lang.Long.toHexString;
import static org.w3c.dom.Node.COMMENT_NODE;
import static org.w3c.dom.Node.DOCUMENT_FRAGMENT_NODE;
import static org.w3c.dom.Node.DOCUMENT_NODE;
import static org.w3c.dom.Node.DOCUMENT_TYPE_NODE;
import static org.w3c.dom.Node.ELEMENT_NODE;
import static org.w3c.dom.Node.TEXT_NODE;

class NodeChecksum {

	static NodeChecksums computeChecksumsOfDescendantsOf(Node root) {
		final var checksums = new NodeChecksums();
		for (final var node : new DepthFirstTreeWalker(root)) {
			final var type = node.getNodeType();
			final var checksum = new NodeChecksum(type);
			switch (type) {
				case TEXT_NODE, COMMENT_NODE:
					checksum.add(node.getNodeValue());
					break;
				case DOCUMENT_TYPE_NODE:
					checksum.add(node.getNodeName());
					break;
				case ELEMENT_NODE:
					checksum.add(node.getNodeName());
					final var attributes = node.getAttributes();
					int index = attributes.getLength();
					while (--index >= 0) {
						final var attribute = attributes.item(index);
						final var attributeName = attribute.getNodeName();
						checksum.add(attributeName);
						final var attributeValue = attribute.getNodeValue();
						checksum.add(attributeValue);
					}
				case DOCUMENT_NODE, DOCUMENT_FRAGMENT_NODE:
					for (var child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
						final var checksumOfChild = checksums.getChecksumOf(child);
						checksum.add(checksumOfChild.h1, checksumOfChild.h2);
					}
			}
			checksum.done();
			checksums.put(node, checksum);
		}
		return checksums;
	}

	private static final int CHUNK_SIZE = 16;
	private static final long C1 = 0x87c37b91114253d5L;
	private static final long C2 = 0x4cf5ad432745937fL;

	private long h1;
	private long h2;
	private int length;
	private int hashCode;

	NodeChecksum() {}

	private NodeChecksum(short seed) {
		h1 = seed;
		h2 = seed;
	}

	public void add(String text) {
		var index = text.length() - 1;
		while (index > 6) {
			add(
				pack(text.charAt(index), text.charAt(index - 1), text.charAt(index - 2), text.charAt(index - 3)),
				pack(text.charAt(index - 4), text.charAt(index - 5), text.charAt(index - 6), text.charAt(index - 7))
			);
			index -= 8;
		}
		switch (index) {
			case 6 -> add(
				pack(text.charAt(6), text.charAt(5), text.charAt(4), text.charAt(3)),
				pack(text.charAt(2), text.charAt(1), text.charAt(0))
			);
			case 5 -> add(
				pack(text.charAt(5), text.charAt(4), text.charAt(3), text.charAt(2)),
				pack(text.charAt(1), text.charAt(0))
			);
			case 4 -> add(pack(text.charAt(4), text.charAt(3), text.charAt(2), text.charAt(1)), pack(text.charAt(0)));
			case 3 -> add(pack(text.charAt(3), text.charAt(2), text.charAt(1), text.charAt(0)));
			case 2 -> add(pack(text.charAt(2), text.charAt(1), text.charAt(0)));
			case 1 -> add(pack(text.charAt(1), text.charAt(0)));
			case 0 -> add(pack(text.charAt(0)));
		}
	}

	private static long pack(char a) {
		return (long) a << 48;
	}

	private static long pack(char a, char b) {
		return (long) a << 48 | (long) b << 32;
	}

	private static long pack(char a, char b, char c) {
		return (long) a << 48 | (long) b << 32 | (long) c << 16;
	}

	private static long pack(char a, char b, char c, char d) {
		return (long) a << 48 | (long) b << 32 | (long) c << 16 | d;
	}

	private void add(long k1) {
		k1 *= C1;
		k1 = rotateLeft(k1, 31);
		k1 *= C2;
		h1 ^= k1;
		h1 = rotateLeft(h1, 27);
		h1 += h2;
		h1 = h1 * 5 + 0x52dce729;
		h2 = rotateLeft(h2, 31);
		h2 += h1;
		h2 = h2 * 5 + 0x38495ab5;
		length += CHUNK_SIZE;
	}

	public void add(long k1, long k2) {
		k1 *= C1;
		k1 = rotateLeft(k1, 31);
		k1 *= C2;
		h1 ^= k1;
		h1 = rotateLeft(h1, 27);
		h1 += h2;
		h1 = h1 * 5 + 0x52dce729;
		k2 *= C2;
		k2 = rotateLeft(k2, 33);
		k2 *= C1;
		h2 ^= k2;
		h2 = rotateLeft(h2, 31);
		h2 += h1;
		h2 = h2 * 5 + 0x38495ab5;
		length += CHUNK_SIZE;
	}

	public void done() {
		h1 ^= length;
		h2 ^= length;
		h1 += h2;
		h2 += h1;
		h1 = fmix64(h1);
		h2 = fmix64(h2);
		h1 += h2;
		h2 += h1;
		hashCode = (int) (h1 ^ h2);
	}

	private static long fmix64(long k) {
		k ^= k >>> 33;
		k *= 0xff51afd7ed558ccdL;
		k ^= k >>> 33;
		k *= 0xc4ceb9fe1a85ec53L;
		k ^= k >>> 33;
		return k;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof final NodeChecksum otherChecksum) {
			return h1 == otherChecksum.h1 && h2 == otherChecksum.h2;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public String toString() {
		return toHexString(h1) + toHexString(h2);
	}

}
