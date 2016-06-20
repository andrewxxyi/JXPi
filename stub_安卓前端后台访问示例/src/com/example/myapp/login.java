package com.example.myapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class login extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.login);

        IDo tip=new IDo() {
            @Override
            public void Do(Object o) throws Exception {
                Toast toast=Toast.makeText(getApplicationContext(), (String)o, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        };


        //httpClient.tip=tip;
        //jxHttpClient.dualError=tip;
        //jxHttpClient.dualNoResult=tip;
        //jxHttpClient.dualResultFormateError=tip;

        final EditText etName=(EditText) findViewById(R.id.txt_login_name);
        final EditText etPasswd=(EditText) findViewById(R.id.txt_login_passwd);


        final SharedPreferences pref =PreferenceManager.getDefaultSharedPreferences(this);
        final String serverip=pref.getString("serverIP_I",null);
        if(serverip!=null){
            etName.setText(pref.getString("UserName",null), TextView.BufferType.EDITABLE);
            etPasswd.setText(pref.getString("Passwd",null), TextView.BufferType.EDITABLE);
            httpClient.serverIP_I=pref.getString("serverIP_I",null);
            httpClient.serverIP_E=pref.getString("serverIP_E",null);
        }


        jxTitle title=(jxTitle)findViewById(R.id.login_title);
        title.setTitle("智能机电");
        title.setCancel(false, null);
        title.setAccept(false, null);
        title.setDiscard(false, null);
        title.setNew(false, null);
        title.setConf(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(login.this, conf.class);
                startActivity(intent);
            }
        });

        SharedPreferences.Editor editor = pref.edit();
        final Button button = (Button) findViewById(R.id.btn_login_ok);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                jxJson json= jxJson.GetObjectNode("param");
                try {
                    String name=etName.getText().toString();
                    String pass=etPasswd.getText().toString();

                    json.setSubObjectValue("Name",name);
                    json.setSubObjectValue("Passwd",pass);
                    jxJson rs = httpClient.login(json);
                    if(rs!=null){
                        //保存预输入数据
                        SharedPreferences.Editor editor=pref.edit();
                        editor.putString("UserName",name);
                        editor.putString("Passwd",pass);
                        editor.commit();
                        //启动主页面
                        Intent intent = new Intent(login.this, MainActivity.class);
                        startActivity(intent);
                    }
                    else{
                        Toast toast=Toast.makeText(getApplicationContext(), "用户无法登录，请检查用户名、密码是否正确", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast toast=Toast.makeText(getApplicationContext(), "用户无法登录，请检查网络连接是否正常", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }

            }
        });


    }




}
