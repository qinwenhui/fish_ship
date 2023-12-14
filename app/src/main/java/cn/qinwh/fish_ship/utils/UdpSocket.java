package cn.qinwh.fish_ship.utils;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;

public class UdpSocket {
    private DatagramSocket socket;
    public static final int UDPPORT = 8088;
    private Handler handler;
    private boolean getMessage;

    public UdpSocket(Handler handler) throws SocketException {
        this.handler = handler;
        getMessage = true;
        if (socket == null) {
            socket = new DatagramSocket(UDPPORT);
        }
    }

    byte data[] = new byte[1024];

    public void initSocket() {
        ThreadPoolUtil.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    DatagramPacket datagramPackage = new DatagramPacket(data, data.length);
                    while (true) {
                        socket.receive(datagramPackage);
//            LogUtils.e("UdpSocket1"+ new String(datagramPackage.getData(), datagramPackage.getOffset(), datagramPackage.getLength()));
                        //接收到的byte[]
                        String data = new String(datagramPackage.getData(), datagramPackage.getOffset(), datagramPackage.getLength());
                        Log.e("udp接收到数据=====>>"+datagramPackage.getAddress()+":"+datagramPackage.getPort(),data);
//                        byte[] m = Arrays.copyOf(datagramPackage.getData(), datagramPackage.getLength());
//                        Log.e("ERROR","UdpSocket2" + datagramPackage.getPort() + "=" + datagramPackage.getAddress() + "=" + bytesToHexString(m));
//                        byte[] bytes = datagramPackage.getData();
//                        Log.e("ERROR","UdpSocket3" + bytesToHexString(bytes));
//                        String msg = bytesToHexString(m);
//                        Log.e("ERROR",msg);
                        Message msg1 = handler.obtainMessage(0x00, data);
//                        msg1.obj = bytesToHexString(bytes);
//                        msg1.what = 0x00;
                        handler.sendMessage(msg1);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void getUdpData() {
        try {
            DatagramPacket udpPacket = new DatagramPacket(data, data.length);
            while (getMessage) {
                try {
                    socket.receive(udpPacket);
                } catch (Exception e) {
                }
                //接收到的byte[]
                byte[] m = Arrays.copyOf(udpPacket.getData(), udpPacket.getLength());
            }
            socket.close();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public void sendUdp(final String message, final String address, final int port) {
        ThreadPoolUtil.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    InetAddress serverAddress = null;
                    serverAddress = InetAddress.getByName(address);
                    byte data[] = message.getBytes();
                    DatagramPacket datagramPacket = new DatagramPacket(data, data.length, serverAddress, port);
                    socket.send(datagramPacket);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void close() {
        socket.close();
    }

    public static String bytesToHexString(byte[] bytes) {
        String result = "";
        for (int i = 0; i < bytes.length; i++) {
            String hexString = Integer.toHexString(bytes[i] & 0xFF);
            if (hexString.length() == 1) {
                hexString = '0' + hexString;
            }
            result += hexString.toUpperCase();
        }
        return result;
    }
}
