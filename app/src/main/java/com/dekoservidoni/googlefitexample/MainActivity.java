package com.dekoservidoni.googlefitexample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * Activity to show choose option to the user
 *
 * Created by DeKo on 05/11/2015.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Button historyBtn = (Button) findViewById(R.id.main_button_history_ui);
        historyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HistoryDataActivity.class);
                startActivity(intent);
            }
        });

        Button customBtn = (Button) findViewById(R.id.main_button_custom_ui);
        customBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CustomDataActivity.class);
                startActivity(intent);
            }
        });
    }
}
