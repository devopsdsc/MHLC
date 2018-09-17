package red.point.checkpoint;

import android.graphics.Point;
import android.view.View;

import com.github.amlcurran.showcaseview.targets.Target;

public class CustomViewTarget implements Target {
    private final View mView;
    private String position;

    public CustomViewTarget(View view) {
        mView = view;
    }

    public CustomViewTarget(View view, String position) {
        this.position = position;
        mView = view;
    }

    @Override
    public Point getPoint() {
        int[] location = new int[2];
        mView.getLocationInWindow(location);
        int x = location[0] + mView.getWidth() / 2;
        int y = location[1] + mView.getHeight() / 2;

        if (position.equals("left")) {
            x = location[0] + 10;
            y = location[1] + mView.getHeight() / 2;
        }

        return new Point(x, y);
    }

}
