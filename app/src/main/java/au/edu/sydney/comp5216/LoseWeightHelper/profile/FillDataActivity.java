package au.edu.sydney.comp5216.LoseWeightHelper.profile;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.comp5216.LoseWeightHelper.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import au.edu.sydney.comp5216.LoseWeightHelper.MainActivity;
import au.edu.sydney.comp5216.LoseWeightHelper.apirelated.UserActivity;

/**
 * Allow users to input their body data, Then calculate the indexs through API
 */
public class FillDataActivity extends AppCompatActivity {
    /**
     * Initialize firebase permission to access
     */
    FirebaseAuth firebaseAuth;

    Spinner spinner;
    EditText userAge;
    EditText userWeight;
    EditText userHeight;
    EditText userWaist;
    EditText userPhone;
    Button submitData;
    Button jump;

    /**
     * Bind all layout elements id and set the view.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill_data);

        firebaseAuth = FirebaseAuth.getInstance();

        spinner = (Spinner)findViewById(R.id.genderSelector);
        userAge = findViewById(R.id.userAge);
        userWeight = findViewById(R.id.userWeight);
        userHeight = findViewById(R.id.userHeight);
        userWaist = findViewById(R.id.userWaist);
        userPhone = findViewById(R.id.userPhone);
        submitData = findViewById(R.id.submitData);
        jump = findViewById(R.id.jump);

        ArrayAdapter<String> genderAdapter = new ArrayAdapter<String>(FillDataActivity.this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.gender));

        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(genderAdapter);
        String text = spinner.getSelectedItem().toString();

        Log.d("selected",text);
        checkUserStatus();

    }

    /**
     * Get users detailed data and send to API.
     * @param
     */
    private void checkUserStatus() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser != null){

            submitData.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                    DatabaseReference reference = firebaseDatabase.getReference("Users");

                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    String email = user.getEmail();
                    String uid = user.getUid();
                    String age = userAge.getText().toString();
                    String weight = userWeight.getText().toString();
                    String height = userHeight.getText().toString();
                    String waist = userWaist.getText().toString();
                    String gender = spinner.getSelectedItem().toString();
                    String phone = userPhone.getText().toString();

                    HashMap<Object, String> hashMap = new HashMap<>();
                    hashMap.put("email", email);
                    hashMap.put("uid", uid);
                    hashMap.put("gender",gender);
                    hashMap.put("age",age);
                    hashMap.put("waist",waist);
                    hashMap.put("height",height);
                    hashMap.put("weight",weight);
                    hashMap.put("image", "");
                    hashMap.put("phone",phone);

                    reference.child(uid).setValue(hashMap);
                    startActivity(new Intent(FillDataActivity.this, UserActivity.class));

                    Toast.makeText(FillDataActivity.this, "upload success", Toast.LENGTH_SHORT).show();

                    finish();
                }
            });

            jump.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(FillDataActivity.this, UserActivity.class));
                    Toast.makeText(FillDataActivity.this, "skip success", Toast.LENGTH_SHORT).show();

                    finish();

                }
            });

        } else {
            startActivity(new Intent(FillDataActivity.this, MainActivity.class));
            finish();
        }

    }
}