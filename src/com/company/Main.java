package com.company;

/*  0                1                   2                   3
    0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9  0  1
    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    |LI | VN  |Mode |    Stratum    |     Poll      |   Precision    |
    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    |                          Root  Delay                           |
    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    |                       Root  Dispersion                         |
    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    |                     Reference Identifier                       |
    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    |                                                                |
    |                    Reference Timestamp (64)                    |
    |                                                                |
    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    |                                                                |
    |                    Originate Timestamp (64)                    |
    |                                                                |
    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    |                                                                |
    |                     Receive Timestamp (64)                     |
    |                                                                |
    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    |                                                                |
    |                     Transmit Timestamp (64)                    |
    |                                                                |
    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    |                 Key Identifier (optional) (32)                 |
    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    |                                                                |
    |                                                                |
    |                 Message Digest (optional) (128)                |
    |                                                                |
    |                                                                |
    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+*/


import java.io.IOException;
import java.net.*;

public class Main {



    public static void main(String[] args) {
        String ntpArray[] = {
                //Malmö:
                "mmo1.ntp.se", "mmo2.ntp.se",
                //Göteborg:
                "gbg1.ntp.se", "gbg2.ntp.se",
                //Stockholm:
                "sth1.ntp.se", "sth2.ntp.se",
                //Sundsvall:
                "svl1.ntp.se", "svl2.ntp.se"};
        int currentServer = 0;
        boolean isException = false;

       do{
        try {
            //TODO utöka koden så att den försöker ansluta till en annan server om anslutningen misslyckas


            DatagramSocket socket = new DatagramSocket();
            InetAddress address = InetAddress.getByName(ntpArray[currentServer]);
            SNTPMessage  message = new SNTPMessage();
            byte [] buf = message.toByteArray();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 123);
            isException = false;
            System.out.println("Conncection succesfull with: " + ntpArray[currentServer]);
            socket.send(packet);
            System.out.println("Sent request");
            socket.receive(packet);
            SNTPMessage response = new SNTPMessage(packet.getData());
            System.out.println("Got reply");
            socket.close();
            System.out.println();

            //TODO räkna ut offseten mellan datorns klocka och tidsservern, se RFC
            response.offsetTime();

        } catch (IOException e) {
            isException = true;
            System.err.println(ntpArray[currentServer]+ " could not connect");
            currentServer++;
            //e.printStackTrace();
        }}
       while(isException);



//Nedan är ett exempel på ett meddelande från en time server
/*        byte [] buf = {  36,  1,  0, -25,
                0,   0,  0,   0,
                0,   0,  0,   2,
                80,  80, 83,   0,
                -29, 116,  5, 61,  0,  0,    0,   0,
                -29, 116,  5, 59, 14, 86,    0,   0,
                -29, 116,  5, 62,  0, 47, -121, -38,
                -29, 116,  5, 62,  0, 47, -113,  -1};*/

        //Första byten 36
        //  0 1  2 3 4 5 6 7
        // |LI |  VN  | Mode |
        //  0 0 1 0 0  1 0 0
        //   0    4      4
        //
        // 0000 0000 -> 0
        // 0000 0001 -> 1
        // 0000 0010 -> 2
        // 0000 0011 -> 3
        // 0000 0100 -> 4
        // 0000 0101 -> 5
/*
        SNTPMessage msg = new SNTPMessage(buf);*/

    }
}
