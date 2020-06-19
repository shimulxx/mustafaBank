package com.example.mustafabank.Models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.mustafabank.Database.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class LoanWorker extends Worker {
    private static final String TAG = "LoanWorker";

    private DatabaseHelper databaseHelper;

    public LoanWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        databaseHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        Data data = getInputData();
        int loanId = data.getInt("loan_id", -1);
        int userId = data.getInt("user_id", -1);
        double monthlyPayment = data.getDouble("monthly_payment", 0.0);
        String name = data.getString("names");
        if(loanId == -1 || userId == -1 || monthlyPayment == 0.0) return Result.failure();
        try{
            SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put("amount", -monthlyPayment);
            contentValues.put("user_id", userId);
            contentValues.put("type", "loan_payment");
            contentValues.put("description", "Monthly payment for " + name + " Loan");
            contentValues.put("recipient", name);

            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String date = simpleDateFormat.format(calendar.getTime());
            contentValues.put("date", date);
            long transactionId = sqLiteDatabase.insert("transactions", null, contentValues);
            if(transactionId == -1) return Result.failure();
            else{
                Cursor cursor = sqLiteDatabase.query("users", new String[]{"remained_amount"}, "_id=?",
                        new String[]{String.valueOf(userId)}, null, null, null);
                if(cursor != null){
                    if(cursor.moveToFirst()){
                        double currentRemainedAmount = cursor.getDouble(cursor.getColumnIndex("remained_amount"));
                        ContentValues values = new ContentValues();
                        values.put("remained_amount", currentRemainedAmount - monthlyPayment);
                        sqLiteDatabase.update("users", values, "_id=?", new String[]{String.valueOf(userId)});
                        cursor.close();

                        Cursor secondCursor = sqLiteDatabase.query("loans", new String[]{"remained_amount"}, "_id=?",
                                new String[]{String.valueOf(loanId)}, null, null, null);
                        if(secondCursor != null){
                            if(secondCursor.moveToFirst()){
                                double currentLoanAmount = secondCursor.getDouble(secondCursor.getColumnIndex("remained_amount"));
                                ContentValues secondContentValues = new ContentValues();
                                secondContentValues.put("remained_amount", currentLoanAmount - monthlyPayment);
                                sqLiteDatabase.update("loans", secondContentValues, "_id=?", new String[]{String.valueOf(loanId)});
                                secondCursor.close(); sqLiteDatabase.close(); return Result.success();
                            }else{ secondCursor.close(); sqLiteDatabase.close(); return Result.failure(); }
                        }else{
                            sqLiteDatabase.close(); return Result.failure();
                        }
                    } else{ cursor.close(); sqLiteDatabase.close(); return Result.failure(); }
                }else { sqLiteDatabase.close(); return Result.failure(); }
            }


        }catch (SQLException e){ e.printStackTrace(); return Result.failure(); }
    }
}
