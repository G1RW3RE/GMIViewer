package com.gzy.gmi.app.GMIViewer;

public class CTWindow {

    private final int MIN_WINDOW_SIZE = 10;

    /** bottom of window */
    private int winLow;

    /** ceiling of window */
    private int winHigh;

    /** window position, = (winLow + winHigh) / 2 */
    private int winMid;

    /** window width, = (winHigh - winLow) */
    private int winSize;

    /** lowest value of CT */
    public static final int LOWEST_CT_VALUE = -1024;
    /** highest value of CT */
    public static final int HIGHEST_CT_VALUE = 3096;
    /** max value diff of CT */
    public static final int MAX_WINDOW_SIZE = HIGHEST_CT_VALUE - LOWEST_CT_VALUE;

    public CTWindow(int winLow, int winHigh) {
        assert winHigh > winLow + MIN_WINDOW_SIZE;
        this.winLow = winLow;
        this.winHigh = winHigh;
        this.winSize = winHigh - winLow;
        this.winMid = (winHigh + winLow) / 2;
    }

    public int getWinLow() {
        return winLow;
    }

    public int getWinHigh() {
        return winHigh;
    }

    public int getWinMid() {
        return winMid;
    }

    public int getWinSize() {
        return winSize;
    }

    public void setWinLow(int winLow) {
        if(this.winLow + MIN_WINDOW_SIZE > this.winHigh) {
            if(this.winLow + MIN_WINDOW_SIZE > HIGHEST_CT_VALUE) {
                this.winLow = HIGHEST_CT_VALUE - MIN_WINDOW_SIZE;
                this.winHigh = HIGHEST_CT_VALUE;
            } else {
                this.winLow = winLow;
                this.winHigh = this.winLow + MIN_WINDOW_SIZE;
            }
        } else if(winLow < LOWEST_CT_VALUE) {
            setWinLow(LOWEST_CT_VALUE);
        } else {
            this.winLow = winLow;
        }
        winMid = (winHigh + this.winLow) / 2;
        winSize = winHigh - this.winLow;
    }

    public void setWinHigh(int winHigh) {
        if(this.winHigh - MIN_WINDOW_SIZE < this.winLow) {
            if(this.winHigh - MIN_WINDOW_SIZE < LOWEST_CT_VALUE) {
                this.winHigh = LOWEST_CT_VALUE + MIN_WINDOW_SIZE;
                this.winLow = LOWEST_CT_VALUE;
            } else {
                this.winHigh = winHigh;
                this.winLow = this.winHigh - MIN_WINDOW_SIZE;
            }
        } else if(winHigh > HIGHEST_CT_VALUE) {
            setWinHigh(HIGHEST_CT_VALUE);
        } else {
            this.winHigh = winHigh;
        }
        winMid = (winHigh + this.winLow) / 2;
        winSize = winHigh - this.winLow;
    }

    public void setWinPosition(int winPosition) {
        if (winPosition + winSize / 2 > HIGHEST_CT_VALUE) {
            setWinLow(HIGHEST_CT_VALUE - winSize);
            setWinHigh(HIGHEST_CT_VALUE);
        } else if(winPosition - winSize / 2 < LOWEST_CT_VALUE) {
            setWinHigh(LOWEST_CT_VALUE + winSize);
            setWinLow(LOWEST_CT_VALUE);
        } else {
            setWinHigh(winPosition + winSize / 2);
            setWinLow(winPosition - winSize / 2);
        }
    }

    public void setWinSize(int winSize) {
        if (this.winMid + winSize / 2 > HIGHEST_CT_VALUE) {
            setWinLow(HIGHEST_CT_VALUE - winSize);
            setWinHigh(HIGHEST_CT_VALUE);
        } else if(this.winMid - winSize / 2 < LOWEST_CT_VALUE) {
            setWinHigh(LOWEST_CT_VALUE + winSize);
            setWinLow(LOWEST_CT_VALUE);
        } else {
            setWinHigh(this.winMid + winSize / 2);
            setWinLow(this.winMid - winSize / 2);
        }
    }

    @Override
    public String toString() {
        return "" + winLow + "~" + winHigh + " : " + winMid + " : " + winSize;
    }
}
