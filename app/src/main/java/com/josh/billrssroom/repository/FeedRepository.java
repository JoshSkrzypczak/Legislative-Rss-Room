package com.josh.billrssroom.repository;

import android.os.AsyncTask;
import android.util.Log;

import com.josh.billrssroom.AppExecutors;
import com.josh.billrssroom.api.ApiResponse;
import com.josh.billrssroom.api.DataService;
import com.josh.billrssroom.api.NetworkBoundResource;
import com.josh.billrssroom.api.Resource;
import com.josh.billrssroom.api.RetrofitClient;
import com.josh.billrssroom.db.FeedDatabase;
import com.josh.billrssroom.db.dao.FeedDao;
import com.josh.billrssroom.model.FeedItem;
import com.josh.billrssroom.model.RssResult;
import com.josh.billrssroom.utilities.RateLimiter;

import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

public class FeedRepository {
    public static final String TAG = FeedRepository.class.getSimpleName();

    private static FeedRepository INSTANCE;
    private FeedDao feedDao;
    private final AppExecutors appExecutors;
    private final FeedDatabase feedDatabase;
    private final DataService apiService;

    private RateLimiter<String> billFeedRateLimit = new RateLimiter<>(10, TimeUnit.MINUTES);


    public FeedRepository(final FeedDatabase feedDatabase, AppExecutors appExecutors) {
        this.feedDatabase = feedDatabase;
        this.appExecutors = appExecutors;


        apiService = RetrofitClient.getInstance().getRestApi();
    }

    public LiveData<Resource<List<FeedItem>>> loadBillItems() {
        return new NetworkBoundResource<List<FeedItem>, RssResult>(appExecutors) {
            @Override
            protected void saveCallResult(@NonNull RssResult item) {
                feedDatabase.feedDao().insertData(item.getChannel().getItems());
            }

            @Override
            protected boolean shouldFetch(@Nullable List<FeedItem> data) {
                return data == null || data.isEmpty() || billFeedRateLimit.shouldFetch(data.toString());
            }

            @NonNull
            @Override
            protected LiveData<List<FeedItem>> loadFromDb() {
                return feedDatabase.feedDao().loadFeedDbItems();
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<RssResult>> createCall() {
                return apiService.getMyService();
            }
        }.asLiveData();
    }


    public LiveData<List<FeedItem>> loadAllFavoriteItems(){
        return feedDatabase.feedDao().getFavorites();
    }

    public void removeItemFromFavorites(FeedItem feedItem){
        new removeItemFromFavoritesAsync(feedDao).execute(feedItem);
    }


    public void updateFeedItemAsFavorite(FeedItem item) {
        new updateFeedItemFavoriteAsync(feedDao).execute(item);
    }

    private static class updateFeedItemFavoriteAsync extends AsyncTask<FeedItem, Void, Void>{

        private FeedDao asyncDao;

        updateFeedItemFavoriteAsync(FeedDao dao){
            asyncDao = dao;
        }

        @Override
        protected Void doInBackground(FeedItem... items) {

            boolean favoriteValue = asyncDao.getItemBoolean(items[0].getTitle());

            if (favoriteValue){
                Log.d(TAG, "doInBackground: favoriteValue " + favoriteValue);
                // Set isFavorite to false
                // Item previously favorite; onClick sets favorite to false
                // Show empty image drawable
                asyncDao.updateAndSetItemToFalse(items[0].getTitle());
            } else {
                Log.d(TAG, "doInBackground: else favoriteValue" + favoriteValue);
                // Set isFavorite to true
                // Item is not favorite; onClick sets favorite to true
                // Show full image drawable
                asyncDao.updateAndSetItemToTrue(items[0].getTitle());
            }

            return null;
        }
    }

    private static class removeItemFromFavoritesAsync extends AsyncTask<FeedItem, Void, Void>{
        private FeedDao asyncRemoveDao;

        public removeItemFromFavoritesAsync(FeedDao asyncRemoveDao) {
            this.asyncRemoveDao = asyncRemoveDao;
        }

        @Override
        protected Void doInBackground(FeedItem... items) {

            asyncRemoveDao.updateAndSetItemToFalse(items[0].getTitle());

            return null;
        }
    }

    public static FeedRepository getInstance(final FeedDatabase feedDatabase, AppExecutors appExecutors) {
        if (INSTANCE == null) {
            synchronized (FeedRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new FeedRepository(feedDatabase, appExecutors);
                }
            }
        }
        return INSTANCE;
    }
}