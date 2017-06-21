package giotto.iot.bdbeeksbeacon.Fragments;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.bluvision.beeks.sdk.constants.BeaconType;
import com.bluvision.beeks.sdk.domainobjects.Beacon;
import com.bluvision.beeks.sdk.domainobjects.ConfigurableBeacon;
import com.bluvision.beeks.sdk.domainobjects.Eddystone;
import com.bluvision.beeks.sdk.domainobjects.EddystoneUIDBeacon;
import com.bluvision.beeks.sdk.domainobjects.EddystoneURLBeacon;
import com.bluvision.beeks.sdk.domainobjects.IBeacon;
import com.bluvision.beeks.sdk.util.BeaconManager;

import java.util.UUID;

import giotto.iot.bdbeeksbeacon.BeaconInstance;
import giotto.iot.bdbeeksbeacon.BeeksBeaconApp;
import giotto.iot.bdbeeksbeacon.MainActivity;
import giotto.iot.bdbeeksbeacon.R;


public class BeaconConfigFragment extends BaseFragment{

    private View rootView;

    private BeaconManager mBeaconManager;
    private BeaconInstance mBeaconInstance;
    private ConfigurableBeacon mEddystoneBeacon = null;
    private ConfigurableBeacon mSBeacon = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_config, container, false);

        mBeaconManager = ((BeeksBeaconApp) getActivity().getApplication()).getBeaconManager();

        getConfigurableBeacon();

        presetAllInformation();

        Button btnSet = (Button) rootView.findViewById(R.id.btnSet);

        btnSet.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                final String newUrl = ((EditText) rootView.findViewById(R.id.txtNewUrl)).getText().toString();
                final String newNamespace = ((EditText) rootView.findViewById(R.id.txtNamespace)).getText().toString();
                final String newInstance = ((EditText) rootView.findViewById(R.id.txtInstance)).getText().toString();
                final String newUUID = ((EditText) rootView.findViewById(R.id.txtUUID)).getText().toString();
                final String newMajorID = ((EditText) rootView.findViewById(R.id.txtMajorID)).getText().toString();
                final String newMinorID = ((EditText) rootView.findViewById(R.id.txtMinorID)).getText().toString();

                // TODO: clarify this stuff up
                if (!newUrl.isEmpty()) {
                    mEddystoneBeacon.setEddystoneUrl(newUrl);
                    mBeaconInstance.url = "http://www." + newUrl;
                }
                if (!newNamespace.isEmpty() && !newInstance.isEmpty()) {

                    byte[] namespace = newNamespace.getBytes(), instance = newInstance.getBytes();
                    mEddystoneBeacon.setEddystoneUID(namespace, instance);
                    mBeaconInstance.uidNamespace = newNamespace;
                    mBeaconInstance.uidInstance = newInstance;
                }
                if (!newUUID.isEmpty()) {

                    mSBeacon.setIBeaconUUID(UUID.fromString(newUUID));
                    mBeaconInstance.uuid = newUUID;
                }
                if (!newMajorID.isEmpty() && !newMinorID.isEmpty()) {

                    int major = Integer.parseInt(newMajorID), minor = Integer.parseInt(newMinorID);
                    mSBeacon.setIBeaconMajorMinor(major, minor);
                    mBeaconInstance.iBeaconMajorID = newMajorID;
                    mBeaconInstance.iBeaconMinorID = newMinorID;
                }

                BeaconDetailFragment retFragment = (BeaconDetailFragment) ((MainActivity) getActivity())
                        .getFragment(BeaconDetailFragment.class, true);

                retFragment.setBeaconInstance(mBeaconInstance);

                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.fragmentContainer, retFragment, retFragment.getClass().getName());
                ft.commitAllowingStateLoss();
//                getFragmentManager().popBackStack();
                mSBeacon.disconnect();
            }
        });

        return rootView;
    }

    private void presetAllInformation() {

        ((EditText) rootView.findViewById(R.id.txtNewUrl)).setHint(mBeaconInstance.url);
        ((EditText) rootView.findViewById(R.id.txtNamespace)).setHint(mBeaconInstance.uidNamespace);
        ((EditText) rootView.findViewById(R.id.txtInstance)).setHint(mBeaconInstance.uidInstance);
        ((EditText) rootView.findViewById(R.id.txtUUID)).setHint(mBeaconInstance.uuid);
        ((EditText) rootView.findViewById(R.id.txtMajorID)).setHint(mBeaconInstance.iBeaconMajorID);
        ((EditText) rootView.findViewById(R.id.txtMinorID)).setHint(mBeaconInstance.iBeaconMinorID);

        ((EditText) rootView.findViewById(R.id.txtNewUrl)).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    ((EditText) rootView.findViewById(R.id.txtNewUrl)).setHint("");
                } else if (((EditText) rootView.findViewById(R.id.txtNewUrl)).getText().toString() == "") {
                    ((EditText) rootView.findViewById(R.id.txtNewUrl)).setHint(mBeaconInstance.url);
                }
            }
        });
        ((EditText) rootView.findViewById(R.id.txtNamespace)).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    ((EditText) rootView.findViewById(R.id.txtNamespace)).setHint("");
                } else if (((EditText) rootView.findViewById(R.id.txtNamespace)).getText().toString() == "") {
                    ((EditText) rootView.findViewById(R.id.txtNamespace)).setHint(mBeaconInstance.uidNamespace);
                }
            }
        });
        ((EditText) rootView.findViewById(R.id.txtInstance)).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    ((EditText) rootView.findViewById(R.id.txtInstance)).setHint("");
                } else if (((EditText) rootView.findViewById(R.id.txtInstance)).getText().toString() == "") {
                    ((EditText) rootView.findViewById(R.id.txtInstance)).setHint(mBeaconInstance.uidInstance);
                }
            }
        });
        ((EditText) rootView.findViewById(R.id.txtUUID)).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    ((EditText) rootView.findViewById(R.id.txtUUID)).setHint("");
                } else if (((EditText) rootView.findViewById(R.id.txtUUID)).getText().toString() == "") {
                    ((EditText) rootView.findViewById(R.id.txtUUID)).setHint(mBeaconInstance.uuid);
                }
            }
        });
        ((EditText) rootView.findViewById(R.id.txtMajorID)).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    ((EditText) rootView.findViewById(R.id.txtMajorID)).setHint("");
                } else if (((EditText) rootView.findViewById(R.id.txtMajorID)).getText().toString() == "") {
                    ((EditText) rootView.findViewById(R.id.txtMajorID)).setHint(mBeaconInstance.iBeaconMajorID);
                }
            }
        });
        ((EditText) rootView.findViewById(R.id.txtMinorID)).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    ((EditText) rootView.findViewById(R.id.txtMinorID)).setHint("");
                } else if (((EditText) rootView.findViewById(R.id.txtMinorID)).getText().toString() == "") {
                    ((EditText) rootView.findViewById(R.id.txtMinorID)).setHint(mBeaconInstance.iBeaconMinorID);
                }
            }
        });
    }

    public void getConfigurableBeacon() {

        for (Beacon beacon : mBeaconInstance.beaconList) {
//            mEddystoneBeacon = (ConfigurableBeacon) beacon;
//            mSBeacon = (ConfigurableBeacon) beacon;
            if (beacon.getBeaconType() == BeaconType.EDDYSTONE_URL_BEACON) {
                mEddystoneBeacon = (ConfigurableBeacon) beacon;
                mSBeacon = (ConfigurableBeacon) beacon;
            } else if (beacon.getBeaconType() == BeaconType.S_BEACON) {
                mSBeacon = (ConfigurableBeacon) beacon;
            }
        }
    }



    public void setBeaconInstance(BeaconInstance beaconInstance) {
        this.mBeaconInstance = beaconInstance;
    }
}
