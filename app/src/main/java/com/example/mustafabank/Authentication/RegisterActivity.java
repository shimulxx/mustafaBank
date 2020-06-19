package com.example.mustafabank.Authentication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mustafabank.Database.DatabaseHelper;
import com.example.mustafabank.MainActivity;
import com.example.mustafabank.Models.User;
import com.example.mustafabank.R;
import com.example.mustafabank.Utils;
import com.example.mustafabank.WebsiteActivity;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";

    private EditText editTextEmail, editTextPassword, editTextAddress, editTextName;
    private TextView textWarning, textLogin, textLicense;
    private ImageView firstImage, secondImage, thirdImage, fourthImage, fifthImage;
    private Button buttonRegister;

    private DatabaseHelper databaseHelper;

    private DoesUserExist doesUserExist;
    private RegisterUser registerUser;

    private String image_url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initViews();

        databaseHelper = new DatabaseHelper(this);

        image_url = "first";
        handleImageUrl();

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initRegister();
            }
        });

        textLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        textLicense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, WebsiteActivity.class);
                startActivity(intent);
            }
        });
    }
    
    private void handleImageUrl(){
        Log.d(TAG, "handleImageUrl: started");
        firstImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                image_url = "first";
            }
        });
        secondImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                image_url = "second";
            }
        });
        thirdImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                image_url = "third";
            }
        });
        fourthImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                image_url = "fourth";
            }
        });
        fifthImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                image_url = "fifth";
            }
        });
    }

    private void initRegister(){
        Log.d(TAG, "initRegister: started");
        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();
        if(email.equals("") || password.equals("")){
            textWarning.setVisibility(View.VISIBLE);
            textWarning.setText("Please enter the password and Email");
        }else{
            textWarning.setVisibility(View.GONE);
            doesUserExist = new DoesUserExist(); doesUserExist.execute(email);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(doesUserExist != null && !doesUserExist.isCancelled()) doesUserExist.cancel(true);
        if(registerUser != null && !registerUser.isCancelled()) registerUser.cancel(true);
    }

    private class DoesUserExist extends AsyncTask<String, Void, Boolean>{
        @Override
        protected Boolean doInBackground(String... strings) {
            try{
                SQLiteDatabase sqLiteDatabase = databaseHelper.getReadableDatabase();
                Cursor cursor = sqLiteDatabase.query("users", new String[]{"_id", "email"}, "email=?",
                        new String[]{strings[0]}, null, null, null);
                if(cursor != null){
                    if(cursor.moveToFirst()){
                        if(cursor.getString(cursor.getColumnIndex("email")).equals(strings[0])){ cursor.close(); sqLiteDatabase.close(); return true; }
                        else{ cursor.close(); sqLiteDatabase.close(); return false; }
                    }else{ cursor.close(); sqLiteDatabase.close(); return false; }
                } else {sqLiteDatabase.close(); return true;}
            }catch (SQLException e){ e.printStackTrace(); return true; }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if(aBoolean){
                textWarning.setVisibility(View.VISIBLE);
                textWarning.setText("There is an user with this email, please try with another email");
            }else{
                textWarning.setVisibility(View.GONE);
                registerUser = new RegisterUser();
                registerUser.execute();
            }
        }
    }

    private class RegisterUser extends AsyncTask<Void, Void, User>{

        private String email;
        private String password;
        private String address;
        private String first_name;
        private String last_name = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            String email = editTextEmail.getText().toString();
            String password = editTextPassword.getText().toString();
            String address = editTextAddress.getText().toString();
            String name = editTextName.getText().toString();

            this.email = email;
            this.password = password;
            this.address = address;

            String[] names = name.split(" ");
            if(names.length > 1){
                this.first_name = names[0];
                for(int i = 1; i < names.length; ++i){
                    if(i == 1) last_name += names[i];
                    else last_name += " " + names[i];
                }
            }else this.first_name = names[0];
        }

        @Override
        protected User doInBackground(Void... voids) {
            try{
                SQLiteDatabase sqLiteDatabase = databaseHelper.getWritableDatabase();

                ContentValues contentValues = new ContentValues();
                contentValues.put("email", this.email);
                contentValues.put("password",this.password);
                contentValues.put("address", this.address);
                contentValues.put("first_name", this.first_name);
                contentValues.put("last_name", this.last_name);
                contentValues.put("remained_amount", 0.0);
                contentValues.put("image_url", image_url);

                long userId = sqLiteDatabase.insert("users", null, contentValues);
                Log.d(TAG, "doInBackground: userid: " + userId);

                Cursor cursor = sqLiteDatabase.query("users", null, "_id=?",
                        new String[]{String.valueOf(userId)}, null, null, null);
                if(cursor != null){
                    if(cursor.moveToFirst()){
                        User user = new User();
                        user.set_id(cursor.getInt(cursor.getColumnIndex("_id")));
                        user.setEmail(cursor.getString(cursor.getColumnIndex("email")));
                        user.setPassword(cursor.getString(cursor.getColumnIndex("password")));
                        user.setFirst_name(cursor.getString(cursor.getColumnIndex("first_name")));
                        user.setLast_name(cursor.getString(cursor.getColumnIndex("last_name")));
                        user.setImage_url(cursor.getString(cursor.getColumnIndex("image_url")));
                        user.setAddress(cursor.getColumnName(cursor.getColumnIndex("address")));
                        user.setRemained_amount(cursor.getDouble(cursor.getColumnIndex("remained_amount")));
                        cursor.close(); sqLiteDatabase.close(); return user;
                    }else{ cursor.close(); sqLiteDatabase.close(); return null; }
                }else{ sqLiteDatabase.close(); return null; }
            }catch (SQLException e){ e.printStackTrace(); return null; }
        }

        @Override
        protected void onPostExecute(User user) {
            super.onPostExecute(user);
            if(user != null){
                Toast.makeText(RegisterActivity.this, "User: " +user.getFirst_name() + "registered successfully", Toast.LENGTH_SHORT).show();
                Utils utils = new Utils(RegisterActivity.this);
                utils.addUserToSharedPreferences(user);
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
            else{ Toast.makeText(RegisterActivity.this, "Wasn't able to register, please try again later.", Toast.LENGTH_SHORT).show(); }
        }
    }

    private void initViews(){
        Log.d(TAG, "initViews: started");
        
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        editTextAddress = (EditText) findViewById(R.id.editTextAddress);
        editTextName = (EditText) findViewById(R.id.editTextName);

        textWarning = (TextView) findViewById(R.id.textWarning);
        textLogin = (TextView) findViewById(R.id.textLogin);
        textLicense = (TextView) findViewById(R.id.textLicense);

        firstImage = (ImageView) findViewById(R.id.firstImage);
        secondImage = (ImageView) findViewById(R.id.secondImage);
        thirdImage = (ImageView) findViewById(R.id.thirdImage);
        fourthImage = (ImageView) findViewById(R.id.fourthImage);
        fifthImage = (ImageView) findViewById(R.id.fifthImage);

        buttonRegister = (Button) findViewById(R.id.btnRegister);
    }
}
