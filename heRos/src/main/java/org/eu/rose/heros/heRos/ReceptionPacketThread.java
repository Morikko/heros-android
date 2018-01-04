package org.eu.rose.heros.heRos;

import org.eu.rose.heros.Control;
import org.eu.rose.heros.connection.PollingThread;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/*
* Received packet from HeRos and interpreted them
* Send to message queue : the information about fight
* Send to MjpegView the frame to display
 */
public class ReceptionPacketThread extends PollingThread {
	private final int PACKET_SIZE = 1392;
	private final int DATA_SIZE = 1350;
	private final int FRAME_SIZE = DATA_SIZE - 8;
	private final int MAX_FRAME_SIZE = 8000;
	
	private DatagramSocket socket;
	
	private int lastMessageID;
	private byte[][] frames;
    private int[] frameLength;
    private BlockingQueue<Integer> frameQueue;

    private BlockingQueue<Byte> messageQueue;

	
	public ReceptionPacketThread(DatagramSocket s, BlockingQueue<Byte> messageQueue){
		this.socket = s;
        lastMessageID = -1;
        frames = new byte[24][MAX_FRAME_SIZE+4];
        frameLength = new int[24];
        frameQueue = new ArrayBlockingQueue<Integer>(24);

        this.messageQueue = messageQueue;
	}
	
	public void run() {
		
		for (int i = 0; i < 24; i++) {
			frames[i][MAX_FRAME_SIZE] = 0; // This byte indicates the number of packets received for this frame.
			frames[i][MAX_FRAME_SIZE+1] = 0; // This byte is at 0 is the frame is incomplete and 1 if it is complete.
			frames[i][MAX_FRAME_SIZE+2] = 0; // This byte indicates if this frame has already been shown.
			frameLength[i] = 0; // This int indicates the length of the image data, counting Jpeg header
		}
		
		while(!end) {
            try{
				byte[] packetBuffer = new byte[PACKET_SIZE];
				byte[] dataBuffer = new byte[DATA_SIZE];
				DatagramPacket currentPacket = new DatagramPacket(packetBuffer, PACKET_SIZE);
				socket.receive(currentPacket);

				dataBuffer = currentPacket.getData();

				int frameID = dataBuffer[0];
				int marker = dataBuffer[7];

				if (frameID > 0 && frameID < 24 && marker == 0) {

//						if (!Control.cameraStarted.get() && !Control.stopReceptionThread.get()){
//							Control.cameraStarted.set(true);
//						}

					int frameOffset = dataBuffer[1];
					int nbrOfPackets = dataBuffer[2];
					byte lastMessage = dataBuffer[3];
					int messageID = dataBuffer[4];
					int imageLength1 = dataBuffer[5] & 0xFF;
					int imageLength2 = dataBuffer[6] & 0xFF;

					int imageLength = imageLength1 + 256*imageLength2;

					if (messageID != lastMessageID) {
						messageQueue.offer(lastMessage); // A new message was received and needs to be interpreted
						lastMessageID = messageID;
					}

					for (int i = 0; i < FRAME_SIZE; i++) {
						frames[frameID][i+frameOffset*FRAME_SIZE] = dataBuffer[i+8];
					}

					frames[frameID][MAX_FRAME_SIZE]++; // Incrementing the number of packets received for this frame
					frames[frameID][MAX_FRAME_SIZE+2] = 0; // The image isn't shown yet
						
						/*int previousFrame = frameID == 0 ? 23:frameID - 1;
	                	frames[previousFrame][MAX_FRAME_SIZE] = 0;*/

					if (frames[frameID][MAX_FRAME_SIZE] == nbrOfPackets) {
						frames[frameID][MAX_FRAME_SIZE + 1] = 1; // If all packets have been received, the frame is complete
						frameLength[frameID] = imageLength;
						frames[frameID][MAX_FRAME_SIZE] = 0;
						frameQueue.offer(frameID);
						for (int i = 0; i < 24; i++) {
							if (i!=frameID && i!= (frameID+1)%24) {
								frames[i][MAX_FRAME_SIZE] = 0;
							}
						}
					} else {
						frames[frameID][MAX_FRAME_SIZE + 1] = 0; // Image not complete
					}

				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

    public Integer getNextFrame(){
        try {
            return frameQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void setFrameLength(int index, int value){
        frameLength[index] = value;
    }

    public int getFrameLength(int index){
        return frameLength[index];
    }

    public byte[] getFrame(int index){
        return frames[index];
    }

}