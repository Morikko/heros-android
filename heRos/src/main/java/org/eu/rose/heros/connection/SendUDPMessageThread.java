package org.eu.rose.heros.connection;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

/**
 * Created by eric on 21/06/15.
 */
public class SendUDPMessageThread extends Thread{
    private DatagramSocket socket;
    private DatagramPacket packet;
    private int numberTimes;
    private boolean stop;

    public SendUDPMessageThread(DatagramSocket socket, DatagramPacket packet, int numberTimes){
        this.socket = socket;
        this.packet = packet;
        this.numberTimes = numberTimes;
        stop = false;
    }

    public void run() {
        try {
            for (int i = 0; i < numberTimes && !stop; i++) {
                socket.send(packet);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public void stopSending(){
        stop = true;
    }
}
