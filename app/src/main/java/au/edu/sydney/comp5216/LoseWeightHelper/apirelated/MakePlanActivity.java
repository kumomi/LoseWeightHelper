package au.edu.sydney.comp5216.LoseWeightHelper.apirelated;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.comp5216.LoseWeightHelper.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * The type Make plan activity.
 * Should make users to make their plan, get APIs plan results back.
 */
public class MakePlanActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;

    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    TextView goal;
    TextView bmi;
    TextView healthStatus;
    TextView range;
    Spinner exerciseSelector;
    Spinner lossSelector;

    Button generatePlan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_plan);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");

        goal = findViewById(R.id.yourGoal);
        bmi = findViewById(R.id.Bmi);
        healthStatus = findViewById(R.id.healthStatus);
        range = findViewById(R.id.range);
        exerciseSelector = (Spinner)findViewById(R.id.exerciseSelector);
        lossSelector = (Spinner)findViewById(R.id.lossSelector);

        generatePlan = findViewById(R.id.generatePlan);

        ArrayAdapter<String> exerciseAdapter = new ArrayAdapter<String>(MakePlanActivity.this, android.R.layout.simple_list_item_1,
                getResources().getStringArray(R.array.exerciseSelector));

        exerciseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        exerciseSelector.setAdapter(exerciseAdapter);

        ArrayAdapter<String> lossAdapter = new ArrayAdapter<String>(MakePlanActivity.this, android.R.layout.simple_list_item_1,
                getResources().getStringArray(R.array.lossSelector));

        lossAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        lossSelector.setAdapter(lossAdapter);

        generatePlan();
    }

    private void generatePlan() {
        if(firebaseUser != null) {
            String email = firebaseUser.getEmail();
            final String userId = firebaseUser.getUid();

            Log.d("email", email);
            Log.d("userId", userId);

            Bundle bundle = getIntent().getExtras();
            String bmiValue = bundle.getString("bmi");
            String healthValue = bundle.getString("health");
            String bmiRange = bundle.getString("range");
            bmi.setText("Your BMI Value: " + bmiValue);
            healthStatus.setText("Your health status: "+healthValue);
            range.setText("Your suitable BMI range: " + bmiRange);

            Query query = databaseReference.orderByChild("email").equalTo(firebaseUser.getEmail());
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot ds: dataSnapshot.getChildren()){
                        String ageValue = "" + ds.child("age").getValue();
                        String weightValue =  "" + ds.child("weight").getValue();
                        String heightValue = "" + ds.child("height").getValue();
                        String genderValue = "" + ds.child("gender").getValue();

                        Log.d("age", ageValue);
                        Log.d("weight", weightValue);
                        Log.d("height", heightValue);
                        Log.d("gender", genderValue);

                        OkHttpClient client = new OkHttpClient();
                        String callUrl = "https://rapidapi.p.rapidapi.com/dailycalory?heigth=" +   heightValue +
                                "&age=" +  ageValue +  "&gender=" + genderValue + "&weigth="+weightValue;

                        Log.d("callUrl", callUrl);
                        Log.d("key: ", ApiKey.key);
                        Log.d("host: ", ApiKey.host);

                        Request request = new Request.Builder()
                                .url(callUrl)
                                .method("GET", null)
                                .addHeader("x-rapidapi-key", ApiKey.key)
                                .addHeader("x-rapidapi-host", ApiKey.host)
                                .build();

                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                            }
                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                if (response.isSuccessful()) {
                                    Log.d("success", "call success");
                                    final String myResponse = response.body().string();
                                    MakePlanActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Log.d("result", myResponse);

                                            try{
                                                JSONObject jsonObject = new JSONObject(myResponse);
                                                Log.d("object", jsonObject.toString());
                                                Log.d("Exercise 1-3 times", jsonObject.getString("data"));
                                                final JSONObject dataObject = new JSONObject(jsonObject.getString("data"));
                                                Log.d("goals", dataObject.getString("goals"));
                                                final JSONObject goalObject = new JSONObject( dataObject.getString("goals"));

                                                generatePlan.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        String exercise = exerciseSelector.getSelectedItem().toString();
                                                        String weightLevel =  lossSelector.getSelectedItem().toString();

                                                        if(exercise.equals("exerciseSelector") || weightLevel.equals("lossSelector")){

                                                            Toast.makeText(MakePlanActivity.this, "please select from spinner",Toast.LENGTH_SHORT).show();

                                                        } else {
                                                            try {
                                                                Log.d("clicked",dataObject.getString("goals") );
                                                                JSONArray goalKey = goalObject.names();
                                                                for(int i = 0; i< goalKey.length();i++){

                                                                    String values = goalObject.getString(exerciseSelector.getSelectedItem().toString());
                                                                    final JSONObject weight = new JSONObject(values);

                                                                    Log.d("values", values);
                                                                    Log.d("weight", weight.toString());

                                                                    String lossValue = weight.getString(lossSelector.getSelectedItem().toString());
                                                                    Log.d("lossValue", lossValue);

                                                                    Intent intent = new Intent(MakePlanActivity.this, ProfileActivity.class);
                                                                    intent.putExtra("Plan", lossValue);
                                                                    startActivity(intent);

                                                                }
                                                            } catch (JSONException e) {
                                                                e.printStackTrace();
                                                            }
                                                        }

                                                    }
                                                });
                                            } catch (Throwable e){
                                                Log.e("JSONError","could not parse");
                                            }
                                        }
                                    });
                                } else {
                                    Log.d("error", "call error");
                                }
                            }
                        });
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        }
}