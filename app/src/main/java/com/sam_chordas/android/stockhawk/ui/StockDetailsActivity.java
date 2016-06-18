package com.sam_chordas.android.stockhawk.ui;

import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;

public class StockDetailsActivity extends AppCompatActivity{

    String selectedStock;

    private String stockName;
    private ArrayList<String> labels;
    private ArrayList<Float> values;
    private LineChartView lineChartView;
    private LineSet mLineSet;
    private TextView textStock;
    int maxRange,minRange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        textStock = (TextView)findViewById(R.id.detail_text);
        selectedStock = getIntent().getStringExtra("stock");
        mLineSet = new LineSet();
        lineChartView = (LineChartView) findViewById(R.id.linechart);
        initLineChart();
        if (savedInstanceState == null) {
            getStockDetails();
        }
    }

    private void initLineChart() {
        Paint gridPaint = new Paint();
        gridPaint.setColor(getResources().getColor(R.color.line_paint));
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setAntiAlias(true);
        gridPaint.setStrokeWidth(Tools.fromDpToPx(1f));
        lineChartView.setBorderSpacing(1)
                .setAxisBorderValues(minRange-100, maxRange+100, 50)
                .setXLabels(AxisController.LabelPosition.OUTSIDE)
                .setYLabels(AxisController.LabelPosition.OUTSIDE)
                .setLabelsColor(getResources().getColor(R.color.line_labels))
                .setXAxis(false)
                .setYAxis(false)
                .setBorderSpacing(Tools.fromDpToPx(5))
                .setGrid(ChartView.GridType.HORIZONTAL, gridPaint);
    }

    private void getStockDetails() {
        OkHttpClient httpClient = new OkHttpClient();

        Request request = new Request.Builder()
                .url("http://chartapi.finance.yahoo.com/instrument/1.0/" + selectedStock+ "/chartdata;type=quote;range=1y/json")
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Response response) throws IOException {
                if (response.code() == 200) {
                    try {
                        // Trim response string
                        String result = response.body().string();
                        if (result.startsWith("finance_charts_json_callback( ")) {
                            result = result.substring(29, result.length() - 2);
                        }
                        // Parse JSON
                        JSONObject object = new JSONObject(result);
                        stockName = object.getJSONObject("meta").getString("Company-Name");
                        labels = new ArrayList<>();
                        values = new ArrayList<>();
                        JSONArray series = object.getJSONArray("series");
                        for (int i = 0; i < series.length(); i++) {
                            JSONObject seriesItem = series.getJSONObject(i);
                            SimpleDateFormat srcFormat = new SimpleDateFormat("yyyyMMdd");
                            String date = android.text.format.DateFormat.
                                    getMediumDateFormat(getApplicationContext()).
                                    format(srcFormat.parse(seriesItem.getString("Date")));
                            labels.add(date);
                            values.add(Float.parseFloat(seriesItem.getString("close")));
                        }
                        onDownloadCompleted();
                    } catch (Exception e) {
                        onDownloadFailed();
                        e.printStackTrace();
                    }
                } else {
                    onDownloadFailed();
                }
            }

            @Override
            public void onFailure(Request request, IOException e) {
                onDownloadFailed();
            }
        });
    }
    private void onDownloadCompleted() {
        StockDetailsActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setTitle(stockName);
                minRange = Math.round(Collections.min(values));
                maxRange = Math.round(Collections.max(values));
                lineChartView.setAxisBorderValues(minRange, maxRange, 1);
                for (int i = 0; i < labels.size(); i++){
                    mLineSet.addPoint("test " + i, values.get(i));
                }
                mLineSet.setColor(getResources().getColor(R.color.line_set))
                        .setDotsStrokeThickness(Tools.fromDpToPx(2))
                        .setDotsStrokeColor(getResources().getColor(R.color.line_stroke))
                        .setDotsColor(getResources().getColor(R.color.line_dots));
                lineChartView.addData(mLineSet);
                lineChartView.show();
            }
        });
    }
    private void onDownloadFailed() {
        StockDetailsActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setTitle(R.string.error);
                textStock.setText(R.string.error);
            }
        });
    }
}
