package org.eu.rose.heros.parse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.BlockingQueue;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Vibrator;
import android.widget.Toast;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;

import org.eu.rose.heros.Control;
import org.eu.rose.heros.R;
import org.eu.rose.heros.activity.GameActivity;
import org.eu.rose.heros.application.HeRosApplication;
import org.eu.rose.heros.connection.PollingThread;

/**
 * Send the evolution of the party to the server
 */
public class SendingGameInfoThread extends PollingThread {
	
	private final GameActivity gameActivity;
	private final String currentGameName;
	private final String macAddress;
	private int directShotCounter;

    private BlockingQueue<Byte> messageQueue;


    private MediaPlayer why;
    private MediaPlayer shock;
    private MediaPlayer hey;
    private MediaPlayer sad;
    private MediaPlayer shuttingDown;
    private MediaPlayer goodnight;
    private MediaPlayer scream;
    private MediaPlayer laugh;

    public SendingGameInfoThread(GameActivity gameActivity, String currentGameName, String macAddress, BlockingQueue<Byte> messageQueue){
        this.gameActivity = gameActivity;
        this.currentGameName = currentGameName;
        this.macAddress = macAddress;
        this.messageQueue = messageQueue;

        why = MediaPlayer.create(gameActivity, R.raw.why);
        hey = MediaPlayer.create(gameActivity, R.raw.hey);
        shock = MediaPlayer.create(gameActivity, R.raw.r2d2_shocked);
        sad = MediaPlayer.create(gameActivity, R.raw.r2d2_sad);
        scream = MediaPlayer.create(gameActivity, R.raw.r2d2_scream);
        shuttingDown = MediaPlayer.create(gameActivity, R.raw.shutting_down);
        goodnight = MediaPlayer.create(gameActivity, R.raw.goodnight);
        laugh = MediaPlayer.create(gameActivity, R.raw.r2d2_laughing);
    }
	
		public void run() {
			
			final Vibrator v = (Vibrator) gameActivity.getSystemService(Context.VIBRATOR_SERVICE);
			
			directShotCounter = 0;
			long lastGlobalTime = 0;
			long currentGlobalTime = 0;
			
			while(!end){
				try {
					
					boolean toSend = true;
					
					final byte message = (byte) messageQueue.take();
					final byte address = (byte) ((byte) message >> 3);
					int t = -1;
					if (getBit(message, 1)) {
						if (getBit(message,0)) {
							t = 3;
						}
					} else {
						if (getBit(message,0)) {
							t = 1;
						} else {
							t = 0;
						}
					}
					
					final int type = t;
					
					if (type == 3) {
						if (directShotCounter < 5) {
							directShotCounter++;
							toSend = false;
						} else {
							directShotCounter = 0;
						}
					} else {
						currentGlobalTime = System.currentTimeMillis();
						if (currentGlobalTime - lastGlobalTime <= 200) {
							toSend = false;
						} else {
							lastGlobalTime = System.currentTimeMillis();
						}
					}
					
					if (address == 0){
						toSend = false;
					}
					
					long currentTime = System.currentTimeMillis();

					if (toSend && (currentTime - HeRosApplication.gameManager.getGameStartTime() > 3000)) {

						HashMap<String, Object> params = new HashMap<String, Object>();
						params.put("macAddress", macAddress);
						params.put("address", address);
						params.put("shotType", type);
						params.put("gameName", currentGameName);

						ParseCloud.callFunctionInBackground("shotReceived", params, new FunctionCallback<ArrayList<String>>() {
							public void done(ArrayList<String> result, ParseException e) {
								if (e == null) {

									final String shooter = result.get(0);
									final String hp = result.get(1);
									final String change = result.get(2);

									if (change.equals("y")) {
										switch(type) {
											case 0:
												gameActivity.runOnUiThread(new Runnable() {
													public void run() {
														laugh.start();
														final Toast toast = Toast.makeText(gameActivity, "Healed by " + shooter, Toast.LENGTH_SHORT);
														toast.show();
														Handler handler = new Handler();
														handler.postDelayed(new Runnable() {
															@Override
															public void run() {
																toast.cancel();
															}
														}, 1000);
														v.vibrate(500);
													}
												});
												break;
											case 1:
												gameActivity.runOnUiThread(new Runnable() {
													public void run() {
														Random r = new Random();
														int i = r.nextInt(2);
														if (i == 0) {
															shock.start();
														} else {
															why.start();
														}
														final Toast toast =Toast.makeText(gameActivity, "Badly injured by " + shooter, Toast.LENGTH_SHORT);
														toast.show();
														Handler handler = new Handler();
														handler.postDelayed(new Runnable() {
															@Override
															public void run() {
																toast.cancel();
															}
														}, 1000);
														v.vibrate(500);
													}
												});
												break;
											case 3:
												gameActivity.runOnUiThread(new Runnable() {
													public void run() {
														Random r = new Random();
														int i = r.nextInt(2);
														if (i == 0) {
															sad.start();
														} else {
															hey.start();
														}
														final Toast toast = Toast.makeText(gameActivity, "Shot by " + shooter, Toast.LENGTH_SHORT);
														toast.show();
														Handler handler = new Handler();
														handler.postDelayed(new Runnable() {
															@Override
															public void run() {
																toast.cancel();
															}
														}, 1000);
														v.vibrate(100);
													}
												});
												break;
										}
										gameActivity.updateHp(hp);
										if (Integer.parseInt(hp) <= 0) {
											Random r = new Random();
											final int i = r.nextInt(3);
											gameActivity.runOnUiThread(new Runnable() {
												public void run() {
													switch (i) {
														case 0:
															scream.start();
															break;
														case 1:
															goodnight.start();
															break;
														case 2:
															shuttingDown.start();
															break;
													}
													Toast.makeText(gameActivity, "Wasted!", Toast.LENGTH_SHORT)
															.show();
													//gameActivity.
												}
											});
										}
									} else {
										gameActivity.runOnUiThread(new Runnable() {
											public void run() {
												final Toast toast = Toast.makeText(gameActivity, "Shot/healed by wrong team!", Toast.LENGTH_SHORT);
												toast.show();
												Handler handler = new Handler();
												handler.postDelayed(new Runnable() {
													@Override
													public void run() {
														toast.cancel();
													}
												}, 1000);
											}
										});
									}

								}

							}

						});
					}

				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

	}
	
	public boolean getBit(byte message, int position) {
		return ((message >> position) & 1) == 1;
	}

}
