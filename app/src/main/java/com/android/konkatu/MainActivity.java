package com.android.konkatu;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.HttpAuthHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.android.konkatu.util.IabHelper;
import com.android.konkatu.util.IabResult;
import com.android.konkatu.util.Inventory;
import com.android.konkatu.util.Purchase;

public class MainActivity extends ActionBarActivity {
    private final static String TAG = MainActivity.class.getSimpleName();
    protected static final String BASE_AUTH_USERNAME = "kon";
    protected static final String BASE_AUTH_PASSWORD = "katsu";
    private WebView webView;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        menu.add(0, 0, 0, "Google");
        menu.add(0, 1, 1, "IT TRICK");
        menu.add(0, 2, 2, "婚活メール");
        return true;
    }
    // オプションメニュー選択された場合、選択項目に合わせて
    // WebViewの表示先URLを変更する。
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        int itemId = item.getItemId();
        switch(itemId) {
            case 0:
                webView.loadUrl("http://www.google.co.jp/");
                break;
            case 1:
                webView.loadUrl("http://it-trick-java.appspot.com/");
                break;
            case 2:
                alert("婚活メール");
                webView.loadUrl("http://54.65.215.200/");
                break;
        }
        return true;
    }
    //課金クラス
    IabHelper mHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //ストアの公開鍵
        String base64EncodedPublicKey = "CONSTRUCT_YOUR_KEY_AND_PLACE_IT_HERE";
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //課金インスタンス作成
        mHelper = new IabHelper(this, base64EncodedPublicKey);
        // デバッグを有効にする場合(デフォルトは無効です)
        mHelper.enableDebugLogging(true);

        //ServiceConnectionの生成と IInAppBillingServiceのバインド
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    //失敗した時
                    return;
                }
                // 破棄されてれば、終了。
                if (mHelper == null) return;
                //成功時
                mHelper.queryInventoryAsync(mGotInventoryListener);
            }
        });

        webView = (WebView) findViewById(R.id.webView1);
        //loading
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Loading...");
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        WebViewClient webViewClient = new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                pd.show();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (pd.isShowing() && pd != null) {
                    pd.dismiss();
                }
            }
            @Override
            public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
                //base auth
                handler.proceed(BASE_AUTH_USERNAME, BASE_AUTH_PASSWORD);
            }
        };
        webView.setWebViewClient(webViewClient);
        webView.loadUrl("http://54.65.215.200/");
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            //　破棄されてれば、終了。
            if (mHelper == null) return;
            // エラー時のハンドリング
            if (result.isFailure()) {
                return;
            }
            // 成功
            String aPrice =
                    inventory.getSkuDetails("<em>サービスID_A</em>").getPrice();
            String bPrice =
                    inventory.getSkuDetails("<em>サービスID_B</em>").getPrice();

        }
    };
    void alert(String message) {
        AlertDialog.Builder bld = new AlertDialog.Builder(this);
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        Log.d(TAG, "Showing alert dialog: " + message);
        bld.create().show();
    }
}
