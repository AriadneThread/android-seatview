package com.kokozu.widget.samples;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewGroup layContent = (ViewGroup) findViewById(R.id.lay_content);
        final int count = layContent.getChildCount();
        for (int i = 0; i < count; i++) {
            layContent.getChildAt(i).setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn1:
                startActivity(new Intent(this, SeatViewDemo.class));
                break;

            default:
                break;
        }
    }
}
