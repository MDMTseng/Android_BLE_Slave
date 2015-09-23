package me.mamx.ble_slave;

import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * Created by ctseng on 9/21/15.
 */
public class WebUIMan {

    WebView webView=null;
    WebAppInterface jsIF=null;
    CommCH_IF BTCommIf=null;

    WebUIMan(WebView webView,String EntryUrl)
    {
        this.webView=webView;
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webView.setWebViewClient(new WebViewClient() {

            public void onPageFinished(WebView view, String url) {
                Log.v(this.getClass().getName(), "onPageFinished!!!");
               // notiTimerHandler.postDelayed(notiTimerRunnable, 2);
               //Message msg=msgHandler.obtainMessage();
               // msg.obj="Hello Web...";
                //msg.what=0;

               // msgHandler.sendMessage(msg);
                // do your stuff here
            }
        });
        webView.addJavascriptInterface(jsIF=new WebAppInterface(), "Android");


        webView.loadUrl(EntryUrl);



    }
    void setBTCommIf(CommCH_IF BTCommIf)
    {
        this.BTCommIf=BTCommIf;
    }

    CommCH_IF getCommIf()
    {
        return jsIF;
    }


    private Handler msgHandler = new Handler( ){
        @Override
        public void handleMessage(Message inputMessage) {

            if(inputMessage.what==0)
                webView.loadUrl("javascript:AndroidCall('"+(String)inputMessage.obj+"')");
        }
    };

    public void UIDataRecvCB(String type,JSONObject jsonData)
    {

    }

    public class WebAppInterface extends CommCH_IF {

        /** Instantiate the interface and set the context */
        WebAppInterface() {
        }



        @JavascriptInterface
        public void SendMsg2BT(String DATA) {
            Log.v(this.getClass().getName(),DATA);
            try {
                JSONObject jobj=new JSONObject(DATA);
                String dataType=jobj.optString("type");
                if(dataType==null||dataType.length()==0||dataType.contentEquals("gattInfo"))
                {
                    byte[] base64Datas = Base64.decode(jobj.getString("base64Data"),Base64.DEFAULT);
                    long uuid16=jobj.getLong("chid");
                    SendData(uuid16,base64Datas);
                }
                else{
                    UIDataRecvCB(dataType,jobj);
                }







            } catch (JSONException e) {
                e.printStackTrace();
            }

        }


        @Override
        public boolean RecvData(Object CH, byte[] data)
        {
            Long ln=(Long)CH;
            String str=Base64.encodeToString(data, Base64.DEFAULT);
            Log.v("CommCH_IF..RecvData", str);


            Message msg=msgHandler.obtainMessage();
            String jsonStr="{\"chid\":"+ln+",\"base64Data\":\""+str+"\"}";
            msg.obj=jsonStr;

            msg.what=0;

            msgHandler.sendMessage(msg);
            return true;
        }




        @Override
        public boolean SendData(Object CH,byte[] data){
            return BTCommIf.RecvData(CH,data);
        }
    }



    public boolean OnKeyDown(int keyCode, KeyEvent event)
    {
        if(event.getAction() == KeyEvent.ACTION_DOWN){
            switch(keyCode)
            {
                case KeyEvent.KEYCODE_BACK:
                    if(webView.canGoBack()){
                        webView.goBack();
                    }else{
                    }
                    return true;
            }

        }
        return false;
    }
}
