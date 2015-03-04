/*
 * John, Mike, Jeff, Steven, and Eddy
 * Android RedLight version 1.0, completed on Monday the 18th of November 3:20AM
 * Modified on the 29th of January 9:20PM --> fixed sound when muted option is selected. 
 * ... No longer mutes entire OS!
 * Plays the game of redlight, which is based off of the folk game of pig
 */

package edu.gettysburg.redlight;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
//import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {
	private boolean isMuted = false;
	// COMPUTER_DELAY - delay between computer rolls in milliseconds
	protected static final long COMPUTER_DELAY = 1000;
	// Game state variables:
	private int userScore = 0, computerScore = 0, turnTotal = 0; 
	// userStartGame - whether or not the user starts the current game 
	private boolean userStartGame = true; 
	// isUserTurn - whether or not it is currently the user's turn
	private boolean isUserTurn = true; 
	// imageName - name of the current displayed image
	private String imageName = "roll"; 
	// Variable to reflect whether or not the user has successfully chosen their difficulty from the opening dialogue
	private boolean hasChosenDifficulty = false;
	private boolean isTestMode=false;

	// GUI views
	private CheckBox checkBoxResetStats;
	private EditText editTextEnteredText;
	private TextView textViewYourScore, textViewCScore, textViewMyScore, textViewTurnTotal, textViewNumGreens, textViewNumReds, textViewUserName;
	private TextView textViewUserNumWinsEasy, textViewUserNumLossesEasy, textViewUserPercEasy, textViewUserNumWinsMedium, textViewUserNumLossesMedium, textViewUserPercMedium, textViewUserNumWinsHard, textViewUserNumLossesHard, textViewUserPercHard;
	private TextView textViewUserNameStats1, textViewUserNameStats2, textViewUserNameStats3, textViewUserNameStats4, textViewUserNameStats5, textViewUserNameStats6, textViewUserNameStats7, textViewUserNameStats8, textViewUserNameStats9;
	private TextView textViewTestDirections;

	// View the traffic light
	private ImageView imageView;
	// GUI buttons
	private Button buttonRoll, buttonHold, buttonEasy, buttonMedium, buttonHard;
	// The opponent. Primarily used to define the opponent's skill level
	private static RedLightPlayer opponent;
	// Mapping from image strings to Drawable resources
	private HashMap<String, Drawable> drawableMap = new HashMap<String, Drawable>(); 
	// Int values to define the number of red, green, and total number of chips along with the goal score
	static public int numReds = 4, numGreens = 24, numChips = numReds + numGreens, goalScore = 50;
	// Chips represented by a boolean value (whether or not the chip is red)
	private boolean[] bag = new boolean[numChips]; 
	// Current chip in bag
	private int chipIndex = 0; 
	// How many reds have been drawn since the last mixing
	private int redsDrawn = 0; 
	// How many greens have been drawn since the last mixing
	private int greensDrawn = 0; // how many greens have been drawn since the last mixing
	// Random value generator
	private static Random random = new Random(); 
	// Hold's the userName. If none is entered in the EditText, then it will default to ``User"
	private String userName="User's";
	// String to keep track of what level the computer has been chosen as
	private String compLevel;
	// Simple Dialog used to show the splash screen
	protected Dialog mSplashDialog;
	// Simple Dialog to show the stats screen
	protected Dialog mStatsDialog;
	// MediaPlayer to plays sounds on user interaction primarily with buttons
	private MediaPlayer mp;

	// Keep track of user wins, losses, and win percents for easy, medium, and hard game types
	private int userNumWinsEasy;
	private int userNumLossesEasy;
	private double userPercEasy;

	private int userNumWinsMedium;
	private int userNumLossesMedium;
	private double userPercMedium;

	private int userNumWinsHard;
	private int userNumLossesHard;
	private double userPercHard;

	// Buttons for the test page
	private Button buttonDrawGreen, buttonDrawRed;

	//Backup data for when going to test mode
	//private int numRedsBack;
	//private int numGreensBack;
	private boolean[] bagBack; 
	private int chipIndexBack; 
	private int redsDrawnBack; 
	private int greensDrawnBack; 
	private int userScoreBack; 
	private int computerScoreBack; 
	private int turnTotalBack;

	/****************************************************************/
	/** Method that is called when the application is instantiated **/
	/****************************************************************/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		String FILENAME = "data.txt";


		//file.delete();

		try {
			FileInputStream fis = openFileInput(FILENAME);

			FileInputStream firstFIS = openFileInput(FILENAME);
			if (firstFIS.available() == 0) {
				//deleteFile(getFilesDir().getPath() + "/data.txt");
				deleteFile("data.txt");
			}
			else {
				BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
				userName=reader.readLine().trim();

				userNumWinsEasy = Integer.valueOf(reader.readLine());
				userNumLossesEasy = Integer.valueOf(reader.readLine());
				userPercEasy = Double.valueOf(reader.readLine());

				userNumWinsMedium = Integer.valueOf(reader.readLine());
				userNumLossesMedium = Integer.valueOf(reader.readLine());
				userPercMedium = Double.valueOf(reader.readLine());

				userNumWinsHard = Integer.valueOf(reader.readLine());
				userNumLossesHard = Integer.valueOf(reader.readLine());
				userPercHard = Double.valueOf(reader.readLine());
			}
		} catch (FileNotFoundException e) { 

			try {
				FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
				writer.write("");
				writer.flush();
			} catch (IOException e1) {
				e.printStackTrace();
			}

		}
		catch (IOException e) {
			resetStats();
			System.out.println("File not found, defaulting all values to 0");
		} 

		showSplashScreen();
		setContentView(R.layout.activity_main);	

		textViewYourScore = (TextView) findViewById(R.id.textViewYourScore);
		textViewMyScore = (TextView) findViewById(R.id.textViewMyScore);
		textViewTurnTotal = (TextView) findViewById(R.id.textViewTurnTotal);
		textViewNumGreens = (TextView) findViewById(R.id.textViewNumGreens);
		textViewNumReds = (TextView) findViewById(R.id.textViewNumReds); 
		textViewUserName = (TextView) findViewById(R.id.textViewUserName);
		//textViewUScore = (TextView) findViewById(R.id.textView1);
		textViewCScore = (TextView) findViewById(R.id.textView3);

		buttonRoll = (Button) findViewById(R.id.buttonRoll);
		buttonHold = (Button) findViewById(R.id.buttonHold);
		imageView = (ImageView) findViewById(R.id.imageView);

		drawableMap.put("go", getResources().getDrawable(R.drawable.go));
		drawableMap.put("stop", getResources().getDrawable(R.drawable.stop));
		drawableMap.put("hold", getResources().getDrawable(R.drawable.hold));
		drawableMap.put("green", getResources().getDrawable(R.drawable.green));
		drawableMap.put("red", getResources().getDrawable(R.drawable.red));
		drawableMap.put("splash", getResources().getDrawable(R.drawable.splash));
		drawableMap.put("splashstopped", getResources().getDrawable(R.drawable.splashstopped));
		drawableMap.put("splashcompstop", getResources().getDrawable(R.drawable.splashcompstop));

		setImage("splash");
		setUserName(userName.trim());

		bag = new boolean[numChips];
		for (int i = 0; i < numReds; i++) {
			bag[i] = true;
		} 

		this.setComputerScore(1);
		this.setNumReds(numReds);
		this.setNumGreens(numGreens);

		setTextColors();

		buttonRoll.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				roll();
			}
		});
		buttonHold.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				hold();
			}
		});
	}

	/******************************************************/
	/** Method that is called when the application quits **/
	/******************************************************/
	protected void onDestroy() {
		try {
			String FILENAME = "data.txt";
			FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
			writer.write(userName.trim());
			writer.newLine();
			writer.write(String.valueOf(userNumWinsEasy));
			writer.newLine();
			writer.write((String.valueOf(userNumLossesEasy)));
			writer.newLine();
			writer.write(String.valueOf(userPercEasy));
			writer.newLine();
			writer.write((String.valueOf(userNumWinsMedium)));
			writer.newLine();
			writer.write((String.valueOf(userNumLossesMedium)));
			writer.newLine();
			writer.write(String.valueOf(userPercMedium));
			writer.newLine();
			writer.write((String.valueOf(userNumWinsHard)));
			writer.newLine();
			writer.write((String.valueOf(userNumLossesHard)));
			writer.newLine();
			writer.write(String.valueOf(userPercHard));
			writer.flush();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		super.onDestroy();
	}

	private void mute() {
		isMuted = true;
		//AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		//am.setStreamMute(AudioManager.STREAM_MUSIC, true);
	}

	public void unmute() {
		isMuted = false;  
		//AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		//am.setStreamMute(AudioManager.STREAM_MUSIC, false);
	}

	/******************************************************************************************************************************/
	/** Shows the splash screen to allow the user to enter their name, reset stats, and choose difficulty before the game starts **/
	/******************************************************************************************************************************/
	protected void showSplashScreen() {
		mSplashDialog = new Dialog(this, R.style.AppTheme);
		mSplashDialog.setContentView(R.layout.difficulty_main);

		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		buttonEasy = (Button) mSplashDialog.findViewById(R.id.buttonEasy);
		buttonMedium = (Button) mSplashDialog.findViewById(R.id.buttonMedium);
		buttonHard = (Button) mSplashDialog.findViewById(R.id.buttonHard);
		editTextEnteredText = (EditText) mSplashDialog.findViewById(R.id.editTextEnteredText);
		checkBoxResetStats = (CheckBox) mSplashDialog.findViewById(R.id.checkBoxReset);

		if(!userName.equals("User's")) {
			editTextEnteredText.setHint("Name Currently Saved As: " + userName.substring(0, userName.length()-2));
		}


		/*
		checkBoxResetStats.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(checkBoxResetStats.isChecked()) {
					resetStats();
				}
			}
		});
		 */

		buttonEasy.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				String inp=editTextEnteredText.getText().toString();
				if(inp.length()>15) {
					inp=inp.substring(0,15);
				}

				if(!inp.equals("")) {
					setUserName(inp.trim() + "'s");
				}
				hasChosenDifficulty=true;
				removeSplashScreen();
				compLevel="easy";
				opponent = new MaxScorePlayer(4);
				opponent.initialize(numReds, numGreens, goalScore);
				if(checkBoxResetStats.isChecked()) {
					resetStats();
				}
			}
		});

		buttonMedium.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				String inp=editTextEnteredText.getText().toString();
				if(!inp.equals("")) {
					setUserName(inp.trim() + "'s");
				}
				hasChosenDifficulty=true;
				removeSplashScreen();
				compLevel="medium";
				opponent = new MaxScorePlayer();
				opponent.initialize(numReds, numGreens, goalScore);
				if(checkBoxResetStats.isChecked()) {
					resetStats();
				}
			}
		});

		buttonHard.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				String inp=editTextEnteredText.getText().toString();
				if(!inp.equals("")) {
					setUserName(inp.trim() + "'s");
				}
				hasChosenDifficulty=true;
				removeSplashScreen();
				compLevel="hard";
				opponent = new ANNPlayer2();
				opponent.initialize(numReds, numGreens, goalScore);
				if(checkBoxResetStats.isChecked()) {
					resetStats();
				}
			}
		});

		mSplashDialog.setCancelable(false);
		mSplashDialog.show();
	}

	/********************************************************/
	/** Removes the Dialog that displays the splash screen **/
	/********************************************************/
	protected void removeSplashScreen() {
		if (mSplashDialog != null) {
			mix();
			//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
			mSplashDialog.dismiss();
			mSplashDialog = null;
		}
	}

	/**********************************/
	/** Resets user's name and stats **/
	/**********************************/
	public void resetStats() {
		System.out.println("-------------\nResetting Stats\n-------------");
		setUserName("User's");
		userNumWinsEasy=0;
		userNumLossesEasy=0;
		userPercEasy=0;
		userNumWinsMedium=0;
		userNumLossesMedium=0;
		userPercMedium=0;
		userNumWinsHard=0;
		userNumLossesHard=0;
		userPercHard=0;
	}

	/*******************************************************/
	/** Methods used for setting and updating GUI objects **/
	/*******************************************************/
	private void setUserName(final String uName) {
		userName = uName;
		textViewUserName.setText(String.valueOf(uName.trim()) + " Score:");
	}

	private void setUserScore(final int newScore) {
		userScore = newScore;
		textViewYourScore.setText(String.valueOf(newScore));
	}

	private void setComputerScore(final int newScore) {
		computerScore = newScore;
		textViewMyScore.setText(String.valueOf(newScore));
	}

	private void setTurnTotal(final int newTotal) {
		turnTotal = newTotal;
		textViewTurnTotal.setText(String.valueOf(newTotal));
	}

	private void setNumReds(final int newNumReds) {
		textViewNumReds.setText(String.valueOf(newNumReds));
	}

	private void setNumGreens(final int newNumGreens) {
		textViewNumGreens.setText(String.valueOf(newNumGreens));
	}

	private void setImage(final String newImageName) {
		imageName = newImageName;
		imageView.setImageDrawable(drawableMap.get(imageName));
	}

	private void setSplash(final long sleepTime) {
		new Thread(new Runnable() {
			public void run() {
				Thread.yield();
				try { Thread.sleep(sleepTime); } 
				catch (InterruptedException e) { e.printStackTrace(); }
				runOnUiThread(new Runnable() {public void run() {setImage("splash");}});
			}
		}).start();
	}

	/**************************************************************************************************************************/
	/** When the go button is clicked, the roll method will be called to get a value in the bag                              **/
	/** Based on this value, the traffic light will change colors and specific values will be changed to reflect the result. **/
	/**************************************************************************************************************************/
	private void roll() {
		System.out.println("Bag value at " + chipIndex + " is " + bag[chipIndex]);

		if (bag[chipIndex++]) {
			playRed();
			redsDrawn++;
			this.setNumReds(Integer.parseInt(String.valueOf(textViewNumReds.getText()))-1);
			setImage("stop");
			setSplash(700);
			setTurnTotal(0);
			if (redsDrawn == numReds) {
				mix();
			}
			changeTurn();
		}
		else {
			playGo();
			greensDrawn++;
			this.setNumGreens(Integer.parseInt(String.valueOf(textViewNumGreens.getText()))-1);
			setTurnTotal(turnTotal + 1);
			setImage("go");
			setSplash(300);
		}
	}

	/**************************************************************************************************************************/
	/** When the stop button is clicked, the hold method will be called to get a value in the bag                            **/
	/** Based on this value, the traffic light will change colors and specific values will be changed to reflect the result  **/
	/**************************************************************************************************************************/
	private void hold() {
		playStop();
		if (isUserTurn) {
			setUserScore(userScore + turnTotal);
		}
		else {
			setComputerScore(computerScore + turnTotal);
		}
		setTurnTotal(0);
		if (userScore >= goalScore || computerScore >= goalScore) {
			endGame();
		}
		else {
			if(isTestMode) {
				setImage("splashcompstop");
				setSplash(900);
				changeTurn();
			}
			else {
				setImage("splashstopped");
				setSplash(800);
				changeTurn();
			}
		}
	}

	/************************************************************************************************************************/
	/** Plays the go sound, and ensures that the MediaPlayer object is released when it completes playing to avoid crashes **/
	/************************************************************************************************************************/
	private void playGo() {
		if(!isMuted) {
			new Thread(new Runnable() {
				public void run() {
					Thread.yield();
					mp = MediaPlayer.create(MainActivity.this, R.raw.car_speeding_by_1);

					if(mp == null) {            
						System.out.println("Create() on MediaPlayer failed.");       
					} else {
						mp.setOnCompletionListener(new OnCompletionListener() {

							@Override
							public void onCompletion(MediaPlayer mediaplayer) {
								mediaplayer.stop();
								mediaplayer.release();
							}
						});
						mp.start();
					}
				}
			}).start();
		}
	}

	/***********************************************************************************************************************************/
	/** Plays the hold rev down sound, and ensures that the MediaPlayer object is released when it completes playing to avoid crashes **/
	/***********************************************************************************************************************************/
	private void playStop() {
		if(!isMuted) {
			new Thread(new Runnable() {
				public void run() {
					Thread.yield();
					mp = MediaPlayer.create(MainActivity.this, R.raw.rev_down);

					if(mp == null) {            
						System.out.println("Create() on MediaPlayer failed.");       
					} else {
						mp.setOnCompletionListener(new OnCompletionListener() {

							@Override
							public void onCompletion(MediaPlayer mediaplayer) {
								mediaplayer.stop();
								mediaplayer.release();
							}
						});
						mp.start();
					}
				}
			}).start();
		}
	}

	/*******************************************************************************************************************************/
	/** Plays the red brake sound, and ensures that the MediaPlayer object is released when it completes playing to avoid crashes **/
	/*******************************************************************************************************************************/
	private void playRed() {
		if(!isMuted) {
			new Thread(new Runnable() {
				public void run() {
					Thread.yield();
					mp = MediaPlayer.create(MainActivity.this, R.raw.brake_short_softer);

					if(mp == null) {            
						System.out.println("Create() on MediaPlayer failed.");       
					} else {
						mp.setOnCompletionListener(new OnCompletionListener() {

							@Override
							public void onCompletion(MediaPlayer mediaplayer) {
								mediaplayer.stop();
								mediaplayer.release();
							}
						});
						mp.start();
					}
				}
			}).start();
		}
	}

	/**************************************************************************************************************************/
	/** Plays the beep sound, and ensures that the MediaPlayer object is released when it completes playing to avoid crashes **/
	/**************************************************************************************************************************/
	private void playBeep() {
		if(!isMuted){
			new Thread(new Runnable() {
				public void run() {
					Thread.yield();
					mp = MediaPlayer.create(MainActivity.this, R.raw.beep_beep);

					if(mp == null) {            
						System.out.println("Create() on MediaPlayer failed.");       
					} else {
						mp.setOnCompletionListener(new OnCompletionListener() {

							@Override
							public void onCompletion(MediaPlayer mediaplayer) {
								mediaplayer.stop();
								mediaplayer.release();
							}
						});
						mp.start();
					}
				}
			}).start();
		}
	}

	/**************************/
	/** Mix the bag of chips **/
	/**************************/
	private void mix() {
		playBeep();
		System.out.println("Mixing chips.");
		chipIndex = 0;
		for (int i = bag.length - 1; i > 0; i--) {
			int j = random.nextInt(i + 1);
			boolean tmp = bag[j];
			bag[j] = bag[i];
			bag[i] = tmp;			
		}
		redsDrawn = greensDrawn = 0;
		this.setNumGreens(numGreens);
		this.setNumReds(numReds);
	}

	/**************************************************/
	/** Change the current players turn to the other **/
	/**************************************************/
	private void changeTurn() {
		setSplash(500);
		isUserTurn = !isUserTurn;
		setTextColors();
		setButtonsState();

		if (!isUserTurn && !isTestMode) {
			computerTurn();
		}
		else if(!isUserTurn && isTestMode) {
			computerTurnTestMode();
			System.out.println("NOW THE COMPUTERS TURN IN TEST MODE");
		}
	}

	/******************************************************/
	/** Set the text color of the current player to Blue **/
	/******************************************************/
	private void setTextColors() {
		if(isUserTurn) {
			//textViewUserName.setTextColor(Color.parseColor("#48cfe1"));
			//textViewUScore.setTextColor(Color.parseColor("#48cfe1"));

			textViewUserName.setShadowLayer(4, 0, 0, Color.GREEN);
			//textViewUScore.setShadowLayer(4, 0, 0, Color.GREEN);
			textViewYourScore.setShadowLayer(4, 0, 0, Color.GREEN);

			textViewCScore.setShadowLayer(0, 0, 0, Color.TRANSPARENT);
			textViewMyScore.setShadowLayer(0, 0, 0, Color.TRANSPARENT);
			//textViewCScore.setTextColor(Color.WHITE);
		}
		else {
			textViewUserName.setTextColor(Color.WHITE);
			//textViewUScore.setTextColor(Color.WHITE);

			textViewUserName.setShadowLayer(0, 0, 0, Color.TRANSPARENT);
			//textViewUScore.setShadowLayer(0, 0, 0, Color.TRANSPARENT);
			textViewYourScore.setShadowLayer(0, 0, 0, Color.TRANSPARENT);

			textViewCScore.setShadowLayer(4, 0, 0, Color.GREEN);
			textViewMyScore.setShadowLayer(4, 0, 0, Color.GREEN);
			//textViewCScore.setTextColor(Color.parseColor("#48cfe1"));
		}
	}

	/*****************************************************************************************/
	/** Executes an entire computerTurn, and ensuring proper functionality through a thread **/
	/*****************************************************************************************/
	private synchronized void computerTurn() {
		new Thread(new Runnable() {
			public void run() {
				Thread.yield();
				try { Thread.sleep(COMPUTER_DELAY); } 
				catch (InterruptedException e) { e.printStackTrace(); }
				while (!isUserTurn) {
					boolean draw = opponent.getAction(computerScore, userScore, turnTotal, redsDrawn, greensDrawn) == RedLightPlayer.DRAW;

					if (!draw && turnTotal == 0) {
						draw = true;
					}
					if (draw && computerScore + turnTotal == goalScore) {
						draw = false;
					}
					if (draw) {
						runOnUiThread(new Runnable() {public void run() {roll();}});
					}
					else {
						runOnUiThread(new Runnable() {public void run() {hold();}});
						break;
					}
					Thread.yield();
					try { Thread.sleep(COMPUTER_DELAY); } 
					catch (InterruptedException e) { e.printStackTrace(); }

				}
			}
		}).start();
	}

	private synchronized void computerTurnTestMode() {
		new Thread(new Runnable() {
			public void run() {
				Thread.yield();
				//try { Thread.sleep(0); } 
				//catch (InterruptedException e) { e.printStackTrace(); }

				boolean draw = opponent.getAction(computerScore, userScore, turnTotal, redsDrawn, greensDrawn) == RedLightPlayer.DRAW;

				if (!draw && turnTotal == 0) {
					draw = true;
				}
				if (draw && computerScore + turnTotal == goalScore) {
					draw = false;
				}
				if (draw) {
					runOnUiThread(new Runnable() {public void run() {showDrawDialog();}});
				}
				else {
					runOnUiThread(new Runnable() {public void run() {showHoldDialog();}});
				}

				Thread.yield();
				try { Thread.sleep(COMPUTER_DELAY); } 
				catch (InterruptedException e) { e.printStackTrace(); }


			}
		}).start();
	}
	/*if(isTestMode) {
		runOnUiThread(new Runnable() {public void run() {textViewTestDirections.setText(R.string.computerTestDrawAndReport);}});
		runOnUiThread(new Runnable() {public void run() {textViewTestDirections.setShadowLayer(2, 0, 0, Color.BLACK);}});
		//runOnUiThread(new Runnable() {public void run() {showHoldDialog();}});
		//onPause();
	}
	if(isTestMode) {
							runOnUiThread(new Runnable() {public void run() {textViewTestDirections.setText(R.string.computerTestDrawAndReport);}});
							runOnUiThread(new Runnable() {public void run() {textViewTestDirections.setShadowLayer(2, 0, 0, Color.BLACK);}});
							//runOnUiThread(new Runnable() {public void run() {showDrawDialog();}});
							//onPause();
						}
	 */
	/*******************************************************************/
	/** Disable buttons when it is not the Users turn, and vice versa **/
	/*******************************************************************/
	private void setButtonsState() {
		if(isTestMode) {
			buttonHold.setEnabled(false);
		}
		else {
			buttonHold.setEnabled(isUserTurn);
			buttonRoll.setEnabled(isUserTurn);
		}
	}

	private void showDrawDialog() {
		/*
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Computer says: Draw")
		       .setCancelable(true)
		       .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   //runOnUiThread(new Runnable() {public void run() {roll();}});

		           }
		       });
		AlertDialog alert = builder.create();
		 */

		/*alert.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				onResume();
			}

		});
		 */
		//alert.show();

		Toast.makeText(this, "Computer says: Draw", Toast.LENGTH_SHORT).show();
	}

	private void showHoldDialog() {
		/*AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Computer says: Hold")
		       .setCancelable(true)
		       .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   runOnUiThread(new Runnable() {public void run() {hold();}});
		           }
		       });
		AlertDialog alert = builder.create();
		 */
		/*alert.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				onResume();
			}

		});
		 */
		//alert.show();
		//Toast.makeText(this, "Computer says: Hold", Toast.LENGTH_SHORT).show();
		runOnUiThread(new Runnable() {public void run() {hold();}});
	}

	/*************************************************************************************************/
	/** Figure out who won, display that, increment stats, and ask if the user wishes to play again **/
	/*************************************************************************************************/
	private void endGame() {
		String uName=userName.substring(0, userName.length()-2);
		String message = (!isUserTurn) 
				? String.format(Locale.US, "Computer won %d to %d.", computerScore, userScore)
						: String.format(uName + " won %d to %d.", userScore, computerScore);
				message += "  Would you like to play again?";

				if(!isTestMode) {
					if(userScore>=goalScore && compLevel=="easy") {
						userNumWinsEasy++;
					}
					else if(userScore>=goalScore && compLevel=="medium") {
						userNumWinsMedium++;
					}
					else if(userScore>=goalScore && compLevel=="hard") {
						userNumWinsHard++;
					}

					if(computerScore>=goalScore && compLevel=="easy") {
						userNumLossesEasy++;
					}
					else if(computerScore>=goalScore && compLevel=="medium") {
						userNumLossesMedium++;
					}
					else if(computerScore>=goalScore && compLevel=="hard") {
						userNumLossesHard++;
					}
				}

				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(message)       
				.setCancelable(false)
				.setPositiveButton("New Game", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						setUserScore(0);
						setComputerScore(0);
						setTurnTotal(0);
						mix();

						userStartGame = !userStartGame;
						if(userStartGame==false) {
							setUserScore(userScore+1);
						}
						else {
							setComputerScore(computerScore+1);
						}
						isUserTurn = userStartGame;
						setButtonsState();
						if (isUserTurn) {
							setImage("splash");
						}
						else {
							if(!isTestMode) {
								computerTurn();
							}
							else {
								computerTurnTestMode();
							}
						}
						setTextColors();
						dialog.cancel();             
					}
				})
				.setNegativeButton("Quit", new DialogInterface.OnClickListener() {           
					public void onClick(DialogInterface dialog, int id) {                
						MainActivity.this.finish();
					}       
				});


				AlertDialog alert = builder.create();
				alert.show();
	}

	/*****************************************************************************************************/
	/** Save instance variables for when the application is tilted to either landscape or portrait mode **/
	/*****************************************************************************************************/
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBooleanArray("bag", bag);
		outState.putInt("chipIndex", chipIndex);
		outState.putInt("redsDrawn", redsDrawn);
		outState.putInt("greensDrawn", greensDrawn);
		outState.putInt("userScore", userScore);
		outState.putInt("computerScore", computerScore);
		outState.putInt("turnTotal", turnTotal);
		outState.putInt("textViewNumReds", Integer.valueOf(textViewNumReds.getText().toString()));
		outState.putInt("textViewNumGreens", Integer.valueOf(textViewNumGreens.getText().toString()));
		outState.putBoolean("userStartGame", userStartGame);
		outState.putBoolean("isUserTurn", isUserTurn);
		outState.putString("imageName", imageName);
		outState.putString("userName", userName.trim());
		outState.putBoolean("hasChosenDifficulty", hasChosenDifficulty);
		outState.putString("editTextEnteredText", editTextEnteredText.getText().toString());
	}

	/********************************************************************************************************/
	/** Restore instance variables for when the application is tilted to either landscape or portrait mode **/
	/********************************************************************************************************/
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		hasChosenDifficulty=savedInstanceState.getBoolean("hasChosenDifficulty");

		if(mSplashDialog.isShowing() && hasChosenDifficulty==true) {
			mSplashDialog.dismiss(); 
		}

		setNumReds(savedInstanceState.getInt("textViewNumReds", 0));
		setNumGreens(savedInstanceState.getInt("textViewNumGreens", 0));
		setUserScore(savedInstanceState.getInt("userScore", 0));
		setComputerScore(savedInstanceState.getInt("computerScore", 0));
		setTurnTotal(savedInstanceState.getInt("turnTotal", 0));
		setImage(savedInstanceState.getString("imageName"));
		setUserName(savedInstanceState.getString("userName"));
		redsDrawn=savedInstanceState.getInt("redsDrawn");
		greensDrawn=savedInstanceState.getInt("greensDrawn");
		bag=savedInstanceState.getBooleanArray("bag");
		chipIndex=savedInstanceState.getInt("chipIndex");
		editTextEnteredText.setText(savedInstanceState.getString("editTextEnteredText"));
		userStartGame = savedInstanceState.getBoolean("userStartGame", true);
		isUserTurn = savedInstanceState.getBoolean("isUserTurn", true);
		setButtonsState();

		if (userScore >= goalScore || computerScore >= goalScore) {
			endGame();
		}
		else if (!isUserTurn) {
			computerTurn();
		}
	}

	/***************************************************************************/
	/** Inflates the menu; this adds items to the action bar if it is present **/
	/***************************************************************************/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.my_options_menu, menu);
		return true;
	}

	/************************************************************************/
	/** Open new dialog windows when either of the menu items get selected **/
	/************************************************************************/
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.about:
			showContinueDialogAbout();
			//showAboutScreen();
			//startActivity(new Intent(this, About.class));
			return true;
		case R.id.stats:
			showContinueDialogStats();
			//showStatsScreen();
			//startActivity(new Intent(this, Help.class));
			return true;
		case R.id.debug:
			showContinueDialogDebug();
			//startActivity(new Intent(this, Help.class));
			return true;
		case R.id.mute:
			showMuteDialog();
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	protected void showMuteDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Would you like to mute sound?")       
		.setCancelable(false)
		.setPositiveButton("Mute", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				mute();
			}
		})
		.setNegativeButton("Unmute", new DialogInterface.OnClickListener() {           
			public void onClick(DialogInterface dialog, int id) {                
				unmute();
			}       
		});
		AlertDialog alert = builder.create();
		alert.show();
	}


	/*******************************************************************************************************/
	/** Show an information dialog to tell the user what is about to happen when entering test/debug mode **/
	/*******************************************************************************************************/
	protected void showContinueDialogDebug() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Entering Test Mode.\nTo Quit, press the back button on your device.")       
		.setCancelable(false)
		.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				isTestMode=true;
				//numRedsBack=Integer.parseInt(textViewNumReds.getText().toString());
				//numGreensBack=Integer.parseInt(textViewNumGreens.getText().toString());
				bagBack=bag; 
				chipIndexBack=chipIndex; 
				redsDrawnBack=redsDrawn; 
				greensDrawnBack=greensDrawn; 
				userScoreBack=userScore; 
				computerScoreBack=computerScore; 
				turnTotalBack=turnTotal;

				showDebugScreen();
			}
		})
		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {           
			public void onClick(DialogInterface dialog, int id) {                

			}       
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	protected void showContinueDialogStats() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Entering Stats Screen.\nTo Quit, press the back button on your device.")       
		.setCancelable(false)
		.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				showStatsScreen();
			}
		})
		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {           
			public void onClick(DialogInterface dialog, int id) {                

			}       
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	protected void showContinueDialogAbout() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Entering About Screen.\nTo Quit, press the back button on your device.")       
		.setCancelable(false)
		.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				showAboutScreen();
			}
		})
		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {           
			public void onClick(DialogInterface dialog, int id) {                

			}       
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	/**************************************************************************************************************************/
	/** Shows a debug screen to allow the user to manually input moves to make sure that the computer player is not cheating **/
	/**************************************************************************************************************************/
	protected void showDebugScreen() {
		mStatsDialog = new Dialog(this, R.style.AppTheme);
		mStatsDialog.setContentView(R.layout.debug_main);
		mStatsDialog.setCancelable(true);

		textViewYourScore = (TextView) mStatsDialog.findViewById(R.id.textViewYourScore);
		textViewMyScore = (TextView) mStatsDialog.findViewById(R.id.textViewMyScore);
		textViewTurnTotal = (TextView) mStatsDialog.findViewById(R.id.textViewTurnTotal);
		textViewNumGreens = (TextView) mStatsDialog.findViewById(R.id.textViewNumGreens);
		textViewNumReds = (TextView) mStatsDialog.findViewById(R.id.textViewNumReds); 
		textViewUserName = (TextView) mStatsDialog.findViewById(R.id.textViewUserName);
		//textViewUScore = (TextView) mStatsDialog.findViewById(R.id.textView1);
		textViewCScore = (TextView) mStatsDialog.findViewById(R.id.textView3);
		textViewTestDirections = (TextView) mStatsDialog.findViewById(R.id.textViewTestDirections);
		imageView = (ImageView) mStatsDialog.findViewById(R.id.imageView);

		buttonDrawGreen = (Button) mStatsDialog.findViewById(R.id.buttonDrawGreen);
		buttonDrawRed = (Button) mStatsDialog.findViewById(R.id.buttonDrawRed);
		buttonHold = (Button) mStatsDialog.findViewById(R.id.buttonHold);

		buttonHold.setEnabled(false);

		numReds = 4;
		numGreens = 24;
		numChips = numReds + numGreens;
		bag = new boolean[numChips]; 
		chipIndex = 0; 
		redsDrawn = 0; 
		greensDrawn = 0; 
		setUserName(userName.trim());
		bag = new boolean[numChips];
		for (int i = 0; i < numReds; i++) {
			bag[i] = true;
		} 
		mix();

		textViewTestDirections.setText(R.string.userTestDrawAndReport);
		textViewTestDirections.setShadowLayer(2, 0, 0, Color.BLACK);


		this.setUserScore(0);
		this.setComputerScore(1);
		this.setNumReds(numReds);
		this.setNumGreens(numGreens);
		setTextColors();

		buttonDrawGreen.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {				
				playGo();
				greensDrawn++;
				if(greensDrawn==24) {
					//setNumGreens(numGreens+1);
					//setNumReds(numReds);
					//greensDrawn=0;
					buttonDrawGreen.setEnabled(false);
				}
				setNumGreens(Integer.parseInt(String.valueOf(textViewNumGreens.getText()))-1);
				setTurnTotal(turnTotal + 1);
				setImage("go");
				setSplash(300);

				if(turnTotal>0) {
					buttonHold.setEnabled(true);
				}
				else{
					buttonHold.setEnabled(false);
				}

				if(!isUserTurn){
					computerTurnTestMode();
				}
			}
		});
		buttonDrawRed.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				buttonHold.setEnabled(false);

				playRed();
				redsDrawn++;
				setNumReds(Integer.parseInt(String.valueOf(textViewNumReds.getText()))-1);
				setImage("stop");
				setSplash(700);
				setTurnTotal(0);


				if (redsDrawn == numReds) {
					mix();
					buttonDrawGreen.setEnabled(true);
					//runOnUiThread(new Runnable() {public void run() {setImage("splash");}});
					Toast.makeText(getApplicationContext(), "Please Reshuffle", Toast.LENGTH_SHORT).show();
				}
				changeTurn();
			}
		});
		buttonHold.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				hold();
			}
		});
		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		mStatsDialog.show();
		/*mStatsDialog.setOnShowListener(new OnShowListener() {
			@Override
			public void onShow(DialogInterface dialog) {


			}
		});
		 */

		mStatsDialog.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				System.out.println("Quitting, setting values back to what they were before...");

				//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
				isTestMode=false;
				//numReds=numRedsBack;
				//numGreens=numGreensBack;
				bag=bagBack; 
				chipIndex=chipIndexBack; 
				redsDrawn=redsDrawnBack; 
				greensDrawn=greensDrawnBack; 
				userScore=userScoreBack; 
				computerScore=computerScoreBack; 
				turnTotal=turnTotalBack;

				setContentView(R.layout.activity_main);	
				textViewYourScore = (TextView) findViewById(R.id.textViewYourScore);
				textViewMyScore = (TextView) findViewById(R.id.textViewMyScore);
				textViewTurnTotal = (TextView) findViewById(R.id.textViewTurnTotal);
				textViewNumGreens = (TextView) findViewById(R.id.textViewNumGreens);
				textViewNumReds = (TextView) findViewById(R.id.textViewNumReds); 
				textViewUserName = (TextView) findViewById(R.id.textViewUserName);
				//textViewUScore = (TextView) findViewById(R.id.textView1);
				textViewCScore = (TextView) findViewById(R.id.textView3);
				buttonRoll = (Button) findViewById(R.id.buttonRoll);
				buttonHold = (Button) findViewById(R.id.buttonHold);
				imageView = (ImageView) findViewById(R.id.imageView);

				setUserScore(userScore);
				setComputerScore(computerScore);
				setTurnTotal(turnTotal);
				setNumReds(numReds-redsDrawn);
				setNumGreens(numGreens-greensDrawn);
				setUserName(userName.trim());
				setTextColors();

				buttonRoll.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						roll();
					}
				});
				buttonHold.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						hold();
					}
				});
			}
		});
	}

	/****************************************************************************************************/
	/** Shows the about screen to allow the user to view info about the game, and the developers (us!) **/
	/****************************************************************************************************/
	protected void showAboutScreen() {
		mStatsDialog = new Dialog(this, R.style.AppTheme);
		mStatsDialog.setContentView(R.layout.about_main);
		mStatsDialog.setCancelable(true);
		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		mStatsDialog.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
			}
		});

		mStatsDialog.show();
	}

	/*********************************************************************************************************************************/
	/** Shows the stats screen to allow the user to view their wins, losses, and percent wins against the easy, medium, and hard AI **/
	/*********************************************************************************************************************************/
	protected void showStatsScreen() {
		mStatsDialog = new Dialog(this, R.style.AppTheme);
		mStatsDialog.setContentView(R.layout.stats_main);
		mStatsDialog.setCancelable(true);

		if(userNumWinsEasy>0) {
			double nWinseasy = ((double)userNumWinsEasy/(userNumWinsEasy+userNumLossesEasy));
			nWinseasy*=100;

			userPercEasy=roundTD(nWinseasy);
			//System.out.println("User percent easy is: "+ userPercEasy);
		}
		if(userNumWinsMedium>0) {
			double nWinsmedium = ((double)userNumWinsMedium/(userNumWinsMedium+userNumLossesMedium));
			nWinsmedium*=100;
			userPercMedium=roundTD(nWinsmedium);
			//System.out.println("User percent medium is: "+ userPercMedium);
		}
		if(userNumWinsHard>0) {
			double nWinshard = ((double)userNumWinsHard/(userNumWinsHard+userNumLossesHard));
			nWinshard*=100;
			userPercHard=roundTD(nWinshard);
			//System.out.println("User percent hard is: "+ userPercHard);
		}

		if(userNumLossesEasy==0) {
			userPercEasy=100;
		}
		if(userNumLossesMedium==0) {
			userPercMedium=100;
		}
		if(userNumLossesHard==0) {
			userPercHard=100;
		}

		if(userNumWinsEasy==0 && userNumLossesEasy==0) {
			userPercEasy=0;
		}
		if(userNumWinsMedium==0 && userNumLossesMedium==0) {
			userPercMedium=0;
		}
		if(userNumWinsHard==0 && userNumLossesHard==0) {
			userPercHard=0;
		}

		textViewUserNumWinsEasy = (TextView) mStatsDialog.findViewById(R.id.textViewUserNumWinsEasy);
		textViewUserNumWinsEasy.setText(String.valueOf(userNumWinsEasy));
		textViewUserNumLossesEasy = (TextView) mStatsDialog.findViewById(R.id.textViewUserNumLossesEasy);
		textViewUserNumLossesEasy.setText(String.valueOf(userNumLossesEasy));
		textViewUserPercEasy = (TextView) mStatsDialog.findViewById(R.id.textViewUserPercEasy);
		textViewUserPercEasy.setText(String.valueOf(userPercEasy) + "%");

		textViewUserNumWinsMedium = (TextView) mStatsDialog.findViewById(R.id.textViewUserNumWinsMedium);
		textViewUserNumWinsMedium.setText(String.valueOf(userNumWinsMedium));
		textViewUserNumLossesMedium = (TextView) mStatsDialog.findViewById(R.id.textViewUserNumLossesMedium);
		textViewUserNumLossesMedium.setText(String.valueOf(userNumLossesMedium));
		textViewUserPercMedium = (TextView) mStatsDialog.findViewById(R.id.textViewUserPercMedium);
		textViewUserPercMedium.setText(String.valueOf(userPercMedium) + "%");

		textViewUserNumWinsHard = (TextView) mStatsDialog.findViewById(R.id.textViewUserNumWinsHard);
		textViewUserNumWinsHard.setText(String.valueOf(userNumWinsHard));
		textViewUserNumLossesHard = (TextView) mStatsDialog.findViewById(R.id.textViewUserNumLossesHard);
		textViewUserNumLossesHard.setText(String.valueOf(userNumLossesHard));
		textViewUserPercHard = (TextView) mStatsDialog.findViewById(R.id.textViewUserPercHard);
		textViewUserPercHard.setText(String.valueOf(userPercHard) + "%");

		textViewUserNameStats1 = (TextView) mStatsDialog.findViewById(Integer.valueOf(R.id.textViewUserName1));
		textViewUserNameStats1.setText(String.valueOf(userName.trim()));
		textViewUserNameStats2 = (TextView) mStatsDialog.findViewById(Integer.valueOf(R.id.textViewUserName2));
		textViewUserNameStats2.setText(String.valueOf(userName.trim()));
		textViewUserNameStats3 = (TextView) mStatsDialog.findViewById(Integer.valueOf(R.id.textViewUserName3));
		textViewUserNameStats3.setText(String.valueOf(userName.trim()));
		textViewUserNameStats4 = (TextView) mStatsDialog.findViewById(Integer.valueOf(R.id.textViewUserName4));
		textViewUserNameStats4.setText(String.valueOf(userName.trim()));
		textViewUserNameStats5 = (TextView) mStatsDialog.findViewById(Integer.valueOf(R.id.textViewUserName5));
		textViewUserNameStats5.setText(String.valueOf(userName.trim()));
		textViewUserNameStats6 = (TextView) mStatsDialog.findViewById(Integer.valueOf(R.id.textViewUserName6));
		textViewUserNameStats6.setText(String.valueOf(userName.trim()));
		textViewUserNameStats7 = (TextView) mStatsDialog.findViewById(Integer.valueOf(R.id.textViewUserName7));
		textViewUserNameStats7.setText(String.valueOf(userName.trim()));
		textViewUserNameStats8 = (TextView) mStatsDialog.findViewById(Integer.valueOf(R.id.textViewUserName8));
		textViewUserNameStats8.setText(String.valueOf(userName.trim()));
		textViewUserNameStats9 = (TextView) mStatsDialog.findViewById(Integer.valueOf(R.id.textViewUserName9));
		textViewUserNameStats9.setText(String.valueOf(userName.trim()));

		mStatsDialog.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
			}
		});

		////setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		mStatsDialog.show();
	}

	/********************************************************************/
	/** Method used to easily round a double [d] to two decimal places **/
	/********************************************************************/
	public double roundTD(double d) {
		d = Math.round(d * 100);
		d = d/100;
		return d;
	}
}