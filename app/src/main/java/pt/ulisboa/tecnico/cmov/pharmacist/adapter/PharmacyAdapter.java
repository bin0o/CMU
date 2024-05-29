package pt.ulisboa.tecnico.cmov.pharmacist.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmov.pharmacist.DatabaseClasses.Pharmacy;
import pt.ulisboa.tecnico.cmov.pharmacist.R;
import pt.ulisboa.tecnico.cmov.pharmacist.helper.PharmacyHelper;

public class PharmacyAdapter extends BaseAdapter implements Filterable {

    private static final String TAG = "PharmacyAdapter";
    private Context context;
    private List<PharmacyHelper> pharmacies;
    private List<PharmacyHelper> filteredPharmacies;
    private PharmacyFilter pharmacyFilter;

    public PharmacyAdapter(Context context, List<PharmacyHelper> pharmacies) {
        this.context = context;
        this.pharmacies = pharmacies;
        this.filteredPharmacies = pharmacies;
        getFilter();
    }

    @Override
    public int getCount() {
        return filteredPharmacies.size();
    }

    @Override
    public Object getItem(int position) {
        return filteredPharmacies.get(position);
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

        PharmacyHelper pharmacy = filteredPharmacies.get(position);

        Log.d(TAG, "pharmacy: " + pharmacy);

        TextView pharmacyName = convertView.findViewById(R.id.pharmacy_name);
        TextView pharmacyDistance = convertView.findViewById(R.id.pharmacy_distance);

        pharmacyName.setText(pharmacy.getName());
        pharmacyDistance.setText(String.format("%.2f km", pharmacy.getDistance() / 1000));

        return convertView;
    }

    @Override
    public Filter getFilter() {
        if (pharmacyFilter == null) {
            pharmacyFilter = new PharmacyFilter();
        }
        return pharmacyFilter;
    }

    private class PharmacyFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if (constraint == null || constraint.length() == 0) {
                // No constraint, return the original list
                results.values = pharmacies;
                results.count = pharmacies.size();
            } else {
                // Perform filtering operation
                String filterString = constraint.toString().toLowerCase();
                List<PharmacyHelper> filteredList = new ArrayList<>();

                for (PharmacyHelper pharmacy : pharmacies) {
                    if (pharmacy.getName().toLowerCase().contains(filterString)) {
                        filteredList.add(pharmacy);
                    }
                }

                results.values = filteredList;
                results.count = filteredList.size();
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredPharmacies = (List<PharmacyHelper>) results.values;
            notifyDataSetChanged();
        }
    }
}
