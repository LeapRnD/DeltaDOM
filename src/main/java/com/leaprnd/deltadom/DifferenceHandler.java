package com.leaprnd.deltadom;

import com.leaprnd.deltadom.selectors.Selector;
import org.w3c.dom.NamedNodeMap;

public interface DifferenceHandler<E extends Throwable> {
	void onDeleteNode(Position position) throws E;
	void onDeleteElement(Selector element) throws E;
	void onInsertComment(Position parent, String content) throws E;
	void onInsertElement(Position parent, String tagName, NamedNodeMap attributes) throws E;
	void onInsertText(Position position, String text) throws E;
	void onMoveNode(Position oldPosition, Position newPosition) throws E;
	void onMoveElement(Selector element, Position newPosition) throws E;
	void onRemoveAttribute(Selector element, String name) throws E;
	void onSetAttribute(Selector element, String name, String value) throws E;
	void onSetValue(Position position, String newValue) throws E;
}