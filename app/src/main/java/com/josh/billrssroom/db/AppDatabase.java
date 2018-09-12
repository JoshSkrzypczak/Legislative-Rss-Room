package com.josh.billrssroom.db;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.josh.billrssroom.AppExecutors;
import com.josh.billrssroom.db.dao.BillDao;
import com.josh.billrssroom.db.entity.BillEntity;
import com.josh.billrssroom.model.BillItem;

@Database(entities = {BillItem.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    public abstract BillDao billDao();

    private static AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context){
        if (INSTANCE == null){
            synchronized (AppDatabase.class){
                if (INSTANCE == null){
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "favorites_database")
                            .fallbackToDestructiveMigration()
                            .addCallback(roomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback roomDatabaseCallback = new RoomDatabase.Callback(){
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            // If you want to keep the data through app restarts,
            // comment out the following line.
            new PopulateDbAsync(INSTANCE).execute();
        }
    };

    /**
     * Populate the database in the background.
     * If you want to start with more words, just add them.
     */
    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void>{

        private final BillDao dao;

        public PopulateDbAsync(AppDatabase db) {
            dao = db.billDao();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            dao.deleteAll();

            return null;
        }
    }
}
