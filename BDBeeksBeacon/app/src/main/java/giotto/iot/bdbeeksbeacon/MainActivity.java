package giotto.iot.bdbeeksbeacon;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.HashMap;

import giotto.iot.bdbeeksbeacon.Fragments.BaseFragment;
import giotto.iot.bdbeeksbeacon.Fragments.BeaconConfigFragment;
import giotto.iot.bdbeeksbeacon.Fragments.BeaconDetailFragment;
import giotto.iot.bdbeeksbeacon.Fragments.ListBeaconsFragment;

public class MainActivity extends AppCompatActivity implements BaseFragment.FragmentInterface{

    private final static int PERMISSION_REQUEST_COARSE_LOCATION = 1984;

    private HashMap<Class, Fragment> mFragmentHashMap = new HashMap<>();
    private giotto.iot.bdbeeksbeacon.Fragments.BeaconDetailFragment mBeaconDetailFragment;
    private giotto.iot.bdbeeksbeacon.Fragments.BeaconConfigFragment mBeaconConfigFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        swapFragment(ListBeaconsFragment.class, true, false);

    }

    @Override
    public void onBackPressed() {

        int count = getFragmentManager().getBackStackEntryCount();

        if (count == 0) {
            super.onBackPressed();
        } else {
            getFragmentManager().popBackStack();
        }
        Log.i("onBackPressed", count + "");

    }

    @Override
    public void swapFragment(Class fragmentToOpen, boolean addToBackStack, boolean newInstance) {

        Fragment fragment = getFragment(fragmentToOpen, newInstance);

        if(fragment!=null) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();

            ft.replace(R.id.fragmentContainer, fragment, fragment.getClass().getName());
            if (addToBackStack) {
                ft.addToBackStack(fragment.getClass().getName());
            }
            ft.commitAllowingStateLoss();
        } else {
            Log.e("Fragment", "Fragment is null");
        }
    }

    @Override
    public Fragment getFragment(Class nameOfFragment, boolean newInstance) {

        Fragment fragment = mFragmentHashMap.get(nameOfFragment);
        if(fragment == null || newInstance){
            try {
                mFragmentHashMap.put(nameOfFragment, (Fragment) nameOfFragment.newInstance());
                fragment = mFragmentHashMap.get(nameOfFragment);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

        return fragment;
    }

    @Override
    public void onBeaconInstanceSelectedFromList(BeaconInstance beaconInstance) {

        mBeaconDetailFragment = (BeaconDetailFragment) getFragment(BeaconDetailFragment.class, true);
        mBeaconDetailFragment.setBeaconInstance(beaconInstance);

        swapFragment(BeaconDetailFragment.class, true, false);
    }

    @Override
    public void onBeaconInstanceConnectedToConfig(BeaconInstance beaconInstance) {

        mBeaconConfigFragment = (BeaconConfigFragment) getFragment(BeaconConfigFragment.class, true);
        mBeaconConfigFragment.setBeaconInstance(beaconInstance);

        swapFragment(BeaconConfigFragment.class, false, false);
    }

}
