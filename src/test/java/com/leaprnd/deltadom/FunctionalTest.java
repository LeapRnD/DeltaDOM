package com.leaprnd.deltadom;

import com.leaprnd.deltadom.json.JSONDifferenceHandler;
import com.leaprnd.deltadom.matching.NodeMatches;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.simpleframework.http.core.ContainerSocketProcessor;
import org.simpleframework.transport.connect.SocketConnection;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.InetSocketAddress;

import static com.google.common.io.ByteStreams.toByteArray;
import static java.lang.System.lineSeparator;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.regex.Pattern.compile;
import static javax.xml.parsers.DocumentBuilderFactory.newInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class FunctionalTest {

	@Test
	public void testCDataSections() {
		assertThrows(UnexpectedNodeTypeException.class, () -> helpTest("", "<![CDATA[after]]>"));
	}

	@Test
	public void testProcessingInstructions() {
		assertThrows(UnexpectedNodeTypeException.class, () -> helpTest("", "<?test after?>"));
	}

	@Test
	public void testRemoveComment() throws Exception {
		helpTest("<!--before-->", "");
	}

	@Test
	public void testInsertComments() throws Exception {
		helpTest("", "<!--after-->");
	}

	@Test
	public void testMoveComment() throws Exception {
		helpTest("<p>Test<!--test--></p>", "<p>Test</p><!--test-->");
	}

	@Test
	public void testChangeComment() throws Exception {
		helpTest("<!--before-->", "<!--after-->");
	}

	@Test
	public void testChangeAttribute() throws Exception {
		helpTest("<p class=\"before\">Test</p>", "<p class=\"after\">Test</p>");
	}

	@Test
	public void testAddAttribute() throws Exception {
		helpTest("<p>Test</p>", "<p class=\"after\">Test</p>");
	}

	@Test
	public void testRemoveAttribute() throws Exception {
		helpTest("<p class=\"before\">Test</p>", "<p>Test</p>");
	}

	@Test
	public void testDeeplyNestedMatch() throws Exception {
		helpTest(
			"<div id=\"a\"><div id=\"b\"><div id=\"c\">Test</div></div></div>",
			"<div id=\"d\"><div id=\"c\">Test</div></div>"
		);
	}

	@Test
	public void testRemoveElement() throws Exception {
		helpTest("<h1>1</h1><h2>2</h2><h3>3</h3><h4>4</h4>", "<h1>1</h1><h2>2</h2><h3>3</h3>");
	}

	@Test
	public void testRemoveTree() throws Exception {
		helpTest("<h1>1</h1><h2>2</h2><h3>3</h3><h4><input type=\"text\" /></h4>", "<h1>1</h1><h2>2</h2><h3>3</h3>");
	}

	@Test
	public void testChangeText() throws Exception {
		helpTest("<p id=\"x\">Before</p>", "<p id=\"x\">After</p>");
	}

	@Test
	public void testReorderElements() throws Exception {
		helpTest(
			"<a>Test</a><p>Test</p><ul><li>3</li><li>4</li></ul>",
			"<ul><li>3</li><li>4</li></ul><p>Test</p><a>Test</a>"
		);
	}

	@Test
	public void testReorderLineBreaks() throws Exception {
		helpTest("1234<img>1234<img>", "1234<img>1234<img><img>");
	}

	@Test
	public void testAddElement() throws Exception {
		helpTest("<p></p>", "<p><span>After</span></p>");
	}

	@Test
	public void testAddCustomElement() throws Exception {
		helpTest("<p></p>", "<p><span is=\"special\">After</span></p>");
	}

	@Test
	public void testAddElements() throws Exception {
		helpTest("", "<h1>After</h1><p>Test</p>");
	}

	@Test
	public void testClearChildNodes() throws Exception {
		helpTest("<div><h1>Before</h1><p>Test</p></div>", "<div></div>");
	}

	@Test
	public void testAppendElement() throws Exception {
		helpTest("<h1>1</h1><h2>2</h2><h3>3</h3>", "<h1>1</h1><h2>2</h2><h3>3</h3><h4>4</h4>");
	}

	@Test
	public void testPrependElement() throws Exception {
		helpTest("<h2>2</h2><h3>3</h3><h4>4</h4>", "<h1>1</h1><h2>2</h2><h3>3</h3><h4>4</h4>");
	}

	@Test
	public void testInsertElement() throws Exception {
		helpTest("<h1>1</h1><h2>2</h2><h4>4</h4>", "<h1>1</h1><h2>2</h2><h3>3</h3><h4>4</h4>");
	}

	@Test
	public void testMoveElementAppend() throws Exception {
		helpTest("<a>Test</a><p>1<span>2</span>3</p>", "<p>1<span>2</span>3<a>Test</a></p>");
	}

	@Test
	public void testMoveElementPrepend() throws Exception {
		helpTest("<a>Test</a><p>1<span>2</span>3</p>", "<p><a>Test</a>1<span>2</span>3</p>");
	}

	@Test
	public void testMoveElementInsert() throws Exception {
		helpTest("<a>Test</a><p>1<span>2</span>3</p>", "<p>1<a>Test</a><span>2</span>3</p>");
	}

	@Test
	public void testAppendText() throws Exception {
		helpTest("<p>Before</p>", "<p>Before then after</p>");
	}

	@Test
	public void testSpecialCharacters() throws Exception {
		helpTest("", "<pre>↘ine\n\t\"More\" \\ine's</pre>");
	}

	@Test
	public void testMoveFirstTextToLast() throws Exception {
		helpTest("<p>First<br>Second<br>Third</p>", "<p><br>Second<br>Third</p>First");
	}

	@Test
	public void testMoveMiddleTextToLast() throws Exception {
		helpTest("<p>First<br>Second<br>Third</p>", "<p>First<br><br>Third</p>Second");
	}

	@Test
	public void testMoveLastTextToLast() throws Exception {
		helpTest("<p>First<br>Second<br>Third</p>", "<p>First<br>Second<br></p>Third");
	}

	@Test
	public void testMoveFirstTextToMiddle() throws Exception {
		helpTest("<p>First<br>Second<br>Third</p>", "First<p><br>Second<br>Third</p>");
	}

	@Test
	public void testMoveMiddleTextToMiddle() throws Exception {
		helpTest("<br><p>First<br>Second<br>Third</p>", "<br>Second<p>First<br><br>Third</p>");
	}

	@Test
	public void testMoveLastTextToMiddle() throws Exception {
		helpTest("<br><p>First<br>Second<br>Third</p>", "<br>Third<p>First<br>Second<br></p>");
	}

	@Test
	public void testMoveFirstTextToFirst() throws Exception {
		helpTest("<br><p>First<br>Second<br>Third</p>", "<br>First<p><br>Second<br>Third</p>");
	}

	@Test
	public void testMoveMiddleTextToFirst() throws Exception {
		helpTest("<p>First<br>Second<br>Third</p>", "Second<p>First<br><br>Third</p>");
	}

	@Test
	public void testMoveLastTextToFirst() throws Exception {
		helpTest("<p>First<br>Second<br>Third</p>", "Third<p>First<br>Second<br></p>");
	}

	@Test
	public void testRemoveFirstText() throws Exception {
		helpTest("<p>First<br>Second<br>Third</p>", "<p><br>Second<br>Third</p>");
	}

	@Test
	public void testRemoveMiddleText() throws Exception {
		helpTest("<p>First<br>Second<br>Third</p>", "<p>First<br><br>Third</p>");
	}

	@Test
	public void testRemoveLastText() throws Exception {
		helpTest("<p>First<br>Second<br>Third</p>", "<p>First<br>Second<br></p>");
	}

	@Test
	public void testReorderTextAppend() throws Exception {
		helpTest("3<span>1</span><span>2</span>", "<span>1</span><span>2</span>3");
	}

	@Test
	public void testReorderTextPrepend() throws Exception {
		helpTest("<span>2</span><span>3</span>1", "1<span>2</span><span>3</span>");
	}

	@Test
	public void testReorderTextInsert() throws Exception {
		helpTest("2<span>1</span><span>3</span>", "<span>1</span>2<span>3</span>");
	}

	@Test
	public void testInsertText() throws Exception {
		helpTest("<span>1</span><span>3</span>", "<span>1</span>2<span>3</span>");
	}

	@Test
	public void testInsertTableRow() throws Exception {
		helpTest(
			"<table><tbody><tr><td>1</td><td>2</td><td>3</td></tr></tbody></table>",
			"<table><tbody><tr><td>1</td><td>2</td><td>3</td></tr><tr><td>4</td><td>5</td><td>6</td></tr></tbody></table>"
		);
	}

	@Test
	public void testRemoveTableRow() throws Exception {
		helpTest(
			"<table><tbody><tr><td>1</td><td>2</td><td>3</td></tr><tr><td>4</td><td>5</td><td>6</td></tr></tbody></table>",
			"<table><tbody><tr><td>1</td><td>2</td><td>3</td></tr></tbody></table>"
		);
	}

	@Test
	public void testRemoveAllTableRows() throws Exception {
		helpTest("<table><tbody><tr><td>1</td><td>2</td><td>3</td></tr></tbody></table>", "<table><tbody></tbody></table>");
	}

	@Test
	public void testChangeTableContents() throws Exception {
		helpTest(
			"<table><tbody><tr id=\"a\"><td>A</td></tr><tr id=\"b\"><td>B</td></tr></tbody></table>",
			"<table><tbody><tr id=\"c\"><td>C</td></tr><tr id=\"d\"><td>D</td></tr></tbody></table>"
		);
	}

	@Test
	public void testNoChanges() throws Exception {
		helpTest(
			"<table><tbody><tr><td>No</td></tr><tr><td>Changes</td></tr></tbody></table>",
			"<table><tbody><tr><td>No</td></tr><tr><td>Changes</td></tr></tbody></table>"
		);
	}

	@Test
	public void testManyIdenticalSiblings() throws Exception {
		final var commands = helpTest("""
			<p>X<meta id="a">X<meta id="b">X<meta id="c">X<meta id="d">X<meta id="e">X<meta id="f">X</p>
			""", """
			<p>X<meta id="a">X<meta id="b">X<meta id="c">Y<meta id="d">X<meta id="e">X<meta id="f">X</p>
			""");
		final var pattern = compile(
			"""
				\\Qparent.insertBefore(document.querySelector("html>body>p").childNodes[\\E[0-9]+\\Q], parent.childNodes[\\E[0-9]+\\Q]);\\E"""
		);
		if (pattern.matcher(commands).find()) {
			fail("The JavaScript should not need to swap any child nodes of the paragraph!");
		}
	}

	protected static final int PORT = 18230;
	protected static final String URL = "http://localhost:" + PORT + '/';

	protected static ChromeDriver driver;
	protected static SocketConnection connection;
	protected static volatile String content;

	@BeforeAll
	public static void setUp() throws IOException {
		final var options = new ChromeOptions();
		options.addArguments("--headless");
		options.addArguments("--no-sandbox");
		options.addArguments("--disable-dev-shm-usage");
		driver = new ChromeDriver(options);
		final var server = new ContainerSocketProcessor((request, response) -> {
			try {
				response.setValue("Content-Type", "text/html");
				try (final var body = response.getPrintStream()) {
					body.print(content);
				}
			} catch (IOException exception) {
				throw new RuntimeException(exception);
			}
		});
		connection = new SocketConnection(server);
		final var address = new InetSocketAddress(PORT);
		connection.connect(address);
	}

	private static String helpTest(String beforeSnippet, String afterSnippet) throws Exception {
		final var before = wrapSnippet(beforeSnippet);
		final var after = wrapSnippet(afterSnippet);
		content = before;
		driver.get(URL);
		driver.manage().timeouts().pageLoadTimeout(10, SECONDS);
		final var javascript = getDifferencesAsJavaScript(before, after);
		final var wrappedJavaScript = wrapJavaScript(javascript);
		final var exception = driver.executeScript(wrappedJavaScript);
		if (exception instanceof final String message) {
			throw new BrowserException(message);
		}
		final var actual = driver.getPageSource();
		assertEquals(after, actual);
		return javascript;
	}

	private static String getDifferencesAsJavaScript(String beforeHtml, String afterHtml) throws Exception {
		final var writer = new StringWriter();
		try (final var stream = FunctionalTest.class.getResourceAsStream("/DeltaDOM.js")) {
			writer.write(new String(toByteArray(stream), UTF_8).replaceAll("\\bexport\\b", ""));
		}
		writer.write("execute(");
		try (final var scripter = new JSONDifferenceHandler(writer)) {
			final var before = toHtmlDocument(beforeHtml);
			final var after = toHtmlDocument(afterHtml);
			final var matches = NodeMatches.between(before, after);
			final var delta = new DeltaDOM<>(before, after, matches, scripter);
			delta.find();
		}
		writer.write(");");
		return writer.toString();
	}

	private static String wrapJavaScript(String javascript) {
		final var writer = new StringWriter();
		writer.write("try {");
		writer.write(lineSeparator());
		writer.write(lineSeparator());
		writer.write(javascript);
		writer.write(lineSeparator());
		writer.write("return null;");
		writer.write(lineSeparator());
		writer.write("} catch (exception) {");
		writer.write(lineSeparator());
		writer.write("return exception.message;");
		writer.write(lineSeparator());
		writer.write("}");
		return writer.toString();
	}

	private static Document toHtmlDocument(String html) throws Exception {
		final var factory = newInstance();
		final var builder = factory.newDocumentBuilder();
		html = html.replace("<br>", "<br/>");
		html = html.replace("<img>", "<img/>");
		html = html.replaceAll("<meta (.+?)>", "<meta $1 />");
		final var reader = new StringReader(html);
		final var source = new InputSource(reader);
		return builder.parse(source);
	}

	private static final String HTML_PREFIX = "<html><head><title>Test</title></head><body>";
	private static final String HTML_SUFFIX = "</body></html>";

	private static String wrapSnippet(String html) {
		return HTML_PREFIX + html + HTML_SUFFIX;
	}

	@AfterAll
	public static void cleanUp() throws IOException {
		connection.close();
		driver.quit();
	}

}
