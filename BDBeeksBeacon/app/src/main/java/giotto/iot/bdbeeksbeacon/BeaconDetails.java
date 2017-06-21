package giotto.iot.bdbeeksbeacon;

import android.content.Context;

import com.bluvision.beeks.sdk.constants.BeaconType;
import com.bluvision.beeks.sdk.domainobjects.Beacon;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Created by GK on 6/12/17.
 */

public class BeaconDetails {

    public void updateBeacon(BeaconInstance beaconInstance, Context context) throws InterruptedException, ExecutionException, JSONException, IOException {

        String mac = "", url = "", battery = "", eid = "", name = "", rssi = "";
        String iBeaconMajorID = "", iBeaconMinorID = "", iBeaconUUID = "", latlong = "";
        String sBeaconID = "", temperature = "", uidInstance = "", uidNamespace = "", uuid = "";

        BDHelper bdHelper = new BDHelper();
        JSONObject makeBDJson = new JSONObject();

        if (beaconInstance != null) {

            name = beaconInstance.name;
            rssi = beaconInstance.rssi;
            mac = beaconInstance.macAddress;

            uuid = bdHelper.getUUID(context, mac);

            for (String tt : beaconInstance.type.keySet()) {
                switch (tt) {
                    case "S_BEACON":
                        sBeaconID = beaconInstance.sBeaconID;
                        break;
                    case "I_BEACON":
                        iBeaconUUID = beaconInstance.uuid;
                        iBeaconMajorID = beaconInstance.iBeaconMajorID;
                        iBeaconMinorID = beaconInstance.iBeaconMinorID;
                        break;
                    case "EDDYSTONE_UID_BEACON":
                        uidNamespace = beaconInstance.uidNamespace;
                        uidInstance = beaconInstance.uidInstance;
                        break;
                    case "EDDYSTONE_URL_BEACON":
                        url = beaconInstance.url;
                        break;
                    case "EDDYSTONE_TLM_BEACON":
                        battery = beaconInstance.batteryUsage;
                        for (Beacon beacon : beaconInstance.beaconList) {
                            if(beacon.getBeaconType() == BeaconType.EDDYSTONE_TLM_BEACON){
                                temperature = String.valueOf(beacon.getTemperature());
                            }
                        }
                        break;
                }
            }

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
            makeBDJson.put("latlong", "");

            bdHelper.updateBeacon(context, makeBDJson);
        }
    }

}
