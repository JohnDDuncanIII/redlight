package edu.gettysburg.redlight;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * OptimalPlayer - Optimal player for Red Light game.
 * @author Todd W. Neller
 * *** PLEASE DO NOT DISTRIBUTE. ***
 */

public class OptimalPlayer extends RedLightPlayer {
	private int numReds = 4; // number of red chips in the bag
	private int numChips = 28; // number of chips in the bag
	private int goalScore = 50; // goal score
	private double epsilon = 1e-14; // convergence threshold for value iteration
	double[][][][][] v; // state value indexed by reds drawn, greens drawn, my score, other score, turn total  
	private boolean[][][][][] draw; // optimal action indexed by reds drawn, greens drawn, my score, other score, turn total  
	private boolean initialized = false;
	private boolean verbose = true;

	private void valueIterate() {
		double maxChange;
		do {
			maxChange = 0.0;
			for (int r = 0; r < numReds; r++) // for all r
				for (int g = 0; g < numChips - numReds + 1; g++) // for all g
					for (int i = 0; i < goalScore; i++) // for all i
						for (int j = 0; j < goalScore; j++) // for all j
							for (int k = 0; k < goalScore - i; k++) { // for all k

								double oldProb = v[r][g][i][j][k];
								double pDraw, pHold;

								// Compute the probability of winning by drawing another chip
								// red drawn (last red ends turn with remixing)
								pDraw = (r == numReds - 1) ? 1.0 - pWin(0, 0, j, i, 0) : 1.0 - pWin(r + 1, g, j, i, 0);
								pDraw *= numReds - r;
								// green drawn
								if (numChips - numReds - g > 0)
									pDraw += (numChips - numReds - g) * pWin(r, g + 1, i, j, k + 1);
								pDraw /= (numChips - r - g);

								// Compute the probability of winning with a hold
								pHold = (k == 0) ? 0.0 : 1 - pWin(r, g, j, i + k, 0); // holding with no greens should be illegal - can lead to stalemate when only reds left; count as loss

								// Choose the best
								boolean shouldDraw = (k == 0) || (pDraw > pHold);
								v[r][g][i][j][k] = shouldDraw ? pDraw : pHold;
								draw[r][g][i][j][k] = shouldDraw;
								double change = Math.abs(v[r][g][i][j][k] - oldProb);
								maxChange = Math.max(maxChange, change);

							}
			System.out.println(maxChange);
		} while (maxChange >= epsilon);
	}

	public double pWin(int r, int g, int i, int j, int k) { 
		if (i + k >= goalScore)
			return 1.0;
		else if (j >= goalScore)
			return 0.0;
		else return v[r][g][i][j][k];
	}

	private boolean exportData() {
		try {
			String filename = String.format("redlight-%d-%d-%d-%e.dat", numReds, numChips, goalScore, epsilon);
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));
			out.writeObject(v);
			out.writeObject(draw);
			out.close();
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	private boolean importData() {
		try {
			String filename = String.format("redlight-%d-%d-%d-%e.dat", numReds, numChips, goalScore, epsilon);
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
			v = (double[][][][][]) in.readObject();
			draw = (boolean[][][][][]) in.readObject();
			in.close();
			return true;
		} catch (FileNotFoundException e) {
			System.out.println("\nFile not found.  Computing optimal policy.");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static void main(String[] args){
		OptimalPlayer player = new OptimalPlayer();
		player.initialize(6, 42, 50);
	}

	@Override
	public void initialize(int numReds, int numGreens, int goalScore) {
		if (initialized) return;
		if (verbose) System.out.println("Initializing optimal player...");
		this.numReds = numReds;
		this.numChips = numReds + numGreens;
		this.goalScore = goalScore;
		int stateCount = 0;
		if (!importData()) {
			v = new double[numReds][][][][];
			draw = new boolean[numReds][][][][];
			for (int w = 0; w < numReds; w++) {
				v[w] = new double[numChips - numReds + 1][goalScore][goalScore][];
				draw[w] = new boolean[numChips - numReds + 1][goalScore][goalScore][];
				for (int c = 0; c < numChips - numReds + 1; c++) 
					for (int i = 0; i < goalScore; i++)
						for (int j = 0; j < goalScore; j++) { 
							v[w][c][i][j] = new double[goalScore - i];
							draw[w][c][i][j] = new boolean[goalScore - i];
							stateCount += goalScore - i;
						}
			}
			System.out.println("State count: " + stateCount);
			valueIterate();
			System.out.println(v[0][0][0][0][0]);
			exportData();
		}
		if (verbose) System.out.println("Optimal player initialized.");
		initialized = true;
	}

	@Override
	public int getAction(int playerScore, int opponentScore, int turnTotal, int redsDrawn, int greensDrawn) {
		if (playerScore + turnTotal >= goalScore) return HOLD;
		return draw[redsDrawn][greensDrawn][playerScore][opponentScore][turnTotal] ? DRAW : HOLD;
	}
	
	public double getV(int playerScore, int opponentScore, int turnTotal, int redsDrawn, int greensDrawn) {
		return v[redsDrawn][greensDrawn][playerScore][opponentScore][turnTotal];
	}
	
	public double getQ(int playerScore, int opponentScore, int turnTotal, int redsDrawn, int greensDrawn, int action) {
		if (action == DRAW) {
			// Compute the probability of winning by drawing another chip
			// red drawn (last red ends turn with remixing)
			double pDraw = (redsDrawn == numReds - 1) ? 1.0 - pWin(0, 0, opponentScore, playerScore, 0) : 1.0 - pWin(redsDrawn + 1, greensDrawn, opponentScore, playerScore, 0);
			pDraw *= numReds - redsDrawn;
			// green drawn
			if (numChips - numReds - greensDrawn > 0)
				pDraw += (numChips - numReds - greensDrawn) * pWin(redsDrawn, greensDrawn + 1, playerScore, opponentScore, turnTotal + 1);
			pDraw /= (numChips - redsDrawn - greensDrawn);
			return pDraw;
		}
		else if (action == HOLD) {
			// Compute the probability of winning with a hold
			double pHold = (turnTotal == 0) ? 0.0 : 1 - pWin(redsDrawn, greensDrawn, opponentScore, playerScore + turnTotal, 0); // holding with no greens should be illegal - can lead to stalemate when only reds left; count as loss
			return pHold;
		}
		else { // Invalid action
			System.err.println("Invalid action: " + action);
			System.exit(1);
		}
		return 0;
	}
	
	public int getHoldValue(int playerScore, int opponentScore, int redsDrawn, int greensDrawn) {
		int turnTotal = 0;
		while (greensDrawn + turnTotal <= (numChips - numReds) && getAction(playerScore, opponentScore, turnTotal, redsDrawn, greensDrawn + turnTotal) == DRAW)
			turnTotal++;
		return turnTotal;
	}
	
}
