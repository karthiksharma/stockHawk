package com.sam_chordas.android.stockhawk.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.sam_chordas.android.stockhawk.R;

public class StockDetailsActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        TextView stockName = (TextView)findViewById(R.id.detail_text);
        String name = getIntent().getStringExtra("stock");
        stockName.setText(name);
    }
}
