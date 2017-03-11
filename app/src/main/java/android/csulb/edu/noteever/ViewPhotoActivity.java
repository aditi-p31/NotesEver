package android.csulb.edu.noteever;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

import static android.csulb.edu.noteever.DatabaseProperties.COLUMN_CAPTION;
import static android.csulb.edu.noteever.DatabaseProperties.COLUMN_IMAGE_PATH;


public class ViewPhotoActivity extends AppCompatActivity {

    DatabaseProperties db_property = new DatabaseProperties(this);
    ImageView note_imageview;
    TextView note_caption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_photo);

        note_imageview = (ImageView) findViewById(R.id.note_image);
        note_caption = (TextView) findViewById(R.id.note_caption);

        String id = getIntent().getStringExtra("id");
        Cursor result = db_property.getImagePath(id);

        if (result.getCount() == 1) {
            result.moveToFirst();
            String caption = result.getString(result.getColumnIndex(COLUMN_CAPTION));
            String path = result.getString(result.getColumnIndex(COLUMN_IMAGE_PATH));

            if(path != null)
            {
                note_imageview.setImageBitmap(rotateImage(path));
            }
            note_caption.setText(caption);
        }
    }

    private Bitmap rotateImage(String imagePath) {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, bounds);

        BitmapFactory.Options opts = new BitmapFactory.Options();
        Bitmap bm = BitmapFactory.decodeFile(imagePath, opts);
        ExifInterface exif = null;

        try {
            exif = new ExifInterface(imagePath);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
        int orientation = orientString != null ? Integer.parseInt(orientString) :  ExifInterface.ORIENTATION_NORMAL;

        int rotationAngle = 0;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;
        Matrix matrix = new Matrix();
        matrix.setRotate(rotationAngle, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true);
        return rotatedBitmap;
    }
}