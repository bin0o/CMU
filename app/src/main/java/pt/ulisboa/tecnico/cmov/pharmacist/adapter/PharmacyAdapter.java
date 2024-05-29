package pt.ulisboa.tecnico.cmov.pharmacist.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import pt.ulisboa.tecnico.cmov.pharmacist.DatabaseClasses.Pharmacy;
import pt.ulisboa.tecnico.cmov.pharmacist.R;
import pt.ulisboa.tecnico.cmov.pharmacist.helper.PharmacyHelper;

public class PharmacyAdapter extends BaseAdapter {

    private static final String TAG = "PharmacyAdapter";
    private Context context;
    private List<PharmacyHelper> pharmacies;

    public PharmacyAdapter(Context context, List<PharmacyHelper> pharmacies) {
        this.context = context;
        this.pharmacies = pharmacies;
    }

    @Override
    public int getCount() {
        return pharmacies.size();
    }

    @Override
    public Object getItem(int position) {
        return pharmacies.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.pharmacy_list_item, parent, false);
        }

        PharmacyHelper pharmacy = pharmacies.get(position);

        Log.d(TAG, "pharmacy: " + pharmacy);

        TextView pharmacyName = convertView.findViewById(R.id.pharmacy_name);
        TextView pharmacyDistance = convertView.findViewById(R.id.pharmacy_distance);

        pharmacyName.setText(pharmacy.getName());
        pharmacyDistance.setText(String.format("%.2f m", pharmacy.getDistance()));

        return convertView;
    }
}

