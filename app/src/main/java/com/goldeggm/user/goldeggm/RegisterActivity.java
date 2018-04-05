package com.goldeggm.user.goldeggm;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

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

public class RegisterActivity extends AppCompatActivity {

    private AlertDialog dialog;
    private String JSON_STRING;
    String json_string, error, success;
    static JSONArray jsonArray;
    static JSONObject jsonObject;
    private EditText idText, passwordText, confirm_pwd, phoneText, emailText;
    String userId, userPwd, userConfirmPwd, userPhone, userEmail, userHp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Button signUp = (Button) findViewById(R.id.sign_up);

        idText = (EditText) findViewById(R.id.idText);
        passwordText = (EditText) findViewById(R.id.passwordText);
        confirm_pwd = (EditText) findViewById(R.id.confirm_pwd);
        phoneText = (EditText) findViewById(R.id.phoneText);
        emailText = (EditText) findViewById(R.id.emailText);

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userId = idText.getText().toString();
                userPwd = passwordText.getText().toString();
                userConfirmPwd = confirm_pwd.getText().toString();
                userPhone = phoneText.getText().toString();
                userEmail = emailText.getText().toString();
                userHp = getIntent().getStringExtra("userHp");

                new JsonExpertPage().execute();

                if (success == "true") {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("로그인에 성공했습니다.")
                            .create();
                    dialog.show();

                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    intent.putExtra("userId", userId);
                    intent.putExtra("userPwd", userPwd);
                    intent.putExtra("userConfirmPwd", userConfirmPwd);
                    intent.putExtra("userPhone", userPhone);
                    intent.putExtra("userEmail", userEmail);
                    intent.putExtra("userHp", userHp);

                    RegisterActivity.this.startActivity(intent);

                } else if (success == "false") {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage(error)
                            .setPositiveButton("다시시도", null)
                            .create();
                    dialog.show();
                }
            }
        });
    }
    class JsonExpertPage extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {

            String json_url = "http://13.125.147.26/phps/join?id=" + userId + "&pwd=" + userPwd + "&pwd_confirmation=" + userConfirmPwd + "&phone=" + userPhone + "&email=" + userEmail + "&hp=" + userHp;

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
                    success = jsonObject.getString("result");
                    error = jsonObject.getString("error");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
