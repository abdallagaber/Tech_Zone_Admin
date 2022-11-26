package com.example.techzoneadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class login extends AppCompatActivity {
    Button button;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://tech-zone-5d9e6-default-rtdb.firebaseio.com/");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText username = findViewById(R.id.textUserName);
        final EditText password = findViewById(R.id.textPassword);
        final Button login = findViewById(R.id.btLogin);


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String usernametext =  username.getText(). toString();
                final String passswordtext = password.getText(). toString();

                if (usernametext.isEmpty() ||passswordtext.isEmpty() ){
                    Toast.makeText(login.this, "please enter your username and Password",Toast.LENGTH_SHORT).show();
                }
                else {
                     databaseReference.child("Adimns").addListenerForSingleValueEvent(new ValueEventListener() {
                         @Override
                         public void onDataChange(@NonNull DataSnapshot snapshot) {
                             if(snapshot.hasChild(usernametext)){
                                 final String getpassword = snapshot.child(usernametext).child("password").getValue(String.class);
                                 if(getpassword.equals(passswordtext)){
                                     Toast.makeText(login.this, "Successfully logged in",Toast.LENGTH_SHORT).show();
                                     Intent intent = new Intent(login.this,MainActivity.class);
                                     startActivity(intent);
                                     finish();

                                 }
                                 else {
                                     Toast.makeText(login.this, "Wrong Password",Toast.LENGTH_SHORT).show();
                                 }
                             }
                             else {
                                 Toast.makeText(login.this, "Wrong User name",Toast.LENGTH_SHORT).show();
                             }

                         }

                         @Override
                         public void onCancelled(@NonNull DatabaseError error) {

                         }
                     });




              /*       Intent intent = new Intent(login.this,MainActivity.class);
                    startActivity(intent);*/
                }
            }
        });

       /* button=findViewById(R.id.btLogin);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(login.this,MainActivity.class);
                startActivity(intent);
            }
        });*/

    }
}