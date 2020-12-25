package com.pans.citypicker;

import android.app.Dialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pans.citypicker.model.City;
import com.pans.citypicker.model.County;
import com.pans.citypicker.model.Province;
import com.pans.citypicker.wheel.OnWheelChangedListener;
import com.pans.citypicker.wheel.OnWheelClickedListener;
import com.pans.citypicker.wheel.WheelView;
import com.pans.citypicker.wheel.adapter.AbstractWheelTextAdapter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pansai on 2019/3/21.
 * 省市区选择器
 */

public class CityPicker implements OnWheelClickedListener{

    private final static int UPDATE_CITY_WHEEL = 11;
    private final static int UPDATE_COUNTY_WHEEL = 12;

    /**
     * 滚轮显示的item个数
     */
    protected int visibleItems = 5;

    /**
     * 滚轮是否循环滚动
     */
    private boolean isCyclic = false;

    /**
     * 是否显示城市*/
    boolean isShowCity =true;

    /**
     * 是否显示区县*/
    private boolean isShowCounty =true;

    /**
     * 默认的显示省份
     */
    private Province defaultProvince;

    /**
     * 默认得显示城市
     */
    private City defaultCity;

    /**
     * 默认得显示区县
     */
    private County defaultCounty;

    /**
     * Color.GRAY
     */
    private int cancelTextColor = Color.GRAY;

    private String cancelText = "取消";

    private float cancelTextSize = 16;

    /**
     * Color.BLUE
     */
    private int doneTextColor = Color.BLUE;

    private String doneText = "确定";

    private float doneTextSize = 16;


    private Context context;

    protected  Builder builder;

    private Dialog dialog;
    View rootView;

    List<Province> mProvinces;

    private ArrayList<City> mCities = new ArrayList<>();
    private ArrayList<County> mCounties = new ArrayList<>();

    public WheelView provinceWheel;
    public WheelView citiesWheel;
    public WheelView countiesWheel;
    public TextView doneView;
    public TextView cancelView;
    /****
     * 保存回调
     */
    public  interface OnCityPickedListener {
        void onPicked(Province selectProvince, City selectCity,
                      County selectCounty);
    }
    private OnCityPickedListener listener;


    public CityPicker(){

    }

    public void setBuilder(Builder builder) {
        this.builder = builder;
    }


    public Builder getBuilder() {
        return builder;
    }

    /**
     * 初始化数据，解析json数据
     */
    public void initData(Context context) {
        this.context = context;

        StringBuilder stringBuilder = new StringBuilder();
        try {
            AssetManager assetManager = context.getAssets();
            BufferedReader bf = new BufferedReader(new InputStreamReader(assetManager.open("china_city_data.json")));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
            Type type = new TypeToken<ArrayList<Province>>() {
            }.getType();
            mProvinces = new Gson().fromJson(stringBuilder.toString(),type);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 初始化数据，解析json数据
     */
    public void initData(Context context,String json) {
        this.context = context;

        try {
            Type type = new TypeToken<ArrayList<Province>>() {
            }.getType();
            mProvinces = new Gson().fromJson(json,type);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(getClass().getName(), "json数据错误");
        }

    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (dialog==null||!dialog.isShowing()) {
                return;
            }
            switch (msg.what) {
                case UPDATE_CITY_WHEEL:
                    mCities.clear();
                    mCities.addAll(mProvinces.get(msg.arg1).getCities());
                    citiesWheel.invalidateWheel(true);
                    citiesWheel.setCurrentItem(0, false);

                    mCounties.clear();
                    mCounties.addAll(mCities.get(0).getCounties());
                    countiesWheel.invalidateWheel(true);
                    countiesWheel.setCurrentItem(0, false);
                    break;
                case UPDATE_COUNTY_WHEEL:
                    mCounties.clear();
                    mCounties.addAll(mCities.get(msg.arg1).getCounties());
                    countiesWheel.invalidateWheel(true);
                    countiesWheel.setCurrentItem(0, false);
                    break;
                default:
                    break;
            }
        }
    };

    public void show(){
        if (mProvinces==null) {
            Log.e(getClass().getName(), "请在Activity中initData初始化数据");
            return;
        }

        if (builder==null) {
            Log.e(getClass().getName(), "请配置builder参数");
            return;
        }

        dialog = new Dialog(context);

        Window window = dialog.getWindow();

        window.requestFeature(Window.FEATURE_NO_TITLE);
        window.setGravity(Gravity.BOTTOM);
        window.setBackgroundDrawable(new ColorDrawable(0x00000000));
        window.setWindowAnimations(R.style.AnimBottom);

        rootView = LayoutInflater.from(context).inflate(R.layout.dialog_city_picker, null);
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        dialog.setContentView(rootView);
        popupInit();


        provinceWheel.setViewAdapter(new AbstractWheelTextAdapter(context,
                R.layout.wheel_text) {

            @Override
            public int getItemsCount() {

                return mProvinces.size();
            }

            @Override
            protected CharSequence getItemText(int index) {

                return mProvinces.get(index).getName();
            }
        });
        provinceWheel.setCyclic(isCyclic);
        provinceWheel.setVisibleItems(visibleItems);
        provinceWheel.addClickingListener(this);


        if (isShowCity){//是否显示城市
            provinceWheel.addChangingListener(new OnWheelChangedListener() {

                @Override
                public void onChanged(WheelView wheel, int oldValue, int newValue) {
                    mHandler.removeMessages(UPDATE_CITY_WHEEL);
                    Message msg = Message.obtain();
                    msg.what = UPDATE_CITY_WHEEL;
                    msg.arg1 = newValue;
                    mHandler.sendMessageDelayed(msg, 50);
                }
            });

            citiesWheel.setViewAdapter(new AbstractWheelTextAdapter(context,
                    R.layout.wheel_text) {

                @Override
                public int getItemsCount() {

                    return mCities.size();
                }

                @Override
                protected CharSequence getItemText(int index) {

                    return mCities.get(index).getName();
                }
            });
            citiesWheel.setCyclic(isCyclic);
            citiesWheel.setVisibleItems(visibleItems);
            citiesWheel.addClickingListener(this);

            citiesWheel.addChangingListener(new OnWheelChangedListener() {

                @Override
                public void onChanged(WheelView wheel, int oldValue, int newValue) {
                    mHandler.removeMessages(UPDATE_COUNTY_WHEEL);
                    Message msg = Message.obtain();
                    msg.what = UPDATE_COUNTY_WHEEL;
                    msg.arg1 = newValue;
                    mHandler.sendMessageDelayed(msg, 50);

                }
            });
        }

        if (isShowCounty){//是否显示区县
            countiesWheel.setViewAdapter(new AbstractWheelTextAdapter(context,
                    R.layout.wheel_text) {

                @Override
                public int getItemsCount() {

                    return mCounties.size();
                }

                @Override
                protected CharSequence getItemText(int index) {

                    return mCounties.get(index).getName();
                }
            });
            countiesWheel.setCyclic(isCyclic);
            countiesWheel.setVisibleItems(visibleItems);
            countiesWheel.addClickingListener(this);
        }

        setDefaultArea(defaultProvince, defaultCity, defaultCounty);


        //确定
        doneView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (listener != null) {
                    Province province = mProvinces.size() > 0 ? mProvinces
                            .get(provinceWheel.getCurrentItem()) : null;
                    City city = mCities.size() > 0 ? mCities.get(citiesWheel
                            .getCurrentItem()) : null;
                    County county = mCounties.size() > 0 ? mCounties
                            .get(countiesWheel.getCurrentItem()) : null;
                    listener.onPicked(province, city, county);
                }
                dialog.dismiss();
            }
        });


        //取消
        cancelView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        dialog.show();

    }

    private void popupInit(){

        this.visibleItems = builder.visibleItems;
        this.isCyclic = builder.isCyclic;
        this.isShowCity = builder.isShowCity;
        this.isShowCounty = builder.isShowCounty;
        this.defaultProvince = builder.defaultProvince;
        this.defaultCity = builder.defaultCity;
        this.defaultCounty = builder.defaultCounty;
        this.listener = builder.listener;

        if (!isShowCity){
            isShowCounty  = false;//如果不显示城市，默认不显示区县
        }

        this.doneText = builder.doneText;
        this.doneTextColor = builder.doneTextColor;
        this.doneTextSize = builder.doneTextSize;

        this.cancelText = builder.cancelText;
        this.cancelTextColor = builder.cancelTextColor;
        this.cancelTextSize = builder.cancelTextSize;

        provinceWheel = (WheelView) rootView.findViewById(R.id.provinceWheel);
        citiesWheel = (WheelView) rootView.findViewById(R.id.citiesWheel);
        countiesWheel = (WheelView) rootView.findViewById(R.id.countiesWheel);
        doneView = (TextView) rootView.findViewById(R.id.done);
        cancelView =(TextView)rootView .findViewById(R.id.cancel);



        citiesWheel.setVisibility(isShowCity?View.VISIBLE:View.GONE);
        countiesWheel.setVisibility(isShowCounty?View.VISIBLE:View.GONE);

        /**
         * 确认按钮文字 颜色 大小**/
        doneView.setText(doneText);
        doneView.setTextColor(doneTextColor);
        doneView.setTextSize(doneTextSize);

        /**
         * 取消按钮文字 颜色 大小**/
        cancelView.setText(cancelText);
        cancelView.setTextColor(cancelTextColor);
        cancelView.setTextSize(cancelTextSize);
    }

    private void setDefaultArea(Province defaultProvince, City defaultCity,
                                County defaultCounty) {

        int provinceItem = 0;
        int cityItem = 0;
        int countyItem = 0;

        if (defaultProvince == null) {
            defaultProvince = mProvinces.get(0);
            provinceItem = 0;
        } else {
            for (int i = 0; i < mProvinces.size(); i++) {
                if (mProvinces.get(i).getName()
                        .equals(defaultProvince.getName())) {
                    provinceItem = i;
                    break;
                }
            }
        }
        mCities.clear();
        mCities.addAll(defaultProvince.getCities());
        if (mCities.size() == 0) {
            mCities.add(new City());
            cityItem = 0;
        } else if (defaultCity == null) {
            defaultCity = mCities.get(0);
            cityItem = 0;
        } else {
            for (int i = 0; i < mCities.size(); i++) {
                if (mCities.get(i).getName().equals(defaultCity.getName())) {
                    cityItem = i;
                    break;
                }
            }
        }

        mCounties.clear();
        mCounties.addAll(defaultCity.getCounties());
        if (mCounties.size() == 0) {
            mCounties.add(new County());
            countyItem = 0;
        } else if (defaultCounty == null) {
            defaultCounty = mCounties.get(0);
            countyItem = 0;
        } else {
            for (int i = 0; i < mCounties.size(); i++) {
                if (mCounties.get(i).getName()
                        .equals(defaultCounty.getName())) {
                    countyItem = i;
                    break;
                }
            }
        }

        provinceWheel.setDefaultCurrentItem(provinceItem);
        citiesWheel.setDefaultCurrentItem(cityItem);
        countiesWheel.setDefaultCurrentItem(countyItem);


    }

    public List<Province> getProvinces() {
        return mProvinces;
    }

    @Override
    public void onItemClicked(WheelView wheel, int itemIndex) {
        if (itemIndex != wheel.getCurrentItem()) {
            wheel.setCurrentItem(itemIndex, true, 500);
        }
    }

    public static class Builder {
        /**
         * 滚轮显示的item个数
         */
        private int visibleItems = 5;

        /**
         * 滚轮是否循环滚动
         */
        private boolean isCyclic = false;

        /**
         * 是否显示城市*/
        boolean isShowCity =true;

        /**
         * 是否显示区县*/
        boolean isShowCounty =true;

        /**
         * 默认的显示省份
         */
        Province defaultProvince;

        /**
         * 默认得显示城市
         */
        City defaultCity;

        /**
         * 默认得显示区县
         */
        County defaultCounty;

        /**
         * Color.GRAY
         */
        private int cancelTextColor = Color.GRAY;

        private String cancelText = "取消";

        private float cancelTextSize = 16;

        /**
         * Color.BLUE
         */
        private int doneTextColor = Color.BLUE;

        private String doneText = "确定";

        private float doneTextSize = 16;


        /***
         * 点击确定  回调函数*/
        OnCityPickedListener listener;

        public Builder() {
        }


        /**
         * 滚轮显示的item个数
         *
         * @param visibleItems
         * @return
         */
        public Builder visibleItemsCount(int visibleItems) {
            this.visibleItems = visibleItems;
            return this;
        }
        /**
         * 滚轮是否循环滚动
         *
         * @param isCyclic
         * @return
         */
        public Builder cyclic(boolean isCyclic) {
            this.isCyclic = isCyclic;
            return this;
        }

        /**
         * 城市是否显示
         *
         * @param isShowCity
         * @return
         */
        public Builder showCity(boolean isShowCity) {
            this.isShowCity = isShowCity;
            return this;
        }

        /**
         * 区县是否显示
         *
         * @param isShowCounty
         * @return
         */
        public Builder showCounty(boolean isShowCounty) {
            this.isShowCounty = isShowCounty;
            return this;
        }

        /**
         * 默认得显示省份
         *
         * @param defaultProvince
         * @return
         */

        public Builder province(Province defaultProvince){
               this.defaultProvince = defaultProvince;
               return this;
        }

        /**
         * 默认得显示城市
         *
         * @param defaultCity
         * @return
         */
        public Builder city(City defaultCity) {
            this.defaultCity = defaultCity;
            return this;
        }

        /**
         * 默认得显示区县
         *
         * @param defaultCounty
         * @return
         */
        public Builder county(County defaultCounty) {
            this.defaultCounty = defaultCounty;
            return this;
        }


        /**
         * 确认按钮文字
         *
         * @param doneText
         * @return
         */
        public Builder doneText(String doneText) {
            this.doneText = doneText;
            return this;
        }

        /**
         * 确认按钮文字颜色
         *
         * @param color
         * @return
         */
        public Builder doneTextColor(int color) {
            this.doneTextColor = color;
            return this;
        }

        /**
         * 确认按钮文字大小
         *
         * @param doneTextSize
         * @return
         */
        public Builder doneTextSize(float doneTextSize) {
            this.doneTextSize = doneTextSize;
            return this;
        }

        /**
         * 取消按钮文字颜色
         *
         * @param color
         * @return
         */
        public Builder cancelTextColor(int color) {
            this.cancelTextColor = color;
            return this;
        }

        /**
         * 取消按钮文字大小
         *
         * @param cancelTextSize
         * @return
         */
        public Builder cancelTextSize(float cancelTextSize) {
            this.cancelTextSize = cancelTextSize;
            return this;
        }

        /**
         * 取消按钮文字
         *
         * @param cancelText
         * @return
         */
        public Builder cancelText(String cancelText) {
            this.cancelText = cancelText;
            return this;
        }

        public Builder cityPickedListener(OnCityPickedListener listener) {
            this.listener = listener;
            return this;
        }


    }
}
