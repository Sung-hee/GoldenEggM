package com.goldeggm.user.goldeggm;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    boolean doubleBackToExitPressedOnce = false;
    private Button logout;
    private WebView mWebView;
    private String myUrl = "http://13.125.213.27:3000/users/sign_in"; // 접속 URL (내장HTML의 경우 왼쪽과 같이 쓰고 아니면 걍 URL

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        else {
            if (mWebView.canGoBack()) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mWebView.goBack();
                    }
                }, 100);
            }

            else {
                this.doubleBackToExitPressedOnce = true;
                Toast.makeText(this, "뒤로 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        doubleBackToExitPressedOnce=false;
                    }
                }, 2000);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Intent intent = getIntent();

        String userId = intent.getStringExtra("userId");
        String userPwd = intent.getStringExtra("userPwd");
        String userHp = intent.getStringExtra("userHp");

        //웹뷰 셋팅
        mWebView = (WebView) findViewById(R.id.webView);
        mWebView.getSettings().setJavaScriptEnabled(true);
        logout = (Button) findViewById(R.id.button);

        mWebView.loadUrl(myUrl + "?id=" + userId + "&pwd=" + userPwd + "&hp=" + userHp); // 접속 URL

        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.setWebViewClient(new WebViewClientClass());

        logout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            MainActivity.this.startActivity(loginIntent);
            finish();
            }
        });
    }

    private class WebViewClientClass extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d("check URL",url);
            view.loadUrl(url);
            return true;
        }
    }
}
