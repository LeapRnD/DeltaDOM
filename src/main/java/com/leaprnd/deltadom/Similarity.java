package com.leaprnd.deltadom;

import static java.lang.Float.NEGATIVE_INFINITY;
import static java.lang.Float.POSITIVE_INFINITY;

public interface Similarity {
	float PERFECT_MATCH = POSITIVE_INFINITY;
	float GOOD_MATCH = 1f;
	float BAD_MATCH = -1f;
	float IMPOSSIBLE_MATCH = NEGATIVE_INFINITY;
}
