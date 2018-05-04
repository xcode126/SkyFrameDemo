package com.tigo.skyframedemo;


import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import net.posprinter.posprinterface.IMyBinder;
import net.posprinter.posprinterface.UiExecute;
import net.posprinter.service.PosprinterService;
import net.posprinter.utils.PosPrinterDev;

import java.util.ArrayList;
import java.util.List;

/**
 * Author：sky on 2018/4/25 0020 11:01.
 * Email：xcode126@126.com
 * Desc：首页
 */

public class MainActivity_v1 extends AppCompatActivity implements View.OnClickListener {
    private Button btnConnect;
    private EditText etDevice;
    private ListView lv_usb;
    private List<String> getUsbList;

    public static String DISCONNECT = "com.posconsend.net.disconnetct";
    //IMyBinder接口，所有可供调用的连接和发送数据的方法都封装在这个接口内
    public static IMyBinder binder;
    //bindService的参数connection
    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //绑定成功
            binder = (IMyBinder) iBinder;
            Log.e("binder", "connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.e("disbinder", "disconnected");
        }
    };

    public static boolean ISCONNECT;//判断是否连接成功

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_v1);
        //绑定service，获取ImyBinder对象
        Intent intent = new Intent(this, PosprinterService.class);
        bindService(intent, conn, BIND_AUTO_CREATE);
        //初始化控件
        initViewEvent();
    }

    private void initViewEvent() {
        btnConnect = (Button) findViewById(R.id.btn_connect);
        etDevice = (EditText) findViewById(R.id.showET);
        btnConnect.setOnClickListener(this);
        findViewById(R.id.btn_disconnect).setOnClickListener(this);
        findViewById(R.id.btn_skip_pos).setOnClickListener(this);
        findViewById(R.id.btn_choose).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_choose:
                setUSB();
                break;
            case R.id.btn_connect:
                connetUSB();
                break;
            case R.id.btn_disconnect:
                if (ISCONNECT) {
                    binder.disconnectCurrentPort(new UiExecute() {
                        @Override
                        public void onsucess() {
                            Toast.makeText(MainActivity_v1.this, "断开连接成功", Toast.LENGTH_SHORT).show();
                            etDevice.setText("");
                            btnConnect.setText("connect");
                        }

                        @Override
                        public void onfailed() {
                            Toast.makeText(MainActivity_v1.this, "断开连接失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(MainActivity_v1.this, "there is no connect", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_skip_pos:
                if (ISCONNECT) {
                    Intent intent = new Intent(this, PosActivity.class);
                    intent.putExtra("isconnect", ISCONNECT);
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity_v1.this, "请先连接打印机", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    /**
     * 选择uSB连接模式
     */
    private void setUSB() {
        View dialogView3 = LayoutInflater.from(this).inflate(R.layout.usb_link, null);
        TextView tv_usb = dialogView3.findViewById(R.id.textView1);
        lv_usb = dialogView3.findViewById(R.id.listView1);
        //获取设备数量
        getUsbList = PosPrinterDev.GetUsbPathNames(this);
        if (getUsbList == null) {
            getUsbList = new ArrayList<>();
        }
        tv_usb.setText("present connected Usb dev" + getUsbList.size());
        ArrayAdapter<String> adapter3 = new ArrayAdapter(this, android.R.layout.simple_list_item_1, getUsbList);
        lv_usb.setAdapter(adapter3);
        //弹窗
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView3).create();
        dialog.show();
        setUsbLisener(dialog);
    }

    /**
     * 选择USB
     *
     * @param dialog
     */
    public void setUsbLisener(final AlertDialog dialog) {
        lv_usb.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String usbDev = getUsbList.get(i);
                etDevice.setText(usbDev);
                dialog.cancel();
                Log.e("usbDev: ", usbDev);
            }
        });
    }

    String usbAdrresss;

    /**
     * USB连接
     */
    private void connetUSB() {
        usbAdrresss = etDevice.getText().toString();
        if (usbAdrresss.equals(null) || usbAdrresss.equals("")) {
            Toast.makeText(MainActivity_v1.this, "plsase choose usb device!", Toast.LENGTH_SHORT).show();
        } else {
            binder.connectUsbPort(getApplicationContext(), usbAdrresss, new UiExecute() {
                @Override
                public void onsucess() {
                    //连接成功后在UI线程中的执行
                    ISCONNECT = true;
                    Toast.makeText(MainActivity_v1.this, "连接成功", 0).show();
                    btnConnect.setText("连接成功");
                }

                @Override
                public void onfailed() {
                    ISCONNECT = false;
                    Toast.makeText(MainActivity_v1.this, "连接失败", 0).show();
                    btnConnect.setText("连接失败");
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binder.disconnectCurrentPort(new UiExecute() {
            @Override
            public void onsucess() {

            }

            @Override
            public void onfailed() {

            }
        });
        unbindService(conn);
    }
}
