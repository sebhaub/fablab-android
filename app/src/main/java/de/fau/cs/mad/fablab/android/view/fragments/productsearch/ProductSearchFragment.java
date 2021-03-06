package de.fau.cs.mad.fablab.android.view.fragments.productsearch;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.pedrogomez.renderers.RVRendererAdapter;

import javax.inject.Inject;

import butterknife.Bind;
import de.fau.cs.mad.fablab.android.R;
import de.fau.cs.mad.fablab.android.model.events.ProductSearchDoNotDeleteProductsEvent;
import de.fau.cs.mad.fablab.android.util.UiUtils;
import de.fau.cs.mad.fablab.android.view.activities.BackButtonPressedEvent;
import de.fau.cs.mad.fablab.android.view.activities.MainActivity;
import de.fau.cs.mad.fablab.android.view.common.binding.MenuItemCommandBinding;
import de.fau.cs.mad.fablab.android.view.common.fragments.BaseFragment;
import de.fau.cs.mad.fablab.android.view.fragments.categorysearch.CategoryDialogFragment;
import de.greenrobot.event.EventBus;
import xyz.danoz.recyclerviewfastscroller.sectionindicator.title.SectionTitleIndicator;
import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller;

public class ProductSearchFragment extends BaseFragment implements
        ProductSearchFragmentViewModel.Listener {

    private RVRendererAdapter<ProductSearchViewModel> mAdapter;
    private EventBus mEventBus = EventBus.getDefault();
    private MenuItem mOrderByItem;
    private boolean mShowCartFAB;
    private boolean mProductSearch;
    private boolean hasFragmentChangedOnPressedBackButton = false;

    @Inject
    ProductSearchFragmentViewModel mViewModel;

    @Bind(R.id.product_recycler_view)
    RecyclerView mProductRecyclerView;
    @Bind(R.id.product_fast_scroller)
    VerticalRecyclerViewFastScroller mProductFastScroller;
    @Bind(R.id.product_fast_scroller_section_title_indicator)
    SectionTitleIndicator mProductSectionTitleIndicator;
    @Bind(R.id.product_recycler_view_container)
    RelativeLayout mProductRecyclerViewContainer;
    @Bind(R.id.product_progress_bar)
    ProgressBar mProductProgressBar;

    private ProductSearchView mSearchView;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mShowCartFAB = args.getBoolean(getResources().getString(R.string.key_show_cart_fab));
        mProductSearch = args.getBoolean(getResources().getString(R.string.key_product_search));
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_search, menu);

        if(mProductSearch) {
            MenuItem searchItem = menu.findItem(R.id.action_search);
            searchItem.setVisible(true);
            mSearchView = (ProductSearchView) MenuItemCompat.getActionView(
                    searchItem);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                    R.layout.dropdown_item, mViewModel.getAutoCompleteWords());
            mSearchView.setAdapter(adapter);
            mSearchView.setCommand(mViewModel.getSearchCommand());
            mSearchView.setQueryHint(getString(R.string.search_all_hint));
            searchItem.expandActionView();

            if(searchItem != null)
                MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener()
                {
                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item)
                    {
                        return true;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item)
                    {
                        // delete products in product search fragment if the back button is pressed in the product search fragment
                        // doesn't delete products if the back button is pressed in locationMap, cart or outOfStock dialog
                        if(!hasFragmentChangedOnPressedBackButton) {
                            mViewModel.deleteView();
                            getFragmentManager().popBackStack();
                        }
                        return true;
                    }
                });
        } else {
            MenuItem searchItem = menu.findItem(R.id.action_search_category);
            searchItem.setVisible(true);
            searchItem.expandActionView();
            new MenuItemCommandBinding().bind(searchItem, mViewModel.getSearchCategoryCommand());
            onCategorySearchClicked();
        }

        mOrderByItem = menu.findItem(R.id.action_orderby);
        if(mOrderByItem != null) {
            new MenuItemCommandBinding().bind(mOrderByItem.getSubMenu().getItem(0),
                    mViewModel.getOrderProductsByNameCommand());
            new MenuItemCommandBinding().bind(mOrderByItem.getSubMenu().getItem(1),
                    mViewModel.getOrderProductsByPriceCommand());
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_productsearch, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel.loadProductNames();

        mAdapter = new ProductRVRendererAdapter(getLayoutInflater(savedInstanceState),
                new ProductSearchViewModelRendererBuilder(),
                mViewModel.getProductSearchViewModelCollection());

        mProductRecyclerView.setAdapter(mAdapter);
        mProductFastScroller.setRecyclerView(mProductRecyclerView);
        mProductRecyclerView.addOnScrollListener(mProductFastScroller.getOnScrollListener());
        mProductFastScroller.setSectionIndicator(mProductSectionTitleIndicator);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mProductRecyclerView.setLayoutManager(layoutManager);

        mViewModel.setListener(this);
        mViewModel.initialize();
    }

    @Override
    public void onResume() {
        super.onResume();
        mEventBus.register(this);
        mViewModel.resume();
        int displayOptions = MainActivity.DISPLAY_LOGO | MainActivity.DISPLAY_NAVDRAWER;
        if (mShowCartFAB) {
            displayOptions |= MainActivity.DISPLAY_CART_PANEL | MainActivity.DISPLAY_FAB;
        }
        setDisplayOptions(displayOptions);
        setNavigationDrawerSelection(mProductSearch ? R.id.drawer_item_productsearch
                : R.id.drawer_item_categorysearch);
    }

    @Override
    public void onPause() {
        super.onPause();
        mEventBus.unregister(this);
        mViewModel.pause();
    }

    @Override
    public void onDataChanged() {
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onSearchStateChanged() {
        if(mViewModel.getSearchState()){
            UiUtils.hideKeyboard(getActivity());
            mProductRecyclerViewContainer.setVisibility(View.GONE);
            mProductProgressBar.setVisibility(View.VISIBLE);
            mSearchView.clearFocus();
        }
        else{
            mProductProgressBar.setVisibility(View.GONE);
            mProductRecyclerViewContainer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRetrofitErrorOccurred() {
        Toast.makeText(getActivity(), R.string.retrofit_callback_failure, Toast.LENGTH_LONG).show();
    }

    public void onNoProductsFound() {
        Toast.makeText(getActivity(), R.string.no_products_found, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onProductOrderChanged() {
        if(mOrderByItem != null) {
            if(mViewModel.isOrderedByName()) {
                mOrderByItem.setIcon(R.drawable.sort_alpha);
                mOrderByItem.setTitle(R.string.appbar_orderedby_name);
                mProductSectionTitleIndicator.setVisibility(View.VISIBLE);
            } else {
                mOrderByItem.setIcon(R.drawable.sort_euro);
                mOrderByItem.setTitle(R.string.appbar_orderedby_price);
                mProductSectionTitleIndicator.setVisibility(View.INVISIBLE);
            }
        }
    }

    public void onEvent(ProductClickedEvent event) {
        ProductDialogFragment dialogFragment = new ProductDialogFragment();
        Bundle arguments = new Bundle();
        arguments.putSerializable(ProductDialogFragmentViewModel.KEY_PRODUCT, event.getProduct());
        dialogFragment.setArguments(arguments);
        dialogFragment.show(getFragmentManager(), "ProductDialogFragment");
    }

    @SuppressWarnings("unused")
    public void onEvent(ProductSearchDoNotDeleteProductsEvent event)
    {
        hasFragmentChangedOnPressedBackButton = true;
    }

    @SuppressWarnings("unused")
    public void onEvent(BackButtonPressedEvent event)
    {
        hasFragmentChangedOnPressedBackButton = false;
    }


    @Override
    public void onCategorySearchClicked()
    {
        Fragment fragment = getFragmentManager().findFragmentByTag("CategoryDialogFragment");
        if(fragment == null) {
            CategoryDialogFragment frag = new CategoryDialogFragment();
            frag.show(getFragmentManager(), "CategoryDialogFragment");
        }
    }

}
