package org.eu.rose.heros.heRos;

import org.eu.rose.heros.application.HeRosApplication;
import org.eu.rose.heros.connection.HeRosConnection;
import org.eu.rose.heros.connection.PollingThread;

import java.io.IOException;
import java.net.*;

/**
 * Tell the robot (which transfers to the shell), I'm still connected !
 */
public class ReverseHeRosPolling extends PollingThread {
	
	private DatagramSocket socket;
	private byte[] sendingBuffer = new byte[2];
	
	public ReverseHeRosPolling(DatagramSocket s){
		this.socket = s;
	}
	
	public void run() {
		
		sendingBuffer[0] = '$';
		sendingBuffer[1] = 'p';
		DatagramPacket sendingPacket = new DatagramPacket(sendingBuffer, 2, HeRosApplication.getHeRosConnection().getHeRosIP(), HeRosConnection.HEROS_UDP_PORT);
		
		while(!end){
			try {
				socket.send(sendingPacket);
				Thread.sleep(900);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}