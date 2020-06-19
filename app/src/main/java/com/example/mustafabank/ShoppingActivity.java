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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mustafabank.Adapters.ItemsAdapter;
import com.example.mustafabank.Database.DatabaseHelper;
import com.example.mustafabank.Dialogs.SelectItemDialog;
import com.example.mustafabank.Models.Item;
import com.example.mustafabank.Models.User;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ShoppingActivity extends AppCompatActivity implements ItemsAdapter.GetItem {
    private static final String TAG = "ShoppingActivity";

    private Calendar calendar = Calendar.getInstance();

    private DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            editTextDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime()));
        }
    };


    private TextView textWarning, textItemName;
    private ImageView itemImage;
    private Button buttonPickItem, buttonPickDate, buttonAdd;
    private EditText editTextDate, editTextDesc, editTextItemPrice, editTextStore;
    private RelativeLayout itemRelLayout;
    private Item selectedItem;

    private DatabaseHelper databaseHelper;

    private AddShopping addShopping;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping);

        initViews();

        buttonPickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(ShoppingActivity.this, dateSetListener, calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        buttonPickItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectItemDialog selectItemDialog = new SelectItemDialog();
                selectItemDialog.show(getSupportFragmentManager(), "Select Item dialog");
            }
        });

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initAdd();
            }
        });
    }

    private void initAdd(){
        Log.d(TAG, "initAdd: started");
        if(selectedItem != null){
            if(!editTextItemPrice.getText().toString().equals("")){
                if(!editTextDate.getText().toString().equals("")){
                    addShopping = new AddShopping();
                    addShopping.execute();
                }else{
                    textWarning.setVisibility(View.VISIBLE);
                    textWarning.setText("Please select a date");
                }
            }else{
                textWarning.setVisibility(View.VISIBLE);
                textWarning.setText("Please add a price");
            }
        }else{
            textWarning.setVisibility(View.VISIBLE);
            textWarning.setText("Please select an item");
        }
    }

    private class AddShopping extends AsyncTask<Void, Void, Void>{

        private User loggedInUser;
        private String date;
        private double price;
        private String store;
        private String description;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            Utils utils = new Utils(ShoppingActivity.this);
            loggedInUser = utils.isUserLoggedIn();
            this.date = editTextDate.getText().toString();
            this.price = -Double.parseDouble(editTextItemPrice.getText().toString());
            this.store = editTextStore.getText().toString();
            this.description = editTextDesc.getText().toString();

            databaseHelper = new DatabaseHelper(ShoppingActivity.this);

        }

        @Override
        protected Void doInBackground(Void... voids) {
            try{
                SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();
                ContentValues transactionValues = new ContentValues();
                transactionValues.put("amount", price);
                transactionValues.put("description", description);
                transactionValues.put("user_id", loggedInUser.get_id());
                transactionValues.put("type", "shopping");
                transactionValues.put("date", date);
                transactionValues.put("recipient", store);
                long id = sqLiteDatabase.insert("transactions", null, transactionValues);

                ContentValues shoppingValues = new ContentValues();
                shoppingValues.put("item_id", selectedItem.get_id());
                shoppingValues.put("transaction_id", id);
                shoppingValues.put("user_id", loggedInUser.get_id());
                shoppingValues.put("price", price);
                shoppingValues.put("description", description);
                shoppingValues.put("date", date);
                long shoppingId = sqLiteDatabase.insert("shopping", null, shoppingValues);

                Log.d(TAG, "doInBackground: shopping id: " + shoppingId);

                Cursor cursor = sqLiteDatabase.query("users", new String[]{"remained_amount"}, "_id=?",
                        new String[]{String.valueOf(loggedInUser.get_id())}, null, null, null);

                if(cursor != null){
                    if(cursor.moveToFirst()){
                        double remained_amount = cursor.getDouble(cursor.getColumnIndex("remained_amount"));
                        ContentValues amountValues = new ContentValues();
                        amountValues.put("remained_amount", remained_amount - price);
                        int affectedRows = sqLiteDatabase.update("users", amountValues, "_id=?",
                                new String[]{String.valueOf(loggedInUser.get_id())});
                        cursor.close(); sqLiteDatabase.close(); return null;
                    }else{ cursor.close(); sqLiteDatabase.close(); return null; }
                }else{ sqLiteDatabase.close(); return null; }
            }catch (SQLException e){ e.printStackTrace(); return null; }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(ShoppingActivity.this, selectedItem.getName() +  " added successfully.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ShoppingActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(addShopping != null && !addShopping.isCancelled()) addShopping.cancel(true);
    }

    private void initViews(){
        Log.d(TAG, "initViews: started");
        textWarning = (TextView) findViewById(R.id.textWarningShopping);
        textItemName = (TextView) findViewById(R.id.textItemNameShopping);

        itemImage = (ImageView) findViewById(R.id.itemImageShopping);

        buttonPickItem = (Button) findViewById(R.id.buttonPickShopping);
        buttonPickDate = (Button) findViewById(R.id.buttonPickDateShopping);
        buttonAdd = (Button) findViewById(R.id.buttonAddShopping);

        editTextDate = (EditText) findViewById(R.id.editTextDateShopping);
        editTextDesc = (EditText) findViewById(R.id.editTextDescriptionShopping);
        editTextItemPrice = (EditText) findViewById(R.id.editTextItemPriceShopping);
        editTextStore = (EditText) findViewById(R.id.editTextStoreShoppinig);

        itemRelLayout = (RelativeLayout) findViewById(R.id.invisibleItemRelLayoutShopping);
    }

    @Override
    public void onGettingItemResult(Item item) {
        Log.d(TAG, "onGettingItemResult item:" + item);
        selectedItem = item;
        itemRelLayout.setVisibility(View.VISIBLE);
        Glide.with(this)
                .asBitmap()
                .load(item.getImage_url())
                .into(itemImage);
        textItemName.setText(item.getName());
        editTextDesc.setText(item.getDescription());
    }
}
