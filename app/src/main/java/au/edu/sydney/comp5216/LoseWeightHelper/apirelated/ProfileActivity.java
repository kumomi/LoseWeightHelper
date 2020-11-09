package au.edu.sydney.comp5216.LoseWeightHelper.apirelated;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

import com.comp5216.LoseWeightHelper.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import au.edu.sydney.comp5216.LoseWeightHelper.MainActivity;

/**
 * Show specific users workout and meal plan.
 */
public class ProfileActivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    TextView userEmail;

    ImageView mealImage;
    ImageView mealImage2;
    ImageView mealImage3;
    TextView mealText;
    TextView mealText2;
    TextView mealText3;
    TextView dateStart;
    TextView dateEnd;
    TextView planText;
    TextView exerciseText;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.index_activity);
        planText = findViewById(R.id.plan_text);
        exerciseText = findViewById(R.id.exercise_text);


        mealImage = findViewById(R.id.meal_image);
        mealImage2 = findViewById(R.id.meal_image2);
        mealImage3 = findViewById(R.id.meal_image3);

        mealText = findViewById(R.id.meal_text);
        mealText2 = findViewById(R.id.meal_text2);
        mealText3 = findViewById(R.id.meal_text3);

        dateStart = findViewById(R.id.date_text);
        dateEnd = findViewById(R.id.end_date_text);


        Toolbar toolbar = findViewById(R.id.signOutToolBar);
        toolbar.setTitle("User plan");

        setSupportActionBar(toolbar);

        FloatingActionButton indexFab = findViewById(R.id.index_btn);
        indexFab.setEnabled(false);
        indexFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, MakePlanActivity.class);
                startActivity(intent);
            }
        });

        FloatingActionButton planFab = findViewById(R.id.plan_btn);
        planFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //intent new activity
                Intent intent=new Intent(ProfileActivity.this, FoodAnalysisActivity.class);
                startActivity(intent);
            }
        });

        FloatingActionButton profileFab = findViewById(R.id.profile_btn);
        profileFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //intent new activity
                Intent intent=new Intent(ProfileActivity.this, UserActivity.class);
                startActivity(intent);
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();
        userEmail = findViewById(R.id.profileTitle);

        checkUserStatus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void checkUserStatus() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser != null){

            Bundle bundle = getIntent().getExtras();
            Log.d("weightInProfile", bundle.getString("Plan").toString());

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
            LocalDateTime now = LocalDateTime.now();

            dateStart.setText((dtf.format(now)));
            dateEnd.setText((dtf.format(now.plusDays(7))));

            try {
                final JSONObject dataObject = new JSONObject(bundle.getString("Plan").toString());
                String calory = dataObject.getString("calory");
                Log.d("calory", calory);
                if(Float.parseFloat(calory) > 1500){
                    planText.setText("Do condition exercise");
                    exerciseText.setText("activity promoting video game (e.g., Wii Fit), light effort (e.g., balance, yoga)");
                    if(Float.parseFloat(calory) > 2000){
                        planText.setText("Do extreme exercise");
                        exerciseText.setText("Golfing, carrying clubs");
                    }
                }
                else if(Float.parseFloat(calory) < 1500){
                    planText.setText("Do physical exercise");
                    exerciseText.setText("Skiing, downhill");
                    if(Float.parseFloat(calory) < 1000){
                        planText.setText("Do daily exercise");
                        exerciseText.setText("Golfing, carrying clubs");
                    }
                }

                OkHttpClient client = new OkHttpClient();
                String callUrl = "https://rapidapi.p.rapidapi.com/recipes/mealplans/generate?targetCalories="+calory+"&timeFrame=day";

                Request request = new Request.Builder()
                        .url(callUrl)
                        .method("GET", null)
                        .addHeader("x-rapidapi-host", ApiKey.recipeHost)
                        .addHeader("x-rapidapi-key", ApiKey.recipeKey)
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
                            ProfileActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("result", myResponse);

                                    try{
                                        JSONObject jsonObject = new JSONObject(myResponse);

                                        Log.d("mealObject", jsonObject.toString());
                                        String meals = jsonObject.getString("meals");
                                        Log.d("mealsDetail", meals);

                                        JSONArray mealsObject = new JSONArray(meals);
                                        Log.d("firstmealsObject", mealsObject.getString(0));
                                        String firstMeal = mealsObject.getString(0);
                                        JSONObject firstMealObject = new JSONObject(firstMeal);

                                        String firstMealImage = "https://spoonacular.com/recipeImages/"+
                                                firstMealObject.getString("id")+"-556x370."+firstMealObject.getString("imageType");

                                        Log.d("firstMealImage", firstMealImage);
                                        mealText.setText(firstMealObject.getString("title"));
                                         Picasso.with(ProfileActivity.this).load(firstMealImage).into(mealImage);

                                         String secondMeal = mealsObject.getString(1);
                                         JSONObject secondMealObject = new JSONObject(secondMeal);
                                         String secondMealImage = "https://spoonacular.com/recipeImages/"+
                                                 secondMealObject.getString("id")+"-556x370."+secondMealObject.getString("imageType");
                                         Log.d("secondMealImage", secondMealImage);

                                         mealText2.setText(secondMealObject.getString("title"));
                                         Picasso.with(ProfileActivity.this).load(secondMealImage).into(mealImage2);
                                         String thirdMeal = mealsObject.getString(2);
                                         JSONObject thirdMealObject = new JSONObject(thirdMeal);
                                         String thirdMealImage = "https://spoonacular.com/recipeImages/"+
                                                 thirdMealObject.getString("id")+"-556x370."+thirdMealObject.getString("imageType");
                                        Log.d("thirdMealImage", thirdMealImage);

                                        mealText3.setText(thirdMealObject.getString("title"));
                                        Picasso.with(ProfileActivity.this).load(thirdMealImage).into(mealImage3);

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

            }catch (Exception e){
                Log.e("error", e.getLocalizedMessage());

            }

        } else {
            startActivity(new Intent(ProfileActivity.this, MainActivity.class));
            finish();
        }

    }

    @Override
    protected void onStart(){
        super.onStart();
    }
}