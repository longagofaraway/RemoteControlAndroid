package com.example.jura.myapplication;

import android.content.Context;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.util.TypedValue;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TableRow;
import android.widget.TableLayout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.net.URLEncoder;

public class JuraActivity extends AppCompatActivity {

    private TextView debugView;
    private TextView textView;
    private TableLayout tableLayout;
    private SeekBar seekBar;


    final private String fileWithServerIp = "serverip";
    private String serverIp = "";
    private String appCookie = "";
    private boolean inWork = false;

    private int duration = 0;

    final private int DEBUG = 0;

    //public interaction
    public void setCookie(String cookie) {
        appCookie = cookie;
    }
    String getStringFromArray(byte[] arr) {
        String ret = "";
        for (int i = 0; i < arr.length; i++){
            if (arr[i] == '\0') {
                ret = new String(arr, 0, i);
                break;
            }
        }
        return ret;
    }
    public void getServerIp() {
        /*try {
            //if file exists
            String filePath = getFilesDir() + "/" + fileWithServerIp;
            File file = new File(filePath);

            if (!file.exists()) {
                //create file in internal storage
                FileOutputStream fout;
                fout = openFileOutput(fileWithServerIp, Context.MODE_PRIVATE);
                fout.close();
                serverIp = "";
                return;
            }
            //or read from file
            FileInputStream fin = openFileInput(fileWithServerIp);
            //serverIp = Http.readStream(fin);

            byte[] buffer = new byte[32];
            int bytes = fin.read(buffer, 0, 32);
            if (bytes == -1) {
                serverIp = "";
                return;
            }
            serverIp = getStringFromArray(buffer);
            setDebugView(serverIp+' '+String.valueOf(bytes)+" ");
        } catch (Exception e) {
            serverIp = "";
            setDebugView("getServerIp:"+e.toString());
        }*/
        serverIp = "192.168.199.14";
    }
    public void setServerIp(String ip) {
        try {
            FileOutputStream fos = openFileOutput(fileWithServerIp, Context.MODE_PRIVATE);
            fos.write(ip.getBytes());
            fos.close();
        } catch (Exception e) {
            setDebugView("setServerIp:"+e.toString());
        }

    }
    public void setDuration(int seconds) { duration = seconds; }
    public void setWorkStatus(boolean status) {
        inWork = status;
        if(status)
            textView.setText("in work");
        else
            textView.setText("not in work");

    }
    public void setDebugView(String s) {
        debugView.setText(debugView.getText().toString()+s);
    }
    public void setTextView(String s) {
        //textView.setText(s);
    }
    public void resetSeekBarProgress() {
        seekBar.setProgress(0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jura);

        tableLayout = (TableLayout) findViewById(R.id.tableLayout);
        assert tableLayout != null;
        tableLayout.setColumnStretchable(0, true);

        seekBar =  (SeekBar) findViewById(R.id.seekBar);
        SeekBarListener seekBarListener = new SeekBarListener();
        seekBarListener.init(this);
        seekBar.setOnSeekBarChangeListener(seekBarListener);

        //debug////////////////
        debugView = (TextView) findViewById(R.id.textView2);
        debugView.setVisibility(View.INVISIBLE);
        textView = (TextView) findViewById(R.id.textView);
        assert textView != null;
        textView.setOnClickListener(debug);
        textView.setClickable(true);
        ///////////////////////

        //set global var serverIp
        getServerIp();
    }

    @Override
    protected void onStart() {
        super.onStart();

        connectToServer();
    }

    private void connectToServer() {
        if (serverIp.equals(""))
            startAsyncTaskFindServer();

        if (!serverIp.equals(""))
            //or else there is an error
            startAsyncTaskCookie();
    }

    private View.OnClickListener fsClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TextView tv = (TextView)v;
            String s = tv.getText().toString();
            String arg;

            try {
                arg = URLEncoder.encode(s, "utf-8");
                startAsyncTask("app?arg="+arg);
            } catch(UnsupportedEncodingException e) {

            }
        }
    };

    View.OnClickListener debug = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            resetSeekBarProgress();
        }
    };

    void buildTable(String serverResponse) {
        tableLayout.removeAllViews();

        //get display width so that textViews would be clickable across the screen
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);

        String[] entities = serverResponse.split("\n");
        for (int i = 0; i < entities.length+1; i++) {
            TableRow row = new TableRow(this);
            row.setMinimumHeight(90);
            TextView tv = new TextView(this);
            tv.setTextSize(22);
            if (i == 0) {
                tv.setText("..");
                tv.setWidth(outMetrics.widthPixels-30);
            }
            else
                tv.setText(entities[i-1]);
            tv.setOnClickListener(fsClick);

            //ripple effect
            TypedValue outValue = new TypedValue();
            tv.getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            tv.setBackgroundResource(outValue.resourceId);

            row.addView(tv);
            tableLayout.addView(row,i);
        }
    }

    /////////// API /////////////
    public boolean startAsyncTaskRoutine() {
        if (serverIp.equals("")) {
            startAsyncTaskFindServer();
            return true;
        }
        if (inWork)
            return true;
        setWorkStatus(true);
        return false;
    }

    public void startAsyncTask(String url) {
        if (startAsyncTaskRoutine())
            return;
        AsyncTasks.HttpGet httpGet = new AsyncTasks.HttpGet();
        httpGet.execute("http://"+serverIp+":5000/"+url, appCookie, this);
    }
    public void startAsyncTaskCookie() {
        if (startAsyncTaskRoutine())
            return;
        AsyncTasks.HttpGetCookie httpGetCookie = new AsyncTasks.HttpGetCookie();
        httpGetCookie.execute("http://"+serverIp+":5000", this);
    }
    public void startAsyncTaskDuration() {
        if (startAsyncTaskRoutine())
            return;
        AsyncTasks.GetDuration getDuration = new AsyncTasks.GetDuration();
        getDuration.execute(this);
    }
    public void startAsyncTaskFindServer() {
        if (inWork)
            return;
        setWorkStatus(true);
        AsyncTasks.FindServerTask findServer = new AsyncTasks.FindServerTask();
        findServer.execute(this);
    }
    public void serverKillAll(View v) {
        startAsyncTask("killall");
        duration = 0;
    }
    public void serverPause(View v) {
        startAsyncTask("pause");
    }
    public void setPosition(int pos) {
        if (duration == 0)
            return;
        int newPos = duration * pos;
        startAsyncTask("setposition?arg="+Integer.toString(newPos)+"0000");
    }
    public void Sync(View v) {
        startAsyncTaskFindServer();
        //connectToServer();
    }
}
