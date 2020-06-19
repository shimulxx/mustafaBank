package com.example.mustafabank;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.WorkManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mustafabank.Adapters.TransactionAdapter;
import com.example.mustafabank.Authentication.LoginActivity;
import com.example.mustafabank.Database.DatabaseHelper;
import com.example.mustafabank.Dialogs.AddTransactionDialog;
import com.example.mustafabank.Models.Shopping;
import com.example.mustafabank.Models.Transaction;
import com.example.mustafabank.Models.User;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private BottomNavigationView bottomNavigationView;
    private TextView textAmount, textWelcome;
    private RecyclerView transactionRecyclerView;
    private BarChart dailySpentBarChart;
    private LineChart profitLineChart;
    private FloatingActionButton fbAddTransaction;
    private Toolbar toolbar;

    private DatabaseHelper databaseHelper;

    private GetAccountAmount getAccountAmount;
    private GetTransactions getTransactions;
    private Getprofit getprofit;
    private GetSpending getSpending;

    private TransactionAdapter transactionAdapter;

    private Utils utils;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initBottomNavigationView();

        setSupportActionBar(toolbar);

        utils = new Utils(this);
        User user = utils.isUserLoggedIn();
        if(user != null){
            Toast.makeText(this, "User: " + user.getFirst_name() + "is logged in", Toast.LENGTH_SHORT).show();
        }else {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

        databaseHelper = new DatabaseHelper(this);

        setupAmount();
        setOnClickListner();
        initTransactionRecView();
        initLineChart();
        initBarChart();
        Log.d(TAG, "onCreate: investment work: " + WorkManager.getInstance(this).getWorkInfosByTag("profit"));
        Log.d(TAG, "onCreate: loan work: " + WorkManager.getInstance(this).getWorkInfosByTag("loan_payment"));
    }
    
    private void initBarChart(){
        Log.d(TAG, "initBarChart: started");
        getSpending = new GetSpending();
        User user = utils.isUserLoggedIn();
        if(user != null) getSpending.execute(user.get_id());
    }

    private void initLineChart(){
        Log.d(TAG, "initLineChart: started");
        getprofit = new Getprofit();
        User user = utils.isUserLoggedIn();
        if(user != null) getprofit.execute(user.get_id());
    }

    private void initTransactionRecView(){
        Log.d(TAG, "initTransactionRecView: started");
        transactionAdapter = new TransactionAdapter();
        transactionRecyclerView.setAdapter(transactionAdapter);
        transactionRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        getTransactionsMethod();
    }

    private void getTransactionsMethod(){
        Log.d(TAG, "getTransactionsMethod: started");
        getTransactions = new GetTransactions();
        User user = utils.isUserLoggedIn();
        if(user != null) getTransactions.execute(user.get_id());
    }


    private void setOnClickListner(){
        Log.d(TAG, "setOnClickListner: started");
        textWelcome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Mustafa Bank")
                        .setMessage("Created and Developed by Mustafa Hamim at google.com")
                        .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).setPositiveButton("Visit", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(MainActivity.this, WebsiteActivity.class);
                                startActivity(intent);
                            }
                        });
                builder.show();
            }
        });

        fbAddTransaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddTransactionDialog addTransactionDialog = new AddTransactionDialog();
                addTransactionDialog.show(getSupportFragmentManager(), "Add transaction");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupAmount();
        getTransactionsMethod();
        initLineChart();
        initBarChart();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setupAmount();
        getTransactionsMethod();
        initLineChart();
        initBarChart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(getTransactions != null && !getTransactions.isCancelled()) getTransactions.cancel(true);
        if(getAccountAmount != null && !getAccountAmount.isCancelled()) getAccountAmount.cancel(true);
        if(getprofit != null && !getprofit.isCancelled()) getprofit.cancel(true);
        if(getSpending != null && !getSpending.isCancelled()) getSpending.cancel(true);
    }

    private void setupAmount(){
        Log.d(TAG, "setupAmount: started");
        User user = utils.isUserLoggedIn();
        if(user != null){
            getAccountAmount = new GetAccountAmount();
            getAccountAmount.execute(user.get_id());
        }
    }

    private class GetTransactions extends AsyncTask<Integer, Void, ArrayList<Transaction>>{
        @Override
        protected ArrayList<Transaction> doInBackground(Integer... integers) {
            try{
                SQLiteDatabase sqLiteDatabase = databaseHelper.getReadableDatabase();
                Cursor cursor = sqLiteDatabase.query("transactions", null, "user_id=?",
                        new String[]{String.valueOf(integers[0])}, null, null, "date desc");
                if(cursor != null){
                    if(cursor.moveToFirst()){
                        ArrayList<Transaction> transactions = new ArrayList<>();
                        for(int i = 0; i < cursor.getCount(); ++i){
                            Transaction transaction = new Transaction();
                            transaction.set_id(cursor.getInt(cursor.getColumnIndex("_id")));
                            transaction.setAmount(cursor.getDouble(cursor.getColumnIndex("amount")));
                            transaction.setDate(cursor.getString(cursor.getColumnIndex("date")));
                            transaction.setDescription(cursor.getString(cursor.getColumnIndex("description")));
                            transaction.setType(cursor.getString(cursor.getColumnIndex("type")));
                            transaction.setUser_id(cursor.getInt(cursor.getColumnIndex("user_id")));
                            transaction.setRecipient(cursor.getString(cursor.getColumnIndex("recipient")));
                            transactions.add(transaction); cursor.moveToNext();
                        }
                        cursor.close(); sqLiteDatabase.close(); return transactions;
                    }else{ cursor.close(); sqLiteDatabase.close(); return null; }
                }else{ sqLiteDatabase.close(); return null; }
            }catch (SQLException e){ e.printStackTrace(); return null; }
        }

        @Override
        protected void onPostExecute(ArrayList<Transaction> transactions) {
            super.onPostExecute(transactions);
            if(transactions != null) transactionAdapter.setTransactions(transactions);
            else transactionAdapter.setTransactions(new ArrayList<Transaction>());
        }
    }

    private class GetAccountAmount extends AsyncTask<Integer, Void, Double>{
        @Override
        protected Double doInBackground(Integer... integers) {
            try{
                SQLiteDatabase sqLiteDatabase = databaseHelper.getReadableDatabase();
                Cursor cursor = sqLiteDatabase.query("users", new String[]{"remained_amount"}, "_id=?",
                        new String[]{String.valueOf(integers[0])}, null, null, null);
                if(cursor != null){
                    if(cursor.moveToFirst()){
                        double amount = cursor.getDouble(cursor.getColumnIndex("remained_amount"));
                        cursor.close(); sqLiteDatabase.close(); return amount;
                    }else{ cursor.close(); sqLiteDatabase.close(); return null; }
                }else{ sqLiteDatabase.close(); return null; }
            }catch (SQLException e){ e.printStackTrace(); return null; }
        }

        @Override
        protected void onPostExecute(Double aDouble) {
            super.onPostExecute(aDouble);
            if(aDouble != null){ textAmount.setText(String.valueOf(aDouble) + " $"); }
            else { textAmount.setText("0.0 $"); }
        }
    }

    private class Getprofit extends AsyncTask<Integer, Void, ArrayList<Transaction>>{
        @Override
        protected ArrayList<Transaction> doInBackground(Integer... integers) {
            try{
                SQLiteDatabase sqLiteDatabase = databaseHelper.getReadableDatabase();
                Cursor cursor = sqLiteDatabase.query("transactions", null, "user_id=? and type=?",
                        new String[]{String.valueOf(integers[0]), "profit"}, null, null, null);
                if(cursor != null){
                    if(cursor.moveToFirst()){
                        ArrayList<Transaction> transactions = new ArrayList<>();
                        for(int i = 0; i < cursor.getCount(); ++i){
                            Transaction transaction = new Transaction();
                            transaction.set_id(cursor.getInt(cursor.getColumnIndex("_id")));
                            transaction.setAmount(cursor.getDouble(cursor.getColumnIndex("amount")));
                            transaction.setDate(cursor.getString(cursor.getColumnIndex("date")));
                            transaction.setDescription(cursor.getString(cursor.getColumnIndex("description")));
                            transaction.setRecipient(cursor.getString(cursor.getColumnIndex("recipient")));
                            transaction.setType(cursor.getString(cursor.getColumnIndex("type")));
                            transaction.setUser_id(cursor.getInt(cursor.getColumnIndex("user_id")));
                            transactions.add(transaction);
                            cursor.moveToNext();
                        }
                        cursor.close(); sqLiteDatabase.close(); return transactions;
                    }else{ cursor.close(); sqLiteDatabase.close(); return null;}
                }else{ sqLiteDatabase.close(); return null; }
            }catch (SQLException e){ e.printStackTrace(); return null; }
        }

        @Override
        protected void onPostExecute(ArrayList<Transaction> transactions) {
            super.onPostExecute(transactions);

            if(transactions != null){
                ArrayList<Entry> entries = new ArrayList<>();

                for(Transaction transaction: transactions){
                    try {
                        Date date = new SimpleDateFormat("yyyy-MM-dd").parse(transaction.getDate());
                        Calendar calendar = Calendar.getInstance();
                        int year = calendar.get(Calendar.YEAR);
                        calendar.setTime(date);
                        int month = calendar.get(Calendar.MONTH) + 1;

                        if(calendar.get(Calendar.YEAR) == 2019){
                            boolean doesMonthExist = false;
                            for(Entry entry: entries){
                                if(entry.getX() == month) { doesMonthExist = true; break; }
                            }
                            if(!doesMonthExist){ entries.add(new Entry(month, (float)transaction.getAmount())); }
                            else{
                                for(Entry entry: entries){
                                    if(entry.getX() == month) { entry.setY(entry.getY() + (float)transaction.getAmount()); }
                                }
                            }
                        }

                    } catch (ParseException e) { e.printStackTrace(); }
                }

                LineDataSet lineDataSet = new LineDataSet(entries, "Profit chart");
                lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                lineDataSet.setDrawFilled(true);
                lineDataSet.setFillColor(Color.GREEN);
                LineData lineData = new LineData(lineDataSet);
                profitLineChart.setData(lineData);
                //profitLineChart.setTouchEnabled(true);
                XAxis xAxis = profitLineChart.getXAxis();
                xAxis.setSpaceMin(1);
                xAxis.setSpaceMax(1);
                xAxis.setAxisMaximum(12);
                xAxis.setEnabled(false);
                YAxis yAxis = profitLineChart.getAxisRight();
                yAxis.setEnabled(false);
                YAxis leftAxis = profitLineChart.getAxisLeft();
                leftAxis.setAxisMaximum(100);
                leftAxis.setAxisMinimum(0);
                leftAxis.setDrawGridLines(false);
                //Description description = new Description();
                //description.setText("My Description");
                profitLineChart.setDescription(null);
                profitLineChart.animateY(2000);
                profitLineChart.notifyDataSetChanged();
                profitLineChart.invalidate();

            }else{ Log.d(TAG, "onPostExecute: transaction array list is null"); }
        }
    }

    private class GetSpending extends AsyncTask<Integer, Void, ArrayList<Shopping>>{
        @Override
        protected ArrayList<Shopping> doInBackground(Integer... integers) {
            try{
                SQLiteDatabase sqLiteDatabase = databaseHelper.getReadableDatabase();
                Cursor cursor = sqLiteDatabase.query("shopping", new String[]{"date", "price"}, "user_id=?",
                        new String[]{String.valueOf(integers[0])}, null, null, null);
                if(cursor != null){
                    if(cursor.moveToFirst()){
                        ArrayList<Shopping> shoppings = new ArrayList<>();
                        for(int i = 0; i < cursor.getCount(); ++i){
                            Shopping shopping = new Shopping();
                            shopping.setDate(cursor.getString(cursor.getColumnIndex("date")));
                            shopping.setPrice(cursor.getDouble(cursor.getColumnIndex("price")));
                            shoppings.add(shopping);
                            cursor.moveToNext();
                        }
                        cursor.close(); sqLiteDatabase.close(); return shoppings;
                    }else{ cursor.close(); sqLiteDatabase.close(); return null; }
                }else{ sqLiteDatabase.close(); return null; }
            }catch (SQLException e){ e.printStackTrace(); return null; }
        }

        @Override
        protected void onPostExecute(ArrayList<Shopping> shoppings) {
            super.onPostExecute(shoppings);
            if(shoppings != null){
                ArrayList<BarEntry> barEntries = new ArrayList<>();
                for(Shopping shopping: shoppings){
                    try {
                        Date date = new SimpleDateFormat("yyyy-MM-dd").parse(shopping.getDate());
                        Calendar calendar = Calendar.getInstance();
                        int month = calendar.get(Calendar.MONTH) + 1;
                        calendar.setTime(date);
                        int day = calendar.get(Calendar.DAY_OF_MONTH);
                        if(true){
                            boolean doesDayExist = false;
                            for(BarEntry barEntry: barEntries){
                                if(barEntry.getX() == day){ doesDayExist = true; break; }
                            }
                            if(!doesDayExist) barEntries.add(new BarEntry(day, (float)shopping.getPrice()));
                            else{
                                for(BarEntry barEntry: barEntries){
                                    if(barEntry.getX() == day) { barEntry.setY(barEntry.getY() + (float) shopping.getPrice());  break;}
                                }
                            }
                        }
                    } catch (ParseException e) { e.printStackTrace(); }
                }

                BarDataSet barDataSet = new BarDataSet(barEntries, "Shopping chart");
                barDataSet.setColor(Color.RED);
                BarData barData = new BarData(barDataSet);
                dailySpentBarChart.getAxisRight().setEnabled(false);
                XAxis xAxis = dailySpentBarChart.getXAxis();
                xAxis.setAxisMaximum(31);
                xAxis.setEnabled(false);
                YAxis yAxis = dailySpentBarChart.getAxisLeft();
                yAxis.setAxisMaximum(40);
                yAxis.setAxisMinimum(0);
                yAxis.setDrawGridLines(false);
                dailySpentBarChart.setData(barData);
                dailySpentBarChart.setDescription(null);
                //dailySpentBarChart.animateY(2000);
                dailySpentBarChart.notifyDataSetChanged();
                dailySpentBarChart.invalidate();
            }else{
                Log.d(TAG, "onPostExecute: shopping arraylist is null");
            }
        }
    }

    private void initBottomNavigationView(){
        Log.d(TAG, "initBottomNavigationView: started");
        bottomNavigationView.setSelectedItemId(R.id.menu_item_home);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.menu_item_stats:
                        Intent statesIntent = new Intent(MainActivity.this, StatsActivity.class);
                        statesIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(statesIntent);
                        break;
                    case R.id.menu_item_transaction:
                        Intent transactionIntent = new Intent(MainActivity.this, TransactionActivity.class);
                        transactionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(transactionIntent);
                        break;
                    case R.id.menu_item_home:
                        break;
                    case R.id.menu_item_loan:
                        Intent loanIntent = new Intent(MainActivity.this, LoanActivity.class);
                        loanIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(loanIntent);
                        break;
                    case R.id.menu_item_investment:
                        Intent intent = new Intent(MainActivity.this, InvestmentActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        break;
                    default: break;
                }
                return false;
            }
        });
    }

    private void initViews(){
        Log.d(TAG, "initViews: started");
        bottomNavigationView = (BottomNavigationView)findViewById(R.id.bottomNavigationView);
        textAmount = (TextView) findViewById(R.id.textAmount);
        textWelcome = (TextView)findViewById(R.id.textWelcome);
        transactionRecyclerView = (RecyclerView) findViewById(R.id.transactionRecView);
        dailySpentBarChart = (BarChart) findViewById(R.id.dailySpentBarChart);
        profitLineChart = (LineChart) findViewById(R.id.profitLineChart);
        fbAddTransaction = (FloatingActionButton) findViewById(R.id.fbAddTransaction);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_toolbar_menu, menu); return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_mustafa:
                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setTitle("Dialog")
                        .setMessage("Desigen and developed by Mustafa Hamim")
                        .setNegativeButton("Visit", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(MainActivity.this, WebsiteActivity.class);
                                startActivity(intent);
                            }
                        })
                        .setPositiveButton("Invite friends", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String message = "Hey, How is everything?\nCheckout this new awesome app. it helps me manage my money stuff" +
                                        "\nhttps://google.com";
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.putExtra(Intent.EXTRA_TEXT, message);
                                intent.setType("text/plain");
                                Intent chooserIntent = Intent.createChooser(intent, "Send message via:");
                                startActivity(chooserIntent);
                            }
                        });
                builder.show(); break;
            default: break;
        }
        return super.onOptionsItemSelected(item);
    }
}
