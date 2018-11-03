package com.josh.billrssroom.screens.feed;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.josh.billrssroom.R;
import com.josh.billrssroom.api.DataService;
import com.josh.billrssroom.api.Resource;
import com.josh.billrssroom.screens.common.BaseActivity;
import com.josh.billrssroom.model.FeedItem;
import com.josh.billrssroom.screens.favorites.FavoritesActivity;
import com.josh.billrssroom.viewmodel.FeedViewModel;

import java.util.List;

import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;

public class MainActivity extends BaseActivity implements FeedListViewMvcImpl.Listener {

    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String PAYLOAD_SAVE_BTN_CLICKED = "PAYLOAD_SAVE_BTN_CLICKED";

    private FeedListViewMvc viewMvc;

    private FeedViewModel feedViewModel;

    private DataService dataServiceApi;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewMvc = getCompositionRoot().getViewMvcFactory().getFeedListViewMvc(null);
        viewMvc.registerListener(this);

        feedViewModel = ViewModelProviders.of(this).get(FeedViewModel.class);

        subscribeFeedUi(feedViewModel);

//        dataServiceApi = getCompositionRoot().getRssFeedApi();


        setContentView(viewMvc.getRootView());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }


    private void subscribeFeedUi(FeedViewModel feedViewModel) {
        feedViewModel.getObservableFeedItems().observe(this, (Resource<List<FeedItem>> listResource) -> {
            if (listResource != null && listResource.data != null) {

                viewMvc.bindFeedItems(listResource.data);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.favorite:
                startActivity(new Intent(MainActivity.this, FavoritesActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onShareBtnClicked(FeedItem feedItem, int position) {
        Toast.makeText(this, "Share: " + feedItem.getTitle() + " " + position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBrowserBtnClicked(FeedItem feedItem, int position) {
        Log.d(TAG, "onBrowserBtnClicked: " + feedItem.getTitle());
        Toast.makeText(this, "Browse: " + feedItem.getTitle() + " position: " + position, Toast.LENGTH_SHORT).show();
    }
}