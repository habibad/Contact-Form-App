package com.example.contactformapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import android.graphics.Bitmap;
import android.util.Base64;

import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.NameValuePair;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.message.BasicNameValuePair;

import java.io.ByteArrayOutputStream;


public class MainActivity extends Activity {
    private static final int GALLERY_REQUEST_CODE = 123;
    private EditText edTxtName, edTxtEmail, edTxtPhoneHome, edTxtPhoneOffice;
    private ContactsDB contactsDB;
    private ImageView imgView;
    private String img = "", contactID = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contactlayout);

        contactsDB = new ContactsDB(this);

        edTxtName = findViewById(R.id.etName);
        edTxtEmail = findViewById(R.id.etEmail);
        edTxtPhoneHome = findViewById(R.id.etPhoneHome);
        edTxtPhoneOffice = findViewById(R.id.etPhoneOffice);
        imgView = findViewById(R.id.ivPhoto);

        imgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, GALLERY_REQUEST_CODE);
            }
        });

        findViewById(R.id.btnSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = edTxtName.getText().toString();
                String email = edTxtEmail.getText().toString();
                String homePhn = edTxtPhoneHome.getText().toString();
                String officePhone = edTxtPhoneOffice.getText().toString();

                String errMsg = "";

                if(!name.isEmpty() && !email.isEmpty() && !homePhn.isEmpty()){
                    if(name.length() < 4 || name.length() > 12 || !name.matches("^[a-zA-Z ]+$")){
                        errMsg += "Invalid Name (4-12 long and only alphabets)\n";
                    }

                    String EMAIL_REGEX = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
                    if(!email.matches(EMAIL_REGEX)){
                        errMsg += "Invalid email address\n";
                    }

                    Pattern pattern = Pattern.compile("^\\+\\d{13}$");
                    if(!homePhn.isEmpty()){
                        Matcher matcher = pattern.matcher(homePhn);
                        if(!matcher.matches()){
                            errMsg += "Invalid phone number (format is: +88017********)\n";
                        }
                    }

                    if(!officePhone.isEmpty()) {
                        Matcher matcher = pattern.matcher(officePhone);
                        if(!matcher.matches()){
                            errMsg += "Invalid phone number (format is: +88017********)\n";
                        }
                    }

                } else{
                    errMsg += "Required: Fill all the fields\n";
                }

                if(errMsg.length() > 0){
                    showErrorDialog(errMsg);
                }else{
                    String contactID = name + System.currentTimeMillis();
                    contactsDB.insertContact(contactID, name, email, homePhn, officePhone, MainActivity.this.img);
                    Toast.makeText(MainActivity.this, "Contact Information Added successfully!", Toast.LENGTH_SHORT).show();
                    // Clear input fields after saving
                    edTxtName.getText().clear();
                    edTxtEmail.getText().clear();
                    edTxtPhoneHome.getText().clear();
                    edTxtPhoneOffice.getText().clear();
                    imgView.setImageResource(R.drawable.baseline_contact24);
                }

                String keys[] = {"action", "sid", "semester", "id", "name", "email", "homePhn", "officePhone", "img"};
                String values[] = {"backup", "2019-3-60-051", "2023-2", contactID, name, email, homePhn, officePhone, img};
                httpRequest(keys, values);
            }
        });

        findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            imgView.setImageURI(selectedImageUri);
            try {
                img = convertImageToBase64(selectedImageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String convertImageToBase64(Uri imageUri) throws IOException {
        InputStream imageStream = getContentResolver().openInputStream(imageUri);
        Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void showErrorDialog(String errMsg){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error");
        builder.setMessage(errMsg);
        builder.setCancelable(true);
        builder.setPositiveButton("Back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void httpRequest(final String keys[],final String values[]){
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                List<NameValuePair> params = new ArrayList<>();
                for (int i = 0; i < keys.length; i++) {
                    params.add(new BasicNameValuePair(keys[i], values[i]));
                }
                String url = "http://localhost/events/";
                String data = "";
                try {
                    data = JSONParser.getInstance().makeHttpRequest(url, "POST", params);
                    System.out.println(data);
                    return data;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String data) {
                if (data != null) {
                    System.out.println(data);
                    System.out.println("Ok2");
                    Toast.makeText(getApplicationContext(), data, Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }
}


