package giotto.iot.beeksmap;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bluvision.beeks.sdk.constants.BeaconType;
import com.bluvision.beeks.sdk.constants.Range;
import com.bluvision.beeks.sdk.domainobjects.Beacon;
import com.bluvision.beeks.sdk.domainobjects.IBeacon;
import com.bluvision.beeks.sdk.domainobjects.SBeacon;
import com.bluvision.beeks.sdk.interfaces.BeaconListener;
import com.bluvision.beeks.sdk.interfaces.OnBeaconChangeListener;
import com.bluvision.beeks.sdk.util.BeaconManager;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;


public class MainActivity extends Activity implements BeaconListener, OnBeaconChangeListener {

    String TAG = "MapActivity";
    int k= 0;

    private final static int PERMISSION_REQUEST_COARSE_LOCATION = 1984;

    private static final int SCAN_TIME_MILLIS = 4000;
    private static final int RESCAN_TIME_MILLIS = 9000;

    private static final Handler handler = new Handler(Looper.getMainLooper());
    Timer timer = new Timer();

    private BeaconManager mBeaconManager;
    private List<Beacon> beaconList = new ArrayList<>();
    private List<BeaconInstance> beaconInstanceList = new ArrayList<>();

    private List<String> mac_list = new ArrayList<>();
    private List<String> not_mac_list = new ArrayList<>();

    private List<String> hack_list = new ArrayList<>();
    private List<String> eddy_list = new ArrayList<>();

    BDHelper bdHelper = new BDHelper();
    boolean isBDBeacon;

    HashMap mapHash = new HashMap();
    HashMap latlong = new HashMap();

    Button btnMap;
    TextView dummy;
    ImageView imageView;
    Bitmap mutableBitmap;
    Canvas canvas;
    Paint paintCircle;
    Paint paintLine;

    String hack_mac;
    int hack_rssi;

    String oldX = "", oldY = "", newX, newY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.image_map);

        initialSetup();
        btnMap = (Button) findViewById(R.id.map);
        dummy = (TextView) findViewById(R.id.dummy);

        beaconInstanceList.add(new BeaconInstance(null));

        mBeaconManager = ((BeeksBeaconApp) getApplication()).getBeaconManager();
        mBeaconManager.addBeaconListener(this);

        /** Add rules to specify what type of beacons you want to discover
         *  in this sample app we are discovering SBeacons and IBeacons.
         *  you can remove the ones you don't want to discover.
         */

        mBeaconManager.addRuleToIncludeScanByType(BeaconType.S_BEACON);
        mBeaconManager.addRuleToIncludeScanByType(BeaconType.I_BEACON);
        mBeaconManager.addRuleToIncludeScanByType(BeaconType.EDDYSTONE_UID_BEACON);
        mBeaconManager.addRuleToIncludeScanByType(BeaconType.EDDYSTONE_URL_BEACON);
        mBeaconManager.addRuleToIncludeScanByType(BeaconType.EDDYSTONE_TLM_BEACON);

        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                performScan();
            }
        });

        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                Log.i(TAG, "Rescanning");
                performScan();
            }

        }, 0, RESCAN_TIME_MILLIS);

    }

    private void performScan() {

        startScan();
        makeToast("Started scan");
        Log.i(TAG, "started scan");

        Runnable stopScanning = new Runnable() {
            @Override
            public void run() {
                stopScan();
                makeToast("Stopped scan");
                Log.i(TAG, "stopped scan");

//                Log.d(TAG, mac_list.toString());
//                Log.d(TAG, not_mac_list.toString());

                // Modifying this part to update all beacons in BD after listing
                try {
                    int beaconsFound = updateBeacon();
                    if(beaconsFound!=0)
                    {
                        String[] xy = findLocation();
                        int least_rssi = hack_rssi;
                        boolean drawOnMap = false;
                        if(!(hack_list.contains(hack_mac))) {
                            if(eddy_list.contains(hack_mac)){
                                if (least_rssi<85)
                                    drawOnMap = true;
                            }
                            else if(least_rssi<90 && !drawOnMap)
                                drawOnMap = true;
                            if(drawOnMap) {
                                btnMap.setText(hack_mac + "  -" + hack_rssi);
                                navigateParser(xy);
                                hack_list.add(hack_mac);
                            }
                        }

                        Log.d(TAG, oldX+" "+oldY+"\t"+newX+" "+newY);
                    }

                } catch (InterruptedException | ExecutionException | IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        handler.postDelayed(stopScanning, SCAN_TIME_MILLIS);
    }

    private void makeToast(final String message){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String[] findLocation() throws InterruptedException, ExecutionException, JSONException, IOException {
        String min_mac = "";
        String min_rssi = "";
        int min = 1000;

        Set mapSet = mapHash.entrySet();
        Iterator mapIterator = mapSet.iterator();
        while(mapIterator.hasNext()) {
            Map.Entry mapEntry = (Map.Entry) mapIterator.next();
            int val = (-1) * Integer.parseInt(String.valueOf(mapEntry.getValue()));
            String key = (String) mapEntry.getKey();
            if(val<min) {
                min = val;
                min_rssi = "-"+val;
                min_mac = key;
            }
        }

        hack_mac = min_mac;
        hack_rssi = min;

        String xy[] = null;
        if(latlong.containsKey(min_mac))
        {
            String temp = (String) latlong.get(min_mac);
            xy = temp.split(",");
        }
        else
        {
            String temp = bdHelper.getLatLong(getApplicationContext(), min_mac);
            xy = temp.split(",");
            latlong.put(min_mac, temp);
        }

        return xy;

    }

    private void stopScan() {
        mBeaconManager.stopScan();
    }

    private void startScan() {
        mBeaconManager.startScan();
    }

    @Override
    public void onPause() {
        super.onPause();
        mBeaconManager.removeBeaconListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mBeaconManager.addBeaconListener(this);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        timer.cancel();
    }

    @Override
    public void onBeaconFound(final Beacon beacon) {

        if(this!=null && beacon!=null)
            beacon.addOnBeaconChangeListener(this);

//        if(beacon!=null && beacon.getBeaconType().equals(BeaconType.S_BEACON)) {
//            String SBeaconID = String.valueOf(((SBeacon) beacon).getsId());
//            Log.d("find", beacon.getDevice().getAddress() + "\t" + SBeaconID);
//        }

//        if(beacon!=null && beacon.getBeaconType().equals(BeaconType.I_BEACON)) {
//            String major = String.valueOf(((IBeacon) beacon).getMajor());
//            String minor = String.valueOf(((IBeacon) beacon).getMinor());
//            if (minor.equals("275")) {
//                Log.d("find", beacon.getDevice().getAddress() + "\t" + ((IBeacon) beacon).getUuid());
//            }
//        }

        runOnUiThread(new Runnable() {
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void run() {

                boolean flag = false;

                for (Beacon beeks : beaconList){
                    if(beacon !=null && (beacon.getDevice().toString().equals(beeks.getDevice())))
                        flag = true;
                }

                if (beacon != null && (!beaconList.contains(beacon)) &&
                        (!not_mac_list.contains(beacon.getDevice().getAddress()))){
                    try {
                        isBDBeacon = bdHelper.isBDBeacon(getApplicationContext(), beacon);
                    } catch (InterruptedException | ExecutionException | IOException | JSONException e) {
                        e.printStackTrace();
                    }

                    if(isBDBeacon) {
                        if(!mac_list.contains(beacon.getDevice().getAddress()))
                            mac_list.add(beacon.getDevice().getAddress());
                    }
                    else {
                        if (!not_mac_list.contains(beacon.getDevice().getAddress()))
                            not_mac_list.add(beacon.getDevice().getAddress());
                    }

                    if(isBDBeacon) {
                        if(!flag)
                            beaconList.add(beacon);
                        for (BeaconInstance beaconInstance : beaconInstanceList) {
                            if (beaconInstance.updateBeaconInstance(beacon)) {
                                break;
                            } else if (beaconInstance == beaconInstanceList.get(beaconInstanceList.size() - 1)) {
                                beaconInstanceList.add(new BeaconInstance(null));
                                beaconInstanceList.get(beaconInstanceList.size() - 1).updateBeaconInstance(beacon);
                            }
                        }
                    }

                }
            }
        });
    }

    @Override
    public void bluetoothIsNotEnabled() {
        Toast.makeText(getApplicationContext(), "Please activate your Bluetooth connection", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRssiChanged(final Beacon beacon, final int i) {

        if(mac_list.contains(beacon.getDevice().getAddress())){
            for (BeaconInstance beaconInstance : beaconInstanceList) {
                if (beaconInstance.updateBeaconInstance(beacon))
                    break;
            }
        }
    }

    @Override
    public void onRangeChanged(Beacon beacon, Range range) {

    }

    @Override
    public void onBeaconExit(Beacon beacon) {

    }

    @Override
    public void onBeaconEnter(Beacon beacon) {
//        Toast.makeText(getApplicationContext(), "Beacon Entered" + beacon.getBeaconType(), Toast.LENGTH_LONG).show();
    }

    private void initialSetup(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check 
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @TargetApi(Build.VERSION_CODES.M)
                    @Override
                    public void onDismiss(DialogInterface dialog) {

                        requestPermissions(
                                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
                builder.show();
            }
        }

        makeBitMap();

    }

    private void makeBitMap() {
        BitmapFactory.Options myOptions = new BitmapFactory.Options();
        myOptions.inScaled = false;
        myOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;// important

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.map, myOptions);

        Bitmap workingBitmap = Bitmap.createBitmap(bitmap);
        mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);
        canvas = new Canvas(mutableBitmap);

        paintCircle = new Paint();
        paintCircle.setAntiAlias(true);
        paintCircle.setColor(Color.BLUE);

        paintLine = new Paint();
        paintLine.setAntiAlias(true);
        paintLine.setColor(Color.GREEN);
        paintLine.setStrokeWidth(25f);

        makeMap();
    }

    private void makeMap() {

        canvas.drawCircle(1500, 2010, 25, paintCircle); // 272
        canvas.drawCircle(2010, 2010, 25, paintCircle); // 276
        canvas.drawCircle(1250, 1800, 25, paintCircle); // Beeks
        canvas.drawCircle(1700, 1700, 25, paintCircle); // Beeks

        imageView.setAdjustViewBounds(true);
        imageView.setImageBitmap(mutableBitmap);

    }

    private void navigateParser(String[] xy) {

        Float prevX, prevY, currX, currY;

        if(oldX.equals("")){
            oldX = xy[0];
            oldY = xy[1];
        }
        newX = xy[0];
        newY = xy[1];

        if(!(oldX.equals(newX) && oldY.equals(newY)))
        {
            prevX = Float.valueOf(oldX);
            prevY = Float.valueOf(oldY);
            currX = Float.valueOf(newX);
            currY = Float.valueOf(newY);

            navigate(prevX, prevY, currX, currY);

            oldX = newX;
            oldY = newY;

        }

    }

    public void navigate(Float prevX, Float prevY, Float currX, Float currY) {

//        canvas.drawLine(1500, 2010, 2010, 2010, paintLine);
        canvas.drawLine(prevX, prevY, currX, currY, paintLine);

        imageView.setAdjustViewBounds(true);
        imageView.setImageBitmap(mutableBitmap);

    }

    private int updateBeacon() throws InterruptedException, ExecutionException, JSONException, IOException {
        BeaconDetails detailsHelper = new BeaconDetails();
        int beaconsFound = 0;
        for (BeaconInstance beaconInstance : beaconInstanceList) {
            if (beaconInstance.name != null) {
                beaconsFound++;
                detailsHelper.updateBeacon(beaconInstance, getApplicationContext());

                String rssi_mod = beaconInstance.rssi;

                if(detailsHelper.isEddyStone(beaconInstance)) {
                    rssi_mod = String.valueOf((Integer.parseInt(rssi_mod)-10));
                    Log.d("Find", beaconInstance.macAddress + "\t" + beaconInstance.rssi + "\t" + rssi_mod);
                    if (!(eddy_list.contains(beaconInstance.macAddress)))
                        eddy_list.add(beaconInstance.macAddress);

                }

                mapHash.put(beaconInstance.macAddress, rssi_mod);
//                Log.d("Find", beaconInstance.macAddress + "\t" + beaconInstance.rssi + "\t" + rssi_local);
            }
        }
//        Log.d("Find", "--------------------------------------------------------");
        return beaconsFound;
    }
}
