package android.csulb.edu.noteever;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AddNoteActivity extends AppCompatActivity {

    private static final int CAMERA_ACCESS = 1515;

    DatabaseProperties db_properties;

    private ImageView image_view;
    private EditText caption_text;
    private Button done_button;
    private Button click_image_button;
    private String caption = "";
    private String imagePath = "";
    private String thumbnailPath = "";
    private Bitmap rotatedBitmap;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        BitmapDrawable drawable = (BitmapDrawable) image_view.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        outState.putParcelable("image", bitmap);
        outState.putString("caption", caption_text.getText().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        image_view.setImageBitmap((Bitmap) savedInstanceState.getParcelable("image"));
        caption_text.setText(savedInstanceState.getString("caption"));
        super.onRestoreInstanceState(savedInstanceState);
    }

    private Bitmap bitmap;
    private Bitmap thumbnailBitmap;
    private Intent callerIntent = getIntent();
    private String mCurrentPhotoPath;
    private Uri photoURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        image_view = (ImageView) findViewById(R.id.image_view);
        caption_text = (EditText) findViewById(R.id.caption_text);
        done_button = (Button) findViewById(R.id.done_button);
        click_image_button = (Button) findViewById(R.id.click_image_button);
        db_properties = new DatabaseProperties(this);
    }

    public void dispatchTakePictureIntent(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this, "android.csulb.edu.noteever.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_ACCESS);
            }
        }
    }

    private File createImageFile() throws IOException {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        mCurrentPhotoPath = image.getAbsolutePath();

        System.out.println("mcurrent - " + mCurrentPhotoPath);
        System.out.println("image - " + image);
        return image;
    }

    public void saveNote(View view) {

        imagePath = saveToInternalStorage(bitmap, false);
        thumbnailPath = saveToInternalStorage(thumbnailBitmap, true);
        if(caption_text.getText().toString().isEmpty()) {
            Toast.makeText(getApplicationContext(), "Caption can not be empty.", Toast.LENGTH_LONG).show();
        }
        else {
            insertNote(caption_text.getText().toString(), thumbnailPath, mCurrentPhotoPath);
        }
    }

    private void insertNote(String caption, String thumnailPath, String imagePath) {
        boolean result = db_properties.insertData(caption, thumnailPath, imagePath);
        if(result == true) {
            setResult(RESULT_OK, callerIntent);
            //finish();
            Intent intent = new Intent(getApplicationContext(), ListActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_ACCESS && resultCode == RESULT_OK) {
            image_view.setImageBitmap(rotateImage(mCurrentPhotoPath));
        }
    }

    private String saveToInternalStorage(Bitmap bitmapImage, boolean isThumbnail) {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory;
        File mypath;
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Timestamp(System.currentTimeMillis()));
        if(isThumbnail) {
            directory = cw.getDir("thumbnailDir", Context.MODE_PRIVATE);
        }
        else {
            directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        }

        mypath=new File(directory,timeStamp+".jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 50, fos);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                fos.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath()+"/"+timeStamp+".jpg";
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
        } catch (IOException e) {
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
        rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true);
        return rotatedBitmap;
    }
}