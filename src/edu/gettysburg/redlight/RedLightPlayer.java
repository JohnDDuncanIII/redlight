package edu.gettysburg.redlight;

/**
 * RedLightPlayer - abstract class for a Red Light player. The initialize method should be called once before use of the getAction method.
 * @author Todd W. Neller
 * *** PLEASE DO NOT DISTRIBUTE. ***
 */
public abstract class RedLightPlayer {

	/**
	 * Action constants
	 */
	public static final int HOLD = 0, DRAW = 1;
	
	/**
	 * initialize - Initialize the player as necessary.
	 * @param numReds - number of red chips in the bag
	 * @param numGreens - number of green chips in the bag
	 * @param goalScore - goal score of game
	 */
	public abstract void initialize(int numReds, int numGreens, int goalScore);
	
	/**
	 * getAction - return one of the above action constants according to your policy.
	 * @param playerScore - current player score
	 * @param opponentScore - opponent player score
	 * @param turnTotal - current turn total
	 * @param redsDrawn - number of red chips drawn since last mixing
	 * @param greensDrawn - number of green chips drawn since last mixing
	 */
	public abstract int getAction(int playerScore, int opponentScore, int turnTotal, int redsDrawn, int greensDrawn);
	
}
