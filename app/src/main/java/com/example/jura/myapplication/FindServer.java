package com.example.jura.myapplication;

import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

/**
 * Created by Jura on 07.07.2016.
 */
public class FindServer {
    public static String error;
    public static String find() {
        int port = 5001;
        String codeword = "abc";
        try {
            byte[] message = codeword.getBytes();

            InetAddress address = InetAddress.getByName("192.168.199.255");

            // sending broadcast message
            DatagramPacket packet = new DatagramPacket(message, message.length, address, port);
            DatagramSocket dsocket = new DatagramSocket();
            dsocket.send(packet);
            dsocket.close();

            // Create a socket to listen on the port.
            DatagramSocket rsocket = new DatagramSocket(port);

            byte[] buffer = new byte[8];

            // Create a packet to receive data into the buffer
            packet = new DatagramPacket(buffer, buffer.length);

            // Set wait limit 10 seconds for response
            rsocket.setSoTimeout(10000);

            try {
                // Wait to receive a datagram
                rsocket.receive(packet);
            } catch (SocketTimeoutException e) {
                rsocket.close();
                throw e;
            }

            rsocket.close();

            // Convert the contents to a string
            String msg = new String(buffer, 0, packet.getLength());
            if (msg.equals(codeword)) {
                return packet.getAddress().getHostName();
            } else {
                error = "wrong codeword: "+msg+" length"+String.valueOf(packet.getLength());
                return "";
            }
        } catch (Exception e) {
            //System.err.println(e);
            error = e.toString();
            return "";
        }
    }
}
