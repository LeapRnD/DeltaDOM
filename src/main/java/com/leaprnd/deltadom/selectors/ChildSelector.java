package com.leaprnd.deltadom.selectors;

import java.io.IOException;

record ChildSelector(Selector parent, Selector child) implements Selector {
	@Override
	public void appendTo(Appendable writer) throws IOException {
		parent.appendTo(writer);
		writer.append('>');
		child.appendTo(writer);
	}
}