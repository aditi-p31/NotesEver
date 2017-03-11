package android.csulb.edu.noteever;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.csulb.edu.noteever.DatabaseProperties;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class ListActivity extends Activity {


    DatabaseProperties db_properties;

    private static final int SAVE_NOTE = 1010;

    SimpleCursorAdapter simpleCursorAdapter;
    ListView listview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        db_properties = new DatabaseProperties(this);
        listview = (ListView) findViewById(R.id.listview);

        getNotes();

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LinearLayout notes_layout = (LinearLayout) view;
                System.out.println(notes_layout.getChildCount());
                TextView note_ID = (TextView) notes_layout.getChildAt(0);
                Intent intent = new Intent(getApplicationContext(), ViewPhotoActivity.class);
                intent.putExtra("id", note_ID.getText());
                startActivity(intent);
            }
        });
    }


    public void getNotes() {
        Cursor result_notes = db_properties.getData();

        if(result_notes.getCount()==0){
            Toast.makeText(getApplicationContext(), "Press + to add notes", Toast.LENGTH_LONG).show();
        }

        String[] columns = new String[] {DatabaseProperties.COLUMN_ID, DatabaseProperties.COLUMN_CAPTION};

        int[] to = new int[] {R.id.note_detail_id, R.id.note_detail_caption};


        try{
            simpleCursorAdapter = new SimpleCursorAdapter(this, R.layout.note_detail, result_notes, columns, to, 0);
            listview.setAdapter(simpleCursorAdapter);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void addNewNote(View view) {
        Intent intent = new Intent(getApplicationContext(), AddNoteActivity.class);
        startActivityForResult(intent, SAVE_NOTE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SAVE_NOTE && resultCode == RESULT_OK) {
            Cursor result = db_properties.getData();
            simpleCursorAdapter.changeCursor(result);
            simpleCursorAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.uninstall:
                uninstall();
                return true;
            case R.id.deleteAll:
                db_properties.deleteAll();
                Cursor result = db_properties.getData();
                simpleCursorAdapter.changeCursor(result);
                simpleCursorAdapter.notifyDataSetChanged();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void uninstall() {
        Intent intent = new Intent(Intent.ACTION_DELETE);
        intent.setData(Uri.parse("package:" + this.getPackageName()));
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }


}