package com.leaprnd.deltadom.matching;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

record MultiplexingByTypeMatcherFactory(
	MatcherFactory<? super Comment> comments,
	MatcherFactory<? super Document> documents,
	MatcherFactory<? super DocumentFragment> fragments,
	MatcherFactory<? super DocumentType> types,
	MatcherFactory<? super Element> elements,
	MatcherFactory<? super Text> texts
) implements MatcherFactory<Node> {
	@Override
	public MultiplexingByTypeMatcher newMatcher() {
		return new MultiplexingByTypeMatcher(
			comments.newMatcher(),
			documents.newMatcher(),
			fragments.newMatcher(),
			types.newMatcher(),
			elements.newMatcher(),
			texts.newMatcher()
		);
	}
}
