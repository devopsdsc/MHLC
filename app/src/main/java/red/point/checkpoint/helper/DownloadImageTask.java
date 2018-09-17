package red.point.checkpoint.helper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    ImageView bmImage;
    private final DownloadImageTaskListener taskListener;
    private int requestCode;

    public interface DownloadImageTaskListener {
        void onFinished(Bitmap result, int requestCode);
    }

    public DownloadImageTask(ImageView bmImage, DownloadImageTaskListener listener) {
        this.bmImage = bmImage;
        this.taskListener = listener;
    }

    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap mIcon11 = null;
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return mIcon11;
    }

    protected void onPostExecute(Bitmap result) {
        bmImage.setImageBitmap(result);

        if(taskListener != null){
            //post result with given requestCode
            this.taskListener.onFinished(result, requestCode);
        }
    }
}
