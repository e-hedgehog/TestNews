package com.ehedgehog.android.testnews;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class NewsListActivity extends SingleFragmentActivity {


    @Override
    protected Fragment createFragment() {
        return NewsListFragment.newInstance();
    }
}
