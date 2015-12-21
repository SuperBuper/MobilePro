package com.processmap.mobilepro.modules.common;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.processmap.mobilepro.data.base.BaseEntity;

import java.util.ArrayList;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

public class DatabaseManager {

    private static final String DATABASE_NAME   = "processmap.db";
    private static final int DATABASE_VERSION   = 1;

    public static final int SQL_ERROR           = -1;

    SQLiteDatabase database = null;

    private boolean initDone = false;
    private Context mContext = null;

    private DatabaseHelper databaseHelper = null;
    private BlockingDeque<String> queue = null;
    ExecutorService executorService = null;
    ProcessQueue processQueue = null;

    private Object lockDB = new Object();

    private static DatabaseManager ourInstance = new DatabaseManager();
    public static DatabaseManager getInstance() { return ourInstance; }

    private DatabaseManager() {
        if (!initDone) {
            queue = new LinkedBlockingDeque<String>();
            executorService = Executors.newFixedThreadPool(1);
            processQueue = new ProcessQueue();
            executorService.execute(processQueue);
            mContext = AppContext.getInstance().getContext();
            databaseHelper = new DatabaseHelper(mContext);
            database = databaseHelper.getWritableDatabase();
            initDone = true;
        }
    }

    public void close() {
        if (executorService != null) {
            executorService.shutdown();
        }
        if (database != null) {
            database.close();
        }

        initDone = false;
    }

    //
    //----------------------------------------------------------------------------------------------
    //
    public void exec(String query) {
        synchronized (lockDB) {
            database.execSQL(query);
        }
    }

    public Cursor query(String query) {
        return database.rawQuery(query, null);
    }

    public long insert(BaseEntity entity) {
        synchronized (lockDB) {
            String table = entity.getTableName();
            ContentValues values = new ContentValues();
            entity.getValues(values);
            return database.insertWithOnConflict(table, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    public void insertInTransaction(ArrayList<BaseEntity> list) {
        synchronized(lockDB) {
            database.beginTransaction();
            try {
                for (BaseEntity entity : list) {
                    insert(entity);
                }
                database.setTransactionSuccessful();
            } finally {
                database.endTransaction();
            }
        }
    }

    public synchronized void addToQueue(String query) {
        try {
            queue.putLast(query);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //
    //
    //
    private class ProcessQueue implements Runnable{
        @Override
        public void run() {
            for(;;){
                try {
                    String query = queue.takeFirst();
                    exec(query);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //
    //----------------------------------------------------------------------------------------------
    //
    private class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase database) {
            ;
        }


        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            ;
        }
    }

}
