package pt.ulisboa.tecnico.cmov.pharmacist.elements;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import pt.ulisboa.tecnico.cmov.pharmacist.R;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.Pharmacy;


public class PharmacyStockAdapter extends ArrayAdapter<Map.Entry<Pharmacy, Integer>> {
    private Context mContext;
    private List<Map.Entry<Pharmacy, Integer>> mEntries;

    public PharmacyStockAdapter(Context context, List<Map.Entry<Pharmacy, Integer>> entries) {
        super(context, 0, entries);
        mContext = context;
        mEntries = entries;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null) {
            listItem = LayoutInflater.from(mContext).inflate(R.layout.pharmacy_stock_item, parent, false);
        }

        Map.Entry<Pharmacy, Integer> entry = mEntries.get(position);
        Pharmacy pharmacy = entry.getKey();
        Integer quantity = entry.getValue();

        TextView nameTextView = listItem.findViewById(R.id.pharmacy_name);
        nameTextView.setText(pharmacy.getName());

        TextView quantityTextView = listItem.findViewById(R.id.pharmacy_quantity);
        quantityTextView.setText(String.format(Locale.getDefault(), "Stock: %d", quantity));

        TextView addressTextView = listItem.findViewById(R.id.pharmacy_address);
        addressTextView.setText(pharmacy.getAddress());

        return listItem;
    }

}

