package giotto.iot.bdbeeksbeacon.Fragments;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bluvision.beeks.sdk.constants.BeaconType;
import com.bluvision.beeks.sdk.constants.Range;
import com.bluvision.beeks.sdk.domainobjects.Beacon;
import com.bluvision.beeks.sdk.domainobjects.EddystoneURLBeacon;
import com.bluvision.beeks.sdk.domainobjects.SBeacon;
import com.bluvision.beeks.sdk.interfaces.BeaconListener;
import com.bluvision.beeks.sdk.interfaces.OnBeaconChangeListener;
import com.bluvision.beeks.sdk.util.BeaconManager;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import giotto.iot.bdbeeksbeacon.BDHelper;
import giotto.iot.bdbeeksbeacon.BeaconDetails;
import giotto.iot.bdbeeksbeacon.BeaconInstance;
import giotto.iot.bdbeeksbeacon.BeaconsListAdapter;
import giotto.iot.bdbeeksbeacon.BeeksBeaconApp;
import giotto.iot.bdbeeksbeacon.R;
import giotto.iot.bdbeeksbeacon.Utils;

import static com.bluvision.beeks.sdk.constants.BeaconType.EDDYSTONE_URL_BEACON;
import static com.bluvision.beeks.sdk.constants.BeaconType.S_BEACON;

public class ListBeaconsFragment extends BaseFragment implements BeaconListener, OnBeaconChangeListener {
    private static final String TAG = ListBeaconsFragment.class.getSimpleName();

    private static final int SCAN_TIME_MILLIS = 5000;
    private static final int REQUEST_DELTA_TIME_MILLIS = 200; // request 5 times per second;
    private static final Handler handler = new Handler(Looper.getMainLooper());

    private BeaconManager mBeaconManager;
    private BeaconsListAdapter mBeaconsListAdapter;
    private List<Beacon> beaconList = new ArrayList<>();
    private List<BeaconInstance> beaconInstanceList = new ArrayList<>();
    private  List<String> mac_list = new ArrayList<>();
    private  List<String> not_mac_list = new ArrayList<>();


    private View rootView;
    private ListView listBeacons;

    BDHelper bdHelper = new BDHelper();
    boolean isBDBeacon;
    int button_counter = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mBeaconManager = ((BeeksBeaconApp) getActivity().getApplication()).getBeaconManager();
        mBeaconManager.addBeaconListener(this);
        beaconInstanceList.add(new BeaconInstance(null));

        mBeaconsListAdapter = new BeaconsListAdapter(getActivity(), beaconInstanceList);

        /** Add rules to specify what type of beacons you want to discover
         *  in this sample app we are discovering SBeacons and IBeacons.
         *  you can remove the ones you don't want to discover.
         */
        mBeaconManager.addRuleToIncludeScanByType(BeaconType.S_BEACON);
        mBeaconManager.addRuleToIncludeScanByType(BeaconType.I_BEACON);
        mBeaconManager.addRuleToIncludeScanByType(BeaconType.EDDYSTONE_UID_BEACON);
        mBeaconManager.addRuleToIncludeScanByType(BeaconType.EDDYSTONE_URL_BEACON);
        mBeaconManager.addRuleToIncludeScanByType(BeaconType.EDDYSTONE_TLM_BEACON);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        setRetainInstance(true);

        rootView = inflater.inflate(R.layout.fragment_beacons, container, false);
        listBeacons = (ListView) rootView.findViewById(R.id.listBeacons);

        final ProgressBar progressBar = (ProgressBar)rootView.findViewById(R.id.progressBar);
        progressBar.setProgress(0);
        progressBar.getProgressDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);

        final Button btnScanner = (Button) rootView.findViewById(R.id.btnScanner);
        btnScanner.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
//                mBeaconsListAdapter.clear();
//                updateArrayAdapter();
//                if(button_counter == 0) {
//                    button_counter = 1;
//                    beaconInstanceList.add(new BeaconInstance(null));
//                }

                Log.i(TAG, "started scan");
                startScan();
                Utils.setEnabledViews(false, btnScanner);

                CountDownTimer countDownTimer = new CountDownTimer(SCAN_TIME_MILLIS, 100) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        double i = (1 - millisUntilFinished / (double) SCAN_TIME_MILLIS) * 100;
                        progressBar.setProgress((int) i);
                    }

                    @Override
                    public void onFinish() {
                        progressBar.setProgress(100);
                    }
                };
                countDownTimer.start();

                Runnable stopScanning = new Runnable() {
                    @Override
                    public void run() {
                        stopScan();
                        Log.i(TAG, "stopped scan");
                        Log.i(TAG, String.valueOf(beaconList.size()));

                        Log.d("MapActivity", mac_list.toString());
                        Log.d("MapActivity", not_mac_list.toString());

                        Utils.setEnabledViews(true, btnScanner);

                        // Modifying this part to update all beacons in BD after listing
                        try {
                            updateBeacon();
                        } catch (InterruptedException | ExecutionException | IOException | JSONException e) {
                            e.printStackTrace();
                        }

                    }
                };
                handler.postDelayed(stopScanning, SCAN_TIME_MILLIS);

            }
        });

        listBeacons.setAdapter(mBeaconsListAdapter);

        listBeacons.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                stopScan();
                BeaconInstance beaconInstance = beaconInstanceList.get(position);
                mInterface.onBeaconInstanceSelectedFromList(beaconInstance);
                return true;
            }
        });

        return rootView;
    }

    private void stopScan() {
        mBeaconManager.stopScan();
    }

    private void startScan() {
        mBeaconManager.startScan();
    }

    public void onPause() {
        super.onPause();
        mBeaconManager.removeBeaconListener(this);
    }

    public void onResume() {
        super.onResume();
        if(beaconList.size()!=0)
            mBeaconManager.addBeaconListener(this);
    }

    @Override
    public void onBeaconFound(final Beacon beacon) {

        if(this!=null)
            beacon.addOnBeaconChangeListener(this);

        getActivity().runOnUiThread(new Runnable() {
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void run() {

                boolean flag = false;

                if (beacon!=null && beacon.getBeaconType() == S_BEACON)
                    Log.i(TAG , beacon.getDevice() + " "
                        + beacon.getRssi()+" "+((SBeacon) beacon).getsId());

                for (Beacon beeks : beaconList){
                    if(beacon !=null && (beacon.getDevice().toString().equals(beeks.getDevice())))
                        flag = true;
                }
                if (beacon != null && (!beaconList.contains(beacon))) {

                    try {
                        isBDBeacon = bdHelper.isBDBeacon(getContext(), beacon);
                    } catch (InterruptedException | ExecutionException | IOException | JSONException e) {
                        e.printStackTrace();
                    }

                    if(isBDBeacon) {
                        if(!mac_list.contains(beacon.getDevice().getAddress()))
                            mac_list.add(beacon.getDevice().getAddress());
                    }
                    else{
                        if(!not_mac_list.contains(beacon.getDevice().getAddress()))
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

                    if (beacon.getBeaconType() == EDDYSTONE_URL_BEACON)
                        Log.i(TAG , beacon.getDevice() + " "
                                + beacon.getRssi()+" " + ((EddystoneURLBeacon)beacon).getURL());

                    updateArrayAdapter();
                }
            }
        });
    }

    @Override
    public void bluetoothIsNotEnabled() {
        Toast.makeText(getActivity(), "Please activate your Bluetooth connection", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRssiChanged(final Beacon beacon, int i) {

        if(mac_list.contains(beacon.getDevice().getAddress())){
            for (BeaconInstance beaconInstance : beaconInstanceList) {
                if (beaconInstance.updateBeaconInstance(beacon))
                    break;
            }
            updateArrayAdapter();
//            Log.d("changed", "rssi " + beacon.getDevice().getAddress() + "\t" + i);
        }

    }

    @Override
    public void onRangeChanged(Beacon beacon, Range range) {
        if(mac_list.contains(beacon.getDevice().getAddress())) {
//            Log.d("changed", "range changed " + range + "\t" + beacon.getDevice().getAddress() + "\t" + beacon.getBeaconType());
        }
    }

    @Override
    public void onBeaconExit(Beacon beacon) {

//        Log.d("changed", "exited " + beacon.getDevice().getAddress());

    }

    @Override
    public void onBeaconEnter(Beacon beacon) {

//        Log.d("changed", "entered " + beacon.getDevice().getAddress());

    }


    private void updateArrayAdapter() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBeaconsListAdapter.notifyDataSetChanged();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void updateBeacon() throws InterruptedException, ExecutionException, JSONException, IOException {
        BeaconDetails detailsHelper = new BeaconDetails();
        Log.d("MapActivity", String.valueOf(beaconInstanceList.size()) + "\t" + beaconInstanceList.get(0).name);
        for (BeaconInstance beaconInstance : beaconInstanceList) {
            detailsHelper.updateBeacon(beaconInstance, getContext());
//            Log.d("fromList", beaconInstance.name);
        }
    }

}
