/*
    Intel License Agreement

For Open Source Computer Vision Library
Copyright (C) 2000, Intel Corporation, all rights reserved.
Third party copyrights are property of their respective owners.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:
	Redistribution's of source code must retain the above copyright notice,
this list of conditions and the following disclaimer.
	Redistribution's in binary form must reproduce the above copyright notice,
this list of conditions and the following disclaimer in the documentation
and/or other materials provided with the distribution.
	The name of Intel Corporation may not be used to endorse or promote products
derived from this software without specific prior written permission.

This software is provided by the copyright holders and contributors "as is" and
any express or implied warranties, including, but not limited to, the implied
warranties of merchantability and fitness for a particular purpose are disclaimed.
In no event shall the Intel Corporation or contributors be liable for any direct,
indirect, incidental, special, exemplary, or consequential damages
(including, but not limited to, procurement of substitute goods or services;
loss of use, data, or profits; or business interruption) however caused
and on any theory of liability, whether in contract, strict liability,
or tort (including negligence or otherwise) arising in any way out of
the use of this software, even if advised of the possibility of such damage.
*/

// Edited by Florent Remis for HeRos project


package org.eu.rose.heros.stream;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.eu.rose.heros.heRos.ReceptionPacketThread;

public class JpegViewStream extends SurfaceView implements SurfaceHolder.Callback {
    public final static int POSITION_UPPER_LEFT  = 9;
    public final static int POSITION_UPPER_RIGHT = 3;
    public final static int POSITION_LOWER_LEFT  = 12;
    public final static int POSITION_LOWER_RIGHT = 6;

    public final static int SIZE_STANDARD   = 1; 
    public final static int SIZE_BEST_FIT   = 4;
    public final static int SIZE_FULLSCREEN = 8;

    private MjpegViewThread videoThread = null;
    private boolean showFps = false;
    private boolean mRun = false;
    private boolean surfaceDone = false;    
    private Paint overlayPaint;
    private int overlayTextColor;
    private int overlayBackgroundColor;
    private int ovlPos;
    private int dispWidth;
    private int dispHeight;
    private int displayMode;

    protected ReceptionPacketThread receptionPacketThread;

	private AtomicBoolean stopVideoThread = new AtomicBoolean(false);

    public class MjpegViewThread extends Thread {
        private SurfaceHolder mSurfaceHolder;
        private int frameCounter = 0;
        private long start;
        private Bitmap ovl;

        public MjpegViewThread(SurfaceHolder surfaceHolder, Context context) { mSurfaceHolder = surfaceHolder; }

        private Rect destRect(int bmw, int bmh) {
            int tempx;
            int tempy;
            if (displayMode == JpegViewStream.SIZE_STANDARD) {
                tempx = (dispWidth / 2) - (bmw / 2);
                tempy = (dispHeight / 2) - (bmh / 2);
                return new Rect(tempx, tempy, bmw + tempx, bmh + tempy);
            }
            if (displayMode == JpegViewStream.SIZE_BEST_FIT) {
                float bmasp = (float) bmw / (float) bmh;
                bmw = dispWidth;
                bmh = (int) (dispWidth / bmasp);
                if (bmh > dispHeight) {
                    bmh = dispHeight;
                    bmw = (int) (dispHeight * bmasp);
                }
                tempx = (dispWidth / 2) - (bmw / 2);
                tempy = (dispHeight / 2) - (bmh / 2);
                return new Rect(tempx, tempy, bmw + tempx, bmh + tempy);
            }
            if (displayMode == JpegViewStream.SIZE_FULLSCREEN) return new Rect(0, 0, dispWidth, dispHeight);
            return null;
        }

        public void setSurfaceSize(int width, int height) {
            synchronized(mSurfaceHolder) {
                dispWidth = width;
                dispHeight = height;
            }
        }

        private Bitmap makeFpsOverlay(Paint p, String text) {
            Rect b = new Rect();
            p.getTextBounds(text, 0, text.length(), b);
            int bwidth  = b.width()+2;
            int bheight = b.height()+2;
            Bitmap bm = Bitmap.createBitmap(bwidth, bheight, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(bm);
            p.setColor(overlayBackgroundColor);
            c.drawRect(0, 0, bwidth, bheight, p);
            p.setColor(overlayTextColor);
            c.drawText(text, -b.left+1, (bheight/2)-((p.ascent()+p.descent())/2)+1, p);
            return bm;           
        }

        public void run() {
            start = System.currentTimeMillis();
            PorterDuffXfermode mode = new PorterDuffXfermode(PorterDuff.Mode.DST_OVER);
            Bitmap bm;
            int width;
            int height;
            Rect destRect;
            Canvas c = null;
            Paint p = new Paint();
            String fps = "";
            
            while (mRun) {

                int frameToShow = receptionPacketThread.getNextFrame();

                if (!stopVideoThread.get()) {

                    if(surfaceDone) {
                        try {
                            c = mSurfaceHolder.lockCanvas();
                            synchronized (mSurfaceHolder) {
                                try {
                                    bm = readMjpegFrame(frameToShow);
                                    receptionPacketThread.setFrameLength(frameToShow, 0);
                                    destRect = destRect(bm.getWidth(),bm.getHeight());
                                    c.drawColor(Color.BLACK);
                                    c.drawBitmap(bm, null, destRect, p);
                                    if(showFps) {
                                        p.setXfermode(mode);
                                        if(ovl != null) {
                                            height = ((ovlPos & 1) == 1) ? destRect.top : destRect.bottom-ovl.getHeight();
                                            width  = ((ovlPos & 8) == 8) ? destRect.left : destRect.right -ovl.getWidth();
                                            c.drawBitmap(ovl, width, height, null);
                                        }
                                        p.setXfermode(null);
                                        frameCounter++;
                                        if((System.currentTimeMillis() - start) >= 1000) {
                                            fps = String.valueOf(frameCounter)+"fps";
                                            frameCounter = 0;
                                            start = System.currentTimeMillis();
                                            ovl = makeFpsOverlay(overlayPaint, fps);
                                        }
                                    }
                                } catch (IOException e) {}
                                catch(NullPointerException e)
                                {
                                    Log.d("nullPointer", "exception");
                                }
                            }
                        } finally { if (c != null) mSurfaceHolder.unlockCanvasAndPost(c); }
                    }

                } else {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void init(Context context) {
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        videoThread = new MjpegViewThread(holder, context);
        setFocusable(true);
        overlayPaint = new Paint();
        overlayPaint.setTextAlign(Paint.Align.LEFT);
        overlayPaint.setTextSize(12);
        overlayPaint.setTypeface(Typeface.DEFAULT);
        overlayTextColor = Color.WHITE;
        overlayBackgroundColor = Color.BLACK;
        ovlPos = JpegViewStream.POSITION_LOWER_RIGHT;
        displayMode = JpegViewStream.SIZE_STANDARD;
        dispWidth = getWidth();
        dispHeight = getHeight();
    }

    public Bitmap readMjpegFrame(int frameID) throws IOException {
        return BitmapFactory.decodeByteArray (receptionPacketThread.getFrame(frameID), 0, receptionPacketThread.getFrameLength(frameID)+1);
    }

    public void startPlayback() {
    	stopVideoThread.set(false);
        mRun = true;
        if (videoThread.getState() == Thread.State.NEW){
        	videoThread.start();
        }
    }

    public void stopPlayback() { 
        stopVideoThread.set(true);
        mRun = false;
    }

    public JpegViewStream(Context context, AttributeSet attrs) { super(context, attrs); init(context); }
    public void surfaceChanged(SurfaceHolder holder, int f, int w, int h) { videoThread.setSurfaceSize(w, h); }

    public void surfaceDestroyed(SurfaceHolder holder) { 
        surfaceDone = false; 
        stopPlayback(); 
    }

    public JpegViewStream(Context context, ReceptionPacketThread receptionPacketThread) {
        super(context);
        this.receptionPacketThread = receptionPacketThread;
        init(context);
    }
    public void surfaceCreated(SurfaceHolder holder) { 
        surfaceDone = true; 
        }
    public void showFps(boolean b) { 
        showFps = b; 
        }
    public void start() {
        startPlayback();
        }
    public void setOverlayPaint(Paint p) { 
        overlayPaint = p; 
        }
    public void setOverlayTextColor(int c) { 
        overlayTextColor = c; 
        }
    public void setOverlayBackgroundColor(int c) { 
        overlayBackgroundColor = c; 
        }
    public void setOverlayPosition(int p) { 
        ovlPos = p; 
        }
    public void setDisplayMode(int s) { 
        displayMode = s; 
        }
}