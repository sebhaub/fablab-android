package de.fau.cs.mad.fablab.android.view.fragments;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import javax.inject.Inject;

import de.fau.cs.mad.fablab.android.R;
import de.fau.cs.mad.fablab.android.model.events.NewsListScrollingEvent;
import de.fau.cs.mad.fablab.android.view.common.binding.RecyclerViewDeltaCommandBinding;
import de.fau.cs.mad.fablab.android.view.common.fragments.BaseFragment;
import de.fau.cs.mad.fablab.android.view.fragments.icals.ICalFragment;
import de.fau.cs.mad.fablab.android.view.fragments.news.NewsFragment;
import de.greenrobot.event.EventBus;

public class ICalAndNewsFragment extends BaseFragment implements ICalAndNewsFragmentViewModel.Listener{

    EventBus mEventBus = EventBus.getDefault();
    @Inject
    ICalAndNewsFragmentViewModel mViewModel;

    @Inject
    public ICalAndNewsFragment() {
        mEventBus.register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ical_and_news, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        setDisplayOptions(R.id.drawer_item_news, true, true);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final String TAG_ICAL_FRAGMENT = "tag_ical_fragment";
        final String TAG_NEWS_FRAGMENT = "tag_news_fragment";

        ICalFragment iCalFragment = (ICalFragment) getChildFragmentManager().findFragmentByTag(
                TAG_ICAL_FRAGMENT);
        if (iCalFragment == null) {
            getChildFragmentManager().beginTransaction().add(R.id.fragment_ical, new ICalFragment(),
                    TAG_ICAL_FRAGMENT).commit();
        }

        NewsFragment newsFragment = (NewsFragment) getChildFragmentManager().findFragmentByTag(
                TAG_NEWS_FRAGMENT);
        if (newsFragment == null) {
            getChildFragmentManager().beginTransaction().add(R.id.fragment_news, new NewsFragment(),
                    TAG_NEWS_FRAGMENT).commit();
        }
    }

    public void onEvent(NewsListScrollingEvent event){
        FrameLayout iCalView = (FrameLayout) getView().findViewById(R.id.fragment_ical);
        iCalView.setTranslationY(iCalView.getTranslationY() - event.getDelta().getDy());
    }
}