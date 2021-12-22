package com.leaprnd.deltadom;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UnexpectedNodeTypeExceptionTest {

	private UnexpectedNodeTypeException exception;

	@BeforeEach
	public void setUp() {
		exception = new UnexpectedNodeTypeException((short) 42);
	}

	@Test
	public void testGetMessage() {
		assertEquals("Unexpected node type: 42!", exception.getMessage());
	}

}
