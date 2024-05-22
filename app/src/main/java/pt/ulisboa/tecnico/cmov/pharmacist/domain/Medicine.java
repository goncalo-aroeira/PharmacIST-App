package pt.ulisboa.tecnico.cmov.pharmacist.domain;

import android.widget.ImageView;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class Medicine implements Serializable {

    String id;
    String name;
    Byte[] imageBytes;
    String usage;

    public Medicine(String name) {
        this.id = null;
        this.name = name;
        this.imageBytes = null;
        this.usage = "No usage information available.";
    }

    public Medicine(String name, String usage) {
        this.id = null;
        this.name = name;
        this.imageBytes = null;
        this.usage = usage;
    }

    public String getName() {
        return name;
    }

    public Byte[] getImageBytes() {
        return imageBytes;
    }

    public void setImageBytes(Byte[] imageBytes) {
        this.imageBytes = imageBytes;
    }

    public String getUsage() {
        return usage;
    }
    public void setUsage(String usage) {
        this.usage = usage;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Medicine medicine = (Medicine) obj;
        return Objects.equals(name, medicine.name);
    }

    public void generateId() {
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }


}
