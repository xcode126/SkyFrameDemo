package com.tigo.skyframedemo;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.common.StringUtils;
import com.zxy.tiny.Tiny;
import com.zxy.tiny.callback.BitmapCallback;

import net.posprinter.posprinterface.ProcessData;
import net.posprinter.posprinterface.UiExecute;
import net.posprinter.utils.BitmapToByteData;
import net.posprinter.utils.DataForSendToPrinterPos58;
import net.posprinter.utils.DataForSendToPrinterPos80;
import net.posprinter.utils.PosPrinterDev;

import java.util.ArrayList;
import java.util.List;

/**
 * Author：sky on 2018/5/3 0003 19:20.
 * Email：xcode126@126.com
 * Desc：
 */

public class PosActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btnPrintText, btnPrintImage, btnChecklink;
    private TextView tip;
    private EditText etContent;
    private ImageView imageView;
    private Receiver netReceiver;

    private Bitmap b1, b2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pos);
        //注册广播
        netReceiver = new Receiver();
        registerReceiver(netReceiver, new IntentFilter(MainActivity_v1.DISCONNECT));
        //初始化控件
        initView();
        //判断是否连接
        if (MainActivity_v1.ISCONNECT) {
            setListener();
        } else {
            Toast.makeText(PosActivity.this, "USB未连接", 0).show();
        }
        //压缩初始化
        Tiny.getInstance().init(getApplication());
    }

    /**
     * 初始化View
     */
    private void initView() {
        etContent = (EditText) findViewById(R.id.et_content);
        tip = (TextView) findViewById(R.id.tv_net_disconnect);
        imageView = (ImageView) findViewById(R.id.image);
        btnPrintText = (Button) findViewById(R.id.btn_text);
        btnPrintImage = (Button) findViewById(R.id.btn_pic);
        btnChecklink = (Button) findViewById(R.id.btn_check_link);
    }

    /**
     * 设置监听
     */
    private void setListener() {
        btnPrintText.setOnClickListener(this);
        btnPrintImage.setOnClickListener(this);
        btnChecklink.setOnClickListener(this);
    }

    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    tip.setVisibility(View.GONE);
                    imageView.setImageBitmap(b1);
                    break;
                case 2://图片打印
//                    Toast.makeText(PosActivity.this, "bimap  " + b2.getWidth() + "  height: " + b2.getHeight(), Toast.LENGTH_LONG).show();
//                    b2 = PrintUtils.resizeImage(b2, 380, false);
//                    printUSBbitamp(b2);
//                    tip.setVisibility(View.GONE);
                    break;
                case 3://断开连接
                    btnPrintText.setEnabled(false);
                    btnPrintImage.setEnabled(false);
                    tip.setVisibility(View.VISIBLE);
                    break;
                case 4:
                    tip.setVisibility(View.VISIBLE);
                    break;
                case 6:
                    printPhoto((Bitmap) msg.obj);
                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_text:
                printText();
                break;
            case R.id.btn_pic:
//                printPIC();
                printPhoto();
                break;
            case R.id.btn_check_link:
                checklink();
                break;
        }
    }

    /**
     * 打印文本
     */
    private void printText() {
        MainActivity_v1.binder.writeDataByYouself(new UiExecute() {
            @Override
            public void onsucess() {

            }

            @Override
            public void onfailed() {

            }
        }, new ProcessData() {
            @Override
            public List<byte[]> processDataBeforeSend() {
                List<byte[]> list = new ArrayList<byte[]>();
                //创建一段我们想打印的文本,转换为byte[]类型，并添加到要发送的数据的集合list中

                String str = etContent.getText().toString();
                if (str.equals(null) || str.equals("")) {
                    Toast.makeText(PosActivity.this, "text_for", 0).show();
                } else {
                    //初始化打印机，清除缓存
                    list.add(DataForSendToPrinterPos58.initializePrinter());
                    //添加需要打印的字节数组
                    list.add(PrintUtils.strTobytes(str));
                    //追加一个打印换行指令，因为，pos打印机满一行才打印，不足一行，不打印
                    list.add(DataForSendToPrinterPos58.printAndFeedLine());
                    //打印并切纸
//                    list.add(DataForSendToPrinterPos80.selectCutPagerModerAndCutPager(66, 1));
                    return list;
                }

                return null;
            }
        });
    }

    /**
     * 打印光栅位图，推荐打印图片使用此方法，这种打印方式可以更好的打印较大的图片，而不受打印机内存限制
     * 去相册选择图像，在onactivityresult里回调，得到一个bitmap对象，然后调用发送printRasteBmp指令
     */
    private void printPIC() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK) {
            //通过去图库选择图片，然后得到返回的bitmap对象
            try {
                Bitmap b = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                b1 = PrintUtils.convertGreyImg(b);
                Message message = new Message();
                message.what = 1;
                handler.handleMessage(message);
                //压缩图片
                Tiny.BitmapCompressOptions options = new Tiny.BitmapCompressOptions();
                Tiny.getInstance().source(b1).asBitmap().withOptions(options).compress(new BitmapCallback() {
                    @Override
                    public void callback(boolean isSuccess, Bitmap bitmap) {
                        if (isSuccess) {
//                            Toast.makeText(PosActivity.this,"bitmap: "+bitmap.getByteCount(),Toast.LENGTH_LONG).show();
                            b2 = bitmap;
//                            b2=resizeImage(b1,380,false);
                            Message message = new Message();
                            message.what = 2;
                            handler.handleMessage(message);
                        }
                    }
                });
//                b2=resizeImage(b1,576,386,false);//576是80型号
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void printPhoto() {
        Bitmap bitmap = PrintUtils.convertGreyImg(BitmapFactory.decodeResource(getResources(), R.drawable.ic_test1));
//        Bitmap bitmap = Glide.with(WrapperApplication.getInstance()).load(logo).asBitmap().into(160, 35).get();
        //压缩图片
        Tiny.BitmapCompressOptions options = new Tiny.BitmapCompressOptions();
        options.width = 260;
        options.height = 260;
        Tiny.getInstance().source(bitmap).asBitmap().withOptions(options).compress(new BitmapCallback() {
            @Override
            public void callback(boolean isSuccess, Bitmap bitmap) {
                if (isSuccess) {
                    Message message = new Message();
                    message.what = 6;
                    message.obj = bitmap;
                    handler.handleMessage(message);
                }
            }
        });
    }

    private void printPhoto(Bitmap bitmap) {
        List<byte[]> list = new ArrayList<>();
        list.add(DataForSendToPrinterPos58.initializePrinter());//初始化打印机，清除缓存

        list.add(PrintUtils.getFormatBytes("打印机配置成功\n"));
        list.add(PrintUtils.getFormatBytes("这是一条测试数据\n"));
        list.add(DataForSendToPrinterPos58.printRasterBmp(0, bitmap, BitmapToByteData.BmpType.Threshold,
                BitmapToByteData.AlignType.Center, 384));
//                BitmapToByteData.AlignType.Center, 419));
//        list.add(PrintUtils.advanceLine(4));
        print(list);
    }

    public void print(final List<byte[]> list) {
        if (MainActivity_v1.ISCONNECT) {
            //向打印机发生打印指令和打印数据，调用此方法
            //第一个参数，还是UiExecute接口的实现，分别是发生数据成功和失败后在ui线程的处理
            MainActivity_v1.binder.writeDataByYouself(new UiExecute() {

                @Override
                public void onsucess() {
                    Log.i("TAG", "打印图片操作成功");
                }

                @Override
                public void onfailed() {
                    Log.i("TAG", "打印图片操作异常");
                }
            }, new ProcessData() {
                @Override
                public List<byte[]> processDataBeforeSend() {
                    return list;
                }
            });
        } else {
            Toast.makeText(PosActivity.this, "打印机未连接", 0).show();
        }
    }

    /**
     * 检查连接
     */
    private void checklink() {
        MainActivity_v1.binder.checkLinkedState(new UiExecute() {
            @Override
            public void onsucess() {
                Toast.makeText(PosActivity.this, "连接未断开", 0).show();
            }

            @Override
            public void onfailed() {
                Toast.makeText(PosActivity.this, "连接已断开", 0).show();
                Message message = new Message();
                message.what = 3;
                handler.handleMessage(message);
            }
        });
    }

    /**
     * 广播接收,打印机的链接状态
     */
    private class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(MainActivity_v1.DISCONNECT)) {
                Message message = new Message();
                message.what = 4;
                handler.handleMessage(message);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(netReceiver);
    }
}
