package com.example.mq661.govproject.repassword;



import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mq661.govproject.AlterRoom.addroom;
import com.example.mq661.govproject.AlterRoom.alterroom;
import com.example.mq661.govproject.Login_Register.saveinfo;
import com.example.mq661.govproject.Login_Register.savetoken;
import com.example.mq661.govproject.Login_Register.zhuce;
import com.example.mq661.govproject.R;
import com.example.mq661.govproject.mytoast.ToastUtil;
import com.example.mq661.govproject.mytoken.sqltoken;
import com.example.mq661.govproject.mytoken.tokenDBHelper;
import com.example.mq661.govproject.tools.TokenUtil;
import com.example.mq661.govproject.tools.tomd5;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class repassword extends AppCompatActivity implements View.OnClickListener {
    EditText edzhanghao,edmima,edremima;
    Button commit;
    private OkHttpClient okhttpClient;
    private String zhanghao,mima,remima;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.repassword_layout);
        initView();

    }

    private void initView() {

        edzhanghao = findViewById(R.id.zhanghao);
        edmima = findViewById(R.id.mima);
        edremima = findViewById(R.id.remima);
        commit = findViewById(R.id.commit);
        commit.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {

        zhanghao = edzhanghao.getText().toString().trim();
        mima = edmima.getText().toString().trim();
        remima = edremima.getText().toString().trim();



        if (TextUtils.isEmpty(zhanghao)) {
            Toast.makeText(this, "请输入员工号", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (TextUtils.isEmpty(mima)) {
            Toast.makeText(this, "请输入新密码", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (TextUtils.isEmpty(remima)) {
            Toast.makeText(this, "请重复输入新密码", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (!mima.equals(remima)) {
            Toast.makeText(this, "两次输入密码不一致", Toast.LENGTH_SHORT).show();
            return;
        }



        new Thread(new Runnable() {
            @Override
            public void run() {
                sendRequest(zhanghao,mima,remima);
            }
        }).start();
    }

    private void sendRequest(String zhanghao1,String mima1,String remima1) {
        Map map = new HashMap();
        map.put("EmployeeNumber", zhanghao1);
        map.put("newPassword", mima1);

        JSONObject jsonObject = new JSONObject(map);
        String jsonString = jsonObject.toString();
        RequestBody body = RequestBody.create(null, jsonString);  //以字符串方式
        okhttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                //dafeng 192.168.2.176
                //  .url("http://192.168.2.176:8080/LoginProject/login")
                // .url("http://192.168.43.174:8080/LoginProject/login")
                // .url("http://39.96.68.13:8080/SmartRoom/LoginServlet")
                .url("http://39.96.68.13:8080/SmartRoom/ResetPasswordServlet")//MQ
                // .url("http://192.168.2.176:8080/SmartRoom/login")
                .post(body)
                .build();
        okhttp3.Call call = okhttpClient.newCall(request);
        call.enqueue(new Callback() {

            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(repassword.this, "连接服务器失败！", Toast.LENGTH_SHORT).show();
                    }
                });
                e.printStackTrace();
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {

                String res = response.body().string();//获取到传过来的字符串
                try {
                    JSONObject jsonObj = new JSONObject(res);
                    String status = jsonObj.getString("status");

                    showRequestResult(status);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private void showRequestResult(final String status) {
        runOnUiThread(new Runnable() {
                          @Override
                          /**
                           * 实时更新，数据库信息改变时，客户端内容发生改变
                           */
                          public void run() {
                              if (status.equals("-1")) {
                                  Toast.makeText(repassword.this, "修改失败！", Toast.LENGTH_SHORT).show();
                              } else if (status.equals("0")) {
                                  Toast.makeText(repassword.this, "修改成功！", Toast.LENGTH_LONG).show();

                              }
                          }
                      }
        );
    }

}


