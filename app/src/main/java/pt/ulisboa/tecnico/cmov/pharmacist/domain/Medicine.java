package pt.ulisboa.tecnico.cmov.pharmacist.domain;

import android.widget.ImageView;

public class Medicine {

    String name;
    ImageView image;

    public Medicine(String name) {
        this.name = name;
        this.image = null;
    }

    public Medicine(String name, ImageView image) {
        this.name = name;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public ImageView getImage() {
        return image;
    }

    public void setImage(ImageView image) {
        this.image = image;
    }

}
