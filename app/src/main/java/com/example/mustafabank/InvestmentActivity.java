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

import com.example.mustafabank.Adapters.InvestmentAdapter;
import com.example.mustafabank.Database.DatabaseHelper;
import com.example.mustafabank.Models.Investment;
import com.example.mustafabank.Models.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class InvestmentActivity extends AppCompatActivity {
    private static final String TAG = "InvestmentActivity";

    private RecyclerView recyclerView;
    private BottomNavigationView bottomNavigationView;

    private InvestmentAdapter investmentAdapter;
    private DatabaseHelper databaseHelper;

    private GetInvestments getInvestments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_investment);

        initViews();
        initBottomNavigationView();

        databaseHelper = new DatabaseHelper(this);

        investmentAdapter = new InvestmentAdapter(this);
        recyclerView.setAdapter(investmentAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        getInvestments = new GetInvestments();
        Utils utils = new Utils(this);
        User user = utils.isUserLoggedIn();
        if(user != null) getInvestments.execute(user.get_id());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(getInvestments != null && !getInvestments.isCancelled()) getInvestments.cancel(true);
    }

    private class GetInvestments extends AsyncTask<Integer, Void, ArrayList<Investment>>{
        @Override
        protected ArrayList<Investment> doInBackground(Integer... integers) {
            try{
                SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
                Cursor cursor = sqLiteDatabase.query("investments", null, "user_id=?",
                        new String[]{ String.valueOf(integers[0]) }, null, null, "init_date desc");
                if(cursor != null){
                    if(cursor.moveToFirst()){
                        ArrayList<Investment> investments = new ArrayList<>();
                        for(int i = 0; i < cursor.getCount(); ++i){
                            Investment investment = new Investment();
                            investment.set_id(cursor.getInt(cursor.getColumnIndex("_id")));
                            investment.setUser_id(cursor.getInt(cursor.getColumnIndex("user_id")));
                            investment.setTransaction_id(cursor.getInt(cursor.getColumnIndex("transaction_id")));
                            investment.setAmount(cursor.getDouble(cursor.getColumnIndex("amount")));
                            investment.setFinish_date(cursor.getString(cursor.getColumnIndex("finish_date")));
                            investment.setInit_date(cursor.getString(cursor.getColumnIndex("init_date")));
                            investment.setMonthly_roi(cursor.getDouble(cursor.getColumnIndex("monthly_roi")));
                            investment.setName(cursor.getString(cursor.getColumnIndex("name")));

                            investments.add(investment);
                            cursor.moveToNext();
                        }
                        cursor.close(); sqLiteDatabase.close(); return investments;
                    }else{ cursor.close(); sqLiteDatabase.close(); return null; }

                }else{ sqLiteDatabase.close(); return null; }
            }catch (SQLException e){ e.printStackTrace(); return null;}

        }

        @Override
        protected void onPostExecute(ArrayList<Investment> investments) {
            super.onPostExecute(investments);

            if(investments != null) investmentAdapter.setInvestments(investments);
            else investmentAdapter.setInvestments(new ArrayList<Investment>());

        }
    }

    private void initViews(){
        Log.d(TAG, "initViews: started");
        recyclerView = (RecyclerView) findViewById(R.id.recViewInvestmentActivity);
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigationViewInvestmentActivity);
    }

    private void initBottomNavigationView(){
        Log.d(TAG, "initBottomNavigationView: started");
        bottomNavigationView.setSelectedItemId(R.id.menu_item_investment);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.menu_item_stats:
                        Intent intentStats = new Intent(InvestmentActivity.this, StatsActivity.class);
                        intentStats.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intentStats);
                        break;
                    case R.id.menu_item_transaction:
                        Intent intentTransaction = new Intent(InvestmentActivity.this, TransactionActivity.class);
                        intentTransaction.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intentTransaction);
                        break;
                    case R.id.menu_item_home:
                        Intent intentHome = new Intent(InvestmentActivity.this, MainActivity.class);
                        intentHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intentHome);
                        break;
                    case R.id.menu_item_loan:
                        Intent intentLoan = new Intent(InvestmentActivity.this, LoanActivity.class);
                        intentLoan.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intentLoan);
                        break;
                    case R.id.menu_item_investment:
                        break;
                    default: break;
                }
                return false;
            }
        });
    }
}
