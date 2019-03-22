package com.pans.picker;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.pans.citypicker.CityPicker;


public class MainActivity extends AppCompatActivity {

    CityPicker cityPicker = new CityPicker();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cityPicker.initData(this);

        CityPicker.Builder builder = new CityPicker.Builder().showCounty(false).
                doneTextColor(ContextCompat.getColor(this,R.color.colorPrimary));
        cityPicker.setBuilder(builder);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                  cityPicker.show();
            }
        });
    }
}
