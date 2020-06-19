package com.example.mustafabank;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.example.mustafabank.Adapters.LoanAdapter;
import com.example.mustafabank.Database.DatabaseHelper;
import com.example.mustafabank.Models.Loan;
import com.example.mustafabank.Models.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Date;

public class LoanActivity extends AppCompatActivity {
    private static final String TAG = "LoanActivity";

    private RecyclerView recyclerView;
    private BottomNavigationView bottomNavigationView;
    private LoanAdapter loanAdapter;

    private DatabaseHelper databaseHelper = new DatabaseHelper(this);
    private GetLoans getLoans;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loan);
        initViews();
        initBottomNavigationView();
        loanAdapter = new LoanAdapter(this);
        recyclerView.setAdapter(loanAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Utils utils = new Utils(this);
        User user = utils.isUserLoggedIn();
        if(user != null){
            getLoans = new GetLoans();
            getLoans.execute(user.get_id());
        }
    }

    private class GetLoans extends AsyncTask<Integer, Void, ArrayList<Loan>>{
        @Override
        protected ArrayList<Loan> doInBackground(Integer... integers) {
            try{
                SQLiteDatabase sqLiteDatabase = databaseHelper.getReadableDatabase();
                Cursor cursor = sqLiteDatabase.query("loans", null, "user_id=?",
                        new String[]{ String.valueOf(integers[0]) }, null, null, "init_date desc");
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
                            loan.setInit_amount(cursor.getDouble(cursor.getColumnIndex("init_amount")));
                            loan.setMonthly_roi(cursor.getDouble(cursor.getColumnIndex("monthly_roi")));
                            loan.setMonthly_payment(cursor.getDouble(cursor.getColumnIndex("monthly_payment")));
                            loan.setRemained_amount(cursor.getDouble(cursor.getColumnIndex("remained_amount")));

                            loans.add(loan); cursor.moveToNext();
                        }
                        return loans;
                    }else{ cursor.close(); sqLiteDatabase.close(); return null; }
                }else{ sqLiteDatabase.close(); return null; }
            }catch (SQLException e) { e.printStackTrace(); return null; }
        }

        @Override
        protected void onPostExecute(ArrayList<Loan> loans) {
            super.onPostExecute(loans);
            if(loans != null) loanAdapter.setLoans(loans);
            else loanAdapter.setLoans(new ArrayList<Loan>());
        }
    }

    private void initViews(){
        Log.d(TAG, "initViews: started");
        recyclerView = (RecyclerView) findViewById(R.id.loanRecViewLoanActivity);
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigationViewLoanActivity);
    }

    private void initBottomNavigationView(){
        Log.d(TAG, "initBottomNavigationView: started");
        bottomNavigationView.setSelectedItemId(R.id.menu_item_loan);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.menu_item_stats:
                        Intent intentStats = new Intent(LoanActivity.this, StatsActivity.class);
                        intentStats.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intentStats);
                        break;
                    case R.id.menu_item_transaction:
                        break;
                    case R.id.menu_item_home:
                        Intent intentHome = new Intent(LoanActivity.this, MainActivity.class);
                        intentHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intentHome);
                        break;
                    case R.id.menu_item_loan:
                        Intent intentLoan = new Intent(LoanActivity.this, LoanActivity.class);
                        intentLoan.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intentLoan);
                        break;
                    case R.id.menu_item_investment:
                        Intent intentInvestment = new Intent(LoanActivity.this, InvestmentActivity.class);
                        intentInvestment.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intentInvestment);
                        break;
                    default: break;
                }
                return false;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(getLoans != null && !getLoans.isCancelled()) getLoans.cancel(true);
    }
}
