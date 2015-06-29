package de.fau.cs.mad.fablab.android.ui;

import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.fau.cs.mad.fablab.android.FabButton;
import de.fau.cs.mad.fablab.android.R;
import de.fau.cs.mad.fablab.android.cart.CartSingleton;
import de.fau.cs.mad.fablab.android.eventbus.DoorEvent;
import de.fau.cs.mad.fablab.android.navdrawer.AppbarDrawerInclude;
import de.fau.cs.mad.fablab.android.productsearch.AutoCompleteHelper;
import de.fau.cs.mad.fablab.android.pushservice.PushException;
import de.fau.cs.mad.fablab.android.pushservice.PushService;
import de.fau.cs.mad.fablab.rest.ICalApiClient;
import de.fau.cs.mad.fablab.rest.NewsApiClient;
import de.fau.cs.mad.fablab.rest.core.ICal;
import de.fau.cs.mad.fablab.rest.core.News;
import de.fau.cs.mad.fablab.rest.myapi.ICalApi;
import de.fau.cs.mad.fablab.rest.myapi.NewsApi;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import roboguice.activity.RoboActionBarActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_news)
public class NewsActivity extends RoboActionBarActivity {

    @InjectView(R.id.cart_recycler_view) RecyclerView cart_rv;
    @InjectView(R.id.news) RecyclerView news_rv;
    @InjectView(R.id.dates_view_pager) ViewPager datesViewPager;
    private DatesSlidePagerAdapter datesSlidePagerAdapter;
    private RecyclerView.LayoutManager newsLayoutManager;
    private NewsAdapter newsAdapter;
    private UiUtils uiUtils;

    private List<News> newsList;
    private NewsApi newsApi;

    private List<ICal> iCalList;
    private ICalApi iCalApi;

    private AppbarDrawerInclude appbarDrawer;

    static final String IMAGE = "IMAGE";
    static final String TITLE = "TITLE";
    static final String TEXT = "TEXT";

    static final String ICAL1 = "ICAL1";
    static final String ICAL2 = "ICAL2";


    //This callback is used for news retrieval.
    private Callback<List<News>> newsCallback = new Callback<List<News>>() {
        @Override
        public void success(List<News> news, Response response) {
            if (news.isEmpty()) {
                Toast.makeText(getBaseContext(), R.string.product_not_found, Toast.LENGTH_LONG).show();
            }
            newsList.clear();
            newsList.addAll(news);
            newsAdapter.notifyDataSetChanged();
        }

        @Override
        public void failure(RetrofitError error) {
            Toast.makeText(getBaseContext(), R.string.retrofit_callback_failure, Toast.LENGTH_LONG).show();
            newsList.clear();
            newsAdapter.notifyDataSetChanged();
        }
    };

    //This callback is used for ical retrieval.
    private Callback<List<ICal>> iCalCallback = new Callback<List<ICal>>() {
        @Override
        public void success(List<ICal> iCals, Response response) {
            if (iCals.isEmpty()) {
                Toast.makeText(getBaseContext(), R.string.product_not_found, Toast.LENGTH_LONG).show();
            }
            iCalList.clear();
            // only add events that are not terminated yet
            Date now = new Date();
            for (ICal event : iCals) {
                if (event.getEndAsDate().after(now)) iCalList.add(event);
            }
            datesSlidePagerAdapter.notifyDataSetChanged();
        }

        @Override
        public void failure(RetrofitError error) {
            Toast.makeText(getBaseContext(), R.string.retrofit_callback_failure, Toast.LENGTH_LONG).show();
            iCalList.clear();
            datesSlidePagerAdapter.notifyDataSetChanged();
        }
    };


    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        uiUtils = new UiUtils();
        registerToPushService();
        appbarDrawer = AppbarDrawerInclude.getInstance(this);
        appbarDrawer.create();

        if (savedInstanceState == null) {
            // init db and cart - always do this on app start
            CartSingleton.MYCART.init(getApplication());
        }

        // init cart panel
        CartSingleton.MYCART.setSlidingUpPanel(this, findViewById(android.R.id.content), true);

        // init Floating Action Menu
        FabButton.MYFABUTTON.init(findViewById(android.R.id.content), this);

        //get news and set them
        newsLayoutManager = new LinearLayoutManager(getApplicationContext());
        news_rv.setLayoutManager(newsLayoutManager);
        newsList = new ArrayList<>();
        newsApi = new NewsApiClient(this).get();
        newsAdapter = new NewsAdapter(newsList);
        news_rv.setAdapter(newsAdapter);
        newsApi.findAll(newsCallback);

        iCalList = new ArrayList<>();
        datesSlidePagerAdapter = new DatesSlidePagerAdapter(getSupportFragmentManager(), iCalList);
        datesViewPager.setAdapter(datesSlidePagerAdapter);
        iCalApi = new ICalApiClient(this).get();
        iCalApi.findAll(iCalCallback);

        //Load Autocompleteionwords
        AutoCompleteHelper.getInstance().loadProductNames(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        appbarDrawer.createMenu(menu);
        appbarDrawer.startTimer();
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        appbarDrawer.stopTimer();
    }

    @Override
    public void onResume() {
        super.onResume();
        CartSingleton.MYCART.setSlidingUpPanel(this, findViewById(android.R.id.content), true);
        appbarDrawer.startTimer();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_opened) {
            appbarDrawer.updateOpenState(item);
            Toast appbar_opened_toast = Toast.makeText(this, appbarDrawer.openedMessage, Toast.LENGTH_SHORT);
            appbar_opened_toast.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

        private List<News> news = new ArrayList<>();

        @Override
        public NewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_entry, parent, false);
            return new NewsViewHolder(view);
        }

        @Override
        public void onBindViewHolder(NewsViewHolder holder, int position) {
            holder.setNews(news.get(position));
        }

        @Override
        public int getItemCount() {
            return news.size();
        }

        public NewsAdapter(List<News> newsList)
        {
            this.news = newsList;
        }

        public class NewsViewHolder extends RecyclerView.ViewHolder {

            private final View view;
            private final TextView titleView, subTitleView;
            private final ImageView iconView;
            private String description;

            public NewsViewHolder(View view) {
                super(view);
                this.view = view;
                this.titleView = (TextView) view.findViewById(R.id.title_news_entry);
                this.subTitleView = (TextView) view.findViewById(R.id.text_news_entry);
                this.iconView = (ImageView) view.findViewById(R.id.icon_news_entry);
            }

            public void setNews(News news)
            {
                this.titleView.setText(news.getTitle());
                this.subTitleView.setText(news.getDescriptionShort());
                if(news.getLinkToPreviewImage() != null && !news.getLinkToPreviewImage().contains("fablab_logo.png")) {
                    //new DownloadImageTask(iconView).execute(news.getLinkToPreviewImage());
                    Picasso.with(iconView.getContext()).load(news.getLinkToPreviewImage()).into(iconView);
                } else {
                    Picasso.with(iconView.getContext()).load(R.drawable.news_nopicture).into(iconView);
                }
                description = news.getDescription();

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TextView title = (TextView) v.findViewById(R.id.title_news_entry);
                        TextView text = (TextView) v.findViewById(R.id.text_news_entry);
                        ImageView image = (ImageView) v.findViewById(R.id.icon_news_entry);
                        Bundle args = new Bundle();
                        args.putString(TITLE, title.getText().toString());
                        args.putString(TEXT, description);
                        args.putParcelable(IMAGE, ((BitmapDrawable) image.getDrawable()).getBitmap());
                        NewsDialog dialog = new NewsDialog();
                        dialog.setArguments(args);
                        dialog.show(getSupportFragmentManager(), "news dialog");
                    }
                });
            }
        }
    }

    private class DatesSlidePagerAdapter extends FragmentStatePagerAdapter {

        List<ICal> dates = new ArrayList<>();
        public DatesSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public DatesSlidePagerAdapter(FragmentManager fm, List<ICal> dates)
        {
            super(fm);
            this.dates = dates;
        }

        @Override
        public Fragment getItem(int position) {
            int arrayPosition = position *2;

            Bundle args = new Bundle();
            args.putSerializable(ICAL1, dates.get(arrayPosition));
            if(arrayPosition+1 < dates.size())
            {
                args.putSerializable(ICAL2, dates.get(arrayPosition+1));
            }
            DatesFragment datesFragment = new DatesFragment();
            datesFragment.setArguments(args);
            return datesFragment;

        }

        @Override
        public int getCount() {
            return (dates.size()+1)/2;
        }
    }

    public void onEvent(DoorEvent event) {
        appbarDrawer.updateOpenStateEvent(event);
    }

    private void registerToPushService(){
        PushService pushService = new PushService(this);
        try {
            pushService.registerDeviceToGCM();
        }catch (PushException pushEx){
            pushEx.printStackTrace();
        }

    }
}
