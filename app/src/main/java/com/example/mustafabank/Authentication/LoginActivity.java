package com.example.mustafabank.Authentication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.mustafabank.Database.DatabaseHelper;
import com.example.mustafabank.MainActivity;
import com.example.mustafabank.Models.User;
import com.example.mustafabank.R;
import com.example.mustafabank.Utils;
import com.example.mustafabank.WebsiteActivity;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private EditText editTextEmail, editTextPassword;
    private TextView textWarning, textLicense, textRegister;
    private Button buttonLogin;

    private DatabaseHelper databaseHelper;

    private LoginUser loginUser;
    private DoesEmailExist doesEmailExist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();

        textLicense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, WebsiteActivity.class);
                startActivity(intent);
            }
        });

        textRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initLogin();
            }
        });
    }

    private void initLogin(){
        Log.d(TAG, "initLogin: started");
        if(!editTextEmail.getText().toString().equals("")){
            if(!editTextPassword.getText().toString().equals("")){
                doesEmailExist = new DoesEmailExist();
                doesEmailExist.execute(editTextEmail.getText().toString());
            }else{
                 textWarning.setVisibility(View.VISIBLE);
                 textWarning.setText("Please enter your password");
            }
        }else{
            textWarning.setVisibility(View.VISIBLE);
            textWarning.setText("Please enter your email address");
        }
    }

    private void initViews(){
        Log.d(TAG, "initViews: started");
        editTextEmail = (EditText) findViewById(R.id.editTextEmailLogin);
        editTextPassword = (EditText) findViewById(R.id.editTextPasswordLogin);
        textWarning = (TextView) findViewById(R.id.textViewWarningLogin);
        textLicense = (TextView) findViewById(R.id.textLicenseLogin);
        textRegister = (TextView) findViewById(R.id.textRegisterLogin);
        buttonLogin = (Button) findViewById(R.id.btnLoginLogin);
    }

    private class DoesEmailExist extends AsyncTask<String, Void, Boolean>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            databaseHelper = new DatabaseHelper(LoginActivity.this);
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            try{
                SQLiteDatabase sqLiteDatabase = databaseHelper.getReadableDatabase();
                Cursor cursor = sqLiteDatabase.query("users", new String[]{"email"}, "email=?",
                        new String[]{strings[0]}, null, null, null);
                if(cursor != null) {
                    if(cursor.moveToFirst()){ cursor.close(); sqLiteDatabase.close(); return true; }
                    else{ cursor.close(); sqLiteDatabase.close(); return false; }
                }
                else{ sqLiteDatabase.close(); return false; }
            } catch (SQLException e){ e.printStackTrace(); return false; }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if(aBoolean){
                loginUser = new LoginUser();
                loginUser.execute();
            }
            else{
                textWarning.setVisibility(View.VISIBLE);
                textWarning.setText("There is no such user with this email address");
            }
        }
    }

    private class LoginUser extends AsyncTask<Void, Void, User>{

        private String email;
        private String password;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            this.email = editTextEmail.getText().toString();
            this.password = editTextPassword.getText().toString();
        }

        @Override
        protected User doInBackground(Void... voids) {
            try{
                SQLiteDatabase sqLiteDatabase = databaseHelper.getReadableDatabase();
                Cursor cursor = sqLiteDatabase.query("users", null, "email=? and password=?",
                        new String[]{email, password}, null, null, null);
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
                    }
                    else { cursor.close(); sqLiteDatabase.close(); return null; }
                }
                else{ sqLiteDatabase.close(); return null; }
            }catch (SQLException e) { e.printStackTrace(); return null; }
        }

        @Override
        protected void onPostExecute(User user) {
            super.onPostExecute(user);

            if(user != null){
                Utils utils = new Utils(LoginActivity.this);
                utils.addUserToSharedPreferences(user);
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
            else{ textWarning.setVisibility(View.VISIBLE); textWarning.setText("Your password is incorrect!"); }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(doesEmailExist != null && !doesEmailExist.isCancelled()) doesEmailExist.cancel(true);
        if(loginUser != null && !loginUser.isCancelled()) loginUser.cancel(true);
    }
}
