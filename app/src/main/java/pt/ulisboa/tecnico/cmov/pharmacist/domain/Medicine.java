package pt.ulisboa.tecnico.cmov.pharmacist.domain;

import android.widget.ImageView;

import java.io.Serializable;

public class Medicine implements Serializable {

    String name;
    ImageView boxPhoto;
    String usage;

    public Medicine(String name) {
        this.name = name;
        this.boxPhoto = null;
        this.usage = "No usage information available.";
    }

    public Medicine(String name, ImageView image, String usage) {
        this.name = name;
        this.boxPhoto = image;
        this.usage = usage;
    }

    public String getName() {
        return name;
    }

    public ImageView getImage() {
        return boxPhoto;
    }

    public void setImage(ImageView image) {
        this.boxPhoto = image;
    }
    public String getUsage() {
        return usage;
    }
    public void setUsage(String usage) {
        this.usage = usage;
    }

}
