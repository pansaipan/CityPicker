package com.pans.picker;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.pans.citypicker.CityPicker;
import com.pans.picker.R;

public class MainActivity extends AppCompatActivity {

    CityPicker cityPicker = new CityPicker();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cityPicker.initData(this);

        CityPicker.Builder builder = new CityPicker.Builder().cyclic(false).doneTextColor(Color.GREEN);
        cityPicker.setBuilder(builder);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                  cityPicker.show();
            }
        });
    }
}
