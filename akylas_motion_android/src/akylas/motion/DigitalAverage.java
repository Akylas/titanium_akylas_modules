package akylas.motion;

public class DigitalAverage
{

    final int history_len = 4;
    double[] mLocHistory = new double[history_len];
    int mLocPos = 0;

    int average(double d) {
        float avg = 0;

        mLocHistory[mLocPos] = d;

        mLocPos++;
        if (mLocPos > mLocHistory.length - 1) {
            mLocPos = 0;
        }
        for (double h : mLocHistory) {
            avg += h;
        }
        avg /= mLocHistory.length;

        return (int) avg;
    }
}