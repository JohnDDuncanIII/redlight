package edu.gettysburg.redlight;

import java.util.Scanner;

/**
 * HumanPlayer - Human player for Red Light game.
 * @author Todd W. Neller
 * *** PLEASE DO NOT DISTRIBUTE. ***
 */

public class HumanPlayer extends RedLightPlayer {
	private int numReds = 6; // number of red chips in the bag
	private int numGreens = 42; // number of green chips in the bag
	private int numChips = numReds + numGreens; // number of chips in the bag
	private int goalScore = 50; // goal score
	private Scanner in = new Scanner(System.in);
	
	@Override
	public void initialize(int numReds, int numGreens, int goalScore) {
		this.numReds = numReds;
		this.numGreens = numGreens;
		this.numChips = numReds + numGreens;
		this.goalScore = goalScore;
		System.out.printf("There are %d red and %d green chips in the bag.  The goal score is %d.\n", numReds, numGreens, goalScore);
		System.out.println("To draw, simply press enter.  To hold, enter any one or more characters.");
	}

	@Override
	public int getAction(int playerScore, int opponentScore, int turnTotal, int redsDrawn, int greensDrawn) {
		int redsRemaining = numReds - redsDrawn;
		int greensRemaining = numChips - numReds - greensDrawn;
		System.out.printf("Your score: %d  Opponent score: %d  Turn total: %d  Reds remaining: %d  Greens remaining: %d  Draw/hold? ", playerScore, opponentScore, turnTotal, redsRemaining, greensRemaining);
		if (playerScore + turnTotal >= goalScore) {
			System.out.println("\n(You can now win by holding.  Holding automatically.)\n");
			return HOLD;
		}
		return in.nextLine().length() == 0 ? DRAW : HOLD;
	}
	
}
