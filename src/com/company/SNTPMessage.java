package com.company;

public class SNTPMessage {
    private byte leapIndicator = 0;
    private byte versionNumber = 4;
    private byte mode = 0;

    private short stratum = 0;
    private short pollInterval = 0;
    private byte precision = 0;

    //rootDelay 32-bit signed fixed-point number
    //fraction point between bits 15 and 16
    //
    // 0000 0000 0000 0000 . 0000 0000 0000 0000
    //
    private double rootDelay = 0;
    private double rootDispersion = 0;

    //Reference identifier
    //32bit sträng, 4 ascii-tecken
    //80,  80, 83,   0,
    //P    P    S
    private byte[] referenceIdentifier = {0, 0, 0, 0};

    private double referenceTimestamp = 0;
    private double originateTimestamp = 0;
    private double receiveTimestamp = 0;
    private double transmitTimestamp = 0;


    public SNTPMessage(byte[] buf) {
        byte b = buf[0];
        // b = 36
        //  0 1  2 3 4 5 6 7
        // |LI |  VN  | Mode |
        //  0 0 1 0 0  1 0 0
        //   0    4      4
        //
        leapIndicator = (byte)((b>>6) & 0x3);
        // 00100100
        // >> shiftar alla bits 6 steg till höger
        // 0001 0010
        // 0000 1001
        // 0000 0100
        // 0000 0010
        // 0000 0001
        // 0000 0000 Resultatet av b>>6
        // 0x3? -> 0000 0011
        // 36 decimalt -> 24 hex -> 0010 0100
        //
        versionNumber = (byte)((b>>3) & 0x7);
        // shifta 3 steg till höger
        // 0010 0100 -> 0000 0100
        // & 0x7 ?
        // 0000 0100 & 0000 0111
        //
        // 0000 0100
        // 0000 0111
        // 0000 0100

        mode = (byte)(b & 0x7);
        // 0010 0100
        // 0000 0111
        // 0000 0100 -> 4

        stratum = unsignedByteToShort(buf[1]);
        pollInterval = unsignedByteToShort(buf[2]);
        precision = buf[3];

        //Vi får datan för root delay som 4 bytes d.v.s. 32 bits i en följd
        // 1000 0100 0110 0010 | 0110 0100 1000 1001
        //     33890
        // buf[4] = 132
        // buf[5] = 98
        //
        rootDelay = (buf[4] * 256.0)
                + unsignedByteToShort(buf[5])
                + (unsignedByteToShort(buf[6]) / (0xff+1.0))
                + (unsignedByteToShort(buf[7]) / (0xffff+1.0));

        rootDispersion = (buf[8] * 256.0)
                + unsignedByteToShort(buf[9])
                + (unsignedByteToShort(buf[10]) / (0xff+1.0)) //256 0xff+1
                + (unsignedByteToShort(buf[11]) / (0xffff+1.0)); //0xffff+1

        //0101 0000 | 0101 0000 | 0101 0011 | 0000 0000
        // 80           80         83          0
        //ASCII PPS
        referenceIdentifier[0] = buf[12];
        referenceIdentifier[1] = buf[13];
        referenceIdentifier[2] = buf[14];
        referenceIdentifier[3] = buf[15];

        referenceTimestamp = byteArrayToDouble(buf, 16);
        originateTimestamp = byteArrayToDouble(buf, 24);
        receiveTimestamp = byteArrayToDouble(buf, 32);
        transmitTimestamp = byteArrayToDouble(buf, 40);

    }

    public SNTPMessage() {
        mode = 3;
        transmitTimestamp = (System.currentTimeMillis() / 1000.0) + 2208988800.0;
    }

    private double byteArrayToDouble(byte[] buf, int index) {
        double result = 0.0;
        for(int i = 0; i < 8; i++){
            result +=  unsignedByteToShort(buf[index+i]) * Math.pow(2, (3-i)*8);
        }
        return result;
    }

    private short unsignedByteToShort(byte b) {
        //Exempel b = 1101 1001, översta biten är satt och java tolkar som ett negativt tal
        //Kolla om översta biten är satt genom bitvis and med 0x80 eller 1000 0000
        if((b & 0x80) == 0x80){
            // 0x80 = 1000 0000
            // 1101 1001
            return (short)(128 + (b & 0x7f));
        }
        return (short) b;
    }

    public byte[] toByteArray() {
        byte [] array = new byte[48];
        array[0] = (byte)(leapIndicator << 6 | versionNumber << 3 | mode);
        //
        // LI == 0
        // 00 << 6 -> 0000 0000
        // versionNumber == 4
        // 0100 << 3 -> 0010 0000
        // mode == 3
        // 0011   -> 0000 0011
        //Med bitvis eller
        //  0000 0000
        //  0010 0000
        // |0000 0011
        //--------------
        //  0010 0011
        // |LI |  VN  | Mode |
        //  0 0 1 0 0  0 1 1
        array[1] = (byte) stratum;
        array[2] = (byte) pollInterval;
        array[3] = precision;

        int data = (int)(rootDelay * (0xff+1));
        array[4] = (byte) ((data >> 24) & 0xff);
        array[5] = (byte) ((data >> 16) & 0xff);
        array[6] = (byte) ((data >> 8) & 0xff);
        array[7] = (byte) (data & 0xff);

        int rd = (int)(rootDispersion * (0xff+1));
        array[8] = (byte) ((rd >> 24) & 0xff);
        array[9] = (byte) ((rd >> 16) & 0xff);
        array[10] = (byte) ((rd >> 8) & 0xff);
        array[11] = (byte) (rd & 0xff);

        array[12] = referenceIdentifier[0];
        array[13] = referenceIdentifier[1];
        array[14] = referenceIdentifier[2];
        array[15] = referenceIdentifier[3];

        doubleToByteArray(array, 16, referenceTimestamp);
        doubleToByteArray(array, 24, originateTimestamp);
        doubleToByteArray(array, 32, receiveTimestamp);
        doubleToByteArray(array, 40, transmitTimestamp);

        return array;
    }

    private void doubleToByteArray(byte[] array, int index, double data) {
        for(int i = 0; i < 8; i++){
            array[index + i] = (byte) (data / Math.pow(2, (3-i)*8));
            data -=  (double) (unsignedByteToShort(array[index+i]) * Math.pow(2, (3-i)*8));
        }
    }

    public String toString(){
        //TODO implementera metoden toString i SNTPMessage så att vi kan skriva ut vårt meddelande
        //LI: 0
        //Verions: 4
        //referenceIdentifier: PPS
        //..
        return "";
    }
}

