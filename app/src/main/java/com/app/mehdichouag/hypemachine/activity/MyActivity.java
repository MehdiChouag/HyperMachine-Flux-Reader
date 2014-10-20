package com.app.mehdichouag.hypemachine.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.app.mehdichouag.hypemachine.R;
import com.app.mehdichouag.hypemachine.fragment.MyFragment;


public class MyActivity extends FragmentActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new MyFragment())
                    .commit();
        }
    }
}
