package com.example.mq661.govproject.BookRoom;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mq661.govproject.Login_Register.Login_noToken;
import com.example.mq661.govproject.Participants.addPerson_handler;
import com.example.mq661.govproject.R;
import com.example.mq661.govproject.SearchRoom.roomAdapterInfo;
import com.example.mq661.govproject.tools.RoomMessage;
import com.example.mq661.govproject.tools.dateToString;
import com.example.mq661.govproject.tools.saveDeviceInfo;
import com.example.mq661.govproject.tools.tokenDBHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class smartbook extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, AdapterView.OnItemSelectedListener {
    EditText size;
    Spinner functions, length;
    Button commit;
    ArrayList<RoomMessage> RoomMessages;
    Intent ssdata = new Intent();
    private String ssBuildingNumber, ssRoomNumber, ssTime, ssSize, ssFunction, ssIsMeeting, ssDays, size1, functions1, Functions, IsMeeting2, Length, Length1;
    private List<roomAdapterInfo> data;
    private long lastClickTime = 0;
    private OkHttpClient okhttpClient;
    private tokenDBHelper helper;
    private String Token1;
    private ListView searchroomlv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.smartbook_lv_layout);
        helper = new tokenDBHelper(this);
        initView();

    }

    private void initView() {

        Token1 = select();
        size = findViewById(R.id.Size);
        functions = findViewById(R.id.functions);
        length = findViewById(R.id.length);
        searchroomlv = findViewById(R.id.searchroomlv);
        length.setOnItemSelectedListener(this);
        functions.setOnItemSelectedListener(this);
        commit = findViewById(R.id.commit);
        commit.setOnClickListener(this);

        searchroomlv.setOnItemClickListener(this);       //设置短按事件
        searchroomlv.setOnItemLongClickListener(this);   //设置长按事件

    }

    @Override
    public void onClick(View v) {


        size1 = size.getText().toString();
        if (TextUtils.isEmpty(size.getText())) {
            size1 = "0";
        }
        functions1 = Functions;
        Length1 = Length;
        if (size1.equals("0") && functions1.equals("") && Length1.equals("")) {
            Toast.makeText(this, "请至少选择填写一项", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "开始查询，请稍后！长按可快速预约！", Toast.LENGTH_SHORT).show();
            data = new ArrayList<roomAdapterInfo>();

            new Thread(new Runnable() {
                @Override
                public void run() {

                    long now = System.currentTimeMillis();
                    if (now - lastClickTime > 1500) {
                        lastClickTime = now;
                        Log.e("aaa", "允许单次点击!!!");
                        //防止短时间多次点按
                        sendRequest(Token1);
                    } else Log.e("aaa", "阻止重复点击!!!");
                }
            }).start();
        }

    }

    // }
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
//        ssBuildingNumber=data.get(position).getBuildingNumber();
//        ssSize=data.get(position).getSize();
//        ssRoomNumber=data.get(position).getRoomNumber();
//        ssTime=data.get(position).getTime();
//        ssFunction=data.get(position).getFunction();
//        ssIsMeeting=data.get(position).getIsMeeting();
//        ssDays=data.get(position).getDays();
//
//        Toast.makeText(this, "短按显示", Toast.LENGTH_LONG).show();
//        ssdata.putExtra("BuildingNumber", ssBuildingNumber);
//        ssdata.putExtra("Size", ssSize);
//        ssdata.putExtra("RoomNumber", ssRoomNumber);
//        ssdata.putExtra("Time", ssTime);
//        ssdata.putExtra("Function", ssFunction);
//        ssdata.putExtra("IsMeeting", ssIsMeeting);
//        ssdata.putExtra("IsMeeting", ssDays);
//        setResult(1, ssdata);
//        finish();
    }

    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        ssBuildingNumber = data.get(position).getBuildingNumber();
        ssSize = data.get(position).getSize();
        ssRoomNumber = data.get(position).getRoomNumber();
        ssTime = data.get(position).getTime();
        ssFunction = data.get(position).getFunction();
        ssIsMeeting = data.get(position).getIsMeeting();
        ssDays = data.get(position).getDays();
        showMultiBtnDialog(ssBuildingNumber, ssSize, ssRoomNumber, ssTime, ssFunction, ssIsMeeting, ssDays);
        return true;      //返回true时可以解除长按与短按的冲突。

    }


    private void sendRequest(String Token1) {
        Map map = new HashMap();
        map.put("Token", Token1);
        map.put("Size", size1);
        map.put("Functions", functions1);
        map.put("Hours", Length1);

        JSONObject jsonObject = new JSONObject(map);
        String jsonString = jsonObject.toString();
        RequestBody body = RequestBody.create(null, jsonString);//以字符串方式
        okhttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(260, TimeUnit.SECONDS)//设置连接超时时间
                .readTimeout(260, TimeUnit.SECONDS)//设置读取超时时间
                .writeTimeout(20, TimeUnit.SECONDS)//设置写的超时时间
                .build();
        final Request request = new Request.Builder()
                .url("http://39.96.68.13:8080/SmartRoom/SmartBookServlet")
                .post(body)
                .build();

        Call call = okhttpClient.newCall(request);

        call.enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(smartbook.this, "连接服务器失败！", Toast.LENGTH_SHORT).show();
                    }
                });
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String res = response.body().string();//获取到传过来的字符串
                try {

                    JSONArray jsonArray = new JSONArray(res);
                    Log.d("nn", "jsonArray.length()  " + jsonArray.length());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObj = jsonArray.getJSONObject(i);

                        String BuildingNumber1 = jsonObj.getString("buildingNumber");
                        Log.d("nn", "BuildingNumber1   " + i + "    " + BuildingNumber1);
                        String RoomNumber1 = jsonObj.getString("roomNumber");
                        String Time1 = jsonObj.getString("time");
                        String Size1 = jsonObj.getString("size");
                        String Function1 = jsonObj.getString("functions");
                        String IsMeeting = jsonObj.getString("isMeeting");
                        if (IsMeeting.equals("0")) {
                            IsMeeting2 = "空闲";
                        } else if (IsMeeting.equals("1")) {
                            IsMeeting2 = "占用";
                        } else if (IsMeeting.equals("2")) {
                            IsMeeting2 = "维修";
                        } else {
                            IsMeeting2 = "未知";
                        }

                        String Days = jsonObj.getString("days");
                        Log.d("ccc", "楼号   " + BuildingNumber1 + RoomNumber1 + Time1);
                        String mapx = "map" + i;
                        if (BuildingNumber1.equals("-1") && RoomNumber1.equals("-1") && Time1.equals("-1")) {
                            Log.d("ccc", "跳出循环     因为 -1");
                            showRequestResult(BuildingNumber1, RoomNumber1, Time1, Size1, Function1, IsMeeting2, Days, mapx);
                            break;
                        } else if (BuildingNumber1.equals("-2") && RoomNumber1.equals("-2") && Time1.equals("-2")) {
                            showRequestResult(BuildingNumber1, RoomNumber1, Time1, Size1, Function1, IsMeeting2, Days, mapx);
                            break;
                        } else if (BuildingNumber1.equals("-3") && RoomNumber1.equals("-3") && Time1.equals("-3")) {
                            showRequestResult(BuildingNumber1, RoomNumber1, Time1, Size1, Function1, IsMeeting2, Days, mapx);
                            break;
                        } else if (Integer.parseInt(Time1.substring(0, 2)) <= Integer.parseInt(dateToString.nowdateToString3()) && Integer.parseInt(dateToString.nowdateToString4()) == Integer.parseInt(Days.substring(8, 10))) {
                            continue;
                        } else
                            showRequestResult(BuildingNumber1, RoomNumber1, Time1, Size1, Function1, IsMeeting2, Days, mapx);
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private void showRequestResult(final String BuildNumber1, final String RoomNumber1, final String Time1, final String Size1, final String Function1, final String IsMeeting1, final String Days1, final String mapx) {
        runOnUiThread(new Runnable() {
            @Override
            /**
             * 实时更新，数据库信息改变时，客户端内容发生改变
             */
            public void run() {

                if (BuildNumber1.equals("-1") && RoomNumber1.equals("-1") && Time1.equals("-1")) {

                    AlertDialog.Builder normalDialog =
                            new AlertDialog.Builder(smartbook.this);
                    normalDialog.setIcon(R.drawable.icon2);
                    normalDialog.setTitle("智能预约").setMessage("没有符合筛选条件的房间！");
                    normalDialog.show();


                    //Toast.makeText(smartbook.this, "没有符合筛选条件的房间！", Toast.LENGTH_SHORT).show();
                } else if (BuildNumber1.equals("-2") && RoomNumber1.equals("-2") && Time1.equals("-2")) {
                    AlertDialog.Builder normalDialog =
                            new AlertDialog.Builder(smartbook.this);
                    normalDialog.setIcon(R.drawable.icon2);
                    normalDialog.setTitle("智能预约").setMessage("请输入正确容量！！");
                    normalDialog.show();

                } else if (BuildNumber1.equals("-3") && RoomNumber1.equals("-3") && Time1.equals("-3")) {
//                    AlertDialog.Builder normalDialog =
//                            new AlertDialog.Builder(smartbook.this);
//                    normalDialog.setIcon(R.drawable.icon2);
//                    normalDialog.setTitle("智能预约").setMessage("认证信息失效，请重新登录！！");
//                    normalDialog.show();
                    delete(Token1);
                    saveDeviceInfo.savelogin(getApplicationContext(), "0");
                    relog();
                } else {
                    roomAdapterInfo mapx = new roomAdapterInfo();
                    mapx.setBuildingNumber(BuildNumber1);
                    mapx.setRoomNumber(RoomNumber1);
                    mapx.setFunction(Function1);
                    mapx.setSize(Size1);
                    mapx.setTime(Time1);
                    mapx.setIsMeeting(IsMeeting1);
                    mapx.setDays(Days1);
                    data.add(mapx);
                    searchroomlv.setAdapter(new smartbook.MyAdapter());
                }
            }
        });
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

//    public void select(View view) {
//        Intent intent=new Intent(this,tosmartroom.class);
//        startActivityForResult(intent,1);
//
//    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String content = parent.getItemAtPosition(position).toString();
        switch (parent.getId()) {
            case R.id.functions:
                if (content.equals("多媒体房间")) {
                    //Toast.makeText(smartbook.this, "选择的功能是：" + content,
                    //            Toast.LENGTH_SHORT).show();
                    Functions = content;
                } else if (content.equals("普通房间")) {
                    //     Toast.makeText(smartbook.this, "选择的功能是：" + content,
                    //       Toast.LENGTH_SHORT).show();
                    Functions = content;
                } else if (content.equals("无特殊要求")) {
                    //  Toast.makeText(smartbook.this, "选择的功能是：" + content,
                    //          Toast.LENGTH_SHORT).show();
                    Functions = "";
                }

                break;
            case R.id.length:
                if (content.equals("1小时")) {
                    //  Toast.makeText(smartbook.this, "选择的时长是：" + content,
                    //         Toast.LENGTH_SHORT).show();
                    Length = content.substring(0, 1);
                } else if (content.equals("2小时")) {
                    //Toast.makeText(smartbook.this, "选择的时长是：" + content,
                    //      Toast.LENGTH_SHORT).show();
                    Length = content.substring(0, 1);
                } else if (content.equals("3小时")) {
                    //Toast.makeText(smartbook.this, "选择的时长是：" + content,
                    //      Toast.LENGTH_SHORT).show();
                    Length = content.substring(0, 1);
                } else if (content.equals("4小时")) {
                    //Toast.makeText(smartbook.this, "选择的时长是：" + content,
                    //      Toast.LENGTH_SHORT).show();
                    Length = content.substring(0, 1);
                } else if (content.equals("无特殊要求")) {
                    //Toast.makeText(smartbook.this, "选择的时长是：" + content,
                    //      Toast.LENGTH_SHORT).show();
                    Length = "";
                }

                break;
            default:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    /* @setNeutralButton 设置中间的按钮
     * 若只需一个按钮，仅设置 setPositiveButton 即可
     */
    public void showMultiBtnDialog(final String BuildingNumber, String Size, final String RoomNumber,
                                   final String Time, String Function, final String IsMeeting, final String Days) {


        AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(smartbook.this);
        normalDialog.setIcon(R.drawable.book2);
        normalDialog.setTitle("GoV").setMessage("房间信息：\n" + "楼号：" + BuildingNumber + " 房间号：" + RoomNumber + " 容量：" + Size + "\n时间段：" + Time + "    功能：" + Function + "\n是否开会：" + IsMeeting
                + "       日期： " + Days
        );

        normalDialog.setPositiveButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        normalDialog.setNegativeButton("预约", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Intent intent = new Intent(smartbook.this, addPerson_handler.class);
                intent.putExtra("BuildingNumber", BuildingNumber);
                intent.putExtra("RoomNumber", RoomNumber);
                intent.putExtra("Days", Days);
                intent.putExtra("Time", Time);
                startActivity(intent);
            }
        });

        // 创建实例并显示
        normalDialog.show();
    }

    public void bookroom() {
        Intent intent;
        intent = new Intent(this, bookroom.class);
        startActivityForResult(intent, 0);

        // finish();
    }

    public void insert(String token) {


        //自定义增加数据
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        //String token =mytoken.getMytoken();

        values.put("token", token);
        long l = db.insert("token", null, values);


        db.close();
    }

    public void update(String token) {
        //自定义更新
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        //     String oldtoken=mytoken.getMytoken();
        values.put("token", token);
//        int i = db.update("token", values, "token=?",new String[]{oldtoken});
        int i = db.update("token", values, null, null);
        if (i == 0) {
            Toast.makeText(this, "更新不成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "更新成功" + i, Toast.LENGTH_SHORT).show();
        }
        db.close();
    }

    public void delete(String token) {

        SQLiteDatabase db = helper.getWritableDatabase();


        int i = db.delete("token", "token=?", new String[]{token});
//        if (i == 0) {
//            Toast.makeText(this, "删除不成功", Toast.LENGTH_SHORT).show();
//        } else {
//
//        }
        db.close();

    }

    //查找
    public String select() {

        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from token", null);
        String token1 = null;
        while (cursor.moveToNext()) {
            token1 = cursor.getString(0);
        }
        db.close();
        return token1;
    }

    public void relog() {

        Toast.makeText(this, "认证信息失效，请重新登录！", Toast.LENGTH_LONG).show();
        Intent intent;
        intent = new Intent(this, Login_noToken.class);
        startActivity(intent);
        finish();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        size1 = data.getStringExtra("Size");
        functions1 = data.getStringExtra("Functions");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        //非默认值
        if (newConfig.fontScale != 1) {
            getResources();
        }
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public Resources getResources() {//还原字体大小
        Resources res = super.getResources();
        //非默认值
        if (res.getConfiguration().fontScale != 1) {
            Configuration newConfig = new Configuration();
            newConfig.setToDefaults();//设置默认
            res.updateConfiguration(newConfig, res.getDisplayMetrics());
        }
        return res;
    }

    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return data.size();

        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view = View.inflate(smartbook.this, R.layout.wty_searchroom_adp_layout, null);


            TextView BuildingNumber = view.findViewById(R.id.BuildNumber);
            TextView RoomNumber = view.findViewById(R.id.RoomNumber);
            TextView Time = view.findViewById(R.id.Time);
            TextView Size = view.findViewById(R.id.Size);
            TextView Function = view.findViewById(R.id.Function);
            TextView IsMeeting = view.findViewById(R.id.IsMeeting);
            TextView Days = view.findViewById(R.id.Days3);

            BuildingNumber.setText(data.get(position).getBuildingNumber());
            Size.setText(data.get(position).getSize());
            RoomNumber.setText(data.get(position).getRoomNumber());
            Time.setText(data.get(position).getTime());
            Function.setText(data.get(position).getFunction());
            IsMeeting.setText(data.get(position).getIsMeeting());
            Days.setText(data.get(position).getDays());
            return view;
        }

//        @Override
//        public void notifyDataSetInvalidated() {
//            super.notifyDataSetInvalidated();
//        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
        }
    }

    //    public static boolean isFastDoubleClick() {
//        long time = SystemClock.uptimeMillis(); // 避免按钮短时间多次点按
//        if (time - lastClickTime < 400) {
//            return true;
//        }
//        lastClickTime = time;
//        Log.d("aaa","阻止持续点按" );
//        return false;
//    }

}

