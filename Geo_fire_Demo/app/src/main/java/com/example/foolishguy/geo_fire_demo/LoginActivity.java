package com.example.foolishguy.geo_fire_demo;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.*;

public class LoginActivity extends AppCompatActivity {

    private Button btnlogin;
    private EditText edtuname, edtpass;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;

    private static String TAG = "Login Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {
            Log.e(TAG, "Already signed in");
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
        } else {
            btnlogin = (Button) findViewById(R.id.signinbtn);
            edtuname = (EditText) findViewById(R.id.uname);
            edtpass = (EditText) findViewById(R.id.pass);

            btnlogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (edtpass.getText().length() == 0) {
                        Log.e (TAG, "Password must not be empty");
                        return;
                    }
                    if (edtuname.getText().length() != 0 && isEmailValid(edtuname.getText())) {
                        firebaseAuth.signInWithEmailAndPassword(edtuname.getText().toString(),
                                                edtpass.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Log.d (TAG, "Signin success");
                                    setUname();
                                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                } else {
                                    Log.e (TAG, "Signin fail");
                                    return;
                                }
                            }
                        });
                    }
                    else {
                        Log.e (TAG, "User name must be entered and it is valid email address");
                        return;
                    }
                }
            });
        }
    }

    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public synchronized void setUname () {
            if (firebaseAuth.getCurrentUser() != null) {
                firebaseDatabase = FirebaseDatabase.getInstance();
                DatabaseReference databaseReference = firebaseDatabase.getReference("users");

                java.util.Map<String, String> map = new HashMap<String, String>();
                String name = edtuname.getText().toString();
                name = name.substring(0, name.indexOf('@'));
                map.put("name", name);
                map.put("phone", "1234");

                databaseReference.child (firebaseAuth.getCurrentUser().getUid()).setValue(map, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            Log.i(TAG, "onComplete: OKAY SAVED ");
                            finish();
                        } else {
                            Log.i(TAG, "onComplete: Failed");
                        }
                    }
                });
            }
    }
}
