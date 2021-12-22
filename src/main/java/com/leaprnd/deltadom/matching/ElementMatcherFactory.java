package com.leaprnd.deltadom.matching;

import org.w3c.dom.Element;

class ElementMatcherFactory extends ElementCalculator implements MatcherFactory<Element> {
	@Override
	public Matcher<Element> newMatcher() {
		return new ElementMatcher(this);
	}
}
