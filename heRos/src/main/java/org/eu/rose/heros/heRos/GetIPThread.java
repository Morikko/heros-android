package org.eu.rose.heros.heRos;

import org.eu.rose.heros.application.HeRosApplication;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Created by eric on 20/06/15.
 * Open an connection to receive HeRos Broadcast and find the ip address
 */
public class GetIPThread implements Runnable {
    private  byte[] receivedData = new byte[2048];
    private String HeRos_IP_ADDRESS_UDP = null;
    private final int WAIT_TIME = 2000;
    private String MAC_ADDRESS;


    public GetIPThread(String mac){
        this.MAC_ADDRESS = mac;
    }

    public void run() {
        try {

            DatagramSocket clientSocket = new DatagramSocket(55555);
            clientSocket.setReuseAddress(true);
            clientSocket.setSoTimeout(WAIT_TIME);
            DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);

            long startTime = System.currentTimeMillis();
            long currentTime = startTime;

            // Wait for getting the ip address of the HeRos
            while (HeRos_IP_ADDRESS_UDP == null && currentTime - startTime < WAIT_TIME) {
                currentTime = System.currentTimeMillis();
                try {
                    clientSocket.receive(receivedPacket);
                    String receivedMessage = new String(receivedPacket.getData());
                    final JSONObject json;
                    try {
                        json = new JSONObject(receivedMessage);
                        if (json.has("mac")){
                            if (json.getString("mac").equals(MAC_ADDRESS)) {
                                HeRos_IP_ADDRESS_UDP = receivedPacket.getAddress().getHostAddress();
                            }
                        }
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    // No packet received under WAIT_TIME
                }catch (SocketTimeoutException timeoutException){
                    System.out.println("No packet received under " + WAIT_TIME + " ms\n");
                    break;
                }
            }

            HeRosApplication.getHeRosConnection().setHeRosIP(HeRos_IP_ADDRESS_UDP);
            clientSocket.close();


        } catch (UnknownHostException e) {
//            activity.runOnUiThread(new Runnable() {
//                public void run() {
//                    Toast.makeText(activity, "Connection failed, please try again.", Toast.LENGTH_SHORT)
//                            .show();
//                }
//            });
        } catch (IOException e) {
//            activity.runOnUiThread(new Runnable() {
//                public void run() {
//                    Toast.makeText(activity, "Connection failed, please try again.", Toast.LENGTH_SHORT)
//                            .show();
//                }
//            });
        }
    }
}
