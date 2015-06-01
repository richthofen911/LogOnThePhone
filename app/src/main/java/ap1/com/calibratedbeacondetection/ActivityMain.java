package ap1.com.calibratedbeacondetection;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.perples.recosdk.RECOBeacon;
import com.perples.recosdk.RECOBeaconManager;
import com.perples.recosdk.RECOBeaconRegion;
import com.perples.recosdk.RECOErrorCode;
import com.perples.recosdk.RECOProximity;
import com.perples.recosdk.RECORangingListener;
import com.perples.recosdk.RECOServiceConnectListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;


public class ActivityMain extends Activity implements RECOServiceConnectListener, RECORangingListener{

    String beaconId;
    //String TOBEFOUND_UUID = "23A01AF0-232A-4518-9C0E-323FB773F5EF";
    String TOBEFOUND_UUID = "E2C56DB5-DFFB-48D2-B060-D0F5A71096E0";
    int TOBEFOUND_MAJOR = 777;
    //int TOBEFOUND_MAJOR = 64612;
    //int TOBEFOUND_MINOR = 1;
    //int TOBEFOUND_MINOR = 48499;

    String macAddress;

    protected RECOBeaconManager mRecoManager;
    protected ArrayList<RECOBeaconRegion> mRegions;

    public static final boolean DISCONTINUOUS_SCAN = false;
    public static final int COMPLETED = 0;
    private TextView tv_log;
    private TextView tv_status;
    private ScrollView scrollView;
    private int count = 0;
    private int previousMinor;
    private int rssi1 = 0;
    private int rssi2 = 0;
    private int rssi3 = 0;
    private int rssi = 0;

    private String proximity;

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
        setContentView(R.layout.activity_main);

        tv_log= (TextView) findViewById(R.id.tv_log);
        tv_status = (TextView) findViewById(R.id.tv_status);
        scrollView = (ScrollView) findViewById(R.id.scrv_log);

        mRecoManager = RECOBeaconManager.getInstance(getApplicationContext(), false, false);
        mRegions = this.generateBeaconRegion();
        mRecoManager.setRangingListener(this);
        mRecoManager.bind(this);

        new LogThread().start();

        findViewById(R.id.btn_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start(mRegions);
            }
        });

        findViewById(R.id.btn_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop(mRegions);
            }
        });

        findViewById(R.id.btn_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_log.setText("");
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onServiceConnect() {
        Log.e("RangingActivity", "onServiceConnect()");
        mRecoManager.setDiscontinuousScan(this.DISCONTINUOUS_SCAN);
        //Write the code when RECOBeaconManager is bound to RECOBeaconService
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<RECOBeacon> recoBeacons, RECOBeaconRegion recoRegion) {
        synchronized (recoBeacons){
            if(rssi1 == 0 || rssi2 == 0 || rssi3 == 0){
                for(RECOBeacon recoBeacon: recoBeacons){
                    if(recoBeacon.getMinor() == 1){
                        if(rssi1 == 0){
                            rssi1 = recoBeacon.getRssi();
                        }
                    }else if(recoBeacon.getMinor() == 2){
                        if(rssi2 == 0){
                            rssi2 = recoBeacon.getRssi();
                        }
                    }else if(recoBeacon.getMinor() == 3){
                        if(rssi3 == 0){
                            rssi3 = recoBeacon.getRssi();
                        }
                    }
                }
            }else{
                rssi = (rssi1 + rssi2 + rssi3) / 3;
                Log.e("beacon detected, rssi1", String.valueOf(rssi1));
                Log.e("beacon detected, rssi2", String.valueOf(rssi2));
                Log.e("beacon detected, rssi3", String.valueOf(rssi3));
                Log.e("rssi calibrated", String.valueOf(rssi));
                rssi1 = 0;
                rssi2 = 0;
                rssi3 = 0;
            }

















/*
            if(rssi1 == 0 || rssi2 == 0){
                for(RECOBeacon recoBeacon: recoBeacons){
                    if(recoBeacon.getMinor() == 1){
                        if(rssi1 == 0){
                            rssi1 = recoBeacon.getRssi();
                        }
                    }else{
                        if(rssi2 == 0){
                            rssi2 = recoBeacon.getRssi();
                        }
                    }
                }
            }else {
                rssi = (rssi1 + rssi2) / 2;
                Log.e("beacon detected, rssi1", String.valueOf(rssi1));
                Log.e("beacon detected, rssi2", String.valueOf(rssi2));
                rssi1 = 0;
                rssi2 = 0;
                Log.e("rssi calibrated", String.valueOf(rssi));
                if(rssi > -70){
                    tv_status.setBackgroundColor(Color.parseColor("#FF3300"));
                    count = 0;
                }else{
                    if(count < 5){
                        count++;
                    }else tv_status.setBackgroundColor(Color.parseColor("#00FF00"));
                }
            }
*/
        }
    }

    protected void start(ArrayList<RECOBeaconRegion> regions) {
        Log.e("start searching", "...");
        for(RECOBeaconRegion region : regions) {
            try {
                Log.e("defined region: ", region.getProximityUuid());
                mRecoManager.startRangingBeaconsInRegion(region);
            } catch (RemoteException e) {
                Log.i("RECORangingActivity", "Remote Exception");
                e.printStackTrace();
            } catch (NullPointerException e) {
                Log.i("RECORangingActivity", "Null Pointer Exception");
                e.printStackTrace();
            }
        }
    }

    protected void stop(ArrayList<RECOBeaconRegion> regions) {
        Log.e("stop searching", "...");
        for(RECOBeaconRegion region : regions) {
            try {
                mRecoManager.stopRangingBeaconsInRegion(region);
            } catch (RemoteException e) {
                Log.i("RECORangingActivity", "Remote Exception");
                e.printStackTrace();
            } catch (NullPointerException e) {
                Log.i("RECORangingActivity", "Null Pointer Exception");
                e.printStackTrace();
            }
        }
    }

    private void unbind() {
        try {
            mRecoManager.unbind();
        } catch (RemoteException e) {
            Log.i("RECORangingActivity", "Remote Exception");
            e.printStackTrace();
        }
    }


    @Override
    public void onServiceFail(RECOErrorCode errorCode) {
        //Write the code when the RECOBeaconService is failed.
        //See the RECOErrorCode in the documents.
        return;
    }

    @Override
    public void rangingBeaconsDidFailForRegion(RECOBeaconRegion region, RECOErrorCode errorCode) {
        //Write the code when the RECOBeaconService is failed to range beacons in the region.
        //See the RECOErrorCode in the documents.
        return;
    }

    private ArrayList<RECOBeaconRegion> generateBeaconRegion() {
        ArrayList<RECOBeaconRegion> regions = new ArrayList<>();
        RECOBeaconRegion recoRegion;
        //recoRegion = new RECOBeaconRegion(this.TOBEFOUND_UUID, "Defined Region");
        //recoRegion = new RECOBeaconRegion(this.TOBEFOUND_UUID, this.TOBEFOUND_MAJOR, this.TOBEFOUND_MINOR, "Defined Region");
        recoRegion = new RECOBeaconRegion(this.TOBEFOUND_UUID, this.TOBEFOUND_MAJOR, "Defined Region");
        regions.add(recoRegion);

        return regions;
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
                //StringBuilder log = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    //log.append(line);
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
