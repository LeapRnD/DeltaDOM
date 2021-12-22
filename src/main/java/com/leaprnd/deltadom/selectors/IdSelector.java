package com.leaprnd.deltadom.selectors;

import java.io.IOException;

record IdSelector(String id) implements Selector {
	@Override
	public void appendTo(Appendable writer) throws IOException {
		writer.append('#');
		writer.append(id);
	}
}