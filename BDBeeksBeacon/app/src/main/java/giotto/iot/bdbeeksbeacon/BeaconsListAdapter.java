package giotto.iot.bdbeeksbeacon;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;


public class BeaconsListAdapter extends BaseAdapter {

    private List<BeaconInstance> listData;
    private Context mContext;
    private static LayoutInflater inflater = null;

    public BeaconsListAdapter(Context mContext, List<BeaconInstance> listData) {
        this.mContext = mContext;
        this.listData = listData;
    }

    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public int getPosition(BeaconInstance beaconInstance) {
        return listData.indexOf(beaconInstance);
    }

    public void clear() {
        listData.clear();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE
            );
            convertView = layoutInflater.inflate(R.layout.item_beacon, null);
        }

        Holder holder = new Holder();

        holder.tvName = (TextView) convertView.findViewById(R.id.txtName);
        holder.tvRSSI = (TextView) convertView.findViewById(R.id.txtRSSI);
        holder.tvMac = (TextView) convertView.findViewById(R.id.txtMac);
        holder.tvTypeSBeacon = (TextView) convertView.findViewById(R.id.txtTypeSBeacon);
        holder.tvTypeIBeacon = (TextView) convertView.findViewById(R.id.txtTypeIBeacon);
        holder.tvTypeEddystoneUID = (TextView) convertView.findViewById(R.id.txtTypeEddystoneUID);
        holder.tvTypeEddystoneURL = (TextView) convertView.findViewById(R.id.txtTypeEddystoneURL);
        holder.tvTypeEddystoneTLM = (TextView) convertView.findViewById(R.id.txtTypeEddystoneTLM);

        BeaconInstance beaconInstance = listData.get(position);
        String name = "Ready to Go!";

        if (beaconInstance == null) {
            return null;
        }

        if (beaconInstance.name != null && !beaconInstance.name.isEmpty()) {
            name = beaconInstance.name;
//            Log.i("Adapter", "Beacon Name: " + name + ", pos: " + position);

        }

        holder.tvName.setText(name);
        holder.tvRSSI.setText(beaconInstance.rssi);
        holder.tvMac.setText(beaconInstance.macAddress);

        for (String tt : beaconInstance.type.keySet()) {
            switch (tt) {
                case "S_BEACON": holder.tvTypeSBeacon.setTextColor(Color.rgb(66,133,244));
                    break;
                case "I_BEACON": holder.tvTypeIBeacon.setTextColor(Color.rgb(219,68,55));
                    break;
                case "EDDYSTONE_UID_BEACON": holder.tvTypeEddystoneUID.setTextColor(Color.rgb(244,180,80));
                    break;
                case "EDDYSTONE_URL_BEACON": holder.tvTypeEddystoneURL.setTextColor(Color.rgb(15,157,88));
                    break;
                case "EDDYSTONE_TLM_BEACON": holder.tvTypeEddystoneTLM.setTextColor(Color.rgb(234,188,255));
                    break;
            }
        }

        switch (beaconInstance.status) {
            case BeaconInstance.NOT_AUTHORIZED:
                holder.tvName.setTextColor(Color.MAGENTA);
                break;
            case BeaconInstance.UNREGISTERED:
                holder.tvName.setTextColor(Color.YELLOW);
                break;
            case BeaconInstance.STATUS_ACTIVE:
                holder.tvName.setTextColor(Color.GREEN);
                break;
            case BeaconInstance.STABILITY_UNSPECIFIED:
                holder.tvName.setTextColor(Color.BLUE);
                break;
            case BeaconInstance.STATUS_DECOMMISSIONED:
                holder.tvName.setTextColor(Color.RED);
                break;
            case BeaconInstance.STATUS_INACTIVE:
                holder.tvName.setTextColor(Color.GRAY);
                break;
            case BeaconInstance.STATUS_UNSPECIFIED:
                holder.tvName.setTextColor(Color.LTGRAY);
                break;
            default:
                holder.tvName.setTextColor(Color.BLACK);
        }

        return convertView;
    }

    @Override
    public void notifyDataSetChanged() {

        Collections.sort(listData);

        super.notifyDataSetChanged();
    }

    public class Holder {

        TextView tvName;
        TextView tvRSSI;
        TextView tvMac;
        TextView tvTypeSBeacon;
        TextView tvTypeIBeacon;
        TextView tvTypeEddystoneUID;
        TextView tvTypeEddystoneURL;
        TextView tvTypeEddystoneTLM;
    }

}
