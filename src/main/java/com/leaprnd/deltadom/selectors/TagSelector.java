package com.leaprnd.deltadom.selectors;

import java.io.IOException;

record TagSelector(String tag) implements Selector {
	@Override
	public void appendTo(Appendable writer) throws IOException {
		writer.append(tag);
	}
}