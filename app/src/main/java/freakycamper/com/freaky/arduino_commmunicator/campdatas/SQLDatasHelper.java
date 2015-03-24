package freakycamper.com.freaky.arduino_commmunicator.campdatas;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import freakycamper.com.freaky.arduino_commmunicator.dialog.DialogWater;
import com.google.common.collect.EvictingQueue;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import freakycamper.com.freaky.arduino_commmunicator.dialog.DialogFridge;

/**
 * Created by lsa on 10/12/14.
 */
public class SQLDatasHelper extends SQLiteOpenHelper {

    Context _context;
    // If you change the database schema, you must increment the database version.
    private static final int    DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "campfridge.db";
    // file size max when new database need to be created (5Mo = 5*1024*1024)
    private static final int    MAX_DATABASE_FILE_SIZE = 5242880;
    private static final int    MIN_KEPT_RECORDS_IN_TABLE = 1000;

    private static final String COLUMN_ID       = "id";
    private static final String[] COLUMN_TEMP   = {
            "T_Frigo",
            "T_Interieur1",
            "T_interieur2",
            "T_Eau_primaire",
            "T_Eau_secondaire",
            "T_Air_chaud",
            "T_Exterieur"
    };
    private static final String[] COLUMN_WATER  = {
            "Water_Level",
            "Water_Flow"
    };
    private static final String[] COLUMN_CURRENT = {
            "I_Cold_Module",
            "I_Water_Module",
            "I_Heat_Module",
            "I_Light_Module",
            "I_Aux_Module",
            "I_Spare_Module",
            "I_Solar_Module"
    };
    private static final String[] COLUMN_TENSION = {
            "U_Primary",
            "U_Secondary"
    };


    private static final String COLUMN_DATE         = "date";

    private static final String TABLE_TEMPERATURE   = "TABLE_TEMPERATURES";
    private static final String TABLE_CONSOS        = "TABLE_CONSOS";
    private static final String TABLE_TENSION       = "TABLE_TENSION";
    private static final String TABLE_WATER         = "TABLE_WATER";

    // Database creation sql statement
    private static final String DATABASE_CREATE_TEMP =
              "CREATE TABLE " + TABLE_TEMPERATURE + " ("
            + COLUMN_ID +   " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_TEMP[0] + " FLOAT, "
            + COLUMN_TEMP[1] + " FLOAT, "
            + COLUMN_TEMP[2] + " FLOAT, "
            + COLUMN_TEMP[3] + " FLOAT, "
            + COLUMN_TEMP[4] + " FLOAT, "
            + COLUMN_TEMP[5] + " FLOAT, "
            + COLUMN_TEMP[6] + " FLOAT, "
            + COLUMN_DATE + " TIMESTAMP NOT NULL DEFAULT current_timestamp);";
    private static final String DATABASE_CREATE_WATER =
            "CREATE TABLE " + TABLE_WATER + " ("
            + COLUMN_ID +   " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_WATER[0] + " INT, "
            + COLUMN_WATER[1] + " FLOAT, "
            + COLUMN_DATE + " TIMESTAMP NOT NULL DEFAULT current_timestamp);";

    private static final String DATABASE_CREATE_CURRENTS =
            "CREATE TABLE " + TABLE_CONSOS + " ("
                    + COLUMN_ID +   " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_CURRENT[0] + " FLOAT, "
                    + COLUMN_CURRENT[1] + " FLOAT, "
                    + COLUMN_CURRENT[2] + " FLOAT, "
                    + COLUMN_CURRENT[3] + " FLOAT, "
                    + COLUMN_CURRENT[4] + " FLOAT, "
                    + COLUMN_CURRENT[5] + " FLOAT, "
                    + COLUMN_CURRENT[6] + " FLOAT, "
                    + COLUMN_DATE + " TIMESTAMP NOT NULL DEFAULT current_timestamp);";

    private static final String DATABASE_CREATE_TENSIONS =
            "CREATE TABLE " + TABLE_TENSION + " ("
                    + COLUMN_ID +   " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_TENSION[0] + " FLOAT, "
                    + COLUMN_TENSION[1] + " FLOAT, "
                    + COLUMN_DATE + " TIMESTAMP NOT NULL DEFAULT current_timestamp);";

    public static SimpleDateFormat DB_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public SQLDatasHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        _context = context;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE_TEMP);
        db.execSQL(DATABASE_CREATE_WATER);
        db.execSQL(DATABASE_CREATE_CURRENTS);
        db.execSQL(DATABASE_CREATE_TENSIONS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(SQLDatasHelper.class.getName(),
            "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TEMPERATURE);
        onCreate(db);
    }

    public void registerTmTemp(float[] temps){
        checkDatabaseFileSize();
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        for (int i=0;i<temps.length; i++) {
            values.put(COLUMN_TEMP[i], temps[i]);
        }
        db.insert(TABLE_TEMPERATURE, null, values);
        db.close();
    }

    public void registerTmWater(int level, float flow){
        checkDatabaseFileSize();
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_WATER[0], level);
        values.put(COLUMN_WATER[1], flow);
        db.insert(TABLE_WATER, null, values);
        db.close();
    }

    public void registerTmCurrents(float[] currents){
        checkDatabaseFileSize();
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        for (int i=0; i<currents.length; i++)
            values.put(COLUMN_CURRENT[i], currents[i]);
        db.insert(TABLE_CONSOS, null, values);
        db.close();
    }

    public void registerTmTensions(float[] tensions){
        checkDatabaseFileSize();
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        for (int i=0; i<tensions.length; i++)
            values.put(COLUMN_TENSION[i], tensions[i]);
        db.insert(TABLE_TENSION, null, values);
        db.close();
    }

    public EvictingQueue<TemperatureItem> retrieveLastLoggedFridgeTemps() {
        EvictingQueue<TemperatureItem> ret = EvictingQueue.create(DialogFridge.FRIDGE_GRAPH_PLOTS_NB);
        Date d;
        float t;
        int i;

        // 1. build the query
        String query = "SELECT  date," + COLUMN_TEMP[0] + " FROM " + TABLE_TEMPERATURE + " ORDER BY " + COLUMN_DATE + " LIMIT " + DialogFridge.FRIDGE_GRAPH_PLOTS_NB + ";";

        // 2. get reference to writable DB
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // 3. go over each row, build book and add it to list
        String dateStr = null;
        SimpleDateFormat format = DB_DATE_FORMAT;
        if (cursor.moveToFirst()) {
            do {
                try {
                    d = format.parse(cursor.getString(0));
                } catch (ParseException e) {
                    d = new Date();
                }
                t = cursor.getFloat(1);

                // Add book to books
                ret.add(new TemperatureItem(t, d));
            } while (cursor.moveToNext());
        }
        db.close();

        return ret;
    }

    public EvictingQueue<WaterItem> retrieveLastLoggedWaterLevels() {
        EvictingQueue<WaterItem> ret = EvictingQueue.create(DialogWater.WATER_LEVEL_GRAPH_PLOTS_NB);
        Date d;
        int level;
        float flow;
        int i;

        // 1. build the query
        String query = "SELECT  date," + COLUMN_WATER[0] + "," + COLUMN_WATER[1] + " FROM " + TABLE_WATER + " ORDER BY " + COLUMN_DATE + " LIMIT " + DialogWater.WATER_LEVEL_GRAPH_PLOTS_NB + ";";

        // 2. get reference to writable DB
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // 3. go over each row, build book and add it to list
        String dateStr = null;
        SimpleDateFormat format = DB_DATE_FORMAT;
        if (cursor.moveToFirst()) {
            do {
                try {
                    d = format.parse(cursor.getString(0));
                } catch (ParseException e) {
                    d = new Date();
                }
                level = cursor.getInt(1);
                flow = cursor.getFloat(2);
                // Add book to books
                ret.add(new WaterItem(level, flow, d));
            } while (cursor.moveToNext());
        }
        db.close();

        return ret;
    }

    public EvictingQueue<CurrentItem> retrieveLastLoggedCurrents(CurrentItem.eCurrentType type){
        EvictingQueue<CurrentItem> ret = EvictingQueue.create(100);
        Date d;
        float current;
        int i;

        // 1. build the query
        String query = "SELECT  date," + COLUMN_CURRENT[type.value] + " FROM " + TABLE_CONSOS + " ORDER BY " + COLUMN_DATE + " LIMIT " + 100 + ";";

        // 2. get reference to writable DB
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // 3. go over each row, build book and add it to list
        String dateStr = null;
        SimpleDateFormat format = DB_DATE_FORMAT;
        if (cursor.moveToFirst()) {
            do {
                try {
                    d = format.parse(cursor.getString(0));
                } catch (ParseException e) {
                    d = new Date();
                }
                current = cursor.getFloat(1);
                ret.add(new CurrentItem(current));
            } while (cursor.moveToNext());
        }
        db.close();

        return ret;
    }

    public EvictingQueue<TensionItem> retrieveLastLoggedCurrents(TensionItem.eTensionType type){
        EvictingQueue<TensionItem> ret = EvictingQueue.create(100);
        Date d;
        float tension;
        int i;

        // 1. build the query
        String query = "SELECT  date," + COLUMN_TENSION[type.value] + " FROM " + TABLE_TENSION + " ORDER BY " + COLUMN_DATE + " LIMIT " + 100 + ";";

        // 2. get reference to writable DB
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // 3. go over each row, build book and add it to list
        String dateStr = null;
        SimpleDateFormat format = DB_DATE_FORMAT;
        if (cursor.moveToFirst()) {
            do {
                try {
                    d = format.parse(cursor.getString(0));
                } catch (ParseException e) {
                    d = new Date();
                }
                tension = cursor.getFloat(1);
                ret.add(new TensionItem(tension));
            } while (cursor.moveToNext());
        }
        db.close();

        return ret;
    }

    private void checkDatabaseFileSize(){
        if (databaseBackup()){
            // remove all old records, only keep MIN_KEPT_RECORDS_IN_TABLE in table
            String q = "DELETE FROM " + TABLE_TEMPERATURE + "  WHERE id <= (" +
            "SELECT id FROM (SELECT id FROM " + TABLE_TEMPERATURE + " ORDER BY id DESC LIMIT 1 OFFSET " + MIN_KEPT_RECORDS_IN_TABLE + "));";
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL(q);
            db.close();

        }
    }

    private  boolean databaseBackup()  {
        boolean ret = false;
        File src = _context.getDatabasePath(DATABASE_NAME);

        if (src.length() < MAX_DATABASE_FILE_SIZE) return false;
        else {
            try {
                File dst = new File(_context.getCacheDir() + "/[TELEMETRY]_" + (new Date()).toString().replace(":","-").replace(" ", "_")+".db");
                Files.copy(src, dst);
                return true;
            } catch (IOException e) {
                return false;
            }


        }
    }

}
