package com.example.mustafabank.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";

    private static final String DB_NAME = "fb_mustafa_bank";
    private static final int DB_VERSION = 1;

    public DatabaseHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate: started");
        String createUserTable = "create table users (_id integer primary key autoincrement, email text not null, " +
                "password text not null, " +
                "first_name text, last_name text, address text, image_url text, remained_amount double)";

        String createShoppingTable = "create table shopping (_id integer primary key autoincrement, item_id integer, " +
                "user_id integer, transaction_id integer, price double, date date, description text)";

        String createInvestmentTable = "create table investments (_id integer primary key autoincrement, amount double,  " +
                "monthly_roi double, name text, init_date date, finish_date date, user_id integer, transaction_id integer)";

        String createLoansTable = "create table loans (_id integer primary key autoincrement, init_date date, " +
                "finish_date date, init_amount double, remained_amount double, monthly_payment double, monthly_roi double, " +
                "name text, user_id integer, transaction_id integer)";

        String createTransactionTable = "create table transactions (_id integer primary key autoincrement, amount double, " +
                "date date, type text, user_id integer, recipient text, description text)";

        String createItemsTable = "create table items (_id integer primary key autoincrement, name text, image_url text, " +
                "description text)";

        db.execSQL(createUserTable);
        db.execSQL(createShoppingTable);
        db.execSQL(createInvestmentTable);
        db.execSQL(createLoansTable);
        db.execSQL(createTransactionTable);
        db.execSQL(createItemsTable);

        addInitialItems(db);
        addTestTransaction(db);
        addTestProfit(db);
        addTestShopping(db);
    }

    private void addTestShopping(SQLiteDatabase sqLiteDatabase){
        Log.d(TAG, "addTestShopping: started");
        ContentValues firstValues = new ContentValues();
        firstValues.put("item_id", 1);
        firstValues.put("transaction_id", 1);
        firstValues.put("user_id", 1);
        firstValues.put("price", 10.0);
        firstValues.put("description", "first description");
        firstValues.put("date", "2019-10-01");
        sqLiteDatabase.insert("shopping", null, firstValues);

        ContentValues secondValue = new ContentValues();
        secondValue.put("item_id", 2);
        secondValue.put("transaction_id", 2);
        secondValue.put("user_id", 1);
        secondValue.put("price", 8.0);
        secondValue.put("description", "second description");
        secondValue.put("date", "2019-10-01");
        sqLiteDatabase.insert("shopping", null, secondValue);

        ContentValues thirdValue = new ContentValues();
        thirdValue.put("item_id", 2);
        thirdValue.put("transaction_id", 3);
        thirdValue.put("user_id", 1);
        thirdValue.put("price", 16.0);
        thirdValue.put("description", "third description");
        thirdValue.put("date", "2019-10-02");
        sqLiteDatabase.insert("shopping", null, thirdValue);
    }

    private void addTestProfit(SQLiteDatabase db) {
        Log.d(TAG, "addTestProfit: started");
        ContentValues firstValue = new ContentValues();
        firstValue.put("amount", 15.0);
        firstValue.put("type", "profit");
        firstValue.put("date", "2019-08-10");
        firstValue.put("description", "Monthly profit from Bank America");
        firstValue.put("user_id", 1);
        firstValue.put("recipient", "Bank of America");
        db.insert("transactions", null, firstValue);

        ContentValues secondValue = new ContentValues();
        secondValue.put("amount", 25.0);
        secondValue.put("type", "profit");
        secondValue.put("date", "2019-08-26");
        secondValue.put("description", "Monthly porfit from Real Estate investment");
        secondValue.put("user_id", 1);
        secondValue.put("recipient", "Real Estate Agency");
        db.insert("transactions", null, secondValue);

        ContentValues thirdvalue = new ContentValues();
        thirdvalue.put("amount", 32.0);
        thirdvalue.put("type", "profit");
        thirdvalue.put("date", "2019-07-11");
        thirdvalue.put("description", "monthly profit stocks");
        thirdvalue.put("user_id", 1);
        thirdvalue.put("recipient", "Vanguard");
        db.insert("transactions", null, thirdvalue);
    }

    private void addTestTransaction(SQLiteDatabase sqLiteDatabase){
        Log.d(TAG, "addTestTransaction: started");
        ContentValues contentValues = new ContentValues();
        contentValues.put("_id", 0);
        contentValues.put("amount", 10.5);
        contentValues.put("date", "2019-07-11");
        contentValues.put("type", "shopping");
        contentValues.put("user_id", 1);
        contentValues.put("description", "Grocery shopping");
        contentValues.put("recipient", "Walmart");
        long newTranscionId = sqLiteDatabase.insert("transactions", null, contentValues);
    }

    private void addInitialItems(SQLiteDatabase db){
        Log.d(TAG, "addInitialItems: started");
        ContentValues values = new ContentValues();
        values.put("name", "Bike");
        values.put("image_url", "https://esmart.com.bd/wp-content/uploads/2018/12/67389609_2451056414974367_4101503702492250112_n.jpg");
        values.put("description", "The perfect mountain bike");

        db.insert("items", null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
