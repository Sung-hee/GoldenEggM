package com.goldeggm.user.goldeggm;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {
    private AlertDialog dialog;
    private String JSON_STRING;
    TelephonyManager tm;
    public String id, pw, hp, json_string, error, results;

    public SharedPreferences setting;
    public SharedPreferences.Editor editor;

    static JSONArray jsonArray;
    static JSONObject jsonObject;

    private EditText idText, passwordText;
    private TextView findIdPasswordText;
    private Button existingMemberYButton, existingMemberNButton;
    private Button registerButton, loginButton;
    private CheckBox autoLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        findIdPasswordText = (TextView) findViewById(R.id.findIdPasswordText);

        existingMemberYButton = (Button) findViewById(R.id.existingMemberYButton);
        existingMemberNButton = (Button) findViewById(R.id.existingMemberNButton);
        loginButton = (Button) findViewById(R.id.loginButton);
        registerButton = (Button) findViewById(R.id.reisterButton);

        idText = (EditText) findViewById(R.id.idText);
        passwordText = (EditText) findViewById(R.id.passwordText);

        autoLogin = (CheckBox)  findViewById(R.id.autoLoginCheck);

        tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

        id = idText.getText().toString();
        pw = passwordText.getText().toString();
        hp = getIntent().getStringExtra("userHp");

        registerButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                registerIntent.putExtra("userHp", hp);
                LoginActivity.this.startActivity(registerIntent);
            }
        });


        setting = getSharedPreferences("setting", Activity.MODE_PRIVATE);
        editor = setting.edit();

        if(setting.getBoolean("autoLogin", false)){
            idText.setText(setting.getString("userId", ""));
            passwordText.setText(setting.getString("userPwd", ""));
            hp = setting.getString("userHp", "");
            autoLogin.setChecked(true);
        }

        if(autoLogin.isChecked()) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("userId", id);
            intent.putExtra("userPwd", pw);
            intent.putExtra("userHp", hp);
            LoginActivity.this.startActivity(intent);
            finish();
        }
        else {
            // 로그인
            loginButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    id = idText.getText().toString();
                    pw = passwordText.getText().toString();
                    if (id.equals("") || id.length() == 0) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                        dialog = builder.setMessage("아이디를 입력해주세요.")
                                .setPositiveButton("확인", null)
                                .create();
                        dialog.show();
                        return;
                    }
                    else if (pw.equals("") || pw.length() == 0) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                        dialog = builder.setMessage("비밀번호를 입력해주세요.")
                                .setPositiveButton("확인", null)
                                .create();
                        dialog.show();
                        return;
                    }
                    new JsonExpertPage().execute();
                }
            });

            // 아이디 비번 찾기
            findIdPasswordText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    dialog = builder.setMessage("아이디 비밀번호 찾기 확인")
                            .setPositiveButton("확인", null)
                            .create();
                    dialog.show();
                }
            });
        }
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        switch(view.getId()) {
            case R.id.existingMemberYButton:
                if (checked) {
                    idText.setText("");
                    idText.setHint("필명");
                }
                break;
            case R.id.existingMemberNButton:
                if (checked) {
                    idText.setText("");
                    idText.setHint("아이디");
                }
                    break;
        }
    }

    class JsonExpertPage extends AsyncTask <String, Void, String>{
        @Override
        protected String doInBackground(String... strings) {
            String json_url = "http:/13.125.147.26/phps/login?id=" + id + "&pwd=" + pw + "&hp=" + hp;

            try {
                URL url = new URL(json_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();

                while ((JSON_STRING = bufferedReader.readLine()) != null) {
                    stringBuilder.append(JSON_STRING + "\n");
                }

                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();

                return stringBuilder.toString().trim();

            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            json_string = result;
            try {
                jsonArray = new JSONArray(json_string);

                for(int i=0; i < jsonArray.length(); i++){
                    jsonObject = jsonArray.getJSONObject(i);
                    results = jsonObject.getString("result");
                    if (!jsonObject.isNull("error")) {
                        error = jsonObject.getString("error");
                    }

                    if(results == "true") {
                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                        dialog = builder.setMessage("로그인에 성공했습니다.")
                                .create();
                        dialog.show();

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);

                        intent.putExtra("userId", id);
                        intent.putExtra("userPwd", pw);
                        intent.putExtra("userHp", hp);
                        if(autoLogin.isChecked()){
                            editor.putString("userId", id);
                            editor.putString("userPwd", pw);
                            editor.putString("userHp", hp);
                            editor.putBoolean("autoLogin", true);
                            editor.commit();
                        }
                        else {
                            editor.clear();
                            editor.commit();
                        }

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable(){
                            public void run(){
                                dialog.dismiss();
                            }
                        }, 500);
                        LoginActivity.this.startActivity(intent);
                        finish();
                    }
                    else if(results == "false"){
                        if (error.contains("처음")) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                            dialog = builder.setMessage(error)
                                    .setPositiveButton("다시시도", null)
                                    .create();
                            dialog.show();
                        }
                        else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                            dialog = builder.setMessage("아이디 혹은 비밀번호가 틀렸습니다.")
                                    .setPositiveButton("다시시도", null)
                                    .create();
                            dialog.show();
                        }

                        if(autoLogin.isChecked()){
                            editor.putString("userId", id);
                            editor.putString("userPwd", pw);
                            editor.putString("userHp", hp);
                            editor.putBoolean("autoLogin", true);
                            editor.commit();
                        }
                        else {
                            editor.clear();
                            editor.commit();
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }
}
