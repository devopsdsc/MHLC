package red.point.checkpoint.ui.pin.menu;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

public class DropPinMenu extends BaseObservable {
    String title;
    int image;
    int color;

    public DropPinMenu(String title, int image, int color) {
        this.title = title;
        this.image = image;
        this.color = color;
    }

    @Bindable
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Bindable
    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    @Bindable
    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
