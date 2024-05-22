package pt.ulisboa.tecnico.cmov.pharmacist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import pt.ulisboa.tecnico.cmov.pharmacist.domain.Medicine;

public class MedicineListAdapter extends RecyclerView.Adapter<MedicineListAdapter.MedicineViewHolder> {
    private List<Medicine> medicines;
    private Context context;

    public MedicineListAdapter(Context context, List<Medicine> medicines) {
        this.context = context;
        this.medicines = medicines;
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

    // Method to update the list based on the search filter
    public void updateList(List<Medicine> newList) {
        medicines = newList;
        notifyDataSetChanged();  // Notify the adapter to refresh the RecyclerView
    }

    public static class MedicineViewHolder extends RecyclerView.ViewHolder {
        TextView textViewMedicineName;

        public MedicineViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMedicineName = itemView.findViewById(R.id.textViewMedicineName);
        }
    }
}
