package giotto.iot.beeksmap;

import android.app.Application;
import android.util.Log;

import com.bluvision.beeks.sdk.util.BeaconManager;

public class BeeksBeaconApp extends Application {

    private BeaconManager mBeaconManager;

    @Override
    public void onCreate() {
        super.onCreate();
        mBeaconManager = new BeaconManager(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        mBeaconManager.stop();
    }

    public BeaconManager getBeaconManager() {
        return mBeaconManager;
    }

    public void setBeaconManager(BeaconManager mBeaconManager) {
        this.mBeaconManager = mBeaconManager;
    }
}
