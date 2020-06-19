package com.example.mustafabank;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.example.mustafabank.Database.DatabaseHelper;
import com.example.mustafabank.Models.InvestmentWorker;
import com.example.mustafabank.Models.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class AddInvestmentActivity extends AppCompatActivity {
    private static final String TAG = "AddInvestmentActivity";

    private EditText editTextName, editTextInitAmount, editTextMonthlyROI, editTextInitDate, editTextFinishDate;
    private Button buttonPickInitDate, buttonPickFinishDate, buttonAddInvestment;
    private TextView textWarning;

    private Calendar initCalendar = Calendar.getInstance();
    private Calendar finishCalendar = Calendar.getInstance();

    private DatabaseHelper databaseHelper;
    private Utils utils;

    private AddTransaction addTransaction;
    private AddInvestment addInvestment;

    private DatePickerDialog.OnDateSetListener initDateListner = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            initCalendar.set(Calendar.YEAR, year);
            initCalendar.set(Calendar.MONTH, month);
            initCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            editTextInitDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(initCalendar.getTime()));
        }
    };

    private DatePickerDialog.OnDateSetListener finishDateListner = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            finishCalendar.set(Calendar.YEAR, year);
            finishCalendar.set(Calendar.MONTH, month);
            finishCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            editTextFinishDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(finishCalendar.getTime()));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_investment);

        initViews();
        databaseHelper = new DatabaseHelper(this);
        utils = new Utils(this);
        setOnClickListners();
    }

    private void setOnClickListners(){
        Log.d(TAG, "setOnClickListners: started");
        buttonPickInitDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(AddInvestmentActivity.this, initDateListner, initCalendar.get(Calendar.YEAR), initCalendar.get(Calendar.MONTH), initCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        buttonPickFinishDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(AddInvestmentActivity.this, finishDateListner, finishCalendar.get(Calendar.YEAR), finishCalendar.get(Calendar.MONTH), finishCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        buttonAddInvestment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateData()){
                    textWarning.setVisibility(View.GONE);
                    initAdding();
                }else{
                    textWarning.setVisibility(View.VISIBLE);
                    textWarning.setText("Please fill all the blanks");
                }
            }
        });
    }

    private void initAdding(){
        Log.d(TAG, "initAdding: started");
        addTransaction = new AddTransaction();
        User user = utils.isUserLoggedIn();
        if(user != null) addTransaction.execute(user.get_id());
    }

    private class AddTransaction extends AsyncTask<Integer, Void, Integer>{

        private String date, name;
        private double amount;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            this.date = editTextInitDate.getText().toString();
            this.name = editTextName.getText().toString();
            this.amount = Double.parseDouble(editTextInitAmount.getText().toString());
        }

        @Override
        protected Integer doInBackground(Integer... integers) {

            try{
                SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();

                ContentValues contentValues = new ContentValues();
                contentValues.put("amount", amount);
                contentValues.put("recipient", name);
                contentValues.put("date", date);
                contentValues.put("description", "Initial amount for " + name + " investment");
                contentValues.put("user_id",integers[0]);
                contentValues.put("type", "investment");

                long id = sqLiteDatabase.insert("transactions", null, contentValues);
                return (int)id;

            }catch (SQLException e){ e.printStackTrace(); return null; }

        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            if(integer != null){
                addInvestment = new AddInvestment();
                addInvestment.execute(integer);
            }
        }
    }

    private class AddInvestment extends AsyncTask<Integer, Void, Void>{

        private int userId;
        private String initDate, finishDate, name;
        private double monthlyROI, amount;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.amount = Double.parseDouble(editTextInitAmount.getText().toString());
            this.monthlyROI = Double.parseDouble(editTextMonthlyROI.getText().toString());
            this.name = editTextName.getText().toString();
            this.initDate = editTextInitDate.getText().toString();
            this.finishDate = editTextFinishDate.getText().toString();

            User user = utils.isUserLoggedIn();
            if(user != null) this.userId = user.get_id();
            else this.userId = -1;
        }

        @Override
        protected Void doInBackground(Integer... integers) {
            if(userId != -1){
                try{
                    SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("name", name);
                    contentValues.put("init_date", initDate);
                    contentValues.put("finish_date", finishDate);
                    contentValues.put("amount", amount);
                    contentValues.put("monthly_roi", monthlyROI);
                    contentValues.put("user_id", userId);
                    contentValues.put("transaction_id", integers[0]);
                    long id = sqLiteDatabase.insert("investments", null, contentValues);
                    if(id != -1){
                        Cursor cursor = sqLiteDatabase.query("users", new String[]{"remained_amount"}, "_id=?",
                                new String[]{String.valueOf(userId)}, null, null, null);
                        if(cursor != null){
                            if(cursor.moveToFirst()){
                                double remainedAmount = cursor.getDouble(cursor.getColumnIndex("remained_amount"));
                                cursor.close();
                                ContentValues newContentValuew = new ContentValues();
                                newContentValuew.put("remained_amount", remainedAmount - amount);
                                int affectedRows = sqLiteDatabase.update("users", newContentValuew, "_id=?", new String[]{String.valueOf(userId)});
                                Log.d(TAG, "doInBackground: affected rows: " + affectedRows); sqLiteDatabase.close();
                            }else{ cursor.close(); sqLiteDatabase.close(); return null; }
                        }
                    }else{ sqLiteDatabase.close(); return null; }
                }catch (SQLException e){ e.printStackTrace(); return null; }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try{
                Date initDate = simpleDateFormat.parse(editTextInitDate.getText().toString());
                calendar.setTime(initDate);
                int initMonths = calendar.get(Calendar.YEAR) * 12 + calendar.get(Calendar.MONTH) ;
                Date finishDate = simpleDateFormat.parse(editTextFinishDate.getText().toString());
                calendar.setTime(finishDate);
                int finishMonths = calendar.get(Calendar.YEAR) * 12 + calendar.get(Calendar.MONTH);

                int difference = finishMonths - initMonths;

                int days = 0;

                for(int i=0; i < difference; ++i){
                    days += 30;
                    Data data = new Data.Builder()
                            .putDouble("amount", amount * monthlyROI/100)
                            .putString("description", "Profit for " + name)
                            .putInt("user_id", userId)
                            .putString("recipiet", name)
                            .build();

                    Constraints constraints = new Constraints.Builder()
                            .setRequiresBatteryNotLow(true).build();

                    OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(InvestmentWorker.class)
                            .setInputData(data)
                            .setConstraints(constraints)
                            .setInitialDelay(days, TimeUnit.DAYS)
                            .addTag("profit")
                            .build();

                    WorkManager.getInstance(AddInvestmentActivity.this).enqueue(oneTimeWorkRequest);
                }

            }catch (ParseException e) { e.printStackTrace(); }

            Intent intent = new Intent(AddInvestmentActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    private boolean validateData(){
        Log.d(TAG, "validateData: started");
        if(editTextName.getText().toString().equals("")) return false;
        if(editTextInitDate.getText().toString().equals("")) return false;
        if(editTextFinishDate.getText().toString().equals("")) return false;
        if(editTextInitAmount.getText().toString().equals("")) return false;
        if(editTextMonthlyROI.getText().toString().equals("")) return false;
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(addTransaction != null && !addTransaction.isCancelled()) addTransaction.cancel(true);
        if(addInvestment != null && !addInvestment.isCancelled()) addInvestment.cancel(true);
    }

    private void initViews(){
        Log.d(TAG, "initView: started");

        editTextName = (EditText) findViewById(R.id.editTextNameAddInvestment);
        editTextInitAmount = (EditText) findViewById(R.id.editTextInitAmountAddInvestment);
        editTextMonthlyROI = (EditText) findViewById(R.id.editTextMonthlyROIAddInvestment);
        editTextInitDate = (EditText) findViewById(R.id.editTextInitDateAddInvestment);
        editTextFinishDate = (EditText) findViewById(R.id.editTextFinishDateAddInvestment);

        buttonPickInitDate = (Button) findViewById(R.id.buttonPickInitDateAddInvestment);
        buttonPickFinishDate = (Button) findViewById(R.id.buttonPickFinishDateAddInvestment);
        buttonAddInvestment = (Button) findViewById(R.id.buttonAddInvestmentAddInvestment);

        textWarning = (TextView)findViewById(R.id.textWarningAddInvestment);
    }
}
