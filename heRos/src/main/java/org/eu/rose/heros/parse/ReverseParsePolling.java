package org.eu.rose.heros.parse;

import java.util.HashMap;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;

import org.eu.rose.heros.connection.PollingThread;

/**
 * Send to the Parse server that the HeRos is controled by an other player and you can't take it
 * HeRos's Identity is provided with mac adress
 */
public class ReverseParsePolling extends PollingThread {
	
	private final String macAddress;

	public ReverseParsePolling(String macAddress){
		this.macAddress = macAddress;
	}

	public void run() {

		while (!end) {

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("macAddress", macAddress);

			ParseCloud.callFunctionInBackground("checkIn", params, new FunctionCallback<Object>() {
				public void done(Object nothing, ParseException e) {
					if (e == null) {

					}
				}
			});

		}

	}
}
