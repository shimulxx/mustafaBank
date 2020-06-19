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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.mustafabank.Adapters.TransactionAdapter;
import com.example.mustafabank.Database.DatabaseHelper;
import com.example.mustafabank.Models.Transaction;
import com.example.mustafabank.Models.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class TransactionActivity extends AppCompatActivity {
    private static final String TAG = "TransactionActivity";

    private RadioGroup radioGroupType;
    private Button buttonSearch;
    private EditText editTextMin;
    private RecyclerView transactionRecView;
    private TextView textNoTransaction;
    private BottomNavigationView bottomNavigationView;
    private TransactionAdapter transactionAdapter;

    private DatabaseHelper databaseHelper;

    private GetTransactions getTransactions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);

        initViews();
        initBottomNavigationView();

        transactionAdapter = new TransactionAdapter();
        transactionRecView.setAdapter(transactionAdapter);
        transactionRecView.setLayoutManager(new LinearLayoutManager(this));
        databaseHelper = new DatabaseHelper(this);
        initSearch();
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initSearch();
            }
        });
        radioGroupType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                initSearch();
            }
        });
    }

    private void initSearch(){
        Log.d(TAG, "initSearch: started");

        Utils utils = new Utils(this);
        User user = utils.isUserLoggedIn();
        if(user != null){
            getTransactions = new GetTransactions();
            getTransactions.execute(user.get_id());
        }

    }

    private class GetTransactions extends AsyncTask<Integer, Void, ArrayList<Transaction>>{

        private String type = "all";
        private double min = 0.0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.min = Double.parseDouble(editTextMin.getText().toString());
            switch (radioGroupType.getCheckedRadioButtonId()){
                case R.id.rbInvestmentTransactionActivity: type = "investment"; break;
                case R.id.rbLoanTransactionActivity: type = "loan"; break;
                case R.id.rbLoanPaymentTransactionActivity: type = "loan_payment"; break;
                case R.id.rbProfitTransactionActivity: type = "profit"; break;
                case R.id.rbShoppingTransactionActivity: type = "shopping"; break;
                case R.id.rbSendTransactionActivity: type = "send"; break;
                case R.id.rbReceiveTransactionActivity: type = "receive"; break;
                default: type = "all"; break;
            }
        }

        @Override
        protected ArrayList<Transaction> doInBackground(Integer... integers) {
            try{
                SQLiteDatabase sqLiteDatabase = databaseHelper.getReadableDatabase();
                Cursor cursor;
                if(type.equals("all")) cursor = sqLiteDatabase.query("transactions", null, "user_id=?",
                            new String[]{ String.valueOf(integers[0]) }, null, null,"date desc");
                else cursor = sqLiteDatabase.query("transactions", null, "type=? and user_id=?",
                            new String[] {type, String.valueOf(integers[0])}, null, null, "date desc");

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

                            double absAmount = Math.abs(transaction.getAmount());
                            if(absAmount > this.min) transactions.add(transaction);
                            cursor.moveToNext();
                        }
                        cursor.close(); sqLiteDatabase.close(); return transactions;
                    }else{ cursor.close(); sqLiteDatabase.close(); return null; }
                }else{ sqLiteDatabase.close(); return null; }

            }catch (SQLException e) { e.printStackTrace(); return null; }
        }

        @Override
        protected void onPostExecute(ArrayList<Transaction> transactions) {
            super.onPostExecute(transactions);
            if(null != transactions){
                textNoTransaction.setVisibility(View.GONE);
                transactionAdapter.setTransactions(transactions);
            }else{
                textNoTransaction.setVisibility(View.VISIBLE);
                transactionAdapter.setTransactions(new ArrayList<Transaction>());
            }
        }
    }

    private void initViews(){
        Log.d(TAG, "initViews: started");
        radioGroupType = (RadioGroup) findViewById(R.id.radioGroupTypeTransactionActivity);
        buttonSearch = (Button) findViewById(R.id.buttonSearchTransactionActivity);
        editTextMin = (EditText) findViewById(R.id.editTextMinTransactionActivity);
        transactionRecView = (RecyclerView) findViewById(R.id.transactionRecViewTransactionActivity);
        textNoTransaction = (TextView) findViewById(R.id.textNoTransactionTransactionActivity);
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigationViewTransactionActivity);
    }

    private void initBottomNavigationView(){
        Log.d(TAG, "initBottomNavigationView: started");
        bottomNavigationView.setSelectedItemId(R.id.menu_item_transaction);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.menu_item_stats:
                        Intent intentStats = new Intent(TransactionActivity.this, StatsActivity.class);
                        intentStats.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intentStats);
                        break;
                    case R.id.menu_item_transaction:
                        break;
                    case R.id.menu_item_home:
                        Intent homeIntent = new Intent(TransactionActivity.this, MainActivity.class);
                        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(homeIntent);
                        break;
                    case R.id.menu_item_loan:
                        Intent loanIntent = new Intent(TransactionActivity.this, LoanActivity.class);
                        loanIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(loanIntent);
                        break;
                    case R.id.menu_item_investment:
                        Intent intent = new Intent(TransactionActivity.this, InvestmentActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
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
        if(getTransactions != null && !getTransactions.isCancelled()) getTransactions.cancel(true);
    }
}
