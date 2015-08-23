package de.fau.cs.mad.fablab.android.view.fragments.inventory;

import javax.inject.Inject;

import de.fau.cs.mad.fablab.android.model.InventoryModel;
import de.fau.cs.mad.fablab.android.model.events.InventoryDeletedEvent;
import de.fau.cs.mad.fablab.android.model.events.InventoryNotDeletedEvent;
import de.fau.cs.mad.fablab.android.model.events.NavigationEventBarcodeScannerInventory;
import de.fau.cs.mad.fablab.android.model.events.NavigationEventProductSearchInventory;
import de.fau.cs.mad.fablab.android.model.events.NavigationEventShowInventory;
import de.fau.cs.mad.fablab.android.viewmodel.common.commands.Command;
import de.fau.cs.mad.fablab.rest.core.User;
import de.greenrobot.event.EventBus;

public class InventoryFragmentViewModel {

    private Listener mListener;
    private User mUser;
    private InventoryModel mModel;
    EventBus mEventBus = EventBus.getDefault();
    boolean deleteResult = false;

    private Command<Void> mOnScanButtonClickedCommand = new Command<Void>()
    {
        @Override
        public void execute(Void parameter) {
            mEventBus.post(new NavigationEventBarcodeScannerInventory(getUser()));
        }
    };

    private Command<Void> mOnSearchButtonClickedCommand = new Command<Void>()
    {
        @Override
        public void execute(Void parameter) {
            mEventBus.post(new NavigationEventProductSearchInventory(getUser()));
        }
    };

    private Command<Void> mOnDeleteButtonClickedCommand = new Command<Void>()
    {
        @Override
        public void execute(Void parameter) {
            deleteResult = false;
            mModel.deleteInventory(mUser.getUsername(), mUser.getPassword());
        }
    };

    private Command<Void> mOnShowInventoryClickedCommand = new Command<Void>()
    {
        @Override
        public void execute(Void parameter) {
            mEventBus.post(new NavigationEventShowInventory(getUser()));
        }
    };

    @Inject
    public InventoryFragmentViewModel(InventoryModel model)
    {
        mModel = model;
        mEventBus.register(this);
    }

    public void setListener(Listener listener)
    {
        mListener = listener;
    }

    public void setUser(User user)
    {
        mUser = user;
    }

    public User getUser()
    {
        return mUser;
    }

    public Command<Void> getOnScanButtonClickedCommand()
    {
        return mOnScanButtonClickedCommand;
    }

    public Command<Void> getOnSearchButtonClickedCommand()
    {
        return mOnSearchButtonClickedCommand;
    }

    public Command<Void> getOnDeleteButtonClickedCommand()
    {
        return mOnDeleteButtonClickedCommand;
    }

    public Command<Void> getOnShowInventoryClickedCommand()
    {
        return mOnShowInventoryClickedCommand;
    }

    public void onEvent(InventoryDeletedEvent event)
    {
        if(mListener != null && !deleteResult)
        {
            deleteResult = true;
            mListener.deletedSuccess();
        }
    }

    public void onEvent(InventoryNotDeletedEvent event)
    {
        if(mListener != null && !deleteResult)
        {
            deleteResult = true;
            mListener.deletedFail();
        }
    }

    public interface Listener
    {
        void deletedSuccess();

        void deletedFail();
    }
}
