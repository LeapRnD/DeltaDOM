package com.leaprnd.deltadom.marriage;

import com.google.common.collect.ImmutableSortedMap;
import org.junit.jupiter.api.Test;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

import static com.google.common.collect.ImmutableSortedMap.naturalOrder;
import static java.lang.Float.NEGATIVE_INFINITY;
import static java.lang.Float.POSITIVE_INFINITY;
import static java.lang.System.out;
import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class StableMarriageProblemSolverTest {

	@Test
	public void testSquareRandomGrid() {
		final var grid = createRandomGrid(10, 10);
		test(grid);
	}

	@Test
	public void testFatRandomGrid() {
		final var grid = createRandomGrid(10, 5);
		test(grid);
	}

	@Test
	public void testTallRandomGrid() {
		final var grid = createRandomGrid(5, 10);
		test(grid);
	}

	@Test
	public void testTinyTallGrid() {
		final var grid = createTinyTallGrid();
		test(grid);
	}

	@Test
	public void testTinyFatGrid() {
		final var grid = createTinyFatGrid();
		test(grid);
	}

	@Test
	public void testLongTallGrid() {
		final var grid = createLongTallGrid();
		test(grid);
	}

	@Test
	public void testLongFatGrid() {
		final var grid = createLongFatGrid();
		test(grid);
	}

	@Test
	public void testInfiniteGrid() {
		final var grid = new FloatGrid(
			new float[][] { { POSITIVE_INFINITY, 2f, NEGATIVE_INFINITY }, { 1f, POSITIVE_INFINITY, 3f },
				{ NEGATIVE_INFINITY, 4f, POSITIVE_INFINITY }, },
			3,
			3
		);
		test(grid);
	}

	@Test
	public void testTrickyGrid() {
		final var grid = new FloatGrid(new float[][] { { 1, 4, 5 }, { 2, 3, 0 }, { 0, 0, 0 }, }, 3, 3);
		test(grid);
	}

	@Test
	public void testManyRandomGrids() {
		final var random = new Random();
		for (var index = 0; index < 10000; index ++) {
			final var width = random.nextInt(100) + 1;
			final var height = random.nextInt(100) + 1;
			final var grid = createRandomGrid(width, height);
			test(grid);
		}
	}

	private FloatGrid createTinyTallGrid() {
		final float[][] array = { { 0.015625f, 1.0f } };
		return new FloatGrid(array, 1, 2);
	}

	private FloatGrid createTinyFatGrid() {
		final float[][] array = { { 0.015625f }, { 1.0f } };
		return new FloatGrid(array, 2, 1);
	}

	private FloatGrid createLongTallGrid() {
		final float[][] array = { { 0, 0, 0, 0, 1 }, { 4, 0, 0, 5, 0 }, { 0, 7, 0, 6, 0 }, { 3, 0, 0, 0, 2 } };
		return new FloatGrid(array, 4, 5);
	}

	private FloatGrid createLongFatGrid() {
		final float[][] array = { { 0, 4, 0, 3 }, { 0, 0, 7, 0 }, { 0, 0, 0, 0 }, { 0, 5, 6, 0 }, { 1, 0, 0, 2 } };
		return new FloatGrid(array, 5, 4);
	}

	private FloatGrid createRandomGrid(int width, int height) {
		final var random = new Random();
		final var array = new float[width][height];
		for (var x = 0; x < width; x ++) {
			for (var y = 0; y < height; y ++) {
				array[x][y] = random.nextFloat();
			}
		}
		return new FloatGrid(array, width, height);
	}

	public void test(FloatGrid grid) {
		try {
			final var xs = new HashSet<Integer>();
			final var ys = new HashSet<Integer>();
			final ImmutableSortedMap.Builder<Integer, Integer> builder = naturalOrder();
			new StableMarriageProblemSolver() {

				@Override
				protected float getStrengthOfPotentialMarriageBetween(int indexOfMan, int indexOfWoman) {
					return grid.getStrengthOfPotentialMarriageBetween(indexOfMan, indexOfWoman);
				}

				@Override
				protected int getNumberOfMen() {
					return grid.getWidth();
				}

				@Override
				protected int getNumberOfWomen() {
					return grid.getHeight();
				}

				@Override
				protected void onMarriageBetween(int x, int y, float strengthOfMarriage) {
					if (!xs.add(x)) {
						throw new IllegalStateException();
					}
					if (!ys.add(y)) {
						throw new IllegalStateException();
					}
					builder.put(x, y);
				}

			}.run();
			final var results = builder.build();
			assertEquals(Math.min(grid.getWidth(), grid.getHeight()), results.size());
		} catch (Throwable throwable) {
			print(grid, emptyMap());
			throw throwable;
		}
	}

	private void print(FloatGrid grid, Map<Integer, Integer> results) {
		final var format = new DecimalFormat("0.000000");
		for (var x = 0; x < grid.getWidth(); x ++) {
			for (var y = 0; y < grid.getHeight(); y ++) {
				final var formatted = format.format(grid.getStrengthOfPotentialMarriageBetween(x, y));
				if (y > 0) {
					out.print(" ");
				}
				if (results.containsKey(x) && results.get(x) == y) {
					out.print('[');
					out.print(formatted);
					out.print(']');
				} else {
					out.print(' ');
					out.print(formatted);
					out.print(' ');
				}
			}
			out.println();
		}
		out.println();
	}

}