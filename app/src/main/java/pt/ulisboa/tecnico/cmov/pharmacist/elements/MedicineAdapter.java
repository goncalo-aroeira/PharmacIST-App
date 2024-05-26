package pt.ulisboa.tecnico.cmov.pharmacist.elements;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;

import pt.ulisboa.tecnico.cmov.pharmacist.MedicineInformationPannel;
import pt.ulisboa.tecnico.cmov.pharmacist.R;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.Medicine;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder> {
    private List<Medicine> medicines;
    private HashMap<String, Integer> pharmacyInventory;
    private Context context; // Need to pass context for starting an activity

    public MedicineAdapter(Context context, List<Medicine> medicines, HashMap<String, Integer> pharmacyInventory) {
        this.context = context;
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
        Medicine med = medicines.get(position);
        Integer quantity = pharmacyInventory.getOrDefault(med.getName(), 0);
        holder.textViewMedicineName.setText(med.getName());
        holder.textViewMedicineQuantity.setText("Quantity: " + quantity);

        // Setting up the OnClickListener to open MedicineInformationActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MedicineInformationPannel.class);
            intent.putExtra("medicine", med);  // Ensure Medicine class implements Serializable or Parcelable
            context.startActivity(intent);
        });
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
