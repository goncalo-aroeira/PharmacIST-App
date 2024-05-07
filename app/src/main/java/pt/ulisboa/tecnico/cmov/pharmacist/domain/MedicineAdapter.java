package pt.ulisboa.tecnico.cmov.pharmacist.domain;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.ulisboa.tecnico.cmov.pharmacist.R;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder> {
    private List<Medicine> medicines;
    private HashMap<Medicine, Integer> pharmacyInventory;

    public MedicineAdapter(List<Medicine> medicines, HashMap<Medicine, Integer> pharmacyInventory) {
        this.medicines = medicines;
        this.pharmacyInventory = pharmacyInventory;
    }

    @NonNull
    @Override
    public MedicineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medicine, parent, false);
        return new MedicineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicineViewHolder holder, int position) {
        Medicine medicine = medicines.get(position);
        Log.d("MedicineAdapter", "Medicine: " + medicine.getName() + " Inventory: " + pharmacyInventory.size());
        Integer quantityInteger = pharmacyInventory.get(medicine);
        int quantity = (quantityInteger != null) ? quantityInteger : 0;
        holder.textViewMedicineName.setText(medicine.getName());
        holder.textViewMedicineQuantity.setText("Quantity: " + quantity);
    }

    @Override
    public int getItemCount() {
        return medicines.size();
    }

    public static class MedicineViewHolder extends RecyclerView.ViewHolder {
        TextView textViewMedicineName;
        TextView textViewMedicineQuantity;

        public MedicineViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMedicineName = itemView.findViewById(R.id.textViewMedicineName);
            textViewMedicineQuantity = itemView.findViewById(R.id.textViewMedicineQuantity);
        }
    }
}
