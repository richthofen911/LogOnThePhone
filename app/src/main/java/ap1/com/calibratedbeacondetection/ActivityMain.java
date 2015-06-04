package ap1.com.calibratedbeacondetection;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import project.richthofen911.callofdroidy.ActivityTwinBeaconDetectionByRECO;

public class ActivityMain extends ActivityTwinBeaconDetectionByRECO{

    public static final int COMPLETED = 0;
    private TextView tv_log;
    private TextView tv_status;
    private ScrollView scrollView;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            if(msg.what == COMPLETED){
                tv_log.append(msg.getData().getCharSequence("newlog"));
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        assignRegionArgs("E2C56DB5-DFFB-48D2-B060-D0F5A71096E0", 777, -30);

        setContentView(R.layout.activity_main);
        tv_log= (TextView) findViewById(R.id.tv_log);
        tv_status = (TextView) findViewById(R.id.tv_status);
        scrollView = (ScrollView) findViewById(R.id.scrv_log);

        new LogThread().start();

        findViewById(R.id.btn_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start(definedRegions);
            }
        });

        findViewById(R.id.btn_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop(definedRegions);
            }
        });

        findViewById(R.id.btn_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_log.setText("");
                tv_status.setBackgroundColor(Color.parseColor("#00FF00"));
            }
        });
    }

    @Override
    protected void actionOnEnter(){
        tv_status.setBackgroundColor(Color.parseColor("#FF0000"));
    }

    @Override
    protected void actionOnExit(){
        tv_status.setBackgroundColor(Color.parseColor("#00FF00"));
    }

    private class LogThread extends Thread {
        @Override
        public void run() {
            String cmd_show = "logcat *:E ActivityManager:S AndroidRuntime:S NotificationService:S WindowManager:S ActivityThread:S";
            try{
                Runtime.getRuntime().exec("logcat -c");
                Process process = Runtime.getRuntime().exec(cmd_show);
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    Bundle bundle = new Bundle();
                    bundle.putCharSequence("newlog", line + "\n");
                    Message msg = new Message();
                    msg.what = COMPLETED;
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                }
            }catch (IOException e){
            }
        }
    }

}
