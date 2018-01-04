package org.eu.rose.heros.connection;

import java.net.DatagramSocket;

/**
 * Created by eric on 21/06/15.
 * Thread that end working with the calling stopPolling function
 */
public abstract class PollingThread extends Thread {
    protected boolean end;

    public PollingThread(){
        end = false;
    }

    public void stopPolling(){
        end = true;
    }
}
