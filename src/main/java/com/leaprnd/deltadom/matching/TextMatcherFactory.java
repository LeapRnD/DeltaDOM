package com.leaprnd.deltadom.matching;

import org.w3c.dom.Text;

enum TextMatcherFactory implements MatcherFactory<Text> {

	TEXT_MATCHER_FACTORY;

	@Override
	public Matcher<Text> newMatcher() {
		return new TextMatcher();
	}

}
