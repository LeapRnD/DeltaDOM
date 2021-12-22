package com.leaprnd.deltadom.matching;

import com.leaprnd.deltadom.UnexpectedNodeTypeException;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import static com.leaprnd.deltadom.Similarity.IMPOSSIBLE_MATCH;
import static java.lang.System.identityHashCode;
import static org.w3c.dom.Node.COMMENT_NODE;
import static org.w3c.dom.Node.DOCUMENT_FRAGMENT_NODE;
import static org.w3c.dom.Node.DOCUMENT_NODE;
import static org.w3c.dom.Node.DOCUMENT_TYPE_NODE;
import static org.w3c.dom.Node.ELEMENT_NODE;
import static org.w3c.dom.Node.TEXT_NODE;

record MultiplexingByTypeMatcher(
	Matcher<? super Comment> comments,
	Matcher<? super Document> documents,
	Matcher<? super DocumentFragment> fragments,
	Matcher<? super DocumentType> types,
	Matcher<? super Element> elements,
	Matcher<? super Text> texts
) implements Matcher<Node> {

	@Override
	public void addX(Node node) {
		final var type = node.getNodeType();
		switch (type) {
			case COMMENT_NODE -> comments.addX((Comment) node);
			case DOCUMENT_NODE -> documents.addX((Document) node);
			case DOCUMENT_FRAGMENT_NODE -> fragments.addX((DocumentFragment) node);
			case DOCUMENT_TYPE_NODE -> types.addX((DocumentType) node);
			case ELEMENT_NODE -> elements.addX((Element) node);
			case TEXT_NODE -> texts.addX((Text) node);
			default -> throw new UnexpectedNodeTypeException(type);
		}
	}

	@Override
	public MatcherRemovalResult removeX(Node node) {
		final var type = node.getNodeType();
		return switch (type) {
			case COMMENT_NODE -> comments.removeX((Comment) node);
			case DOCUMENT_NODE -> documents.removeX((Document) node);
			case DOCUMENT_FRAGMENT_NODE -> fragments.removeX((DocumentFragment) node);
			case DOCUMENT_TYPE_NODE -> types.removeX((DocumentType) node);
			case ELEMENT_NODE -> elements.removeX((Element) node);
			case TEXT_NODE -> texts.removeX((Text) node);
			default -> throw new UnexpectedNodeTypeException(type);
		};
	}

	@Override
	public void addY(Node node) {
		final var type = node.getNodeType();
		switch (type) {
			case COMMENT_NODE -> comments.addY((Comment) node);
			case DOCUMENT_NODE -> documents.addY((Document) node);
			case DOCUMENT_FRAGMENT_NODE -> fragments.addY((DocumentFragment) node);
			case DOCUMENT_TYPE_NODE -> types.addY((DocumentType) node);
			case ELEMENT_NODE -> elements.addY((Element) node);
			case TEXT_NODE -> texts.addY((Text) node);
			default -> throw new UnexpectedNodeTypeException(type);
		}
	}

	@Override
	public MatcherRemovalResult removeY(Node node) {
		final var type = node.getNodeType();
		return switch (type) {
			case COMMENT_NODE -> comments.removeY((Comment) node);
			case DOCUMENT_NODE -> documents.removeY((Document) node);
			case DOCUMENT_FRAGMENT_NODE -> fragments.removeY((DocumentFragment) node);
			case DOCUMENT_TYPE_NODE -> types.removeY((DocumentType) node);
			case ELEMENT_NODE -> elements.removeY((Element) node);
			case TEXT_NODE -> texts.removeY((Text) node);
			default -> throw new UnexpectedNodeTypeException(type);
		};
	}

	@Override
	public float getSimilarityOf(Node a, Node b) {
		final var typeOfA = a.getNodeType();
		final var typeOfB = b.getNodeType();
		if (typeOfA == typeOfB) {
			return switch (typeOfA) {
				case COMMENT_NODE -> comments.getSimilarityOf((Comment) a, (Comment) b);
				case DOCUMENT_NODE -> documents.getSimilarityOf((Document) a, (Document) b);
				case DOCUMENT_FRAGMENT_NODE -> fragments.getSimilarityOf((DocumentFragment) a, (DocumentFragment) b);
				case DOCUMENT_TYPE_NODE -> types.getSimilarityOf((DocumentType) a, (DocumentType) b);
				case ELEMENT_NODE -> elements.getSimilarityOf((Element) a, (Element) b);
				case TEXT_NODE -> texts.getSimilarityOf((Text) a, (Text) b);
				default -> throw new UnexpectedNodeTypeException(typeOfA);
			};
		} else {
			return IMPOSSIBLE_MATCH;
		}
	}

	@Override
	public void findMatches(Calculator<Node> calculator, NodeMatches matches) {
		comments.findMatches(calculator, matches);
		documents.findMatches(calculator, matches);
		fragments.findMatches(calculator, matches);
		types.findMatches(calculator, matches);
		elements.findMatches(calculator, matches);
		texts.findMatches(calculator, matches);
	}

	@Override
	public boolean equals(Object that) {
		return this == that;
	}

	@Override
	public int hashCode() {
		return identityHashCode(this);
	}

}
