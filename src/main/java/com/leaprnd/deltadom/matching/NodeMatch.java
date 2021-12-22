package com.leaprnd.deltadom.matching;

import org.w3c.dom.Node;

// TODO: Convert to value type once Valhalla is released
public record NodeMatch(Node node, float similarity) {}