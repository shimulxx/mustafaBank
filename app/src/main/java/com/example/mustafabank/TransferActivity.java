package com.example.mustafabank;

import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.mustafabank.Database.DatabaseHelper;
import com.example.mustafabank.Models.User;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TransferActivity extends AppCompatActivity {
    private static final String TAG = "TransferActivity";

    private EditText editTextAmount, editTextDescription, editTextRecipieint, editTextDate;
    private TextView textWarning;
    private Button buttonPickDate, buttonAdd;
    private RadioGroup radioGroupType;

    private AddTransaction addTransaction;

    private Calendar calendar = Calendar.getInstance();

    private DatePickerDialog.OnDateSetListener initDateSetListner = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            editTextDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime()));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

        initViews();
        setOnClickListners();

    }

    private void setOnClickListners(){
        Log.d(TAG, "setOnClickListners: started");
        buttonPickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(TransferActivity.this, initDateSetListner,
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateDate()){
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
        Utils utils = new Utils(this);
        User user = utils.isUserLoggedIn();
        if(user != null){
            addTransaction = new AddTransaction();
            addTransaction.execute(user.get_id());
        }
    }

    private class AddTransaction extends AsyncTask<Integer, Void, Void>{

        private double amount;
        private String recipient, date, desccription, type;

        private DatabaseHelper databaseHelper;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.amount = Double.parseDouble(editTextAmount.getText().toString());
            this.recipient = editTextRecipieint.getText().toString();
            this.date = editTextDate.getText().toString();
            this.desccription = editTextDescription.getText().toString();

            int rbId = radioGroupType.getCheckedRadioButtonId();

            switch (rbId){
                case R.id.buttonReceiveTransfer: type = "receive"; break;
                case R.id.buttonSendTransfer: type = "send"; amount = -amount; break;
                default: break;
            }

            databaseHelper = new DatabaseHelper(TransferActivity.this);
        }

        @Override
        protected Void doInBackground(Integer... integers) {

            try{
                SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
                ContentValues contentValues = new ContentValues();
                contentValues.put("amount", this.amount);
                contentValues.put("recipient", recipient);
                contentValues.put("date", date);
                contentValues.put("type", type);
                contentValues.put("description", desccription);
                contentValues.put("user_id", integers[0]);

                long id = sqLiteDatabase.insert("transactions", null, contentValues);
                Log.d(TAG, "doInBackground: new Transaction id: " + id);

                if(id != -1){
                    Cursor cursor = sqLiteDatabase.query("users", new String[]{"remained_amount"},
                            "_id=?", new String[]{String.valueOf(integers[0])}, null, null, null);
                    if(cursor != null){
                        if(cursor.moveToFirst()){
                            double currentRemainedAmount = cursor.getDouble(cursor.getColumnIndex("remained_amount"));
                            ContentValues contentValues2 = new ContentValues();
                            contentValues2.put("remained_amount", currentRemainedAmount + amount);
                            int affectedRows = sqLiteDatabase.update("users", contentValues2, "_id=?", new String[]{ String.valueOf(integers[0]) });
                            Log.d(TAG, "doInBackground: affected rows: " + affectedRows);
                            cursor.close(); sqLiteDatabase.close(); return null;
                        }
                        else{ cursor.close(); sqLiteDatabase.close(); return null; }
                    }else { sqLiteDatabase.close(); return null; }
                }
            }catch (SQLException e){ e.printStackTrace(); return null; }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Intent intent = new Intent(TransferActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    private boolean validateDate(){
        Log.d(TAG, "validateDate: started");
        if(editTextAmount.getText().toString().equals("")) return false;
        if(editTextDate.getText().toString().equals("")) return false;
        if(editTextRecipieint.getText().toString().equals("")) return false;
        return true;
    }

    private void initViews(){
        Log.d(TAG, "initViews: started");
        editTextAmount = (EditText) findViewById(R.id.editTextAmountTransfer);
        editTextDescription = (EditText) findViewById(R.id.editTextDescriptionTransfer);
        editTextRecipieint = (EditText) findViewById(R.id.editTextRecipientTransfer);
        editTextDate = (EditText) findViewById(R.id.editTextDateTransfer);

        textWarning = (TextView) findViewById(R.id.textWarningTransfer);

        buttonPickDate = (Button) findViewById(R.id.buttonPickDateTransfer);
        buttonAdd = (Button) findViewById(R.id.buttonAddTransfer);

        radioGroupType = (RadioGroup) findViewById(R.id.radioGroupTypeTransfer);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(addTransaction != null && !addTransaction.isCancelled()) addTransaction.cancel(true);
    }
}
