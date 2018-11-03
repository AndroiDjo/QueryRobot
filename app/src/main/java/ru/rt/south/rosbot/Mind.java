package ru.rt.south.rosbot;

import android.content.Context;
import android.os.Bundle;

/*Мозги*/
public class Mind {

    private SQLitecon sqlcon;

    public Mind(Context context) {
        sqlcon = SQLitecon.getInstance(context);

    }

}
