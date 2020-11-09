package au.edu.sydney.comp5216.LoseWeightHelper.apirelated;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.comp5216.LoseWeightHelper.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.OkHttpClient;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Main API works class
 */
public class UserActivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth;

    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    EditText gender;
    EditText age;
    EditText height;
    EditText weight;
    BarChart barChart;
    Button makePlan;
    Intent intent;
    ArrayList<BarEntry> healthyData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_user);

            firebaseAuth = FirebaseAuth.getInstance();
            firebaseUser = firebaseAuth.getCurrentUser();
            firebaseDatabase = FirebaseDatabase.getInstance();
            databaseReference = firebaseDatabase.getReference("Users");

            gender = findViewById(R.id.GenderEdit);
            age = findViewById(R.id.AgeEdit);
            height = findViewById(R.id.heightEdit);
            weight = findViewById(R.id.WeightEdit);
            barChart =(BarChart) findViewById(R.id.barchart);
            makePlan = findViewById(R.id.makePlan);

            intent = new Intent(UserActivity.this, MakePlanActivity.class);

            generateData();

            makePlan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(intent);
                    finish();
                }
            });

        }

    private void generateData() {

        if(firebaseUser != null){

            String email = firebaseUser.getEmail();
            final String userId = firebaseUser.getUid();

            Log.d("email", email);
            Log.d("userId", userId);
            Query query = databaseReference.orderByChild("email").equalTo(firebaseUser.getEmail());
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot ds: dataSnapshot.getChildren()){
                        String ageValue = "" + ds.child("age").getValue();
                        String weightValue =  "" + ds.child("weight").getValue();
                        String heightValue = "" + ds.child("height").getValue();
                        String genderValue = "" + ds.child("gender").getValue();
                        String waistValue = "" + ds.child("waist").getValue();
                        String host = "fitness-calculator.p.rapidapi.com";
                        String key = "IoN8git50Kmshpk9GjKmLDxQOnJZp1zZhnRjsnnxAEcwDlh0PA";


                        Log.d("age", ageValue);
                        Log.d("weight", weightValue);
                        Log.d("height", heightValue);
                        Log.d("gender", genderValue);
                        Log.d("waist", waistValue);

                        Toast.makeText(UserActivity.this, "user data updated",Toast.LENGTH_SHORT).show();

                        gender.setText(genderValue);
                        age.setText(ageValue);
                        height.setText(heightValue);
                        weight.setText(weightValue);

                        Log.d("gender", gender.getText().toString());
                        Log.d("age", age.getText().toString());
                        Log.d("height", height.getText().toString());
                        Log.d("weight", weight.getText().toString());


                        OkHttpClient client = new OkHttpClient();
                        String callUrl = "https://fitness-calculator.p.rapidapi.com/bmi?age=" +   ageValue +
                                "&height=" +  heightValue +  "&weight=" + weightValue;

                        Log.d("this is calledUrl", callUrl);

                        String url = "https://fitness-calculator.p.rapidapi.com/bmi?age=25&height=180&weight=65";
                        Request request = new Request.Builder()
                                .url(callUrl)
                                .method("GET", null)
                                .addHeader("x-rapidapi-host", host)
                                .addHeader("x-rapidapi-key", key)
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
                                    UserActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Log.d("result", myResponse);

                                            try{
                                                JSONObject jsonObject = new JSONObject(myResponse);
                                                Log.d("value: ", jsonObject.getString("bmi"));
                                                String bmi = jsonObject.getString("bmi");
                                                String healthStatus = jsonObject.getString("health");
                                                String range = jsonObject.getString("healthy_bmi_range");
                                                Log.d("bmi", bmi);
                                                Log.d("health", healthStatus);
                                                Log.d("range",range);


                                                intent.putExtra("bmi",bmi);
                                                intent.putExtra("health", healthStatus);
                                                intent.putExtra("range",range);

                                                Log.d("this is bmi", bmi);
                                                healthyData.add(new BarEntry(Float.parseFloat(bmi),0));
                                                barChart.notifyDataSetChanged();

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

                        String BodyFactUrl = "https://rapidapi.p.rapidapi.com/bodyfat?waist="+waistValue + "&gender=" +genderValue + "&neck=50&height="
                                + heightValue + "&hip=92&age=" + ageValue + "&weight=" + weightValue;
                        //"96&gender=male&neck=50&heigth=184&hip=92&age=25&weigth=70";
                        Log.d("BodyFact", BodyFactUrl);

                        Request requestForBody = new Request.Builder()
                                .url(BodyFactUrl)
                                .method("GET", null)
                                .addHeader("x-rapidapi-key", key)
                                .addHeader("x-rapidapi-host", host)
                                .build();
                        client.newCall(requestForBody).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                            }
                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                if (response.isSuccessful()) {
                                    Log.d("success", "call success");
                                    final String myResponse = response.body().string();
                                    UserActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Log.d("result", myResponse);

                                            try{
                                                JSONObject jsonObject = new JSONObject(myResponse);
                                                String BodyFactMass = jsonObject.getString("Body Fat Mass");
                                                String LeanFactMass = jsonObject.getString("Lean Body Mass");
                                                Log.d("Body Fact Mass: ", jsonObject.getString("Body Fat Mass"));
                                                Log.d("Lean Fact Mass: ", jsonObject.getString("Lean Body Mass"));

                                                healthyData.add(new BarEntry(Float.parseFloat(BodyFactMass),1));
                                                barChart.notifyDataSetChanged();

                                                healthyData.add(new BarEntry(Float.parseFloat(LeanFactMass),2));
                                                barChart.notifyDataSetChanged();

                                                BarDataSet barDataSet = new BarDataSet(healthyData, "Healthy Data");

                                                ArrayList<String> labels = new ArrayList<>();
                                                labels.add("BMI");
                                                labels.add("Body Fat Mass");
                                                labels.add("Lean Body Mass");

                                                BarData data = new BarData(labels, barDataSet);
                                                barChart.setData(data);
                                                barChart.setDescription("Here is your healthy Report");
                                                barDataSet.setColors(ColorTemplate.VORDIPLOM_COLORS);
                                                barDataSet.setValueTextColor(R.color.darkGray);
                                                barDataSet.setValueTextSize(16f);
                                                barChart.animateY(5000);

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