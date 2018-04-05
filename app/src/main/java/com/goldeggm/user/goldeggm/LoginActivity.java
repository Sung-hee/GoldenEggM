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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION = 10;

    private AlertDialog dialog;
    private String JSON_STRING;
    String id, pwd, json_string, error, results;

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

        grantExternalStoragePermission();

        findIdPasswordText = (TextView) findViewById(R.id.findIdPasswordText);

        existingMemberYButton = (Button) findViewById(R.id.existingMemberYButton);
        existingMemberNButton = (Button) findViewById(R.id.existingMemberNButton);
        loginButton = (Button) findViewById(R.id.loginButton);
        registerButton = (Button) findViewById(R.id.reisterButton);

        idText = (EditText) findViewById(R.id.idText);
        passwordText = (EditText) findViewById(R.id.passwordText);

        autoLogin = (CheckBox)  findViewById(R.id.autoLoginCheck);

        registerButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                LoginActivity.this.startActivity(registerIntent);
            }
        });


        setting = getSharedPreferences("setting", Activity.MODE_PRIVATE);
        editor = setting.edit();

        if(setting.getBoolean("autoLogin", false)){
            idText.setText(setting.getString("userId", ""));
            passwordText.setText(setting.getString("userPwd", ""));
            autoLogin.setChecked(true);
        }

        String loginId = setting.getString("inputId", null);
        String loginPwd = setting.getString("inputPwd", null);
        String userId = idText.getText().toString();
        String userPwd = passwordText.getText().toString();

        if(autoLogin.isChecked()) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("userPwd", userPwd);
            LoginActivity.this.startActivity(intent);
            finish();
        }
        else {
            // 로그인
            loginButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {

                    String userId = idText.getText().toString();
                    String userPwd = passwordText.getText().toString();

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        switch (requestCode) {
//            case 1 :
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    //권한 허가
//                    //해당 권한을 사용해서 작업을 진행할 수 있습니다
//                    Toast.makeText(this,"확인", Toast.LENGTH_SHORT).show();
//                }
//                else {
//                    //권한 거부
//                    // 사용자가 해당 권한을 거부했을 때 해주어야 할 동작을 수행합니다.
//                    Toast.makeText(this,"취소", Toast.LENGTH_SHORT).show();
//                }
//                return;
//        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (Build.VERSION.SDK_INT >= 23) {
            if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "External Storage Permission is Grant", Toast.LENGTH_SHORT).show();
                Log.v("tag","Permission: "+permissions[0]+ "was "+grantResults[0]);
                //resume tasks needing this permission
            }
        }
    }

    private boolean grantExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            int permissionCheck = checkSelfPermission(Manifest.permission.READ_PHONE_NUMBERS);

            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                return true;
            }
            else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_NUMBERS}, 1);
                return false;
            }
        }
        else {
            Toast.makeText(this, "External Storage Permission is Grant", Toast.LENGTH_SHORT).show();
            return true;
        }
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        switch(view.getId()) {
            case R.id.existingMemberYButton:
                if (checked) {
                    idText.setText("");
                    idText.setHint("필명");
//                    idText.setInputType(InputType.TYPE_CLASS_TEXT);
//                    idText.setKeyListener(DigitsKeyListener.getInstance(false, true));
                }
                break;
            case R.id.existingMemberNButton:
                if (checked) {
                    idText.setText("");
                    idText.setHint("아이디");
//                    idText.setKeyListener(DigitsKeyListener.getInstance());
                }
                    break;
        }
    }

    class JsonExpertPage extends AsyncTask <String, Void, String>{
        @Override
        protected String doInBackground(String... strings) {
            String userId = idText.getText().toString();
            String userPwd = passwordText.getText().toString();
            String json_url = "http://61.72.187.6/phps/login.php?id=" + userId + "&pwd=" + userPwd;

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

                    String userId = idText.getText().toString();
                    String userPwd = passwordText.getText().toString();

                    if(results == "true") {
                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                        dialog = builder.setMessage("로그인에 성공했습니다.")
                                .create();
                        dialog.show();

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);

                        intent.putExtra("userId", userId);
                        intent.putExtra("userPwd", userPwd);
                        if(autoLogin.isChecked()){
                            editor.putString("userId", userId);
                            editor.putString("userPwd", userPwd);
                            editor.putBoolean("autoLogin", true);
                            editor.commit();
                        }
                        else {
                            editor.clear();
                            editor.commit();
                        }
                        LoginActivity.this.startActivity(intent);
                        finish();
                    }
                    else if(results == "false"){
                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                        dialog = builder.setMessage("아이디 혹은 비밀번호가 틀렸습니다.")
                                .setPositiveButton("다시시도", null)
                                .create();
                        dialog.show();

                        if(autoLogin.isChecked()){
                            editor.putString("userId", userId);
                            editor.putString("userPwd", userPwd);
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
