package giotto.iot.bdbeeksbeacon.Fragments;


import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bluvision.beeks.sdk.constants.BeaconType;
import com.bluvision.beeks.sdk.domainobjects.Beacon;
import com.bluvision.beeks.sdk.domainobjects.SBeacon;
import com.bluvision.beeks.sdk.interfaces.BeaconConfigurationListener;
import com.bluvision.beeks.sdk.util.BeaconManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import giotto.iot.bdbeeksbeacon.BDHelper;
import giotto.iot.bdbeeksbeacon.BeaconInstance;
import giotto.iot.bdbeeksbeacon.BeeksBeaconApp;
import giotto.iot.bdbeeksbeacon.R;


public class BeaconDetailFragment extends BaseFragment implements BeaconConfigurationListener {
    private static final String TAG = BeaconDetailFragment.class.getSimpleName();

    private View rootView;

    private BeaconManager mBeaconManager;
    private BeaconInstance mBeaconInstance;
    private SBeacon sBeacon;

    BDHelper bdHelper = new BDHelper();

    String mac = "", url = "", battery = "", eid = "", name = "", rssi = "";
    String iBeaconMajorID = "", iBeaconMinorID = "", iBeaconUUID = "", latlong = "";
    String sBeaconID = "", temperature = "", uidInstance = "", uidNamespace = "", uuid = "";

    JSONObject makeBDJson = new JSONObject();

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        mBeaconManager = ((BeeksBeaconApp) getActivity().getApplication()).getBeaconManager();

        try {
            refreshStatus(mBeaconInstance);

//            Log.d("building", "mac = "+mac+" uuid = "+uuid+", rssi = "+rssi+" name = "+name);
//            Log.d("building", "url = "+url+" battery = "+battery+", temperature = "+temperature+" sBeaconID = "+sBeaconID);
//            Log.d("building", "iBeaconMajorID = "+iBeaconMajorID+" iBeaconMinorID = "+iBeaconMinorID);
//            Log.d("building", "uidInstance = "+uidInstance+" uidNamespace = "+uidNamespace);

            makeBDJson.put("mac", mac);
            makeBDJson.put("uuid", uuid);
            makeBDJson.put("rssi", rssi);
            makeBDJson.put("name", name);
            makeBDJson.put("url", url);
            makeBDJson.put("battery", battery);
            makeBDJson.put("temperature", temperature);
            makeBDJson.put("sBeaconID", sBeaconID);
            makeBDJson.put("iBeaconUUID", iBeaconUUID);
            makeBDJson.put("iBeaconMajorID", iBeaconMajorID);
            makeBDJson.put("iBeaconMinorID", iBeaconMinorID);
            makeBDJson.put("uidInstance", uidInstance);
            makeBDJson.put("uidNamespace", uidNamespace);
            makeBDJson.put("latlong", latlong);

            bdHelper.updateBeacon(getContext(), makeBDJson);


        } catch (InterruptedException | ExecutionException | JSONException | IOException e) {
            e.printStackTrace();
        }

        ((Button) rootView.findViewById(R.id.btnConnect)).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Toast.makeText(getActivity(), "Connecting..", Toast.LENGTH_SHORT).show();

                        if (sBeacon != null) {

                            sBeacon.setBeaconConfigurationListener(BeaconDetailFragment.this);

                            String password = ((EditText) rootView.findViewById(R.id.txtPassword)).getText().toString();

                            // if password is empty, try to connect to the beacon without password
                            sBeacon.connect(getActivity(), password);
                        } else {
                            Toast.makeText(getActivity(), "BEACON is NULL", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        ((Button) rootView.findViewById(R.id.btnDisconnect)).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (sBeacon != null) {
                            sBeacon.disconnect();
                        }
                    }
                }
        );

        ((Button) rootView.findViewById(R.id.btnDisconnect)).setEnabled(
                !((Button) rootView.findViewById(R.id.btnConnect)).isClickable());

        return rootView;
    }

    public void setBeaconInstance(BeaconInstance mBeaconInstance) {
        this.mBeaconInstance = mBeaconInstance;
    }

    @Override
    public void onConnect(boolean connected, boolean authenticated) {
        if (connected && authenticated) {
            sBeacon.alert(true, true);

//            ConfigurableBeacon configurableBeacon = sBeacon;

//            configurableBeacon.readIBeaconUUID();

            Toast.makeText(getActivity(), "Connected", Toast.LENGTH_SHORT).show();

            mInterface.onBeaconInstanceConnectedToConfig(mBeaconInstance);

        } else {
            Toast.makeText(getActivity(), "Connection Failed", Toast.LENGTH_SHORT).show();
        }
    }

    public BeaconInstance getBeaconInstance() {
        return mBeaconInstance;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void refreshStatus(BeaconInstance beaconInstance) throws InterruptedException, ExecutionException, JSONException, IOException {

        Log.i(TAG, "Start");
        if (beaconInstance != null) {
            Log.e(TAG, mBeaconInstance.name + " " + mBeaconInstance.uidNamespace);

            ((TextView) rootView.findViewById(R.id.txtName)).setText(beaconInstance.name);
            ((TextView) rootView.findViewById(R.id.txtRSSI)).setText(beaconInstance.rssi);
            ((TextView) rootView.findViewById(R.id.txtMac)).setText(beaconInstance.macAddress);

            TextView txtName = (TextView) rootView.findViewById(R.id.txtName);
            TextView txtRSSI = (TextView) rootView.findViewById(R.id.txtRSSI);
            TextView txtMac = (TextView) rootView.findViewById(R.id.txtMac);
            TextView txtTypeSBeacon = (TextView) rootView.findViewById(R.id.txtTypeSBeacon);
            TextView txtTypeIBeacon = (TextView) rootView.findViewById(R.id.txtTypeIBeacon);
            TextView txtTypeEddystoneUID = (TextView) rootView.findViewById(R.id.txtTypeEddystoneUID);
            TextView txtTypeEddystoneURL = (TextView) rootView.findViewById(R.id.txtTypeEddystoneURL);
            TextView txtTypeEddystoneTLM = (TextView) rootView.findViewById(R.id.txtTypeEddystoneTLM);

            TextView txtSBeaconID = (TextView) rootView.findViewById(R.id.textView5);
            TextView txtUUID = (TextView) rootView.findViewById(R.id.textView8);
            TextView txtMajorID = (TextView) rootView.findViewById(R.id.textView18);
            TextView txtMinorID = (TextView) rootView.findViewById(R.id.textView22);
            TextView txtUIDNamespace = (TextView) rootView.findViewById(R.id.textView11);
            TextView txtUIDInstance = (TextView) rootView.findViewById(R.id.textView24);
            TextView txtURL = (TextView) rootView.findViewById(R.id.textView14);
            TextView txtBatteryUsage = (TextView) rootView.findViewById(R.id.textView17);

            txtName.setText(beaconInstance.name);
            txtRSSI.setText(beaconInstance.rssi);
            txtMac.setText(beaconInstance.macAddress);

            name = beaconInstance.name;
            rssi = beaconInstance.rssi;
            mac = beaconInstance.macAddress;

            uuid = bdHelper.getUUID(getContext(), mac);

            for (String tt : beaconInstance.type.keySet()) {
                switch (tt) {
                    case "S_BEACON": txtTypeSBeacon.setTextColor(Color.rgb(66, 133, 244));
                        ((TextView) rootView.findViewById(R.id.textView2)).setTextColor(Color.rgb(66, 133, 244));
                        for (Beacon beacon : beaconInstance.beaconList) {
                            if (beacon.getBeaconType() == BeaconType.S_BEACON) {
                                sBeacon = (SBeacon) beacon;
                                Log.i("refreshStatus", "s");
                                break;
                            }
                        }
                        txtSBeaconID.setText(beaconInstance.sBeaconID);
                        txtSBeaconID.setTextColor(Color.BLACK);

                        sBeaconID = beaconInstance.sBeaconID;

                        break;
                    case "I_BEACON": txtTypeIBeacon.setTextColor(Color.rgb(219,68,55));
                        Log.i("refreshStatus", "i");
                        ((TextView) rootView.findViewById(R.id.textView6)).setTextColor(Color.rgb(219, 68, 55));
                        txtUUID.setText(beaconInstance.uuid);
                        txtUUID.setTextColor(Color.BLACK);
                        txtMajorID.setText(beaconInstance.iBeaconMajorID);
                        txtMinorID.setText(beaconInstance.iBeaconMinorID);
                        txtMajorID.setTextColor(Color.BLACK);
                        txtMinorID.setTextColor(Color.BLACK);

                        iBeaconUUID = beaconInstance.uuid;
                        iBeaconMajorID = beaconInstance.iBeaconMajorID;
                        iBeaconMinorID = beaconInstance.iBeaconMinorID;

                        break;
                    case "EDDYSTONE_UID_BEACON": txtTypeEddystoneUID.setTextColor(Color.rgb(244,180,80));
                        Log.i("refreshStatus", "euid");
                        ((TextView) rootView.findViewById(R.id.textView9)).setTextColor(Color.rgb(244, 180, 80));
                        txtUIDNamespace.setText(beaconInstance.uidNamespace);
                        txtUIDInstance.setText(beaconInstance.uidInstance);
                        txtUIDNamespace.setTextColor(Color.BLACK);
                        txtUIDInstance.setTextColor(Color.BLACK);

                        uidNamespace = beaconInstance.uidNamespace;
                        uidInstance = beaconInstance.uidInstance;

                        break;
                    case "EDDYSTONE_URL_BEACON": txtTypeEddystoneURL.setTextColor(Color.rgb(15,157,88));
                        Log.i("refreshStatus", "eurl");
                        ((TextView) rootView.findViewById(R.id.textView12)).setTextColor(Color.rgb(15, 157, 88));
                        txtURL.setText(beaconInstance.url);
                        txtURL.setTextColor(Color.BLACK);

                        url = beaconInstance.url;

                        break;
                    case "EDDYSTONE_TLM_BEACON": txtTypeEddystoneTLM.setTextColor(Color.rgb(234,188,255));
                        Log.i("refreshStatus", "etlm");
                        ((TextView) rootView.findViewById(R.id.textView15)).setTextColor(Color.rgb(234, 188, 255));
                        txtBatteryUsage.setText(beaconInstance.batteryUsage);
                        txtBatteryUsage.setTextColor(Color.BLACK);

                        battery = beaconInstance.batteryUsage;
                        for (Beacon beacon : beaconInstance.beaconList) {
                            if(beacon.getBeaconType() == BeaconType.EDDYSTONE_TLM_BEACON){
                                temperature = String.valueOf(beacon.getTemperature());
                            }
                        }
                            break;
                }
            }

        }
    }

    @Override
    public void onDisconnect() {

        Toast.makeText(getActivity(), "Disconnected", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onCommandToNotConnectedBeacon() {

    }

    @Override
    public void onReadConnectionSettings(int i, int i1, int i2, int i3) {

    }

    @Override
    public void onSetConnectionSettings(int i, int i1, int i2, int i3) {

    }

    @Override
    public void onFailedToReadConnectionSettings() {

    }

    @Override
    public void onFailedToSetConnectionSettings() {

    }

    @Override
    public void onReadTemperature(double v) {

    }

    @Override
    public void onFailedToReadTemperature() {

    }

    @Override
    public void onConnectionExist() {

    }

    @Override
    public void onReadIBeaconUUID(UUID uuid) {

    }

    @Override
    public void onSetIBeaconUUID(UUID uuid) {

    }

    @Override
    public void onFailedToReadIBeaconUUID() {

    }

    @Override
    public void onFailedToSetIBeaconUUID() {

    }

    @Override
    public void onReadIBeaconMajorAndMinor(int i, int i1) {

    }

    @Override
    public void onSetIBeaconMajorAndMinor(int i, int i1) {

    }

    @Override
    public void onFailedToReadIBeaconMajorAndMinor() {

    }

    @Override
    public void onFailedToSetIBeaconMajorAndMinor() {

    }

    @Override
    public void onReadEddystoneUID(byte[] bytes, byte[] bytes1) {

    }

    @Override
    public void onSetEddystoneUID(byte[] bytes, byte[] bytes1) {

    }

    @Override
    public void onFailedToReadEddystoneUID() {

    }

    @Override
    public void onFailedToSetEddystoneUID() {

    }

    @Override
    public void onReadEddystoneURL(String s) {

    }

    @Override
    public void onSetEddystoneURL(String s) {

    }

    @Override
    public void onFailedToReadEddystoneURL() {

    }

    @Override
    public void onFailedToSetEddystoneURL() {

    }

    @Override
    public void onReadDeviceStatus(float v, float v1, short i) {

    }

    @Override
    public void onFailedToReadDeviceStatus() {

    }

    @Override
    public void onReadFrameTypeIntervalTxPower(byte b, byte b1, byte b2, float v, float v1) {

    }

    @Override
    public void onSetFrameTypeIntervalTxPower(byte b, byte b1, byte b2, float v, float v1) {

    }

    @Override
    public void onFailedToReadFrameTypeIntervalTxPower() {

    }

    @Override
    public void onFailedToSetFrameTypeIntervalTxPower() {

    }

    @Override
    public void onSetFrameTypeConnectionRates(byte b, byte b1, byte b2) {

    }

    @Override
    public void onReadFrameTypeConnectionRates(byte b, byte b1, byte b2) {

    }

    @Override
    public void onFailedToReadFrameTypeConnectionRates() {

    }

    @Override
    public void onFailedToSetFrameTypeConnectionRates() {

    }

    @Override
    public void onReadAdvertisementSettings(float v, float v1, float v2) {

    }

    @Override
    public void onSetAdvertisementSettings(float v, float v1, float v2) {

    }

    @Override
    public void onFailedToReadAdvertisementSettings() {

    }

    @Override
    public void onFailedToSetAdvertisementSettings() {

    }

    @Override
    public void onSetAccelerometerConfiguration() {

    }

    @Override
    public void onFailedToSetAccelerometerConfiguration() {

    }

    @Override
    public void onSetPassword(boolean b) {

    }

    @Override
    public void onUpdateFirmware(double v) {

    }

    @Override
    public void onFailedToUpdateFirmware(int i) {

    }
}
