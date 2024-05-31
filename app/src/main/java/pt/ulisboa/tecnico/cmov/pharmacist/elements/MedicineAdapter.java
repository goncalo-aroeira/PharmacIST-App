package pt.ulisboa.tecnico.cmov.pharmacist.elements;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;

import pt.ulisboa.tecnico.cmov.pharmacist.R;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.Medicine;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder> {
    private List<Medicine> medicines;
    private HashMap<String, Integer> pharmacyInventory;
    private OnMedicineItemClickListener listener;

    private Context context; // Need to pass context for starting an activity

    public MedicineAdapter(Context context, List<Medicine> medicines, HashMap<String, Integer> pharmacyInventory, OnMedicineItemClickListener listener) {
        this.context = context;
        this.medicines = medicines;
        this.pharmacyInventory = pharmacyInventory;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MedicineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medicine, parent, false);
        return new MedicineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicineViewHolder holder, int position) {
        Medicine med = medicines.get(position);
        Integer quantity = pharmacyInventory.getOrDefault(med.getId(), 0);
        holder.textViewMedicineName.setText(med.getName());
        holder.textViewMedicineQuantity.setText("Quantity: " + quantity);

        // Setting up the OnClickListener to open MedicineInformationActivity
        holder.itemView.setOnClickListener(v -> {
            listener.onMedicineClick(med);
        });

        // Setting up the button click listeners
        holder.purchaseButton.setOnClickListener(v -> {
            Log.d("MedicineAdapter", "onBindViewHolder: Purchase button clicked for " + med.getName() + " with id " + med.getId() + " and quantity " + quantity + " in pharmacy inventory.");
            if (listener != null) {
                listener.onPurchaseClick(med);
            }
        });

    }

    public interface OnMedicineItemClickListener {
        void onPurchaseClick(Medicine medicine);

        void onMedicineClick(Medicine medicine);
    }


    @Override
    public int getItemCount() {
        return medicines.size();
    }

    public static class MedicineViewHolder extends RecyclerView.ViewHolder {
        TextView textViewMedicineName;
        TextView textViewMedicineQuantity;

        ImageButton purchaseButton;


        public MedicineViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMedicineName = itemView.findViewById(R.id.textViewMedicineName);
            textViewMedicineQuantity = itemView.findViewById(R.id.textViewMedicineQuantity);
            purchaseButton = itemView.findViewById(R.id.imageButtonPurcharse);
        }
    }
}
