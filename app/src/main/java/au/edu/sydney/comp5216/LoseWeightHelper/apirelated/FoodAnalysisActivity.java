package au.edu.sydney.comp5216.LoseWeightHelper.apirelated;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.comp5216.LoseWeightHelper.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Food Analysis Database
 * A class to look up all the foods nutrition and show
 */
public class FoodAnalysisActivity extends AppCompatActivity {

    /**
     * Initialize all the buttons elemements
     */
    Button refresh;
    Button searchFood;
    EditText foodInput;
    FirebaseAuth firebaseAuth;

    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    BarChart barChart;
    ImageView foodAnalysisImage;
    TextView foodName;

    /**
     * store all related data here.
     */
    ArrayList<BarEntry> nutritionData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_analysis);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");

        refresh = findViewById(R.id.refresh);
        searchFood = findViewById(R.id.searchFood);
        foodInput = findViewById(R.id.foodInput);

        barChart = findViewById(R.id.Nutritionresult);
        foodAnalysisImage = findViewById(R.id.foodAnalysisImage);
        foodName = findViewById(R.id.foodName);


        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(getIntent());
            }
        });

        generateData();

    }

    /**
     * generate detailed data by communicating with API
     * need to send related data.
     */
    private void generateData() {
        if(firebaseUser != null){
            String email = firebaseUser.getEmail();
            final String userId = firebaseUser.getUid();
            Log.d("email", email);
            Log.d("userId", userId);

            searchFood.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (TextUtils.isEmpty(foodInput.getText().toString())){
                        Toast.makeText(FoodAnalysisActivity.this,
                                "Please input a food", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(FoodAnalysisActivity.this,
                                "search processing...", Toast.LENGTH_SHORT).show();

                        OkHttpClient client = new OkHttpClient();
                        String callUrl = "https://rapidapi.p.rapidapi.com/parser?ingr="+foodInput.getText().toString();
                        Log.d("foodUrl", callUrl);
                        Request request = new Request.Builder()
                                .url(callUrl)
                                .method("GET", null)
                                .addHeader("x-rapidapi-key", ApiKey.EdamamKey)
                                .addHeader("x-rapidapi-host", ApiKey.EdamamHost)
                                .addHeader("Cookie", ApiKey.EdamamCookie)
                                .build();

                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                if(response.isSuccessful()){
                                    Log.d("success", "call Edamam success");
                                    final String myResponse = response.body().string();
                                    FoodAnalysisActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Log.d("Food analysis: ", myResponse);
                                            try {
                                                JSONObject jsonObject = new JSONObject(myResponse);
                                                final JSONArray parsedFood = new JSONArray(jsonObject.getString("parsed"));

                                                Log.d("parsedFood", parsedFood.toString());
                                                String foodDetail = parsedFood.getString(0);
                                                Log.d("food Detail", foodDetail);

                                                final JSONObject foodJson = new JSONObject(foodDetail);
                                                Log.d("food detail", foodJson.getString("food"));

                                                final JSONObject foodDetailed = new JSONObject(foodJson.getString("food"));
                                                Log.d("label",foodDetailed.getString("label"));
                                                Log.d("image",foodDetailed.getString("image"));
                                                Picasso.with(FoodAnalysisActivity.this).load(foodDetailed.getString("image")).into(foodAnalysisImage);
                                                foodName.setText(foodDetailed.getString("label"));
                                                JSONObject nutritionJson = new JSONObject(foodDetailed.getString("nutrients"));
                                                Log.d("nutrition", nutritionJson.toString());

                                                /**
                                                 * parse the String and add it into data bank.
                                                 */
                                                nutritionData.add(new BarEntry(Float.parseFloat(nutritionJson.getString("ENERC_KCAL")),0));
                                                barChart.notifyDataSetChanged();
                                                nutritionData.add(new BarEntry(Float.parseFloat(nutritionJson.getString("PROCNT")),1));
                                                barChart.notifyDataSetChanged();
                                                nutritionData.add(new BarEntry(Float.parseFloat(nutritionJson.getString("FAT")),2));
                                                barChart.notifyDataSetChanged();
                                                nutritionData.add(new BarEntry(Float.parseFloat(nutritionJson.getString("CHOCDF")),3));
                                                barChart.notifyDataSetChanged();
                                                nutritionData.add(new BarEntry(Float.parseFloat(nutritionJson.getString("FIBTG")),4));
                                                barChart.notifyDataSetChanged();

                                                BarDataSet barDataSet = new BarDataSet(nutritionData, "Nutrition Data");
                                                ArrayList<String> labels = new ArrayList<>();
                                                labels.add("ENERC_KCAL");
                                                labels.add("PROCNT");
                                                labels.add("FAT");
                                                labels.add("CHOCDF");
                                                labels.add("FIBTG");

                                                BarData data = new BarData(labels, barDataSet);
                                                barChart.setData(data);
                                                barChart.setDescription("Here is Nutrition Report");
                                                barDataSet.setColors(ColorTemplate.VORDIPLOM_COLORS);
                                                barDataSet.setValueTextColor(R.color.darkGray);
                                                barDataSet.setValueTextSize(16f);
                                                barChart.animateY(5000);

                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                }else {
                                }
                            }
                        });
                    }
                }
            });
        }
    }
}