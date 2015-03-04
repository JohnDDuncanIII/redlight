package edu.gettysburg.redlight;

/**
 * MaxScorePlayer - Player for Red Light game that maximizes expected score gain per turn.
 * @author Todd W. Neller
 * *** PLEASE DO NOT DISTRIBUTE. ***
 */

public class MaxScorePlayer extends RedLightPlayer {
	private int numReds = 6; // number of red chips in the bag
	private int numChips = 48; // number of chips in the bag
	private int goalScore = 50; // goal score
	private double riskAversion = 1; // 1 -> neutral; >1 -> risk averse, amplifying utility of hold
	
	public MaxScorePlayer() {
	}
	
	public MaxScorePlayer(double riskAversion) {
		this.riskAversion = riskAversion;
	}
	
	@Override
	public void initialize(int numReds, int numGreens, int goalScore) {
		this.numReds = numReds;
		this.numChips = numReds + numGreens;
		this.goalScore = goalScore;
	}

	@Override
	public int getAction(int playerScore, int opponentScore, int turnTotal, int redsDrawn, int greensDrawn) {
		if (playerScore + turnTotal >= goalScore) return HOLD;
		if (turnTotal == 0) return DRAW;
		int redsRemaining = numReds - redsDrawn;
		int greensRemaining = numChips - numReds - greensDrawn;
		return greensRemaining > riskAversion * (redsRemaining * turnTotal) ? DRAW : HOLD;
	}
	
	public int getHoldValue(int playerScore, int opponentScore, int redsDrawn, int greensDrawn) {
		int turnTotal = 0;
		while (getAction(playerScore, opponentScore, turnTotal, redsDrawn, greensDrawn) == DRAW)
			turnTotal++;
		return turnTotal;
	}
}
