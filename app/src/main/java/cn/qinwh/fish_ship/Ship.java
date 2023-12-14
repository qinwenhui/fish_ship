package cn.qinwh.fish_ship;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cn.qinwh.fish_ship.utils.ThreadPoolUtil;
import cn.qinwh.fish_ship.utils.UdpSocket;

public class Ship {
    private boolean connect;
    private boolean on;
    private int led;    // 0=关闭,1=常亮,2=闪烁
    private int leftSpeed;
    private int rightSpeed;
    private int maxSpeed;
    private UdpSocket udpSocket;
    private String ip;
    private int port;
    private Handler shipHandler;
    public static final int SHIP_CONNECT = 0x01;
    public static final int SHIP_DISCONNECT = 0x02;
    public static final int SHIP_START = 0x03;
    public static final int SHIP_STOP = 0x04;
    public static final int SHIP_SPEED = 0x05;
    public static final int SHIP_LED = 0x06;
    public static final int SHIP_FLASH = 0x07;
    public static final int SHIP_HEART = 0x08;

    public static final int UDP_RECV = 0x00;
    private Handler udpHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case UDP_RECV:
                    String data = (String) msg.obj;
                    String[] commonds = data.split(",");
                    String commond = commonds[0];
                    Message message = shipHandler.obtainMessage();

                    // 解析接收到的消息
                    if ("CONNECT".equals(commond)) {
                        // 说明已经连接到设备
                        setConnect(true);
                        message.what = SHIP_CONNECT;
                    }
                    if ("DISCONNECT".equals(commond)) {
                        // 说明已经断开连接
                        setConnect(false);
                        message.what = SHIP_DISCONNECT;
                    }
                    if ("start".equals(commond)) {
                        // 启动
                        setOn(true);
                        message.what = SHIP_START;
                    }
                    if ("stop".equals(commond)) {
                        // 关闭
                        setOn(false);
                        message.what = SHIP_STOP;
                    }
                    if ("speed".equals(commond)) {
                        int left = Integer.valueOf(commonds[1]);
                        int right = Integer.valueOf(commonds[2]);
                        // 设置左右两个电机的速度
                        setLeftSpeed(left);
                        setRightSpeed(right);
                        message.what = SHIP_SPEED;
                        List<Integer> speed = new ArrayList<>();
                        speed.add(left);
                        speed.add(right);
                        message.obj = speed;
//                        message.obj = new int[]{left, right};
                    }
                    if ("led".equals(commond)) {
                        int value = Integer.valueOf(commonds[1]);
                        // 设置led的状态
                        setLed(value);
                        message.what = SHIP_LED;
                        message.obj = value;
                    }
                    if ("flash".equals(commond)) {
                        // 设置LED闪烁
                        setLed(2);
                        message.what = SHIP_FLASH;
                        message.obj = 2;
                    }
                    if ("HEART".equals(commond)) {
                        // 心跳
                        int active = Integer.valueOf(commonds[1]);
                        if (active == 1) {
                            // 心跳正常，连接正常
                            setConnect(true);
                        }else {
                            setConnect(false);
                        }
                        message.what = SHIP_HEART;
                        message.obj = active;
                    }
                    shipHandler.sendMessage(message);
                    break;
            }
        }
    };

    public Ship(Handler handler) {
        this.ip = "192.168.4.1";
        this.port = 8888;
        this.shipHandler = handler;
        try {
            udpSocket = new UdpSocket(udpHandler);
            udpSocket.initSocket();
        } catch (Exception e) {
            Log.e("Ship", "Ship初始化UDP失败");
            e.printStackTrace();
        }
        // 异步发送心跳
        ThreadPoolUtil.execute(new Runnable() {
            @Override
            public void run() {
                // 判断是否处于连接状态，连接状态才进行心跳检测
                while (true) {
                    if (isConnect()) {
                        heart();
                    }
                    // 延时1秒
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * 销毁udp，防止重进程序时提示端口占用
     */
    public void destroy() {
        setConnect(false);
        udpSocket.close();
    }

    public void sendCommond(String commond) {
        udpSocket.sendUdp(commond, ip, port);
    }

    public boolean connect() {
        // 发送连接的请求
        Log.i("Ship", "发送连接请求");
        sendCommond("CONNECT");
        return true;
    }

    public boolean disconnect() {
        // 发送断开连接的请求
        Log.i("Ship", "发送断开请求");
        sendCommond("DISCONNECT");
        return true;
    }

    public boolean isConnect() {
        return connect;
    }

    public void setConnect(boolean connect) {
        this.connect = connect;
    }

    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }

    public int getLed() {
        return led;
    }

    public void setLed(int led) {
        this.led = led;
    }


    public boolean onLed() {
        // 发送开灯请求
        Log.i("Ship", "发送开灯请求");
        sendCommond("led+1");
        return true;
    }

    public boolean offLed() {
        // 发送关灯请求
        Log.i("Ship", "发送关灯请求");
        sendCommond("led+0");
        return true;
    }

    public boolean flashLed() {
        // 发送闪烁灯请求
        Log.i("Ship", "发送闪烁灯请求");
        sendCommond("flash+500");
        return true;
    }

    public boolean start() {
        // 启动
        Log.i("Ship", "发送启动请求");
        sendCommond("start");
        return true;
    }

    public boolean stop() {
        // 关闭
        Log.i("Ship", "发送关闭请求");
        sendCommond("stop");
        return true;
    }

    public void setShipSpeed(int left, int right) {
        // 设置速度
        Log.i("Ship", "发送设置速度请求");
        sendCommond("speed+"+left+","+right);
    }

    public void heart() {
        // 发送心跳包
        sendCommond("HEART");
    }

    public int getLeftSpeed() {
        return leftSpeed;
    }

    public void setLeftSpeed(int leftSpeed) {
        this.leftSpeed = leftSpeed;
    }

    public int getRightSpeed() {
        return rightSpeed;
    }

    public void setRightSpeed(int rightSpeed) {
        this.rightSpeed = rightSpeed;
    }

    public int getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(int maxSpeed) {
        this.maxSpeed = maxSpeed;
    }
}
