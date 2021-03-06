package de.fau.cs.mad.fablab.android.view.common.binding;

import android.view.MenuItem;

import de.fau.cs.mad.fablab.android.viewmodel.common.commands.Command;
import de.fau.cs.mad.fablab.android.viewmodel.common.commands.CommandListener;

public class MenuItemCommandBinding implements Binding<MenuItem, Void>, CommandListener,
        MenuItem.OnMenuItemClickListener {
    private Command<Void> mCommand;
    private MenuItem mMenuItem;

    @Override
    public void bind(MenuItem menuItem, Command<Void> command) {
        mCommand = command;
        mMenuItem = menuItem;

        mCommand.setListener(this);
        mMenuItem.setOnMenuItemClickListener(this);
    }

    @Override
    public void onIsAvailableChanged(boolean newValue) {
        mMenuItem.setVisible(newValue);
    }

    @Override
    public void onIsExecutableChanged(boolean newValue) {
        mMenuItem.setEnabled(newValue);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (mCommand.isExecutable()) {
            mCommand.execute(null);
        }
        return true;
    }
}
