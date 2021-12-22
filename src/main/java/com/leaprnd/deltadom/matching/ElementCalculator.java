package com.leaprnd.deltadom.matching;

import org.w3c.dom.Element;

import java.util.HashMap;

import static com.leaprnd.deltadom.Similarity.BAD_MATCH;
import static com.leaprnd.deltadom.Similarity.GOOD_MATCH;
import static java.lang.Long.SIZE;
import static java.lang.Long.bitCount;

class ElementCalculator implements Calculator<Element> {

	public static final String ID_ATTRIBUTE = "id";
	public static final String IS_ATTRIBUTE = "is";

	private static final int LONGS = 16;
	private static final int BITS = LONGS * SIZE;

	private final HashMap<Element, Spec> specs = new HashMap<>();

	private static class Spec {

		private final long[] bloom = new long[LONGS];

		private Spec(Element element) {
			final var attributes = element.getAttributes();
			var index = attributes.getLength();
			while (--index >= 0) {
				final var attribute = attributes.item(index);
				final var name = attribute.getNodeName();
				final var value = attribute.getNodeValue();
				switch (name) {
					case ID_ATTRIBUTE:
					case IS_ATTRIBUTE:
						continue;
					default:
						final var offset = (int) (unsign(name.hashCode() ^ value.hashCode()) % BITS);
						bloom[offset / SIZE] |= 1L << (offset % SIZE);
				}
			}
		}

		private static long unsign(int integer) {
			return integer & 0x00000000ffffffffL;
		}

		public static float getSimilarityOf(Spec a, Spec b) {
			int matches = 0;
			int mismatches = 0;
			for (var index = 0; index < LONGS; index ++) {
				matches += bitCount(a.bloom[index] & b.bloom[index]);
				mismatches += bitCount(a.bloom[index] ^ b.bloom[index]);
			}
			if (mismatches == 0) {
				return GOOD_MATCH;
			}
			return BAD_MATCH + matches * GOOD_MATCH / (matches + mismatches);
		}

	}

	@Override
	public float getSimilarityOf(Element a, Element b) {
		return Spec.getSimilarityOf(specs.computeIfAbsent(a, Spec::new), specs.computeIfAbsent(b, Spec::new));
	}

}
