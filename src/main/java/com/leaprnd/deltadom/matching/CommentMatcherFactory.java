package com.leaprnd.deltadom.matching;

import org.w3c.dom.Comment;

import static com.leaprnd.deltadom.Similarity.BAD_MATCH;
import static com.leaprnd.deltadom.Similarity.GOOD_MATCH;

enum CommentMatcherFactory implements MatcherFactory<Comment>, Calculator<Comment> {

	DEFAULT_COMMENT_MATCHER_FACTORY;

	@Override
	public float getSimilarityOf(Comment a, Comment b) {
		final var valueOfA = a.getNodeValue();
		final var valueOfB = b.getNodeValue();
		if (valueOfA.equals(valueOfB)) {
			return GOOD_MATCH;
		} else {
			return BAD_MATCH;
		}
	}

	@Override
	public Matcher<Comment> newMatcher() {
		return new SimilarityGrid<>(this);
	}

}
