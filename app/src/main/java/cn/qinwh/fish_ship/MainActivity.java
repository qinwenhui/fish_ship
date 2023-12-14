package cn.qinwh.fish_ship;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button connectBtn, onLedBtn, offLedBtn, flashLedBtn;
    private ImageButton forwardBtn, leftBtn, rightBtn;
    private Switch onSwitch;
    private TextView connectText, onText, ledText, leftSpeedText, rightSpeedText, seekbarText;
    private SeekBar seekBar;
    private Ship ship;
    private boolean isForward;
    private boolean isLeft;
    private boolean isRight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化船
        ship = new Ship(shipHandler);

        initView();
        initListener();

        //  船的初始速度和进度条保持一致
        ship.setLeftSpeed(seekBar.getProgress());
        ship.setRightSpeed(seekBar.getProgress());
        ship.setMaxSpeed(seekBar.getProgress());
        // 让船先进行一次心跳检测，解决重新进入程序时实际处于连接状态，但是页面ui没变化的问题
        ship.heart();
    }

    /**
     * 初始化控件
     */
    private void initView() {
        connectBtn = findViewById(R.id.connect_btn);
        onLedBtn = findViewById(R.id.on_led);
        offLedBtn = findViewById(R.id.off_led);
        flashLedBtn = findViewById(R.id.flash_led);

        forwardBtn = findViewById(R.id.button_forward);
        leftBtn = findViewById(R.id.button_left);
        rightBtn = findViewById(R.id.button_right);

        onSwitch = findViewById(R.id.on_switch);

        connectText = findViewById(R.id.connect_text);
        onText = findViewById(R.id.on_text);
        ledText = findViewById(R.id.led_text);
        leftSpeedText = findViewById(R.id.left_speed_text);
        rightSpeedText = findViewById(R.id.right_speed_text);
        seekbarText = findViewById(R.id.seekbar_text);

        seekBar = findViewById(R.id.seekBar);

    }


    /**
     * 初始化按钮监听
     */
    private void initListener() {
        // 连接按钮
        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 判断是否已经连接了设备
                if (ship.isConnect()) {
                    // 已经连接设备了，本次点击会断开连接
                    boolean disconnect = ship.disconnect();
//                    if (disconnect) {
//                        // 把按钮的文字改为“连接设备”
//                        connectBtn.setText("连接设备");
//                    }else {
//                        Toast.makeText(MainActivity.this, "连接设备失败！请检查wifi是否已经连接", Toast.LENGTH_SHORT).show();
//                    }

                }else {
                    // 本次点击将会连接设备
                    boolean connect = ship.connect();
//                    if (connect) {
                        // 把按钮的文字改为“断开设备”
//                        connectBtn.setText("断开设备");
//                    }
                }
            }
        });

        // 启动按钮
        onSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // 先判断是否连接到机器
                if (ship.isConnect()) {
                    if (isChecked) {
                        // 启动
                        boolean start = ship.start();
//                        if (start) {
//                            // 启动成功，更新上方的提示信息
//                            onText.setText("已启动");
//                        }else {
//                            // 启动失败，不改变switch按钮的状态
//                            Toast.makeText(MainActivity.this, "启动失败！", Toast.LENGTH_SHORT).show();
//                            onSwitch.setChecked(!isChecked);
//                        }
                    } else {
                        // 关闭
                        boolean stop = ship.stop();
//                        if (stop) {
//                            // 关闭成功，更新上方的提示信息
//                            onText.setText("已关闭");
//                        }else {
//                            // 停止失败，不改变switch按钮的状态
//                            Toast.makeText(MainActivity.this, "关闭失败！", Toast.LENGTH_SHORT).show();
//                            onSwitch.setChecked(!isChecked);
//                        }
                    }
                }else {
                    // 未连接到设备
                    Toast.makeText(MainActivity.this, "请先连接设备！", Toast.LENGTH_SHORT).show();
                    onSwitch.setChecked(!isChecked);
                }

            }
        });

        // 开灯按钮
        onLedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 开灯
                boolean on = ship.onLed();
//                if (on) {
//                    // 开灯成功
//                    ledText.setText("常亮");
//                }
            }
        });

        // 关灯按钮
        offLedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 关灯
                boolean off = ship.offLed();
//                if (off) {
//                    // 关灯成功
//                    ledText.setText("关闭");
//                }
            }
        });

        // 灯闪烁按钮
        flashLedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 闪烁
                boolean ok = ship.flashLed();
//                if (ok) {
//                    // 闪烁成功
//                    ledText.setText("闪烁");
//                }
            }
        });

        // 速度调节
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekbarText.setText("当前速度："+progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 滑动停止时的值
                int speed = seekBar.getProgress();
                ship.setLeftSpeed(speed*10);
                ship.setRightSpeed(speed*10);
                ship.setMaxSpeed(speed*10);
            }
        });

        // 前进按钮
        forwardBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!ship.isOn()) {
                    // 船没启动，无法操作
//                    Toast.makeText(MainActivity.this, "请先连接并启动设备！", Toast.LENGTH_SHORT).show();
                    return true;
                }

                if (event.getAction() == 0) {
                    // 设置按钮背景
                    forwardBtn.setBackgroundResource(R.drawable.btn_2);
                    // 按下了，船前进，速度是之前设置好的速度
                    isForward = true;
                    // 判断如果按下前进之前有转向，那就不处理
                    if(!isLeft && !isRight) {
                        ship.setShipSpeed(ship.getMaxSpeed(), ship.getMaxSpeed());
                    }

                }
                if (event.getAction() == 1) {
                    // 设置按钮背景
                    forwardBtn.setBackgroundResource(R.drawable.btn_1);
                    // 抬起了，船停止,设置速度为0
                    ship.setShipSpeed(0, 0);
                    isForward = false;
                }
                return true;
            }
        });

        // 左转
        leftBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!ship.isOn()) {
                    // 船没启动，无法操作
//                    Toast.makeText(MainActivity.this, "请先连接并启动设备！", Toast.LENGTH_SHORT).show();
                    return true;
                }
                if (event.getAction() == 0) {
                    // 设置按钮背景
                    leftBtn.setBackgroundResource(R.drawable.btn_2);
                    // 按下了，船左转，速度是之前设置好的速度
                    isLeft = true;
                    ship.setShipSpeed(ship.getMaxSpeed()/2, ship.getMaxSpeed());
                }
                if (event.getAction() == 1) {
                    // 设置按钮背景
                    leftBtn.setBackgroundResource(R.drawable.btn_1);
                    // 抬起了，船恢复速度前进
                    isLeft = false;
                    if (isForward)
                        ship.setShipSpeed(ship.getMaxSpeed(), ship.getMaxSpeed());
                    else {
                        ship.setShipSpeed(0, 0);
                    }
                }
                return true;
            }
        });

        // 右转
        rightBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!ship.isOn()) {
                    // 船没启动，无法操作
//                    Toast.makeText(MainActivity.this, "请先连接并启动设备！", Toast.LENGTH_SHORT).show();
                    return true;
                }
                if (event.getAction() == 0) {
                    // 设置按钮背景
                    rightBtn.setBackgroundResource(R.drawable.btn_2);
                    // 按下了，船右转，速度是之前设置好的速度
                    isRight = true;
                    ship.setShipSpeed(ship.getMaxSpeed(), ship.getMaxSpeed()/2);
                }
                if (event.getAction() == 1) {
                    // 设置按钮背景
                    rightBtn.setBackgroundResource(R.drawable.btn_1);
                    // 抬起了，船恢复速度前进
                    isRight = false;
                    if (isForward)
                        ship.setShipSpeed(ship.getMaxSpeed(), ship.getMaxSpeed());
                    else {
                        ship.setShipSpeed(0, 0);
                    }
                }
                return true;
            }
        });
    }

    @Override
    protected void onDestroy() {
        // 销毁udp
        ship.destroy();
        super.onDestroy();
    }

    private Handler shipHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case Ship.SHIP_CONNECT:
                    // 已经连接到设备
                    connectBtn.setText("断开设备");
                    connectText.setText("已连接");
                    break;
                case Ship.SHIP_DISCONNECT:
                    // 已经断开连接
                    connectBtn.setText("连接设备");
                    connectText.setText("未连接");
                    break;
                case Ship.SHIP_START:
                    // 启动
//                    onSwitch.setChecked(true);
                    onText.setText("已启动");
                    ledText.setText("常亮");
                    break;
                case Ship.SHIP_STOP:
                    // 停止
//                    onSwitch.setChecked(false);
                    onText.setText("已关闭");
                    break;
                case Ship.SHIP_SPEED:
                    // 设置左右速度
                    List<Integer> speed = (List<Integer>) msg.obj;
                    leftSpeedText.setText(String.valueOf(speed.get(0)));
                    rightSpeedText.setText(String.valueOf(speed.get(1)));
                    break;
                case Ship.SHIP_LED:
                    // 设置灯的状态
                    int value = (int) msg.obj;
                    if (value == 0) {
                        ledText.setText("关闭");
                    }
                    if (value == 1) {
                        ledText.setText("常亮");
                    }
                    break;
                case Ship.SHIP_FLASH:
                    // 设置闪烁
                    ledText.setText("闪烁");
                    break;
                case Ship.SHIP_HEART:
                    // 心跳检测
                    int active = (int) msg.obj;
                    if (active == 1) {
                        connectBtn.setText("断开设备");
                        connectText.setText("已连接");
                    }else {
                        Log.i("HEART", "连接已断开");
                        connectBtn.setText("连接设备");
                        connectText.setText("未连接");
                    }
                    break;
            }
        }
    };
}