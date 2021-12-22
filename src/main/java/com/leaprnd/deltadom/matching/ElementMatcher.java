package com.leaprnd.deltadom.matching;

import org.w3c.dom.Element;

class ElementMatcher extends MultiplexingMatcher<ElementKey, Element> {

	private final ElementCalculator calculator;

	ElementMatcher(ElementCalculator calculator) {
		this.calculator = calculator;
	}

	@Override
	protected ElementKey getKeyOf(Element node) {
		return new ElementKey(node);
	}

	@Override
	protected Matcher<Element> delegate() {
		return new SimilarityGrid<>(calculator);
	}

}
