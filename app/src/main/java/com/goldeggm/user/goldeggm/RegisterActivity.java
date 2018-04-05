package com.goldeggm.user.goldeggm;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
    String userId, userPwd, user_nick, user_nickPass, user_confirm_pwd, user_phoneText, user_emailText;
    static JSONArray jsonArray;
    static JSONObject jsonObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Button signUp = (Button) findViewById(R.id.sign_up);

        final EditText idText = (EditText) findViewById(R.id.idText);
        final EditText passwordText = (EditText) findViewById(R.id.passwordText);
        final EditText confirm_pwd = (EditText) findViewById(R.id.confirm_pwd);
        final EditText phoneText = (EditText) findViewById(R.id.phoneText);
        final EditText emailText = (EditText) findViewById(R.id.emailText);

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = idText.getText().toString();
                String userPwd = passwordText.getText().toString();
                String user_confirm_pwd = confirm_pwd.getText().toString();
                String user_phoneText = phoneText.getText().toString();
                String user_emailText = emailText.getText().toString();

                new JsonExpertPage().execute();

                if (success == "true") {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("로그인에 성공했습니다.")
                            .setPositiveButton("확인", null)
                            .create();
                    dialog.show();

                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    intent.putExtra("user_nick", user_nick);
                    intent.putExtra("user_nickPass", user_nickPass);
                    intent.putExtra("userId", userId);
                    intent.putExtra("userPwd", userPwd);
                    intent.putExtra("user_confirm_pwd", user_confirm_pwd);
                    intent.putExtra("user_phoneText", user_phoneText);
                    intent.putExtra("user_emailText", user_emailText);

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

        final EditText idText = (EditText) findViewById(R.id.idText);
        final EditText passwordText = (EditText) findViewById(R.id.passwordText);
        final EditText confirm_pwd = (EditText) findViewById(R.id.confirm_pwd);
        final EditText phoneText = (EditText) findViewById(R.id.phoneText);
        final EditText emailText = (EditText) findViewById(R.id.emailText);

        @Override
        protected String doInBackground(String... strings) {
            String userId = idText.getText().toString();
            String userPwd = passwordText.getText().toString();
            String user_confirm_pwd = confirm_pwd.getText().toString();
            String user_phoneText = phoneText.getText().toString();
            String user_emailText = emailText.getText().toString();

            String json_url = "http://61.72.187.6/phps/join.php?nickname=" + user_nick + "&nick_pass=" + user_nickPass + "&id=" + userId + "&pwd=" + userPwd + "&pwd_confirmation=" + user_confirm_pwd + "&phone=" + user_phoneText + "&email=" + user_emailText;

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
