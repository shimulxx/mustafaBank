package com.example.mustafabank;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.example.mustafabank.Database.DatabaseHelper;
import com.example.mustafabank.Models.Loan;
import com.example.mustafabank.Models.Transaction;
import com.example.mustafabank.Models.User;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class StatsActivity extends AppCompatActivity {
    private static final String TAG = "StatsActivity";

    private BarChart barChart;
    private PieChart pieChart;

    private BottomNavigationView bottomNavigationView;

    private DatabaseHelper databaseHelper;
    private Utils utils;

    private GetLoans getLoans;
    private GetTransaction getTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        initViews();
        initBottomNavigationView();

        databaseHelper = new DatabaseHelper(this);
        utils = new Utils(this);

        User user = utils.isUserLoggedIn();
        if(user != null){
            getTransaction = new GetTransaction();
            getLoans = new GetLoans();
            getTransaction.execute(user.get_id());
            getLoans.execute(user.get_id());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(getTransaction != null && !getTransaction.isCancelled()) getTransaction.cancel(true);
        if(getLoans != null && !getLoans.isCancelled()) getLoans.cancel(true);
    }

    private void initViews(){
        Log.d(TAG, "initViews: started");
        barChart = (BarChart) findViewById(R.id.barChartActivitivitiesStatsActivity);
        pieChart = (PieChart) findViewById(R.id.pieChartLoanStatsActivity);
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigationViewStatsActivity);
    }

    private void initBottomNavigationView(){
        Log.d(TAG, "initBottomNavigationView: started");
        bottomNavigationView.setSelectedItemId(R.id.menu_item_stats);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.menu_item_stats:
                        break;
                    case R.id.menu_item_transaction:
                        Intent transactionIntent = new Intent(StatsActivity.this, TransactionActivity.class);
                        transactionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(transactionIntent);
                        break;
                    case R.id.menu_item_home:
                        Intent intentHome = new Intent(StatsActivity.this, MainActivity.class);
                        intentHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intentHome);
                        break;
                    case R.id.menu_item_loan:
                        Intent loanIntent = new Intent(StatsActivity.this, LoanActivity.class);
                        loanIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(loanIntent);
                        break;
                    case R.id.menu_item_investment:
                        Intent intent = new Intent(StatsActivity.this, InvestmentActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        break;
                    default: break;
                }
                return false;
            }
        });
    }

    private class GetTransaction extends AsyncTask<Integer, Void, ArrayList<Transaction>>{
        @Override
        protected ArrayList<Transaction> doInBackground(Integer... integers) {
            try{
                SQLiteDatabase sqLiteDatabase = databaseHelper.getReadableDatabase();
                Cursor cursor = sqLiteDatabase.query("transactions", null, null, null, null, null, null);
                if(cursor != null){
                    if(cursor.moveToFirst()){
                        ArrayList<Transaction> transactions = new ArrayList<>();
                        for(int i = 0; i < cursor.getCount(); ++i){
                            Transaction transaction = new Transaction();
                            transaction.set_id(cursor.getInt(cursor.getColumnIndex("_id")));
                            transaction.setUser_id(cursor.getInt(cursor.getColumnIndex("user_id")));
                            transaction.setType(cursor.getString(cursor.getColumnIndex("type")));
                            transaction.setDescription(cursor.getString(cursor.getColumnIndex("description")));
                            transaction.setRecipient(cursor.getString(cursor.getColumnIndex("recipient")));
                            transaction.setDate(cursor.getString(cursor.getColumnIndex("date")));
                            transaction.setAmount(cursor.getDouble(cursor.getColumnIndex("amount")));
                            transactions.add(transaction); cursor.moveToNext();
                        }
                        cursor.close(); sqLiteDatabase.close(); return transactions;
                    }else{ cursor.close(); sqLiteDatabase.close(); return null; }
                }else{ sqLiteDatabase.close(); return null; }
            }catch (SQLException e) { e.printStackTrace(); return null; }
        }

        @Override
        protected void onPostExecute(ArrayList<Transaction> transactions) {
            super.onPostExecute(transactions);
            if(transactions != null){
                Calendar calendar = Calendar.getInstance();
                int currentMonth = calendar.get(Calendar.MONTH);
                int currentYear = calendar.get(Calendar.YEAR);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

                ArrayList<BarEntry> barEntries = new ArrayList<>();
                for(Transaction transaction: transactions){
                    try{
                        Date date = simpleDateFormat.parse(transaction.getDate());
                        calendar.setTime(date);
                        int month = calendar.get(Calendar.MONTH);
                        int year = calendar.get(Calendar.YEAR);
                        int day = calendar.get(Calendar.DAY_OF_MONTH) + 1;

                        if(month == currentMonth && year == currentYear){
                            boolean doesDayExist = false;
                            for(BarEntry barEntry: barEntries){
                                if(barEntry.getX() == day) { doesDayExist = true; break; }
                            }

                            if(doesDayExist){
                                for(BarEntry barEntry: barEntries){
                                    if(barEntry.getX() == day){
                                        barEntry.setY(barEntry.getY() + (float)transaction.getAmount());
                                    }
                                }
                            }
                            else{ barEntries.add(new BarEntry(day, (float)transaction.getAmount())); }
                        }

                    }catch (ParseException e) { e.printStackTrace(); }
                }

                BarDataSet barDataSet = new BarDataSet(barEntries, "Account Activity");
                barDataSet.setColor(Color.GREEN);
                BarData barData = new BarData(barDataSet);
                barChart.getAxisRight().setEnabled(false);
                XAxis xAxis = barChart.getXAxis();
                xAxis.setAxisMaximum(31);
                xAxis.setEnabled(false);
                YAxis yAxis = barChart.getAxisLeft();
                //yAxis.setAxisMaximum(40);
                yAxis.setAxisMinimum(0);
                yAxis.setDrawGridLines(false);
                barChart.setData(barData);
                Description description = new Description();
                description.setText("All of the account transactions");
                description.setTextSize(12f);
                barChart.setDescription(description);
                //dailySpentBarChart.animateY(2000);
                barChart.notifyDataSetChanged();
                barChart.invalidate();
            }
        }
    }

    private class GetLoans extends AsyncTask<Integer, Void, ArrayList<Loan>>{
        @Override
        protected ArrayList<Loan> doInBackground(Integer... integers) {
            try{
                SQLiteDatabase sqLiteDatabase = databaseHelper.getReadableDatabase();
                Cursor cursor = sqLiteDatabase.query("loans", null, null, null, null, null, null);
                if(cursor != null){
                    if(cursor.moveToFirst()){
                        ArrayList<Loan> loans = new ArrayList<>();
                        for(int i = 0; i < cursor.getCount(); ++i){
                            Loan loan = new Loan();
                            loan.set_id(cursor.getInt(cursor.getColumnIndex("_id")));
                            loan.setUser_id(cursor.getInt(cursor.getColumnIndex("user_id")));
                            loan.setTransaction_id(cursor.getInt(cursor.getColumnIndex("transaction_id")));
                            loan.setName(cursor.getString(cursor.getColumnIndex("name")));
                            loan.setInit_date(cursor.getString(cursor.getColumnIndex("init_date")));
                            loan.setFinish_date(cursor.getString(cursor.getColumnIndex("finish_date")));
                            loan.setMonthly_roi(cursor.getDouble(cursor.getColumnIndex("monthly_roi")));
                            loan.setMonthly_payment(cursor.getDouble(cursor.getColumnIndex("monthly_payment")));
                            loan.setRemained_amount(cursor.getDouble(cursor.getColumnIndex("remained_amount")));
                            loan.setInit_amount(cursor.getDouble(cursor.getColumnIndex("init_amount")));
                            loans.add(loan); cursor.moveToNext();
                        }
                        cursor.close(); sqLiteDatabase.close(); return loans;
                    }else{ cursor.close(); sqLiteDatabase.close(); return null; }
                }else{ sqLiteDatabase.close(); return null; }
            }catch (SQLException e) { e.printStackTrace(); return null; }
        }

        @Override
        protected void onPostExecute(ArrayList<Loan> loans) {
            super.onPostExecute(loans);
            if(loans != null){
                ArrayList<PieEntry> entries = new ArrayList<>();
                double totalLoansAmount = 0.0;
                double totalReaminedAmount = 0.0;
                for(Loan l: loans){
                    totalLoansAmount += l.getInit_amount();
                    totalReaminedAmount += l.getRemained_amount();
                }

                entries.add(new PieEntry((float)totalLoansAmount, "Total Loans"));
                entries.add(new PieEntry((float)totalReaminedAmount, "Remained Loans"));

                PieDataSet pieDataSet = new PieDataSet(entries, "");
                pieDataSet.setColors(ColorTemplate.JOYFUL_COLORS);
                pieDataSet.setValueTextColor(Color.BLACK);
                pieDataSet.setValueTextSize(12f);
                //pieDataSet.setColors(Color.RED, Color.GREEN);
                int val = getResources().getColor(R.color.deep_red);
                long valINt = Color.RED;
                long valINt2 = Color.GREEN;
                //long colorW = Color.parseColor(val);
                //pieDataSet.setColors(getResources().getColor(R.color.deep_red), getResources().getColor(R.color.deep_green));
                pieDataSet.setColors(Color.RED, Color.GREEN);
                pieDataSet.setSliceSpace(5f);
                PieData pieData = new PieData(pieDataSet);
                //pieChart.setDrawHoleEnabled(false);
                pieChart.animateY(2000, Easing.EaseInOutCubic);
                pieChart.setEntryLabelColor(Color.BLACK);
                pieChart.setData(pieData);
                pieChart.invalidate();
            }
        }
    }

}