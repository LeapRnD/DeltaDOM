package com.leaprnd.deltadom.marriage;

import static com.leaprnd.deltadom.marriage.WalkResult.FOUND_BETTER_MARRIAGE;
import static com.leaprnd.deltadom.marriage.WalkResult.NO_BETTER_MARRIAGES;
import static com.leaprnd.deltadom.marriage.WalkResult.NO_MORE_UNMARRIED_MEN;
import static com.leaprnd.deltadom.marriage.WalkResult.NO_MORE_UNMARRIED_WOMEN;

public abstract class StableMarriageProblemSolver implements Runnable {

	private final IndexSet men = new IndexSet(getNumberOfMen());
	private final IndexSet women = new IndexSet(getNumberOfWomen());

	@Override
	public void run() {
		final var moreWomenThanMen = getNumberOfMen() < getNumberOfWomen();
		if (moreWomenThanMen) {
			while (true) {
				var bestMan = men.first();
				if (bestMan < 0) {
					return;
				}
				final var firstWoman = women.first();
				var strengthOfStrongestMarriage = getStrengthOfPotentialMarriageBetween(bestMan, firstWoman);
				var nextMan = men.next(bestMan);
				while (nextMan >= 0) {
					final var strengthOfMarriageToNextMan = getStrengthOfPotentialMarriageBetween(nextMan, firstWoman);
					if (strengthOfMarriageToNextMan > strengthOfStrongestMarriage) {
						bestMan = nextMan;
						strengthOfStrongestMarriage = strengthOfMarriageToNextMan;
					}
					nextMan = men.next(nextMan);
				}
				if (walkWomen(bestMan, strengthOfStrongestMarriage) == NO_BETTER_MARRIAGES) {
					women.remove(firstWoman);
					onMarriageBetween(bestMan, firstWoman, strengthOfStrongestMarriage);
				}
			}
		} else {
			while (true) {
				var bestWoman = women.first();
				if (bestWoman < 0) {
					return;
				}
				final var firstMan = men.first();
				var strengthOfStrongestMarriage = getStrengthOfPotentialMarriageBetween(firstMan, bestWoman);
				var nextWoman = women.next(bestWoman);
				while (nextWoman >= 0) {
					final var strengthOfMarriageToNextWoman = getStrengthOfPotentialMarriageBetween(firstMan, nextWoman);
					if (strengthOfMarriageToNextWoman > strengthOfStrongestMarriage) {
						bestWoman = nextWoman;
						strengthOfStrongestMarriage = strengthOfMarriageToNextWoman;
					}
					nextWoman = women.next(nextWoman);
				}
				if (walkMen(bestWoman, strengthOfStrongestMarriage) == NO_BETTER_MARRIAGES) {
					men.remove(firstMan);
					onMarriageBetween(firstMan, bestWoman, strengthOfStrongestMarriage);
				}
			}
		}
	}

	private WalkResult walkMen(int bestWoman, float strengthOfPreviousBestMarriage) {
		women.remove(bestWoman);
		while (true) {
			var bestMan = men.first();
			if (bestMan < 0) {
				return NO_MORE_UNMARRIED_MEN;
			}
			var strengthOfStrongestMarriage = getStrengthOfPotentialMarriageBetween(bestMan, bestWoman);
			var nextMan = men.next(bestMan);
			while (nextMan >= 0) {
				final var strenghOfMarriageToNextMan = getStrengthOfPotentialMarriageBetween(nextMan, bestWoman);
				if (strenghOfMarriageToNextMan > strengthOfStrongestMarriage) {
					bestMan = nextMan;
					strengthOfStrongestMarriage = strenghOfMarriageToNextMan;
				}
				nextMan = men.next(nextMan);
			}
			if (strengthOfStrongestMarriage <= strengthOfPreviousBestMarriage) {
				return NO_BETTER_MARRIAGES;
			}
			switch (walkWomen(bestMan, strengthOfStrongestMarriage)) {
				case FOUND_BETTER_MARRIAGE:
					continue;
				case NO_BETTER_MARRIAGES:
					onMarriageBetween(bestMan, bestWoman, strengthOfStrongestMarriage);
					return FOUND_BETTER_MARRIAGE;
				case NO_MORE_UNMARRIED_WOMEN:
					onMarriageBetween(bestMan, bestWoman, strengthOfStrongestMarriage);
					return NO_MORE_UNMARRIED_WOMEN;
			}
		}
	}

	private WalkResult walkWomen(int bestMan, float strengthOfPreviousBestMarriage) {
		men.remove(bestMan);
		while (true) {
			var bestWoman = women.first();
			if (bestWoman < 0) {
				return NO_MORE_UNMARRIED_WOMEN;
			}
			var strengthOfStrongestMarriage = getStrengthOfPotentialMarriageBetween(bestMan, bestWoman);
			var nextWoman = women.next(bestWoman);
			while (nextWoman >= 0) {
				final var strengthOfMarriageToNextWoman = getStrengthOfPotentialMarriageBetween(bestMan, nextWoman);
				if (strengthOfMarriageToNextWoman > strengthOfStrongestMarriage) {
					bestWoman = nextWoman;
					strengthOfStrongestMarriage = strengthOfMarriageToNextWoman;
				}
				nextWoman = women.next(nextWoman);
			}
			if (strengthOfStrongestMarriage <= strengthOfPreviousBestMarriage) {
				return NO_BETTER_MARRIAGES;
			}
			switch (walkMen(bestWoman, strengthOfStrongestMarriage)) {
				case FOUND_BETTER_MARRIAGE:
					continue;
				case NO_BETTER_MARRIAGES:
					onMarriageBetween(bestMan, bestWoman, strengthOfStrongestMarriage);
					return FOUND_BETTER_MARRIAGE;
				case NO_MORE_UNMARRIED_MEN:
					onMarriageBetween(bestMan, bestWoman, strengthOfStrongestMarriage);
					return NO_MORE_UNMARRIED_MEN;
			}
		}
	}

	protected abstract int getNumberOfMen();
	protected abstract int getNumberOfWomen();
	protected abstract float getStrengthOfPotentialMarriageBetween(int indexOfMan, int indexOfWoman);
	protected abstract void onMarriageBetween(int indexOfMan, int indexOfWoman, float strengthOfMarriage);

}
