package com.androidjo.queryrobot;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class DatabaseActivity extends AppCompatActivity {

    private EditText sqlEdit;
    private TextView outputText;
    private SQLitecon sqlcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database);
        sqlEdit = (EditText)findViewById(R.id.sql_edit);
        outputText = (TextView)findViewById(R.id.sql_result_label);
        sqlcon = SQLitecon.getInstance(this);
    }

    public void sqlExecClick(View view) {
        try {
            SQLiteDatabase db = sqlcon.getWritableDatabase();
            db.execSQL(sqlEdit.getText().toString().trim());
            outputText.setText("Команда выполнена");
        }
        catch (Exception e) {
            outputText.setText("Exception: "+e.getMessage());
        }
    }

    public void sqlQueryClick(View view) {
        Cursor result;
        try (SQLiteDatabase db = sqlcon.getWritableDatabase()) {
            result = db.rawQuery(sqlEdit.getText().toString().trim(), null);
            outputText.setText("Результат выполнения запроса:\n");
            int colcnt = result.getColumnCount();
            while (result.moveToNext()) {
                for (int i=0; i < colcnt; i++) {
                    String colname = result.getColumnName(i);
                    int coltype = result.getType(i);
                    if (coltype == Cursor.FIELD_TYPE_STRING) {
                        String strval = result.getString(i);
                        outputText.append(colname+"="+strval+" ");
                    }
                    else if (coltype == Cursor.FIELD_TYPE_INTEGER) {
                        int intval = result.getInt(i);
                        outputText.append(colname+"="+Integer.toString(intval)+" ");
                    }
                    else if (coltype == Cursor.FIELD_TYPE_FLOAT) {
                        float floatval = result.getFloat(i);
                        outputText.append(colname+"="+Float.toString(floatval)+" ");
                    }
                }
                outputText.append("\n");
            }
        }
        catch (Exception e) {
            outputText.setText("Exception: "+e.getMessage());
        }
    }
}
