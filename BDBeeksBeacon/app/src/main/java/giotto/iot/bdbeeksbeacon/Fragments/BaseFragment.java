package giotto.iot.bdbeeksbeacon.Fragments;

import android.app.Fragment;
import android.content.Context;

import giotto.iot.bdbeeksbeacon.BeaconInstance;


public class BaseFragment extends Fragment {

   protected FragmentInterface mInterface;

   @Override
   public void onAttach(Context context) {

      super.onAttach(context);

      try {
         mInterface = (FragmentInterface) context;
      } catch (ClassCastException e) {
         throw new ClassCastException(context.toString() +
                 " must implement BottomMenuInterface." );
      }
   }

   public interface FragmentInterface {

      void swapFragment(Class fragmentToOpen, boolean addToBackStack, boolean newInstance);

      Fragment getFragment(Class nameOfFragment, boolean newInstance);

      void onBeaconInstanceSelectedFromList(BeaconInstance beaconInstance);

      void onBeaconInstanceConnectedToConfig(BeaconInstance beaconInstance);
   }
}
