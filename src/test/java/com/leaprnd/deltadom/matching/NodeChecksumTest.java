package com.leaprnd.deltadom.matching;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;
import java.io.StringReader;

import static com.leaprnd.deltadom.matching.NodeChecksum.computeChecksumsOfDescendantsOf;
import static javax.xml.parsers.DocumentBuilderFactory.newInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class NodeChecksumTest {

	private static final String HTML = """
		<!DOCTYPE html>
		<html>
			<head>
				<title>Test</title>
			</head>
			<body>
				<p>a</p>
				<p>ab</p>
				<p>abc</p>
				<p>abcd</p>
				<p>abcde</p>
				<p>abcdef</p>
				<p>abcdefg</p>
				<p>abcdefgh</p>
				<p>abcdefghi</p>
				<p>abcdefghij</p>
				<p>abcdefghijk</p>
				<p>abcdefghijkl</p>
				<p>abcdefghijklm</p>
				<p>abcdefghijklmn</p>
				<p>abcdefghijklmno</p>
				<p>abcdefghijklmnop</p>
				<p>abcdefghijklmnopq</p>
				<p>abcdefghijklmnopqr</p>
				<p>abcdefghijklmnopqrs</p>
				<p>abcdefghijklmnopqrst</p>
				<p>abcdefghijklmnopqrstu</p>
				<p>abcdefghijklmnopqrstuv</p>
				<p>abcdefghijklmnopqrstuvw</p>
				<p>abcdefghijklmnopqrstuvwx</p>
				<p>abcdefghijklmnopqrstuvwxy</p>
				<p>abcdefghijklmnopqrstuvwxyz</p>
			</body>
		</html>
		""";

	@Test
	public void test() throws IOException, SAXException, ParserConfigurationException {
		final var document = loadDocument();
		final var checksums = computeChecksumsOfDescendantsOf(document);
		assertEquals(91, checksums.getTotalSize());
		assertEquals(60, checksums.getUniqueSize());
		final var copy = loadDocument();
		final var copyChecksums = computeChecksumsOfDescendantsOf(copy);
		assertEquals(checksums.getUniqueChecksums(), copyChecksums.getUniqueChecksums());
		assertEquals(checksums.getDuplicateChecksums(), copyChecksums.getDuplicateChecksums());
	}

	@Test
	public void testAddString() {
		helpTestAdd("a", "42a91bc0eeceb0fdcf6ef57f1fabe117");
		helpTestAdd("ab", "3c2b7aff31ca0cd56dbe5decc790faa9");
		helpTestAdd("abc", "92c00d6c29e0e2b278555649ae03f9d4");
		helpTestAdd("abcd", "68e3f0bc267342233f655b945eb95dbf");
		helpTestAdd("abcde", "85a517ed6daf9a3aa9d3d4425f490a4");
		helpTestAdd("abcdef", "c065a3eb046fd81e9077179bddead7c0");
		helpTestAdd("abcdefg", "b5615085732a59d07ec5c8875e5f7a81");
		helpTestAdd("abcdefgh", "7712666a5769002740d32c194a5d9714");
		helpTestAdd("abcdefghi", "638f4226bbc3bb8b67eaa8c6644a0cb6");
		helpTestAdd("abcdefghij", "f2f79b0041276f6eac53ccb78b551625");
		helpTestAdd("abcdefghijk", "fad8491a1edc67858a59efea8fd624c1");
		helpTestAdd("abcdefghijkl", "4a78306dd0325926b4911a8de45148c1");
		helpTestAdd("abcdefghijklm", "322cfc6990faad477e808b185fb73c4");
		helpTestAdd("abcdefghijklmn", "3b6f1943253a13d1cedc3450dbfc9874");
		helpTestAdd("abcdefghijklmno", "70800b55c904d5ca5472908b40a4084c");
		helpTestAdd("abcdefghijklmnop", "a2faac1d77bbe377e1d133092042ec27");
		helpTestAdd("abcdefghijklmnopq", "cdc71d8c149ca51b2416a87b82f210ba");
		helpTestAdd("abcdefghijklmnopqr", "6832b50a2dc96e106b39f604e936b190");
		helpTestAdd("abcdefghijklmnopqrs", "166bf9c3aa75a0f9e84265ad06e638b8");
		helpTestAdd("abcdefghijklmnopqrst", "1aab4c6e138ed359aaddb0903595528c");
		helpTestAdd("abcdefghijklmnopqrstu", "1ddef629aa30a3a0adec3518a5952749");
		helpTestAdd("abcdefghijklmnopqrstuv", "ac3650901852d92eaaca65cfbb47741f");
		helpTestAdd("abcdefghijklmnopqrstuvw", "8556479f93bb22a66bc340c848a5b4bc");
		helpTestAdd("abcdefghijklmnopqrstuvwx", "97bc4d29d40da4e2e6a35f2acf4d1bb9");
		helpTestAdd("abcdefghijklmnopqrstuvwxy", "151d4d4cafef42d709b15ff52bb07c6");
		helpTestAdd("abcdefghijklmnopqrstuvwxyz", "302aec67cd496ff9b46c8db0c17bbbb3");
	}

	private void helpTestAdd(String stringToAdd, String expectedString) {
		final var checksum = new NodeChecksum();
		checksum.add(stringToAdd);
		checksum.done();
		assertEquals(expectedString, checksum.toString());
	}

	private Document loadDocument() throws IOException, SAXException, ParserConfigurationException {
		final var builder = newInstance().newDocumentBuilder();
		final var reader = new StringReader(HTML);
		final var source = new InputSource(reader);
		return builder.parse(source);
	}

}
