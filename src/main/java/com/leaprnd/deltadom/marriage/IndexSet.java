package com.leaprnd.deltadom.marriage;

public class IndexSet {

	private int first = 0;
	private final int[] nexts;
	private final int[] previouses;

	public IndexSet(int size) {
		nexts = new int[size];
		previouses = new int[size];
		for (var index = 0; index < size; index ++) {
			nexts[index] = index + 1;
			previouses[index] = index - 1;
		}
		nexts[size - 1] = -1;
	}

	public int first() {
		return first;
	}

	public int next(int current) {
		return nexts[current];
	}

	public void remove(int index) {
		if (index == first) {
			first = nexts[index];
			if (first >= 0) {
				previouses[first] = -1;
			}
		} else {
			final var previous = previouses[index];
			final var next = nexts[index];
			if (previous >= 0) {
				nexts[previous] = next;
			}
			if (next >= 0) {
				previouses[next] = previous;
			}
		}
	}

}
