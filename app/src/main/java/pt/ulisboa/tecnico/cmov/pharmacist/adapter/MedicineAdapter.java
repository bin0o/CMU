package pt.ulisboa.tecnico.cmov.pharmacist.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmov.pharmacist.R;

public class MedicineAdapter extends ArrayAdapter<String> implements Filterable {

    private Context context;
    private List<String> medicines;
    private List<String> filteredMedicines;
    private MedicineFilter medicineFilter;
    private OnAddMedicineClickListener listener;

    public MedicineAdapter(Context context, List<String> medicines, OnAddMedicineClickListener listener) {
        super(context, R.layout.medicines_list_item, medicines);
        this.context = context;
        this.medicines = new ArrayList<>(medicines);
        this.filteredMedicines = new ArrayList<>(medicines);
        this.listener = listener;
        getFilter();
    }

    @Override
    public int getCount() {
        return filteredMedicines.size();
    }

    @Override
    public String getItem(int position) {
        return filteredMedicines.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.medicines_list_item, parent, false);
        }

        TextView medicineName = convertView.findViewById(R.id.medicine_name);
        Button addMedicineButton = convertView.findViewById(R.id.add_medicine);

        String medicine = filteredMedicines.get(position);
        medicineName.setText(medicine);

        addMedicineButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddMedicineClick(medicine);
            }
        });

        return convertView;
    }

    @Override
    public Filter getFilter() {
        if (medicineFilter == null) {
            medicineFilter = new MedicineFilter();
        }
        return medicineFilter;
    }

    private class MedicineFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if (constraint == null || constraint.length() == 0) {
                results.values = medicines;
                results.count = medicines.size();
            } else {
                List<String> filteredList = new ArrayList<>();
                String filterString = constraint.toString().toLowerCase();

                for (String medicine : medicines) {
                    if (medicine.toLowerCase().contains(filterString)) {
                        filteredList.add(medicine);
                    }
                }

                results.values = filteredList;
                results.count = filteredList.size();
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredMedicines = (List<String>) results.values;
            notifyDataSetChanged();
        }
    }

    public interface OnAddMedicineClickListener {
        void onAddMedicineClick(String medicineName);
    }
}


