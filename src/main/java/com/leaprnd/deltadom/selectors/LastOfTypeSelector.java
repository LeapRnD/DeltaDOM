package com.leaprnd.deltadom.selectors;

import java.io.IOException;

record LastOfTypeSelector(Selector decorated) implements Selector {
	@Override
	public void appendTo(Appendable writer) throws IOException {
		decorated.appendTo(writer);
		writer.append(":last-of-type");
	}
}