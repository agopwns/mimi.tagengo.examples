package jp.fairydevices.mimi.example.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import jp.fairydevices.mimi.example.model.TranslationHistory;


@Database(entities = TranslationHistory.class, version = 1, exportSchema = false)
public abstract class HistoryDB extends RoomDatabase {

    public abstract HistoryDao historyDao();

    public static final String DATABASE_NAME = "historyDB";
    private static HistoryDB instance;

    public static HistoryDB getInstance(Context context){
        if(instance == null){
            instance = Room.databaseBuilder(context, HistoryDB.class, DATABASE_NAME)
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }

    public static void closeInstance(){

        if(instance != null){
            instance.getOpenHelper().close();
        }
    }
}
