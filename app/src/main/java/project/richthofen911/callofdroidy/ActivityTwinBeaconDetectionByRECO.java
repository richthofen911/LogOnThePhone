package project.richthofen911.callofdroidy;

import android.app.Activity;
import android.os.RemoteException;
import android.os.Bundle;
import android.util.Log;

import com.perples.recosdk.RECOBeacon;
import com.perples.recosdk.RECOBeaconManager;
import com.perples.recosdk.RECOBeaconRegion;
import com.perples.recosdk.RECOErrorCode;
import com.perples.recosdk.RECORangingListener;
import com.perples.recosdk.RECOServiceConnectListener;

import java.util.ArrayList;
import java.util.Collection;

public class ActivityTwinBeaconDetectionByRECO extends Activity implements RECOServiceConnectListener, RECORangingListener{

    private final boolean DISCONTINUOUS_SCAN = false;

    private boolean entered = false;
    private int exitCount = 0;
    private int failedCount = 0;
    private boolean exited = false;
    private int rssi1 = 0;
    private int rssi2 = 0;
    private int rssi = 0;
    private int[] rssiSum = new int[5];
    private int wantedMinor = 0;
    private int actualMinor1 = 0;
    private int actualMinor2 = 0;
    private int rssiBorder = 0;

    protected RECOBeaconManager mRecoManager = RECOBeaconManager.getInstance(this, false, false);
    protected ArrayList<RECOBeaconRegion> definedRegions;

    protected void assignRegionArgs(String uuid, int borderValue){
        definedRegions = generateBeaconRegion(uuid);
        rssiBorder = borderValue;
    }

    protected void assignRegionArgs(String uuid, int major, int borderValue){
        definedRegions = generateBeaconRegion(uuid, major);
        rssiBorder = borderValue;
    }

    protected void assignRegionArgs(String uuid, int major, int minor, int borderValue){
        definedRegions = generateBeaconRegion(uuid, major, minor);
        rssiBorder = borderValue;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRecoManager.setRangingListener(this);
        mRecoManager.bind(this);
    }

    protected void start(ArrayList<RECOBeaconRegion> regions) {
        for(RECOBeaconRegion region : regions) {
            try {
                Log.e("setup a region", region.getProximityUuid());
                Log.e("start detecting", "...");
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
        Log.e("stop detecting", "...");
        for(RECOBeaconRegion region : regions) {
            try {
                mRecoManager.stopRangingBeaconsInRegion(region);
                entered = false;
            } catch (RemoteException e) {
                Log.i("RECORangingActivity", "Remote Exception");
                e.printStackTrace();
            } catch (NullPointerException e) {
                Log.i("RECORangingActivity", "Null Pointer Exception");
                e.printStackTrace();
            }
        }
    }

    private ArrayList<RECOBeaconRegion> generateBeaconRegion(String uuid) {
        ArrayList<RECOBeaconRegion> regions = new ArrayList<>();
        RECOBeaconRegion recoRegion;
        recoRegion = new RECOBeaconRegion(uuid, "Defined Region");
        regions.add(recoRegion);
        return regions;
    }

    private ArrayList<RECOBeaconRegion> generateBeaconRegion(String uuid, int major) {
        ArrayList<RECOBeaconRegion> regions = new ArrayList<>();
        RECOBeaconRegion recoRegion;
        recoRegion = new RECOBeaconRegion(uuid, major, "Defined Region");
        regions.add(recoRegion);
        return regions;
    }

    private ArrayList<RECOBeaconRegion> generateBeaconRegion(String uuid, int major, int minor) {
        ArrayList<RECOBeaconRegion> regions = new ArrayList<>();
        RECOBeaconRegion recoRegion;
        recoRegion = new RECOBeaconRegion(uuid, major, minor, "Defined Region");
        regions.add(recoRegion);
        return regions;
    }

    @Override
    public void onServiceConnect() {
        Log.e("RangingActivity", "onServiceConnect()");
        mRecoManager.setDiscontinuousScan(DISCONTINUOUS_SCAN);
    }

    @Override
    public void onServiceFail(RECOErrorCode recoErrorCode) {
        Log.e("RECO service error:", recoErrorCode.toString());
    }

    private void assignWantedMinor(int minor, int thisRssi){
        if(minor % 3 == 1){
            actualMinor1 = minor;
            Log.e("detectedMinor", String.valueOf(actualMinor1));
            Log.e("beacon1", "is ready");
            rssi1 = thisRssi;
            Log.e("detectedRSSI", String.valueOf(rssi1));
            wantedMinor = minor + 1;
            Log.e("wantedMinor", String.valueOf(wantedMinor));
        }else{
            actualMinor2 = minor;
            Log.e("detectedMinor", String.valueOf(actualMinor2));
            Log.e("beacon2", "is ready");
            rssi2 = thisRssi;
            Log.e("detectedRSSI", String.valueOf(rssi2));
            wantedMinor = minor - 1;
            Log.e("wantedMinor", String.valueOf(wantedMinor));
        }
    }

    private int isWantedMinorDetected(){
        if(actualMinor1 == 0 || actualMinor2 == 0){
            Log.e("twins not ready", ".");
            failedCount++;
            if(failedCount < 5){
                if(rssi1 == 0){
                    rssiSum[failedCount - 1] = rssi2;
                }else {
                    rssiSum[failedCount - 1] = rssi1;
                }
                return 0;
            }else {
                failedCount = 0;
                return 2;
            }
        }else if((actualMinor2 - actualMinor1) > 1){
            Log.e("not twins", "");
            //failedCount++;
            return 0;
        }else
            return 1;
    }

    private void resetArgs() {
        rssi1 = 0;
        rssi2 = 0;
        rssi = 0;
        wantedMinor = 0;
        actualMinor1 = 0;
        actualMinor2 = 0;
    }

    protected void actionOnEnter(){}

    protected void actionOnExit(){}

    private void inOut(int theRssi){
        if(theRssi > rssiBorder){
            if(!entered){
                actionOnEnter();
                exitCount = 0;
                entered = true;
                exited = false;
            }else{
                Log.e("checkin already", ")");
            }
        }else{
            if(exitCount < 2){
                exitCount++;
            }else {
                if(!exited){
                    actionOnExit();
                    entered = false;
                    exited = true;
                }else {
                    Log.e("exited already", ")");
                }
            }
        }
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<RECOBeacon> recoBeacons, RECOBeaconRegion recoBeaconRegion) {
        synchronized (recoBeacons){
            for(RECOBeacon recoBeacon: recoBeacons){
                int tmpMinor = recoBeacon.getMinor();
                int tmpRssi = recoBeacon.getRssi();
                assignWantedMinor(tmpMinor, tmpRssi);
                int result = isWantedMinorDetected();
                if(result > 0){
                    Log.e("beacon detected, rssi1", String.valueOf(rssi1));
                    Log.e("beacon detected, rssi2", String.valueOf(rssi2));
                    if(result == 1){
                        rssi = (rssi1 + rssi2) / 2;
                        Log.e("rssi calibrated", String.valueOf(rssi));
                    }else {
                        rssi = (rssiSum[0] + rssiSum[1] + rssiSum[2] + rssiSum[3]) / 4;
                        Log.e("rssi calibrated by avg", String.valueOf(rssi));
                    }
                    inOut(rssi);
                    resetArgs();
                }else{
                    Log.e("not able to range yet", "(");
                }
            }
        }
    }

    @Override
    public void rangingBeaconsDidFailForRegion(RECOBeaconRegion recoBeaconRegion, RECOErrorCode recoErrorCode) {
        Log.e("RECO ranging error:", recoErrorCode.toString());
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        try{
            mRecoManager.unbind();
        }catch (RemoteException e){
            Log.e("on destroy error", e.toString());
        }
    }
}
