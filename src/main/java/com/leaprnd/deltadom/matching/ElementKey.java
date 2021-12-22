package com.leaprnd.deltadom.matching;

import org.w3c.dom.Element;

import static com.google.common.base.Strings.emptyToNull;
import static com.leaprnd.deltadom.matching.ElementCalculator.ID_ATTRIBUTE;
import static com.leaprnd.deltadom.matching.ElementCalculator.IS_ATTRIBUTE;

public record ElementKey(String tagName, String is, String id) {

	public ElementKey(Element node) {
		this(node.getTagName(), node.getAttribute(IS_ATTRIBUTE), node.getAttribute(ID_ATTRIBUTE));
	}

	public ElementKey {
		tagName = emptyToNull(tagName);
		is = emptyToNull(is);
		id = emptyToNull(id);
	}

}
