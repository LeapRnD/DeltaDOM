package com.leaprnd.deltadom.selectors;

import java.io.IOException;

record FirstOfTypeSelector(Selector decorated) implements Selector {
	@Override
	public void appendTo(Appendable writer) throws IOException {
		decorated.appendTo(writer);
		writer.append(":first-of-type");
	}
}