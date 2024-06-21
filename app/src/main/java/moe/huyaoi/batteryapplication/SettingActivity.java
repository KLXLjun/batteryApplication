package moe.huyaoi.batteryapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Objects;

import moe.huyaoi.batteryapplication.configs.configs;

public class SettingActivity extends Activity {
    private String TAG = "SettingActivity";

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView tcpServerUrlText = (TextView) findViewById(R.id.tcpServerUrlText);
        tcpServerUrlText.setText(getResources().getText(R.string.now_setting_value) + configs.GetTcpServerAddress());

        TextView tcpServerPortText = (TextView) findViewById(R.id.tcpServerPortText);
        tcpServerPortText.setText(String.format("%s %d",getResources().getText(R.string.now_setting_value),configs.GetTcpServerPort()));

        TextView udpPortText = (TextView) findViewById(R.id.udpPortText);
        udpPortText.setText(String.format("%s %d",getResources().getText(R.string.now_setting_value),configs.GetUdpPort()));

        Context ctx = this;
        //
        ToggleButton tcpToggleButton = (ToggleButton) findViewById(R.id.TcpSwitch);
        tcpToggleButton.setChecked(configs.GetTcpSwitch());
        tcpToggleButton.setOnClickListener(v -> configs.SetTcpSwitch(tcpToggleButton.isChecked()));

        RelativeLayout tcpServerUrlLayout = (RelativeLayout) findViewById(R.id.tcpServerUrlLayout);
        tcpServerUrlLayout.setOnClickListener(v -> {
            final EditText inputServer = new EditText(ctx);
            inputServer.setText(configs.GetTcpServerAddress());
            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            builder.setTitle("输入TCP服务器地址").setView(inputServer)
            .setNegativeButton("取消", (dialog, which) -> dialog.dismiss());

            builder.setPositiveButton("确定", (dialog, which) -> {
                String str = inputServer.getText().toString();
                configs.SetTcpServerAddress(str);
                Toast.makeText(ctx,"地址已设定", Toast.LENGTH_LONG).show();
                tcpServerUrlText.setText(getResources().getText(R.string.now_setting_value) + configs.GetTcpServerAddress());
                dialog.dismiss();
            });
            builder.show();
        });

        RelativeLayout tcpServerPortLayout = (RelativeLayout) findViewById(R.id.tcpServerPortLayout);
        tcpServerPortLayout.setOnClickListener(v -> {
            final EditText inputServer = new EditText(ctx);
            inputServer.setText(String.format("%d",configs.GetTcpServerPort()));
            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            builder.setTitle("输入TCP服务器端口").setView(inputServer)
                    .setNegativeButton("取消", (dialog, which) -> dialog.dismiss());

            builder.setPositiveButton("确定", (dialog, which) -> {
                String str = inputServer.getText().toString();
                int num = 0;
                try {
                    num = Integer.parseInt(str);
                    if(num > 1000 && num < 65535) {
                        configs.SetTcpServerPort(num);
                        Toast.makeText(ctx,"端口已设定", Toast.LENGTH_LONG).show();
                        return;
                    }
                    throw new Exception("端口过大或过小");
                } catch (Exception e) {
                    // 处理转换异常
                    Log.d(TAG, Objects.requireNonNull(e.getMessage()));
                    AlertDialog alertDialog = new AlertDialog.Builder(ctx)
                    .setTitle("端口不正确")
                    .setMessage("请重新输入端口 端口范围为1001 - 65534")
                    .setPositiveButton("好", (dialogInterface, i) -> {
                        //执行代码
                        dialogInterface.dismiss();//销毁对话框
                    })
                    .create();
                    alertDialog.show();
                }
            });
            builder.show();
        });

        //
        ToggleButton udpToggleButton = (ToggleButton) findViewById(R.id.UdpSwitch);
        udpToggleButton.setChecked(configs.GetUdpSwitch());
        udpToggleButton.setOnClickListener(v -> configs.SetUdpSwitch(udpToggleButton.isChecked()));

        RelativeLayout udpPortLayout = (RelativeLayout) findViewById(R.id.udpPortLayout);
        udpPortLayout.setOnClickListener(v -> {
            final EditText inputServer = new EditText(ctx);
            inputServer.setText(String.format("%d",configs.GetUdpPort()));
            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            builder.setTitle("输入UDP端口").setView(inputServer)
                    .setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
            builder.setPositiveButton("确定", (dialog, which) -> {
                String str = inputServer.getText().toString();
                int num = 0;
                try {
                    num = Integer.parseInt(str);
                    if(num > 1000 && num < 65535) {
                        configs.SetUdpPort(num);
                        Toast.makeText(ctx,"端口已设定", Toast.LENGTH_LONG).show();
                        return;
                    }
                    throw new Exception("端口过大或过小");
                } catch (Exception e) {
                    // 处理转换异常
                    Log.d(TAG, Objects.requireNonNull(e.getMessage()));
                    AlertDialog alertDialog = new AlertDialog.Builder(ctx)
                            .setTitle("端口不正确")
                            .setMessage("请重新输入端口 端口范围为1001 - 65534")
                            .setPositiveButton("好", (dialogInterface, i) -> {
                                //执行代码
                                dialogInterface.dismiss();//销毁对话框
                            })
                            .create();
                    alertDialog.show();
                }
            });
            builder.show();
        });
    }

    @Override
    public void onBackPressed(){
        finish();
    }
}