package com.example.jura.myapplication;

import android.os.AsyncTask;
import android.os.SystemClock;

import java.util.List;
import java.util.ArrayList;

import java.io.InputStream;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;

import java.net.URL;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;
/**
 * Created by Jura on 11.06.2016.
 */
public class AsyncTasks {
    final static int SERVER_CODE_LIST = 1;
    final static int SERVER_CODE_DURATION = 2;
    final static int SERVER_CODE_POSITION = 3;
    final static int SERVER_CODE_PLAYBACK = 4;

    public static class HttpGetCookie extends AsyncTask<Object, String, Http.HttpAnswer> {
        JuraActivity mainClass;

        @Override
        protected Http.HttpAnswer doInBackground(Object... params) {
            String url = (String) params[0];
            mainClass = (JuraActivity) params[1];

            return Http.getCookie(url);
        }

        protected void onPostExecute(Http.HttpAnswer result) {
            mainClass.setWorkStatus(false);
            if (result.code == 1) {
                //some error occurred
                mainClass.setDebugView("HttpGetCookie exception: "+result.message);
                return;
            }
            mainClass.setCookie(result.message);
            //got cookie, now get root dir
            mainClass.startAsyncTask("app");
        }
    }

    public static class HttpGet extends AsyncTask<Object, String, Http.HttpAnswer> {
        JuraActivity mainClass;

        @Override
        protected Http.HttpAnswer doInBackground(Object... params) {
            String url = (String)params[0];
            String appCookie = (String)params[1];
            mainClass = (JuraActivity) params[2];

            return Http.sendRequest(url, appCookie);
        }
        /*protected void onProgressUpdate(String... progress) {
            if (progress[0].length() == 0){}
                //mainClass.setDebugView("empty string!");
            //mainClass.setDebugView(progress[0]);
        }*/

        protected void onPostExecute(Http.HttpAnswer result) {
            mainClass.setWorkStatus(false);
            if (result.code == 1) {
                //some error occurred
                mainClass.setDebugView("HttpGet exception: "+result.message);
                return;
            }

            if (result.code == 401) {
                //cookie wasn't found on the server (restarted), get new cookie
                mainClass.startAsyncTaskCookie();
                return;
            }
            mainClass.setDebugView(result.message);
            if (result.message.isEmpty()) {
                mainClass.setDebugView("Empty!");
                return;
            }
            try {
                JSONObject reader = new JSONObject(result.message);
                //mainClass.setDebugView(reader.getString("code"));
                int code = reader.getInt("code");
                //mainClass.setDebugView(String.valueOf(code));
                switch (code) {
                    case SERVER_CODE_DURATION:
                        mainClass.setDuration(reader.getInt("duration"));
                        mainClass.setDebugView(Integer.toString(reader.getInt("duration")));
                        break;
                    case SERVER_CODE_LIST:
                        mainClass.buildTable(reader.getString("list"));
                        break;
                    case SERVER_CODE_POSITION:
                        //mainClass.buildTable(reader.getInt("position"));
                        break;
                    case SERVER_CODE_PLAYBACK:
                        mainClass.resetSeekBarProgress();
                        mainClass.startAsyncTaskDuration();
                        break;

                }
            } catch (JSONException e) {
                mainClass.setDebugView(e.toString());
            }
        }
    }

    public static class GetDuration extends AsyncTask<Object, Void, Void> {
        JuraActivity mainClass;

        @Override
        protected Void doInBackground(Object... params) {
            mainClass = (JuraActivity) params[0];
            SystemClock.sleep(1000);
            return null;
        }

        protected void onPostExecute(Void result) {
            mainClass.setWorkStatus(false);
            mainClass.startAsyncTask("duration");
        }
    }

    public static class FindServerTask extends AsyncTask<Object, Void, String> {
        JuraActivity mainClass;

        @Override
        protected String doInBackground(Object... params) {
            mainClass = (JuraActivity) params[0];
            return FindServer.find();
        }

        protected void onPostExecute(String result) {
            mainClass.setWorkStatus(false);
            if (result.equals("")) {

                mainClass.setDebugView("FindServerTask: "+FindServer.error);
                return;
            }
            // Remember server ip
            mainClass.setServerIp(result);
            // Connect to server
            mainClass.startAsyncTaskCookie();
        }
    }
}
