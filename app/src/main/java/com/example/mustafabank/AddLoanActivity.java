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
import com.example.mustafabank.Models.LoanWorker;
import com.example.mustafabank.Models.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class AddLoanActivity extends AppCompatActivity {
    private static final String TAG = "AddLoanActivity";

    private EditText editTextName, editTextInitAmount, editTextMonthlyROI, editTextInitDate, editTextFinishDate, editTextMonthlyPayment;
    private Button buttonPickInitDate, buttonPickFinishDate, buttonAddLoan;
    private TextView textWarning;

    private Calendar initCalendar = Calendar.getInstance();
    private Calendar finishCalendar = Calendar.getInstance();

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

    private Utils utils;

    private DatabaseHelper databaseHelper;

    private AddTransactions addTransactions;
    private AddLoan addLoan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_loan);
        utils = new Utils(this);
        databaseHelper = new DatabaseHelper(this);
        initViews();
        setOnClickListners();
    }

    private void initViews(){
        Log.d(TAG, "initViews: started");

        editTextName = (EditText) findViewById(R.id.editTextNameAddLoan);
        editTextInitAmount = (EditText) findViewById(R.id.editTextInitAmountAddLoan);
        editTextMonthlyROI = (EditText) findViewById(R.id.editTextMontlyROIAddLoan);
        editTextInitDate = (EditText) findViewById(R.id.editTextInitDateAddLoan);
        editTextFinishDate = (EditText) findViewById(R.id.editTextFinishDateAddLoan);
        editTextMonthlyPayment = (EditText) findViewById(R.id.editTextMonthlyPaymentAddLoan);

        buttonPickInitDate = (Button) findViewById(R.id.buttonPickInitDateAddLoan);
        buttonPickFinishDate = (Button) findViewById(R.id.buttonPickFinishDateAddLoan);
        buttonAddLoan = (Button) findViewById(R.id.buttonAddLoanAddLoan);

        textWarning = (TextView) findViewById(R.id.textWarningAddLoan);

    }

    private void setOnClickListners(){
        Log.d(TAG, "setOnClickListners: started");

        Log.d(TAG, "setOnClickListners: started");
        buttonPickInitDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(AddLoanActivity.this, initDateListner, initCalendar.get(Calendar.YEAR), initCalendar.get(Calendar.MONTH), initCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        buttonPickFinishDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(AddLoanActivity.this, finishDateListner, finishCalendar.get(Calendar.YEAR), finishCalendar.get(Calendar.MONTH), finishCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        buttonAddLoan.setOnClickListener(new View.OnClickListener() {
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

    private boolean validateData(){
        Log.d(TAG, "validateData: started");
        if(editTextName.getText().toString().equals("")) return false;
        if(editTextInitDate.getText().toString().equals("")) return false;
        if(editTextFinishDate.getText().toString().equals("")) return false;
        if(editTextInitAmount.getText().toString().equals("")) return false;
        if(editTextMonthlyROI.getText().toString().equals("")) return false;
        if(editTextMonthlyPayment.getText().toString().equals("")) return false;
        return true;
    }

    private void initAdding(){
        Log.d(TAG, "initAdding: started");
        User user = utils.isUserLoggedIn();
        if(user != null){
            addTransactions = new AddTransactions();
            addTransactions.execute(user.get_id());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(addTransactions != null && !addTransactions.isCancelled()) addTransactions.cancel(true);
        if(addLoan != null && !addLoan.isCancelled()) addLoan.cancel(true);
    }

    private class AddTransactions extends AsyncTask<Integer, Void, Integer>{

        private double amount;
        private String date, name;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            this.amount = Double.parseDouble(editTextInitAmount.getText().toString());
            this.name = editTextName.getText().toString();
            this.date = editTextInitDate.getText().toString();
        }

        @Override
        protected Integer doInBackground(Integer... integers) {
            try{
                SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
                ContentValues contentValues = new ContentValues();
                contentValues.put("amount", amount);
                contentValues.put("recipient", name);
                contentValues.put("date", date);
                contentValues.put("user_id", integers[0]);
                contentValues.put("description", "Received amount for " + name + " Loan");
                contentValues.put("type", "loan");
                long transactionId = sqLiteDatabase.insert("transactions", null, contentValues);
                sqLiteDatabase.close();
                return (int)transactionId;
            }catch (SQLException e){ e.printStackTrace(); return null; }
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            if(integer != null){
                addLoan = new AddLoan();
                addLoan.execute(integer);
            }
        }
    }

    private class AddLoan extends AsyncTask<Integer, Void, Integer>{

        private int userId;
        private String name, initDate, finishDate;
        private double initAmount, monthlyROI, monthlyPayment;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.name = editTextName.getText().toString();
            this.initDate = editTextInitDate.getText().toString();
            this.finishDate = editTextFinishDate.getText().toString();
            this.initAmount = Double.parseDouble(editTextInitAmount.getText().toString());
            this.monthlyROI = Double.parseDouble(editTextMonthlyROI.getText().toString());
            this.monthlyPayment = Double.parseDouble(editTextMonthlyPayment.getText().toString());

            User user = utils.isUserLoggedIn();
            if(user != null) this.userId = user.get_id();
            else this.userId = -1;
        }

        @Override
        protected Integer doInBackground(Integer... integers) {
            if(userId != -1){
                try{
                    SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("name", name);
                    contentValues.put("init_date", initDate);
                    contentValues.put("finish_date", finishDate);
                    contentValues.put("init_amount", initAmount);
                    contentValues.put("remained_amount", initAmount);
                    contentValues.put("monthly_roi", monthlyROI);
                    contentValues.put("monthly_payment", monthlyPayment);
                    contentValues.put("user_id", userId);
                    contentValues.put("transaction_id", integers[0]);

                    long loanId = sqLiteDatabase.insert("loans", null, contentValues);

                    if(loanId != -1){
                        Cursor cursor = sqLiteDatabase.query("users", new String[]{"remained_amount"}, "_id=?",
                                new String[]{String.valueOf(userId)}, null, null, null);
                        if(cursor != null){
                            if(cursor.moveToFirst()){
                                double currentRemainedAmount = cursor.getDouble(cursor.getColumnIndex("remained_amount"));
                                ContentValues newContentValues = new ContentValues();
                                newContentValues.put("remained_amount", currentRemainedAmount + initAmount);
                                sqLiteDatabase.update("users", newContentValues, "_id=?", new String[]{String.valueOf(userId)});
                                cursor.close(); sqLiteDatabase.close(); return (int)loanId;
                            }else{ cursor.close(); sqLiteDatabase.close(); return null; }
                        }else{ sqLiteDatabase.close(); return null; }

                    }else{ sqLiteDatabase.close(); return null; }

                }catch (SQLException e) { e.printStackTrace(); return null; }
            }
            else return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

            if(integer != null){
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                try{
                    Date initDate = simpleDateFormat.parse(this.initDate);
                    calendar.setTime(initDate);
                    int initMonth = calendar.get(Calendar.YEAR) * 12 + calendar.get(Calendar.MONTH);

                    Date finishDate = simpleDateFormat.parse(this.finishDate);
                    calendar.setTime(finishDate);
                    int finishMonth = calendar.get(Calendar.YEAR) * 12 + calendar.get(Calendar.MONTH);
                    int months = finishMonth - initMonth;

                    int days = 0;
                    for(int i = 0; i < months; ++i){
                        days +=30;

                        Data data = new Data.Builder()
                                .putInt("loan_id", integer)
                                .putInt("user_id", userId)
                                .putDouble("monthly_payment", monthlyPayment)
                                .putString("name", name).build();

                        Constraints constraints = new Constraints.Builder()
                                .setRequiresBatteryNotLow(true).build();

                        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(LoanWorker.class)
                                .setInputData(data)
                                .setConstraints(constraints)
                                .setInitialDelay(days, TimeUnit.DAYS)
                                .addTag("loan_payment").build();

                        WorkManager.getInstance(AddLoanActivity.this).enqueue(oneTimeWorkRequest);
                        Intent intent = new Intent(AddLoanActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }

                }catch (ParseException e){ e.printStackTrace(); }
            }
        }
    }

}
