package com.thebluealliance.androidclient.listitems;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.thebluealliance.androidclient.R;

/**
 * Created by Nathan on 5/19/2015.
 */
public class SpacerListItem implements ListItem {
    @Override
    public int getViewType() {
        return 0;
    }

    @Override
    public View getView(Context c, LayoutInflater inflater, View convertView) {
        return inflater.inflate(R.layout.list_item_spacer, null, false);
    }
}
