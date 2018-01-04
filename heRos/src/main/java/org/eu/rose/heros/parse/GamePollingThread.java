package org.eu.rose.heros.parse;

import java.util.ArrayList;
import java.util.HashMap;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;

import org.eu.rose.heros.manager.GameManager;
import org.eu.rose.heros.connection.PollingThread;

/**
 * Ask parse server to get the information about the party
 */
public class GamePollingThread extends PollingThread {
	private GameManager gameManager;
	private final String gameName;


	public GamePollingThread(GameManager gameManager, String gameName){
        this.gameManager = gameManager;
		this.gameName = gameName;
	}

	public void run() {

		while (!end) {

			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("gameName", gameName);

			ParseCloud.callFunctionInBackground("getInfo", params, new FunctionCallback<ArrayList<Object>>() {
				public void done(ArrayList<Object> info, ParseException e) {
					if (e == null) {

                        // Get player information
						final ArrayList<Integer> hp = (ArrayList<Integer>) info.get(0);
						final ArrayList<String> winners = (ArrayList<String>) info.get(1);
						final ArrayList<String> herosNames = (ArrayList<String>) info.get(2);
						final ArrayList<Integer> teams = (ArrayList<Integer>) info.get(3);

                        // Update information
                        gameManager.updateGameState(hp, winners, herosNames, teams);

                    }
                }
            });

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}


        }

    }
}
