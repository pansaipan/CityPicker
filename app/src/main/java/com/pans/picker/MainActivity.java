package com.pans.picker;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.pans.citypicker.CityPicker;
import com.pans.citypicker.model.City;
import com.pans.citypicker.model.County;
import com.pans.citypicker.model.Province;


public class MainActivity extends AppCompatActivity {

    CityPicker cityPicker = new CityPicker();
    Province defaultProvince = null;
    City defaultCity= null;
    County defaultCounty= null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cityPicker.initData(this);



        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CityPicker.Builder builder = new CityPicker.Builder().
                        doneTextColor(ContextCompat.getColor(MainActivity.this,R.color.colorPrimary))
                        .province(defaultProvince).city(defaultCity).county(defaultCounty)
                        .cityPickedListener(new CityPicker.OnCityPickedListener() {
                            @Override
                            public void onPicked(Province selectProvince, City selectCity, County selectCounty) {
                                defaultProvince = selectProvince;
                                defaultCity = selectCity;
                                defaultCounty = selectCounty;
                            }
                        });
                cityPicker.setBuilder(builder);

                cityPicker.show();
            }
        });
    }
}
