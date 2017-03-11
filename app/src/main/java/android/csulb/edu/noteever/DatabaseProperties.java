package android.csulb.edu.noteever;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.EditText;

/**
 * Created by Aditi on 3/9/2017.
 */

public class DatabaseProperties extends SQLiteOpenHelper {

    private static final String DATABASE = "NotesEver.db";
    private static final String TABLE = "Notes";
    public static final String COLUMN_ID = "id";
    private static final String COLUMN_ID_DATATYPE = "INTEGER";
    private static final String COLUMN_ID_CONSTRAINT = "PRIMARY KEY AUTOINCREMENT";
    public static final String COLUMN_CAPTION = "Caption";
    private static final String COLUMN_CAPTION_DATATYPE = "TEXT";
    public static final String COLUMN_THUMBNAIL_PATH = "Thumbnail_Path";
    private static final String COLUMN_THUMBNAIL_PATH_DATATYPE = "TEXT";
    public static final String COLUMN_IMAGE_PATH = "Image_Path";
    private static final String COLUMN_IMAGE_PATH_DATATYPE = "TEXT";

    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "
            + TABLE + " ("
            + COLUMN_ID + " " + COLUMN_ID_DATATYPE +" "+ COLUMN_ID_CONSTRAINT +", "
            + COLUMN_CAPTION + " " + COLUMN_CAPTION_DATATYPE + ", "
            + COLUMN_THUMBNAIL_PATH + " " + COLUMN_THUMBNAIL_PATH_DATATYPE + ", "
            + COLUMN_IMAGE_PATH + " " + COLUMN_IMAGE_PATH_DATATYPE + ")";

    private static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE;


    public DatabaseProperties(Context context) {
        super(context, DATABASE, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_TABLE);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE);
        onCreate(db);
    }

    public boolean insertData(String Caption, String Thumbnail_Path, String Image_Path) {
        SQLiteDatabase db  = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_CAPTION, Caption);
        contentValues.put(COLUMN_THUMBNAIL_PATH, Thumbnail_Path);
        contentValues.put(COLUMN_IMAGE_PATH, Image_Path);
        long result = db.insert(TABLE,null,contentValues);
        if(result==-1){
            return false;
        }
        return true;
    }

    public Cursor getData(){
        SQLiteDatabase db  = this.getReadableDatabase();
        Cursor result = db.rawQuery("Select * from "+ TABLE, null);
        return result;
    }

    public void deleteAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = null;
        String[] whereArgs = {};
        db.delete(TABLE, whereClause, whereArgs);
    }

    public Cursor getImagePath(String note_ID) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor result = db.rawQuery("Select * from "+ TABLE + " Where " + COLUMN_ID +" = " + note_ID, null);
        return result;
    }


}