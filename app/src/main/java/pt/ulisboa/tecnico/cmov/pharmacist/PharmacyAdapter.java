package pt.ulisboa.tecnico.cmov.pharmacist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Locale;

import pt.ulisboa.tecnico.cmov.pharmacist.domain.Pharmacy;

public class PharmacyAdapter extends ArrayAdapter<Pharmacy> {
    private Context mContext;
    private ArrayList<Pharmacy> mPharmacies;

    public PharmacyAdapter(Context context, ArrayList<Pharmacy> pharmacies) {
        super(context, 0, pharmacies);
        mContext = context;
        mPharmacies = pharmacies;
    }

    @NonNull
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null) {
            listItem = LayoutInflater.from(mContext).inflate(R.layout.pharmacy_item, parent, false);
        }

        Pharmacy currentPharmacy = mPharmacies.get(position);

        TextView nameTextView = listItem.findViewById(R.id.name_text_view);
        nameTextView.setText(currentPharmacy.getName());

        TextView distanceTextView = listItem.findViewById(R.id.number_text_view);
        distanceTextView.setText(String.format(Locale.getDefault(), "%.2f km", currentPharmacy.getDistance()));

        TextView addressTextView = listItem.findViewById(R.id.address_text_view);
        addressTextView.setText(currentPharmacy.getAddress());

        return listItem;
    }
}
