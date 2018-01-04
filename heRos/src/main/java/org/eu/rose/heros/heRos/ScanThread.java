package org.eu.rose.heros.heRos;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketOptions;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eu.rose.heros.R;
import org.eu.rose.heros.activity.ScanActivity;
import org.eu.rose.heros.application.HeRosApplication;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseConfig;
import com.parse.ParseException;

/**
 * Thread that watch over the network to find all the HeRos connected
 * Duration 5s
 */
public class ScanThread extends Thread{
	
	private byte[] receivedData = new byte[2048];

	private final int SCAN_TIME = 5000; // 5s

	private final ScanActivity activity;
	private List<String> listOfPossibleAddresses;
	private List<String> listOfScannedAddresses;

	public ScanThread(ScanActivity activity, ParseConfig config) {
		this.activity = activity;
		// Get HeRos mac
		listOfPossibleAddresses = config.getList("macAddresses");
		listOfScannedAddresses = new ArrayList<String>();
	}

	// Look over the network to find the HeRos mac address
	public void scan(){
		DatagramSocket scanSocket = null;
		try {
			scanSocket = new DatagramSocket(55555);
			scanSocket.setReuseAddress(true);
            scanSocket.setSoTimeout(SCAN_TIME);

			DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);

			long startTime = System.currentTimeMillis();
			long currentTime = startTime;

			while (currentTime - startTime < SCAN_TIME) {
                try {
                    scanSocket.receive(receivedPacket);
                    String receivedMessage = new String(receivedPacket.getData());
                    final JSONObject json;
                    try {
                        json = new JSONObject(receivedMessage);
                        // Add all the mac adress of HeRos found
                        if (json.has("mac")) {
                            if (listOfPossibleAddresses.contains(json.getString("mac")) && !listOfScannedAddresses.contains(json.getString("mac"))) {
                                listOfScannedAddresses.add(json.getString("mac"));
                            }
                        }
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    currentTime = System.currentTimeMillis();
                // No packet received under SCAN_TIME
                }catch (SocketTimeoutException timeoutException){
                    System.out.println("No packet received under " + SCAN_TIME + " ms\n");
                    break;
                }
			}

			scanSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
			activity.runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(activity, "Error in network connection", Toast.LENGTH_SHORT)
							.show();
				}
			});
		}
	}

	@Override
	public void run() {

		scan();

		if (listOfScannedAddresses.isEmpty()) {
			activity.runOnUiThread(new Runnable() {
                public void run() {
                    activity.connectUnavailable();
                    Toast.makeText(activity, "No HeRos detected on your network", Toast.LENGTH_SHORT)
                            .show();
					activity.stopStandingState();
				}
			});
		} else {

			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("macAddresses", listOfScannedAddresses);

			ParseCloud.callFunctionInBackground("getHerosNames", params, new FunctionCallback<ArrayList<HashMap<String, Object>>>() {
				public void done(ArrayList<HashMap<String, Object>> object, ParseException e) {
					if (e == null) {
						HeRosApplication.getHeRosConnection().setHeRosInfos(object);
						List<String> listHeros = new ArrayList<String>();
						for (int i = 0; i< object.size(); i++) {
							listHeros.add(object.get(i).get("name").toString());
						}
						final ArrayAdapter<String> adapterHeros = new ArrayAdapter<String>(activity,
								R.layout.spinner_item, listHeros);
						adapterHeros.setDropDownViewResource(R.layout.spinner_item);
						activity.connectAvailable(adapterHeros, listOfScannedAddresses.size());
						activity.stopStandingState();
					} else {
						activity.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(activity, "Unable to get info on HeRos", Toast.LENGTH_SHORT)
                                        .show();
                            }
                        });
						activity.stopStandingState();
					}
				}
			});
		}
	}
};