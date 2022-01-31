# DeltaDOM

[![Maven](https://badgen.net/maven/v/maven-central/com.leaprnd.deltadom/core)](https://search.maven.org/artifact/com.leaprnd.deltadom/core) [![NPM](https://badgen.net/npm/v/deltadom)](https://www.npmjs.com/package/deltadom) [![Tests](https://github.com/Leap-R-D/DeltaDOM/actions/workflows/test.yml/badge.svg)](https://github.com/Leap-R-D/DeltaDOM/actions)

DeltaDOM small, fast Java library for finding the differences between two HTML documents. This is primarily useful for web applications employing server-side rendering that want to push changes to the browser.

## Usage

First you will need to add DeltaDOM to your project. If you are using [Gradle](https://gradle.org/), you will need to add the following dependencies to your `build.gradle` file:

```groovy
dependencies {
    implementation group: "com.leaprnd.deltadom", name: "core", version: "1.0.1"
}
```

Once that is done, you can use DeltaDOM to find the differences between any two HTML documents.

Consider the following verbose example:

```java
package com.example;

import org.w3c.dom.Document;
import com.leaprnd.deltadom.DeltaDOM;
import com.leaprnd.deltadom.json.JSONDifferenceHandler;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;

import java.io.StringReader;

import static com.leaprnd.deltadom.matching.NodeMatches.between;

public class Example {

	private static final String BEFORE_HTML = """
			<html>
				<body>
					<h1>Table of Prices</h1>
					<p>Here is our list of prices, as of <time>June 21, 2021</time></p>
					<table>
						<thead>
							<tr>
								<th scope="col">Feature</th>
								<th scope="col">Price</th>
							</tr>
						</thead>
						<tbody>
							<tr>
								<th scope="row">Widget</th>
								<td>12.99</td>
							</tr>
							<tr>
								<th scope="row">Gizmo</th>
								<td>9.99</td>
							</tr>
							<tr>
								<th scope="row">Gadget</th>
								<td>10.99</td>
							</tr>
							<tr>
								<th scope="row">Thingamabob</th>
								<td>4.99 <strong>Sale!</strong></td>
							</tr>
							<tr>
								<th scope="row">Thingamajig</th>
								<td>6.99</td>
							</tr>
						</tbody>
					</table>
				</body>
			</html>
			""";

	private static final String AFTER_HTML = """
			<html>
				<body>
					<h1>Table of Prices</h1>
					<p>Here is our list of prices, as of <time>December 22, 2021</time></p>
					<table>
						<thead>
							<tr>
								<th scope="col">Feature</th>
								<th scope="col">Price</th>
							</tr>
						</thead>
						<tbody>
							<tr>
								<th scope="row">Widget</th>
								<td>13.99</td>
							</tr>
							<tr>
								<th scope="row">Gizmo</th>
								<td>9.99</td>
							</tr>
							<tr>
								<th scope="row">Gadget</th>
								<td>10.99</td>
							</tr>
							<tr>
								<th scope="row">Thingamabob</th>
								<td>6.99</td>
							</tr>
							<tr>
								<th scope="row">Thingamajig</th>
								<td>4.99 <strong>Sale!</strong></td>
							</tr>
						</tbody>
					</table>
				</body>
			</html>
			""";

	public static void main(String ... arguments) throws Exception {
		final var before = parse(BEFORE_HTML);
		final var after = parse(AFTER_HTML);
		final var nodeMatches = between(before, after);
		try (final var json = new JSONDifferenceHandler(System.out)) {
			new DeltaDOM<>(before, after, nodeMatches, json).find();
		}
	}

	private static Document parse(String html) throws Exception {
		final var factory = DocumentBuilderFactory.newInstance();
		final var reader = new StringReader(html);
		final var source = new InputSource(reader);
		return factory.newDocumentBuilder().parse(source);
	}

}
```

When runned, the above class will output the difference between `BEFORE_HTML` and `AFTER_HTML` as a JSON array of operations:

```json
[
    [4,"html>body>p>time",0,"December 22, 2021"],
    [6,"html>body>table>tbody>tr:last-of-type>td","html>body>table>tbody>tr:nth-of-type(4)",3],
    [5,"html>body>table>tbody>tr:last-of-type",3,"html>body>table>tbody>tr:nth-of-type(4)",4],
    [6,"html>body>table>tbody>tr:nth-of-type(4)>td:last-of-type","html>body>table>tbody>tr:last-of-type",3],
    [5,"html>body>table>tbody>tr:nth-of-type(4)",5,"html>body>table>tbody>tr:last-of-type",4],
    [4,"html>body>table>tbody>tr:first-of-type>td",0,"13.99"],
    [0,"html>body>table>tbody>tr:first-of-type>td",1],
    [0,"html>body>p>time",1]
]
```

You can install [the client-side logic](https://github.com/Leap-R-D/DeltaDOM/blob/main/src/main/javascript/DeltaDOM.js) for executing these operations via [NPM](https://www.npmjs.com/):

```sh
npm install deltadom
```

After which you can import the `execute` function into your project.

```javascript
import {execute} from "deltadom";
```

It's up to you to somehow get the array of commands from the server to the browser, but either an [EventSource](https://developer.mozilla.org/en-US/docs/Web/API/EventSource) or a [WebSocket](https://developer.mozilla.org/en-US/docs/Web/API/WebSocket) should do the trick.