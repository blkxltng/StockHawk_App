package com.sam_chordas.android.stockhawk.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DetailsActivity extends AppCompatActivity {

    String id, symbol;
    int rangeMin, rangeMax;
    List<String> chartLabels = new ArrayList<>();
    List<Float> chartValues = new ArrayList<>();

    LineChartView mLineChartView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);

        id = getIntent().getStringExtra("id");
        symbol = getIntent().getStringExtra("symbol");

        mLineChartView = (LineChartView) findViewById(R.id.linechart);

        getChartInfo();
    }

    private void getChartInfo(){

        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://chartapi.finance.yahoo.com/instrument/1.0/"+symbol+"/chartdata;type=close;range=1d/json")
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.code() == 200) {
                    try {
                        String result = response.body().string();
                        if (result.startsWith("finance_charts_json_callback( ")) {
                            result = result.substring(29, result.length() - 2);
                        }

                        JSONObject jsonObject = new JSONObject(result);
                        JSONObject jsonObjectRange = jsonObject.getJSONObject("ranges");
                        JSONObject rangeClose = jsonObjectRange.getJSONObject("close");
                        rangeMin = rangeClose.getInt("min");
                        rangeMax = rangeClose.getInt("max");

                        JSONArray jsonArraySeries = jsonObject.getJSONArray("series");
                        for (int i = 0; i < jsonArraySeries.length(); i++) {
                            JSONObject series_object = jsonArraySeries.getJSONObject(i);

                            int time_stamp = series_object.getInt("Timestamp");
                            Date date = new Date(time_stamp*1000);
                            SimpleDateFormat date_format = new SimpleDateFormat("HH:mm", Locale.getDefault());
                            String time = date_format.format(date);
                            chartLabels.add(time);
                            chartValues.add(Float.parseFloat(series_object.getString("close")));
                        }

                        makeChart();

                    } catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void makeChart(){

        String[] labelsArray = new String[chartLabels.size()];
        float[] valuesArray = new float[chartValues.size()];

        chartLabels.toArray(labelsArray);

        int i = 0;
        for (Float f : chartValues){
            valuesArray[i++] = (f != null ? f : Float.NaN);
        }

        LineSet chartData = new LineSet(labelsArray, valuesArray);
        chartData.setColor(Color.parseColor("#2196F3"));
        chartData.setThickness(3);
        mLineChartView.addData(chartData);

        mLineChartView.setBorderSpacing(1)
                .setXLabels(AxisController.LabelPosition.NONE)
                .setYLabels(AxisController.LabelPosition.OUTSIDE)
                .setXAxis(false)
                .setYAxis(false)
                .setAxisBorderValues(rangeMin - 1, rangeMax + 1)
                .setBorderSpacing(Tools.fromDpToPx(1))
                .setAxisColor(Color.WHITE)
                .setLabelsColor(Color.WHITE);

        mLineChartView.show();
    }
}
