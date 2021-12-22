package com.leaprnd.deltadom.matching;

import org.w3c.dom.Text;

import static com.leaprnd.deltadom.matching.AlwaysGoodMatch.ALWAYS_GOOD_MATCH;

class TextMatcher extends MultiplexingMatcher<String, Text> {

	@Override
	protected String getKeyOf(Text node) {
		return node.getTextContent();
	}

	@Override
	protected Matcher<Text> delegate() {
		return new SimilarityGrid<>(ALWAYS_GOOD_MATCH);
	}

}
