package au.edu.sydney.comp5216.LoseWeightHelper.profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.comp5216.LoseWeightHelper.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.muddzdev.styleabletoastlibrary.StyleableToast;

import java.util.HashMap;

/**
 * Signup new users, interact with firebase
 */
public class RegisterActivity extends AppCompatActivity {

    EditText emailInput, passwordInput;
    Button registerBtn;
    TextView registeredAccountUser;

    //progressbar to display register user
    ProgressDialog progressDialog;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //initialize input
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        registerBtn = findViewById(R.id.registerBtn);
        registeredAccountUser = findViewById(R.id.RegisteredUser);

        // Initialize Firebase Auth

        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering User..." + emailInput.getText().toString().trim());


        //handle register button click
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = emailInput.getText().toString().trim();
                String password = passwordInput.getText().toString().trim();

                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    emailInput.setError("Invalid email, please input correct format");
                    StyleableToast.makeText(RegisterActivity.this, "please input a correct format email", R.style.errorToast).show();

                    emailInput.setFocusable(true);

                }

                else if(password.length()<8){
                    passwordInput.setError("Password length at least 8 characters");
                    StyleableToast.makeText(RegisterActivity.this, "your password should at least 8 characters", R.style.errorToast).show();

                    passwordInput.setFocusable(true);
                }
                else if(password.isEmpty()){
                    passwordInput.setError("Please set a password");
                    StyleableToast.makeText(RegisterActivity.this, "Please set a password", R.style.errorToast).show();

                    passwordInput.setFocusable(true);
                }
                else {
                    registerUser(email, password);
                }
            }
        });

        registeredAccountUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });
    }

    private void registerUser(String email, String password) {
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            progressDialog.dismiss();
                           final FirebaseUser user = mAuth.getCurrentUser();
                            assert user != null;
                            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        StyleableToast.makeText(RegisterActivity.this, "Registering...\n"+ "Please check your email " +user.getEmail(), R.style.successToast).show();
                                      //  Toast.makeText(RegisterActivity.this, "Registering...\n"+ "Please check your email " +user.getEmail(), Toast.LENGTH_SHORT).show();

                                        String email = user.getEmail();
                                        String uid = user.getUid();

                                        HashMap<Object, String> hashMap = new HashMap<>();
                                        hashMap.put("email", email);
                                        hashMap.put("uid", uid);
                                        hashMap.put("gender","");
                                        hashMap.put("age","");
                                        hashMap.put("waist","");
                                        hashMap.put("height","");
                                        hashMap.put("weight","");
                                        hashMap.put("image", "");
                                        hashMap.put("phone","");

                                        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                                        DatabaseReference reference = firebaseDatabase.getReference("Users");
                                        //put data within hashmap in database
                                        reference.child(uid).setValue(hashMap);
                                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                        finish();
                                    } else {
                                        progressDialog.dismiss();
                                        StyleableToast.makeText(RegisterActivity.this, task.getException().getMessage(), R.style.errorToast).show();
                                    }
                                }
                            });
//                            Snackbar.make(v, "Registering...\n"+user.getEmail(), Snackbar.LENGTH_LONG).show();

                        } else {
                            StyleableToast.makeText(RegisterActivity.this, "Authentication failed:  " +user.getEmail(), R.style.errorToast).show();

                            // If sign in fails, display a message to the user.
//                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
//                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //error, dismiss progress dialog and get and show the error messages
                progressDialog.dismiss();
                StyleableToast.makeText(RegisterActivity.this, e.getMessage(), R.style.errorToast).show();
            }
        });

    }

//    public void onStart() {
//        FirebaseApp.initializeApp(this);
//        super.onStart();
//        // Check if user is signed in (non-null) and update UI accordingly.
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}