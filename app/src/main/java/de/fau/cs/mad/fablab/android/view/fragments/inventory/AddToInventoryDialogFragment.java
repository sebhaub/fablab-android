package de.fau.cs.mad.fablab.android.view.fragments.inventory;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.Bind;
import de.fau.cs.mad.fablab.android.R;
import de.fau.cs.mad.fablab.android.model.events.NavigationEventInventory;
import de.fau.cs.mad.fablab.android.util.UiUtils;
import de.fau.cs.mad.fablab.android.view.activities.MainActivity;
import de.fau.cs.mad.fablab.android.view.common.binding.EditTextCommandBinding;
import de.fau.cs.mad.fablab.android.view.common.binding.EnterKeyCommandBinding;
import de.fau.cs.mad.fablab.android.view.common.binding.ViewCommandBinding;
import de.fau.cs.mad.fablab.android.view.common.fragments.BaseDialogFragment;
import de.fau.cs.mad.fablab.rest.core.Product;
import de.fau.cs.mad.fablab.rest.core.User;
import de.greenrobot.event.EventBus;

public class AddToInventoryDialogFragment extends BaseDialogFragment implements AddToInventoryDialogFragmentViewModel.Listener {

    @Inject
    AddToInventoryDialogFragmentViewModel mViewModel;

    @Bind(R.id.add_to_inventory_name)
    TextView mNameTextView;
    @Bind(R.id.button_send_inventory)
    Button mButtonSend;
    @Bind(R.id.add_to_inventory_amount)
    EditText mAmountET;

    private EventBus mEventBus = EventBus.getDefault();

    public static AddToInventoryDialogFragment newInstance(Product product, User user) {
        AddToInventoryDialogFragment dialogFragment = new AddToInventoryDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(AddToInventoryDialogFragmentViewModel.KEY_PRODUCT, product);
        bundle.putSerializable(AddToInventoryDialogFragmentViewModel.KEY_USER, user);
        dialogFragment.setArguments(bundle);
        return dialogFragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel.restoreState(getArguments(), savedInstanceState);

        mNameTextView.setText(mViewModel.getName());

        if (mViewModel.isDecimalAmount()) {
            mAmountET.setInputType(InputType.TYPE_CLASS_NUMBER
                    | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        } else {
            mAmountET.setInputType(InputType.TYPE_CLASS_NUMBER);
        }

        mAmountET.requestFocus();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                UiUtils.showKeyboard(getActivity(), mAmountET);
            }
        }, 250);

        new EditTextCommandBinding().bind(mAmountET, mViewModel.getChangeAmountCommand());
        new EnterKeyCommandBinding().bind(mAmountET, mViewModel.getAddToInventoryCommand());

        mViewModel.setListener(this);

        new ViewCommandBinding().bind(mButtonSend, mViewModel.getAddToInventoryCommand());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        return inflater.inflate(R.layout.fragment_add_to_inventory, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        setDisplayOptions(MainActivity.DISPLAY_LOGO);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mViewModel.saveState(outState);
    }

    @Override
    public void onDismiss() {
        UiUtils.hideKeyboard(getActivity());
        getFragmentManager().popBackStack();
        mEventBus.post(new NavigationEventInventory(mViewModel.getUser()));
    }
    @Override
    public String getUUID()
    {
        return Settings.Secure.getString(getActivity().getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

}
