package giotto.iot.beeksmap;


import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.bluvision.beeks.sdk.domainobjects.Beacon;
import com.bluvision.beeks.sdk.domainobjects.EddystoneTLMBeacon;
import com.bluvision.beeks.sdk.domainobjects.EddystoneUIDBeacon;
import com.bluvision.beeks.sdk.domainobjects.EddystoneURLBeacon;
import com.bluvision.beeks.sdk.domainobjects.IBeacon;
import com.bluvision.beeks.sdk.domainobjects.SBeacon;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static com.bluvision.beeks.sdk.constants.BeaconType.EDDYSTONE_TLM_BEACON;
import static com.bluvision.beeks.sdk.constants.BeaconType.EDDYSTONE_UID_BEACON;
import static com.bluvision.beeks.sdk.constants.BeaconType.EDDYSTONE_URL_BEACON;
import static com.bluvision.beeks.sdk.constants.BeaconType.I_BEACON;
import static com.bluvision.beeks.sdk.constants.BeaconType.S_BEACON;


public class BeaconInstance implements Comparable<BeaconInstance>, Parcelable {
    private static final String TAG = BeaconInstance.class.getSimpleName();

    // These constants are in the Proximity Service Status enum:
    public static final String STATUS_UNSPECIFIED = "STATUS_UNSPECIFIED";
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";
    public static final String STATUS_DECOMMISSIONED = "DECOMMISSIONED";
    public static final String STABILITY_UNSPECIFIED = "STABILITY_UNSPECIFIED";

    // These constants are convenience for this app:
    public static final String UNREGISTERED = "UNREGISTERED";
    public static final String NOT_AUTHORIZED = "NOT_AUTHORIZED";

    public String name;
    public String macAddress;
    public HashMap<String, Boolean> type;
    public String rssi;
    public String sBeaconID;
    public String uidNamespace;
    public String uidInstance;
    public String url;
    public String batteryUsage;
    public String iBeaconMajorID;
    public String iBeaconMinorID;
    public String uuid;
    public List<Beacon> beaconList;

    public byte[] id;
    public String status;
    public String placeId;
    public Double latitude;
    public Double longitude;
    public String expectedStability;
    public String description;

    public String googleType;

    public BeaconInstance(String macAddress) {
        this.macAddress = macAddress;
        this.type = new HashMap<>();
        this.beaconList = new ArrayList<>();
        this.status = null;
        this.placeId = null;
        this.latitude = null;
        this.longitude = null;
        this.expectedStability = null;
        this.description = null;

        this.status = STATUS_UNSPECIFIED;
        this.id = null;
        this.googleType = null;
    }

    public BeaconInstance(JSONObject response, BeaconInstance beaconInstance) {
//        Log.d(TAG, "BI, JSON resp: " + response);
        try {
            JSONObject json = response.getJSONObject("advertisedId");
            id = Utils.base64Decode(json.getString("id"));
        } catch (JSONException e) {
            // NOP
        }

        try {
            status = response.getString("status");
        } catch (JSONException e) {
            status = STATUS_UNSPECIFIED;
        }

        try {
            placeId = response.getString("placeId");
        } catch (JSONException e) {
            //
        }

        try {
            JSONObject latLngJson = response.getJSONObject("latLng");
            latitude = latLngJson.getDouble("latitude");
            longitude = latLngJson.getDouble("longitude");
        } catch (JSONException e) {
            latitude = null;
            longitude = null;
        }

        try {
            expectedStability = response.getString("expectedStability");
        } catch (JSONException e) {
            //
        }

        try {
            description = response.getString("description");
        } catch (JSONException e) {
            //
        }

        beaconList = beaconInstance.beaconList;
        type = beaconInstance.type;
        name = beaconInstance.name;
        macAddress = beaconInstance.macAddress;
    }

    public BeaconInstance(String status, BeaconInstance beaconInstance) {

        this.beaconList = beaconInstance.beaconList;
        this.type = beaconInstance.type;
        this.name = beaconInstance.name;
        this.macAddress = beaconInstance.macAddress;
        this.status = status;

        this.placeId = null;
        this.latitude = null;
        this.longitude = null;
        this.expectedStability = null;
        this.description = null;
        this.rssi = beaconInstance.rssi;
        this.id = beaconInstance.id;
    }

    public boolean updateBeaconInstance(Beacon beacon) {

        Log.i(TAG, beacon.getDevice().getName() + " " + beacon.getRssi() + "");

        if (Objects.equals(beacon.getDevice().getAddress(), this.macAddress) || this.macAddress == null) {

            this.name = beacon.getDevice().getName();
            this.macAddress = String.valueOf(beacon.getDevice().getAddress());
            this.type.put(beacon.getBeaconType().toString(), true);
            this.googleType = type.containsKey(EDDYSTONE_UID_BEACON.toString()) ? "EDDYSTONE" : "NOT EDDYSTONE";
//            Log.i(TAG, "googleType: " + googleType);
            this.rssi = String.valueOf(beacon.getRssi());

            if (beacon.getBeaconType() == S_BEACON) {
                this.sBeaconID = ((SBeacon) beacon).getsId();
            } else if (beacon.getBeaconType() == I_BEACON) {
                this.iBeaconMajorID = String.valueOf(((IBeacon) beacon).getMajor());
                this.iBeaconMinorID = String.valueOf(((IBeacon) beacon).getMinor());
                this.uuid = ((IBeacon) beacon).getUuid();
            } else if (beacon.getBeaconType() == EDDYSTONE_UID_BEACON) {
                try {
                    Log.i(TAG, ((EddystoneUIDBeacon) beacon).getNameSpace() + "");
                    byte[] ns = ((EddystoneUIDBeacon) beacon).getNameSpace(),
                            is = ((EddystoneUIDBeacon) beacon).getInstanceId();
                    this.uidNamespace = ns.toString();
                    this.uidInstance = is.toString();
                    this.id = Utils.hexToByteArray(((EddystoneUIDBeacon) beacon).getUID().replace("-", ""));
//                    Log.i(TAG, "esUid: " + ((EddystoneUIDBeacon) beacon).getUID() + ", fromId: " + Utils.toHexString(id));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else if (beacon.getBeaconType() == EDDYSTONE_TLM_BEACON) {
                this.batteryUsage = String.valueOf(((EddystoneTLMBeacon) beacon).getBattery());
            } else if (beacon.getBeaconType() == EDDYSTONE_URL_BEACON) {
                this.url = ((EddystoneURLBeacon) beacon).getURL();
            }

            if (!this.beaconList.contains(beacon)) {
                this.beaconList.add(beacon);
            }

            return true;
        } else {

            Log.i(TAG, "Update Failed");
            return false;
        }
    }

    public String getBeaconName() {
        return getHexId() == null ? null : String.format("beacons/3!%s", getHexId());
    }


    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        JSONObject advertisedId = new JSONObject()
                .put("type", googleType)
                .put("id", Utils.base64Encode(id));
        json.put("advertisedId", advertisedId);
        if (!status.equals(STATUS_UNSPECIFIED)) {
            json.put("status", status);
        }
        if (placeId != null) {
            json.put("placeId", placeId);
        }
        if (latitude != null && longitude != null) {
            JSONObject latLng = new JSONObject();
            latLng.put("latitude", latitude);
            latLng.put("longitude", longitude);
            json.put("latLng", latLng);
        }
        if (expectedStability != null && !expectedStability.equals(STABILITY_UNSPECIFIED)) {
            json.put("expectedStability", expectedStability);
        }
        if (description != null) {
            json.put("description", description);
        }
        // TODO: beacon properties
        return json;
    }

    public JSONObject toObservationJson() throws JSONException {
        if (!type.containsKey(EDDYSTONE_TLM_BEACON.toString())) return null;
        JSONObject json = new JSONObject();
        JSONObject ret = new JSONObject();
        JSONObject advertisedId = new JSONObject()
                .put("type", googleType)
                .put("id", Utils.base64Encode(id));
        json.put("advertisedId", advertisedId);
        for (Beacon beacon : beaconList) {
            if (beacon.getBeaconType() == EDDYSTONE_TLM_BEACON) {
                json.put("telemetry", encodeTelemetry((EddystoneTLMBeacon) beacon));
            }
        }
        json.put("timestampMs", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'000000Z'")
                .format(new Date()));
        ret.put("observations", new JSONArray().put(json));
        ret.put("namespacedTypes", new JSONArray().put("*"));
        Log.i(TAG, "Obeservation: " + ret);

        return ret;
    }

    /*
     * https://github.com/google/eddystone/blob/master/eddystone-tlm/tlm-plain.md
     */
    private String encodeTelemetry(EddystoneTLMBeacon beacon) {
        String battery = Utils.toHexString(beacon.getBattery()),
                temperature = Utils.toHexString(beacon.getTemperature());
        byte[] telemetry = Utils.hexToByteArray("2000" + battery + temperature
                + "0100000001000000");
        return Utils.base64Encode(telemetry);
    }

    public String getHexId() {
        return Utils.toHexString(id);
    }


    @Override
    public int compareTo(BeaconInstance beaconInstance) {
        if (this.rssi == null) {
            return -1;
        }
        if (beaconInstance.rssi == null) {
            return 1;
        }

        if (Integer.parseInt(this.rssi) < Integer.parseInt(beaconInstance.rssi)) {
            return 1;
        } else if (Integer.parseInt(this.rssi) == Integer.parseInt(beaconInstance.rssi)) {
            return 0;
        } else {
            return -1;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    private BeaconInstance(Parcel source) {
        int size = source.readInt();
        for(int i = 0; i < size; i++){
            String key = source.readString();
            Boolean value = source.readByte() != 0;
            type.put(key, value);
        }
        googleType = source.readString();
        int len = source.readInt();
        id = new byte[len];
        source.readByteArray(id);
        status = source.readString();
        if (source.readInt() == 1) {
            placeId = source.readString();
        }
        if (source.readInt() == 1) {
            latitude = source.readDouble();
        }
        if (source.readInt() == 1) {
            longitude = source.readDouble();
        }
        if (source.readInt() == 1) {
            expectedStability = source.readString();
        }
        if (source.readInt() == 1) {
            description = source.readString();
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type.size());
        for(HashMap.Entry<String, Boolean> entry : type.entrySet()){
            dest.writeString(entry.getKey());
            dest.writeByte((byte) (entry.getValue() ? 1 : 0));
        }
        dest.writeString(googleType);
        dest.writeByteArray(id);
        dest.writeString(status);
        if (placeId != null) {
            dest.writeInt(1);
            dest.writeString(placeId);
        } else {
            dest.writeInt(0);
        }
        if (latitude != null) {
            dest.writeInt(1);
            dest.writeDouble(latitude);
        } else {
            dest.writeInt(0);
        }
        if (longitude != null) {
            dest.writeInt(1);
            dest.writeDouble(longitude);
        } else {
            dest.writeInt(0);
        }
        if (expectedStability != null ) {
            dest.writeInt(1);
            dest.writeString(expectedStability);
        } else {
            dest.writeInt(0);
        }
        if (description != null) {
            dest.writeInt(1);
            dest.writeString(description);
        } else {
            dest.writeInt(0);
        }
    }

    public static final Parcelable.Creator<BeaconInstance> CREATOR = new Parcelable.Creator<BeaconInstance>() {

        @Override
        public BeaconInstance createFromParcel(Parcel source) {
            return new BeaconInstance(source);
        }

        @Override
        public BeaconInstance[] newArray(int size) {
            return new BeaconInstance[size];
        }
    };
}
