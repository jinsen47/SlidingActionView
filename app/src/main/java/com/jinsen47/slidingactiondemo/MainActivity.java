package com.jinsen47.slidingactiondemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.jinsen47.view.SlidingActionView;

public class MainActivity extends AppCompatActivity {

    SlidingActionView mView;
    boolean isRunning = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mView = ((SlidingActionView) findViewById(R.id.view_action));

        mView.setCenterIcon(R.drawable.center_button_start);

        mView.setActionListener(new SlidingActionView.SlidingActionListener() {
            @Override
            public void onLeftEdgeReached() {
                Toast.makeText(MainActivity.this, "Left edge reached!", Toast.LENGTH_SHORT).show();
                if (isRunning) {
                    mView.setCenterIcon(R.drawable.center_button_stop);
                } else {
                    mView.setCenterIcon(R.drawable.center_button_start);
                }
                isRunning = !isRunning;
            }

            @Override
            public void onRightEdgeReached() {
                Toast.makeText(MainActivity.this, "Right edge reached!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
