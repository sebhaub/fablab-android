package de.fau.cs.mad.fablab.android.view.fragments.alert;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import de.fau.cs.mad.fablab.android.R;
import de.fau.cs.mad.fablab.android.util.UiUtils;
import de.fau.cs.mad.fablab.android.view.activities.MainActivity;
import de.fau.cs.mad.fablab.android.view.common.binding.ViewCommandBinding;
import de.fau.cs.mad.fablab.android.view.common.fragments.BaseDialogFragment;
import de.fau.cs.mad.fablab.rest.core.FabTool;

public class AlertDialogFragment extends BaseDialogFragment
        implements AlertDialogFragmentViewModel.Listener {

    @Bind(R.id.alert_dialog_tool_spinner)
    Spinner mToolSpinner;
    @Bind(R.id.alert_dialog_edit_text_tools)
    EditText mEditTextTools;
    @Bind(R.id.alert_dialog_edit_text)
    EditText mEditText;
    @Bind(R.id.alert_dialog_ok_button)
    Button mOKButton;

    @Inject
    AlertDialogFragmentViewModel mViewModel;

    private boolean mBoolSpinner = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        return inflater.inflate(R.layout.alert_dialog, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel.setListener(this);
        mEditText.setText("");

        //check if tools are available
        List<FabTool> mTools = mViewModel.getTools();
        if (mTools.isEmpty()) {
            mBoolSpinner = false;
            mEditTextTools.setVisibility(View.VISIBLE);
        } else {
            //set spinner items
            List<String> mToolNames = new ArrayList<>();
            for (FabTool f : mTools) {
                mToolNames.add(f.getTitle());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                    R.layout.spinner_item, mToolNames);
            mToolSpinner.setAdapter(adapter);
            mToolSpinner.setVisibility(View.VISIBLE);
        }

        new ViewCommandBinding().bind(mOKButton, mViewModel.getOKCommand());
    }

    @Override
    public void onResume() {
        super.onResume();
        setDisplayOptions(MainActivity.DISPLAY_LOGO | MainActivity.DISPLAY_NAVDRAWER);
        setNavigationDrawerSelection(R.id.drawer_item_alert);
    }

    @Override
    public void onOK() {
        String text;
        if (mBoolSpinner) {
            text = mToolSpinner.getSelectedItem().toString();
        } else {
            text = mEditTextTools.getText().toString();
        }

        Intent sendIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", mViewModel.getMailAddress(), null));
        String subject = getActivity().getString(R.string.alert_messaging_subject);
        String body = "tool name: \n" + text + "\n";
        body += "problem: \n" + mEditText.getText() + "\n";

        sendIntent.putExtra(Intent.EXTRA_TEXT, body);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject);

        getActivity().startActivity(Intent.createChooser(sendIntent, getActivity().getString(
                R.string.alert_messaging_chooser_title)));

        dismiss();
        UiUtils.hideKeyboard(getActivity());
        getFragmentManager().popBackStack();
    }
}
