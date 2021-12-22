package com.leaprnd.deltadom.selectors;

import java.io.IOException;

record NthOfTypeSelector(Selector decorated, int offset) implements Selector {
	@Override
	public void appendTo(Appendable writer) throws IOException {
		decorated.appendTo(writer);
		writer.append(":nth-of-type(");
		writer.append(Integer.toString(offset));
		writer.append(')');
	}
}