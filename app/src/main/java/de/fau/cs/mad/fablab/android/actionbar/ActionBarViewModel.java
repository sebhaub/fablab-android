package de.fau.cs.mad.fablab.android.actionbar;

import javax.inject.Inject;

import de.fau.cs.mad.fablab.android.eventbus.DoorEvent;
import de.fau.cs.mad.fablab.android.viewmodel.common.BaseViewModel;
import de.fau.cs.mad.fablab.android.viewmodel.common.commands.Command;
import de.greenrobot.event.EventBus;

public class ActionBarViewModel extends BaseViewModel {

    Listener listener;
    ActionBarModel model;
    EventBus mEventBus;

    private boolean opened;
    private ActionBarTime time;

    Command<Integer> refreshOpenedStateCommand = new Command<Integer>() {
        @Override
        public void execute(Integer parameter) {
            mEventBus.post(DoorEvent.GET);
            //listener.onActionBarItemSelected();
        }
    };

    @Inject
    public ActionBarViewModel() {
        this.model = new ActionBarModel();
        this.mEventBus = EventBus.getDefault();
    }

    public void setListener(Listener listener){
        this.listener = listener;
    }

    public Command<Integer> getRefreshOpenedStateCommand () {
        return refreshOpenedStateCommand;
    }

    public interface Listener extends BaseViewModel.Listener {
        //void onActionBarItemSelected();
    }
}
