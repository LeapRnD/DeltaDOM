package com.leaprnd.deltadom.matching;

import org.w3c.dom.Node;

@FunctionalInterface
interface Calculator<T extends Node> {
	float getSimilarityOf(T a, T b);
}
