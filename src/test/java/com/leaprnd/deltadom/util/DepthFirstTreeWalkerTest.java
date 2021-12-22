package com.leaprnd.deltadom.util;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import static javax.xml.parsers.DocumentBuilderFactory.newInstance;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.w3c.dom.Node.DOCUMENT_TYPE_NODE;
import static org.w3c.dom.Node.TEXT_NODE;

public class DepthFirstTreeWalkerTest {

	private static final String HTML = """
		<!DOCTYPE html>
		<html>
			<head>
				<title>Test</title>
			</head>
			<body>
				<p>Apple</p>
				<p>Sauce</p>
				<p>Jedi</p>
				<p>Knight</p>
			</body>
		</html>
		""";

	@Test
	public void test() throws IOException, SAXException, ParserConfigurationException {
		final var walker = new DepthFirstTreeWalker(loadDocument());
		final var actualNames = new ArrayList<String>();
		for (final var node : walker) {
			if (node.getNodeType() == TEXT_NODE && node.getNodeValue().isBlank()) {
				continue;
			}
			actualNames.add(switch (node.getNodeType()) {
				case DOCUMENT_TYPE_NODE -> "DOCTYPE";
				case TEXT_NODE -> node.getNodeValue();
				default -> node.getNodeName();
			});
		}
		final String[] expectedNames = { "DOCTYPE", "Test", "title", "head", "Apple", "p", "Sauce", "p", "Jedi", "p",
			"Knight", "p", "body", "html", "#document" };
		assertArrayEquals(expectedNames, actualNames.toArray(String[]::new));
	}

	private Document loadDocument() throws ParserConfigurationException, IOException, SAXException {
		final var builder = newInstance().newDocumentBuilder();
		final var reader = new StringReader(HTML);
		final var source = new InputSource(reader);
		return builder.parse(source);
	}

}
