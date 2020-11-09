package au.edu.sydney.comp5216.LoseWeightHelper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.comp5216.LoseWeightHelper.R;

import au.edu.sydney.comp5216.LoseWeightHelper.profile.RegisterActivity;
import au.edu.sydney.comp5216.LoseWeightHelper.profile.LoginActivity;

/**
 * COMP5216 Group Project - LoseWeightHelper
 * This is a start page with login and signup functions.
 *
 * @version 1.0
 */
public class MainActivity extends AppCompatActivity {

    Button RegisterBtn, LoginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * Bind the button id
         */
        RegisterBtn = findViewById(R.id.register_btn);
        LoginBtn = findViewById(R.id.login_btn);

        /**
         * Set buttons' listeners
         */
        RegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,
                        RegisterActivity.class));
            }
        });

        LoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,
                        LoginActivity.class));
            }
        });
    }
}