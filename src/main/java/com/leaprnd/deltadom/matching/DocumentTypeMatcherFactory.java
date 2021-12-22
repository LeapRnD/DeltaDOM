package com.leaprnd.deltadom.matching;

import org.w3c.dom.DocumentType;

import static com.leaprnd.deltadom.Similarity.BAD_MATCH;
import static com.leaprnd.deltadom.Similarity.GOOD_MATCH;

enum DocumentTypeMatcherFactory implements MatcherFactory<DocumentType>, Calculator<DocumentType> {

	DOCUMENT_TYPE_MATCHER_FACTORY;

	@Override
	public Matcher<DocumentType> newMatcher() {
		return new SimilarityGrid<>(this);
	}

	@Override
	public float getSimilarityOf(DocumentType a, DocumentType b) {
		final var nameOfA = a.getNodeName();
		final var nameOfB = b.getNodeName();
		if (nameOfA.equals(nameOfB)) {
			return GOOD_MATCH;
		} else {
			return BAD_MATCH;
		}
	}

}
