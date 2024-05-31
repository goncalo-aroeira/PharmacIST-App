package pt.ulisboa.tecnico.cmov.pharmacist.elements;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import pt.ulisboa.tecnico.cmov.pharmacist.R;
import pt.ulisboa.tecnico.cmov.pharmacist.domain.Medicine;

public class MedicineListAdapter extends RecyclerView.Adapter<MedicineListAdapter.MedicineViewHolder> {

    private List<Medicine> medicines;
    private final onMedicineClicked listener;

    public MedicineListAdapter(Context context, List<Medicine> medicines, onMedicineClicked listener) {
        this.medicines = medicines;
        this.listener = listener;
    }

    public void updateList(List<Medicine> newList) {
        medicines = newList;
        notifyDataSetChanged(); // Notify the adapter to refresh the RecyclerView
    }

    @NonNull
    @Override
    public MedicineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medicine_simple, parent, false);
        return new MedicineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicineViewHolder holder, int position) {
        Medicine medicine = medicines.get(position);
        holder.textViewMedicineName.setText(medicine.getName());
    }

    @Override
    public int getItemCount() {
        return medicines.size();
    }

    public interface onMedicineClicked {
        void onMedicineSelected(Medicine medicine);
    }

    public class MedicineViewHolder extends RecyclerView.ViewHolder {
        TextView textViewMedicineName;

        public MedicineViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMedicineName = itemView.findViewById(R.id.textViewMedicineName);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onMedicineSelected(medicines.get(position));
                }
            });
        }
    }
}
