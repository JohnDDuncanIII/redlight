package edu.gettysburg.redlight;

import java.util.Random;

/**
 * RedLightGame - Text-based interface for playing Red Light.  Text output may be suppressed through use of the "verbose" setting.
 * @author Todd W. Neller
 * *** PLEASE DO NOT DISTRIBUTE. ***
 */
public class RedLightGame {
	static public int numReds = 4, numGreens = 24, numChips = numReds + numGreens, goalScore = 50;
	private int[] score = new int[3]; // scores indexed by 1-based player number 
	private boolean[] bag = new boolean[numChips]; // chips represented by a boolean value (whether or not the chip is red)
	private int chipIndex = 0; // current chip in bag
	private int redsDrawn = 0; // how many reds have been drawn since the last mixing
	private int greensDrawn = 0; // how many greens have been drawn since the last mixing
	private static Random random = new Random(); 
	private boolean verbose = false; // whether or not there is printed output for game information.  This is automatically set to true if a HumanPlayer is detected.

	/**
	 * @return whether or not there is text-output during play.
	 */
	public boolean isVerbose() {
		return verbose;
	}

	/**
	 * @param verbose - whether or not there is text-output during play.
	 */
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/**
	 * play - play a single game of Red Light
	 * 
	 * @param player1 first player
	 * @param player2 second player
	 * @return whether or not the first player won the game
	 */
	boolean play(RedLightPlayer player1, RedLightPlayer player2) { // NOTE: Players are assumed to have already been initialized
		RedLightPlayer[] player = {null, player1, player2};
		if (player1 instanceof HumanPlayer || player2 instanceof HumanPlayer)
			verbose = true;
		if (verbose) {
			System.out.println("***RED LIGHT RULES***");
			System.out.printf("First player to %d points wins.  The first player starts with 1 point.\nOn each turn, draw chips from the %d chip bag until you \n(1) draw a red chip and score nothing, or \n(2) hold and score the green chips you've drawn.\nYou must draw at least one chip.\nDrawing red chip number %d causes a remixing of all chips.\n", goalScore, numChips, numReds);
		}
		bag = new boolean[numChips];
		for (int i = 0; i < numReds; i++)
			bag[i] = true;
		// play a game
		mix();
		int currentPlayer = 1;
		score[1] = 0;
		score[2] = 1;
		int turnTotal = 0;
		while (score[1] < goalScore && score[2] < goalScore) { // take a turn
			// get decision
			boolean draw = player[currentPlayer].getAction(score[currentPlayer], score[3 - currentPlayer], turnTotal, redsDrawn, greensDrawn) == RedLightPlayer.DRAW;

			// if player tries to hold with no turn total, indicate that this is wrong and flip.
			if (!draw && turnTotal == 0) {
				System.out.printf("Player %d must draw at least one chip.  Drawing...\n", currentPlayer);
				draw = true;
			}

			// if player can win by holding, force it.
			if (draw && score[currentPlayer] + turnTotal == goalScore) {
				System.out.printf("Player %d can win by holding.  Engaging emergency rationality safety net.  Holding...\n", currentPlayer);
				draw = false;
			}

			if (draw) { // flip (draw)
				if (bag[chipIndex++]) { // red draw
					if (verbose) System.out.printf("Player %d: RED\n", currentPlayer);
					redsDrawn++;
					turnTotal = 0;
					currentPlayer = 3 - currentPlayer;
					if (redsDrawn == numReds)
						mix();
				}
				else {
					if (verbose) System.out.printf("Player %d: green\n", currentPlayer);
					greensDrawn++;
					turnTotal++;
				}
			}
			else {
				if (verbose) System.out.printf("Player %d: HOLD\n", currentPlayer);
				score[currentPlayer] += turnTotal;
				turnTotal = 0;
				currentPlayer = 3 - currentPlayer;
			}
		}
		if (verbose) {
			System.out.printf("Player 1 score: %2d  Player 2 score: %2d\n", score[1], score[2]);
			System.out.printf("Player %d wins!\n", 3 - currentPlayer);
		}

		return currentPlayer == 2;
	}

	/**
	 * mix the bag of chips
	 */
	private void mix() {
		if (verbose) System.out.println("Mixing chips.");
		chipIndex = 0;
		for (int i = bag.length - 1; i > 0; i--) {
			int j = random.nextInt(i + 1);
			boolean tmp = bag[j];
			bag[j] = bag[i];
			bag[i] = tmp;			
		}
		redsDrawn = greensDrawn = 0;
	}

	public static void main(String[] args) {
		// Example usage: Here a human plays a computer player.
		RedLightPlayer player1 = new HumanPlayer();
//		RedLightPlayer player2 = new MaxScorePlayer();
//		RedLightPlayer player2 = new MaxScorePlayer(4); // Note: This constant 4 can be adjusted for the "easy" risk-averse player.
//		RedLightPlayer player2 = new OptimalPlayer();
//		RedLightPlayer player2 = new ANNPlayer();
		RedLightPlayer player2 = new ANNPlayer2();
		while(true) {
			player1.initialize(numReds, numGreens, goalScore);
			player2.initialize(numReds, numGreens, goalScore);
			new RedLightGame().play(player1, player2);
			System.out.println("Restarting...");
		}
	}

}
