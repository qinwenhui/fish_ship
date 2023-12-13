package cn.qinwh.fish_ship.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UdpSocketServer {
    private static final String TAG = "UdpSocketServer";
    private static String IP;
    private static UdpSocketServer instance;
    private InetAddress inetAddress = null;
    private static int BROADCAST_PORT = 9999;
    private static String BROADCAST_IP = "255.255.255.255";

    private ReceiveThread receiveThread;
    private DatagramSocket sendSocket = null;
    private DatagramSocket receiveSocket = null;
    private volatile boolean isRuning = true;
    private BroadcastThread broadcastThread;
    private String reviceData;
    private ReviceCallBack reviceCallBack;

    private UdpSocketServer() {
    }

    public void initWifi(Context context) {
        //Wifi状态判断
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            IP = getIpString(wifiInfo.getIpAddress());
            Log.i(TAG, "initWifi: Ip: " + IP
            );
        }
        initThread();
        initAddress();
    }

    private void initAddress() {
        try {
            inetAddress = InetAddress.getByName(BROADCAST_IP);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initThread() {
        broadcastThread = new BroadcastThread();
        broadcastThread.start();
        receiveThread = new ReceiveThread();
        receiveThread.start();
    }

    public class BroadcastThread extends Thread {
        private Handler mhandler = null;

        @SuppressLint("HandlerLeak")
        @Override
        public void run() {
            Looper.prepare();
            mhandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    String message = (String) msg.obj;
                    byte[] data = message.getBytes();
                    DatagramPacket dpSend = null;
                    dpSend = new DatagramPacket(data, data.length, inetAddress, BROADCAST_PORT);
                    try {
                        sendSocket = new DatagramSocket();
                        sendSocket.send(dpSend);
                        sendSocket.close();
                        Thread.sleep(80);
                        Log.i(TAG, "sendMessage: data " + new String(data));
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            Looper.loop();
        }
    }

    public void SendData(String message) {
        Message msg = Message.obtain();
        msg.obj = message;
        msg.what = 1;
        broadcastThread.mhandler.sendMessage(msg);
    }

    public class ReceiveThread extends Thread {
        @Override
        public void run() {
            try {
                while (true) {
                    if (isRuning) {
                        byte[] receiveData = new byte[1024];
                        DatagramPacket dpReceive = null;
                        dpReceive = new DatagramPacket(receiveData, receiveData.length);
                        if (receiveSocket == null) {
                            receiveSocket = new DatagramSocket(BROADCAST_PORT);
                        }
                        receiveSocket.receive(dpReceive);
                        String recIp = dpReceive.getAddress().toString().substring(1);
                        String reviceMsg = new String(receiveData, 0, dpReceive.getLength());
                        if (reviceMsg != null) {
                            reviceCallBack.reviceData(reviceMsg);
                        }
                        Thread.sleep(80);
                        Log.i(TAG, "run: reviceMsg: " + reviceMsg);
                    }
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private String getIpString(int i) {
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "."
                + ((i >> 16) & 0xFF) + "." + (i >> 24 & 0xFF);
    }

    public void setReviceCallBack(ReviceCallBack reviceCallBack) {
        this.reviceCallBack = reviceCallBack;
    }

    public interface ReviceCallBack {
        void reviceData(String data);
    }
}
