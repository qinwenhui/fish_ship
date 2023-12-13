//package cn.qinwh.fish_ship.utils;
//
//import android.annotation.SuppressLint;
//import android.content.Context;
//import android.net.wifi.WifiInfo;
//import android.net.wifi.WifiManager;
//import android.util.Log;
//
//import java.io.IOException;
//import java.net.DatagramPacket;
//import java.net.DatagramSocket;
//import java.net.InetAddress;
//
//public class UdpSocketClient {
//    private static final String TAG = "UdpSocketClient";
//
//    private static String IP;
//    private static int BROADCAST_PORT = 9999;
//
//    private String receiveIp;
//    private String sendIp;
//    private DatagramSocket receiveSocket = null;
//    private DatagramSocket sendSocket = null;
//    private DatagramPacket dpReceive = null;
//    private SendThread sendThread;
//    private ReceiveThread receiveThread;
//    private boolean isRunning = true;
//    private String previousContent = new String();
//    private ReviceCallBack reviceCallBack;
//    private String sendData;
//
//    private UdpSocketClient() {
//    }
//
//    public void initWifi(Context context) {
//        Log.i(TAG, "initWifi: is run.");
//        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//        if (wifiManager.isWifiEnabled()) {
//            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
//            IP = getIpString(wifiInfo.getIpAddress());
//            Log.i(TAG, "onCreate: Ip: " + IP);
//        }
//
//        receiveThread = new ReceiveThread();
//        sendThread = new SendThread();
//        receiveThread.start();
//        sendThread.start();
//    }
//
//    private class ReceiveThread extends Thread {
//        @SuppressLint("LongLogTag")
//        @Override
//        public void run() {
//            while (true) {
//                if (isRunning) {
//                    String receiveContent = null;
//                    byte[] buf = new byte[1024];
//                    dpReceive = new DatagramPacket(buf, buf.length);
//                    try {
//                        if (receiveSocket == null) {
//                            receiveSocket = new DatagramSocket(BROADCAST_PORT);
//                        }
//                        receiveSocket.receive(dpReceive);
//                        receiveContent = new String(buf, 0, dpReceive.getLength());
//                        receiveIp = dpReceive.getAddress().toString().substring(1);
//                        if (receiveIp != IP) {
//                            sendIp = receiveIp;
//                        }
//                        if (reviceCallBack != null && reviceBean.getFlag().equals("server")) {
//                            reviceCallBack.reviceData(receiveContent);
//                        }
//                        previousContent = receiveContent;
//
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
//    }
//
//    private class SendThread extends Thread {
//
//        @SuppressLint("HandlerLeak")
//        @Override
//        public void run() {
//            while (true) {
//                try {
//                    Log.i(TAG, "handleMessage: is run.");
//                    DatagramPacket dpSend = null;
//                    sendData = "感谢点赞!";
//                    byte[] sendMsg = sendData.getBytes();
//                    if (receiveIp != IP) {
//                        InetAddress inetAddress = InetAddress.getByName(sendIp);
//                        dpSend = new DatagramPacket(ip, ip.length, inetAddress, BROADCAST_PORT);
//                    }
//                    String sendData = new String(sendMsg, 0, dpSend.getLength());
//                    sendSocket = new DatagramSocket();
//                    sendSocket.send(dpSend);
//                    Log.i(TAG, "run: Client: send message : " + sendData);
//                    sendSocket.close();
//                    Thread.sleep(80);
//                } catch (IOException | InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//
//        }
//    }
//
//    private String getIpString(int i) {
//        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "."
//                + ((i >> 16) & 0xFF) + "." + (i >> 24 & 0xFF);
//    }
//
//    public void setReviceCallBack(ReviceCallBack reviceCallBack) {
//        this.reviceCallBack = reviceCallBack;
//    }
//
//    public interface ReviceCallBack {
//        void reviceData(String data);
//    }
//}
