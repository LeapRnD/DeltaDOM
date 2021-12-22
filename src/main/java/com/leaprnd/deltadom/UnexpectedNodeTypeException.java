package com.leaprnd.deltadom;

import static java.lang.String.format;

public class UnexpectedNodeTypeException extends RuntimeException {

	private final short nodeType;

	public UnexpectedNodeTypeException(short nodeType) {
		this.nodeType = nodeType;
	}

	@Override
	public String getMessage() {
		return format("Unexpected node type: %s!", nodeType);
	}

}
