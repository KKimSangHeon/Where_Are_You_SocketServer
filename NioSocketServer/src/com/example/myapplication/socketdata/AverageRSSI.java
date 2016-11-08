package com.example.myapplication.socketdata;

import java.io.Serializable;

/*
*  Created by KSH on 2016-03-19.
*  비콘 ?��?��?��?�� ?��균치�? 구하�? ?��?�� ?��?��?��
*  getBeaconAverage ?��?��?���? ?��?�� ?��?��?��
*/
public class AverageRSSI implements Serializable{
    public int minor;
    public int count;
    public double sumOfRSSI;
    public double[] arrRSSI = new double[50];//분산?�� 구하�? ?��?�� �? rssi 값을 배열?�� ???��
    public double sumOfAccuracy;


    public AverageRSSI() {
    }

    public AverageRSSI(int minor, int RSSI, double Accuracy) {
        this.minor = minor;
        sumOfRSSI = RSSI;
        sumOfAccuracy = Accuracy;
        count = 1;
        arrRSSI[count - 1] = RSSI;
    }

    public void setMinorAndRSSI(int minor, double RSSI) {
        this.minor = minor;
        sumOfRSSI = RSSI;
        count = 1;

    }


    public void increase(double RSSI)   //RSSI�? ?��?��?�� 경우
    {
        count++;
        sumOfRSSI += RSSI;
    }

    public void increase(double RSSI, double accuracy)   //RSSI Accuracy ?��?�� ?��?��?�� 경우
    {
        arrRSSI[count] = RSSI;
        count++;
        sumOfAccuracy += accuracy;
        sumOfRSSI += RSSI;
    }

    public double getAverageRSSI() {
        return sumOfRSSI / count;
    }

    public double getAverageAccuracy() {
        return sumOfAccuracy / count;
    }

    public double getVar() {
        double var = 0;
        double temp = 0;

        for (int i = 0; i < count; i++) {
            temp += (arrRSSI[i] - getAverageRSSI()) * (arrRSSI[i] - getAverageRSSI());
        }
        var = temp / count;

        return var;
    }
}


