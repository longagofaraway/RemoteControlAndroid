package com.example.jura.myapplication;

import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jura on 07.07.2016.
 */
public class Http {

    static class HttpAnswer {
        String message;
        int code;
        HttpAnswer(int xcode, String xmessage) {
            code = xcode;
            message = xmessage;
        }
    }

    public static String readStream(InputStream is) {
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            int i = is.read();
            while (i != -1) {
                bo.write(i);
                i = is.read();
            }
            return bo.toString();
        } catch (IOException e) {
            return "";
        }
    }

    public static HttpAnswer getCookie(String url) {
        URL net_url;
        HttpURLConnection urlConnection;
        List<String> cookies;
        String cookie;
        List<String> res = new ArrayList<String>();

        try {
            net_url = new URL(url);

            urlConnection = (HttpURLConnection) net_url.openConnection();
            urlConnection.setConnectTimeout(10*1000);
            urlConnection.connect();
            cookies = urlConnection.getHeaderFields().get("Set-Cookie");
            urlConnection.disconnect();

            if (cookies == null)
                return new HttpAnswer(1, "cookies are null");

            if (cookies.isEmpty())
                return new HttpAnswer(1, "cookie is empty");

            cookie = cookies.get(0).split(";")[0];
            if ((cookie.length() < 5) && !cookie.substring(0,3).equals("user"))
                return new HttpAnswer(1, "malformed cookie");
        } catch (Exception e) {
            return new HttpAnswer(1, e.toString());
        }

        return new HttpAnswer(0, cookie);
    }

    public static HttpAnswer sendRequest(String url, String appCookie) {
        URL net_url;
        HttpURLConnection urlConnection;
        String s = "didn't trigger";
        int code = 0;

        try {
            net_url = new URL(url);
        } catch (MalformedURLException e) {
            return new HttpAnswer(1, e.toString());
        }

        try {
            urlConnection = (HttpURLConnection) net_url.openConnection();
            urlConnection.addRequestProperty("Cookie", appCookie);
            urlConnection.connect();
            code = urlConnection.getResponseCode();
            if (code == 401) {
                urlConnection.disconnect();
                return new HttpAnswer(code, "");
            }
        } catch (IOException e) {
            return new HttpAnswer(1, e.toString());
        }

        try {
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            s = readStream(in);
            //publishProgress(s);
            urlConnection.disconnect();
        } catch (IOException e) {
            urlConnection.disconnect();
            return new HttpAnswer(1, e.toString());
        }

        return new HttpAnswer(code, s);
    }
}
