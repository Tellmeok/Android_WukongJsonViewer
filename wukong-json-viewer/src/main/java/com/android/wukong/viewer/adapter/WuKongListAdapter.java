package com.android.wukong.viewer.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.android.wukong.viewer.activity.WuKongListActivity;

import java.util.ArrayList;
import java.util.List;

public class WuKongListAdapter extends BaseAdapter implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {


    // be attention for nested
    public static List<Event> eventsStack = new ArrayList<>();


    // push
    public static void pushEvent(Event event) {
        eventsStack.add(event);
    }

    // pop
    public static Event popEvent() {
        return !eventsStack.isEmpty() ? eventsStack.remove(eventsStack.size() - 1) : null;
    }


    private Event event = popEvent();


    public LayoutInflater inflater;

    private Activity mActivity;

    public Activity getOwnerActivity() {
        return this.mActivity;
    }

    public void setOwnerActivity(Activity activity) {
        this.mActivity = activity;
    }

    private ListView mListView;

    public ListView getOwnerListView() {
        return this.mListView;
    }

    public void setOwnerListView(ListView listView) {
        this.mListView = listView;
    }

    // datasource
    @Override
    public int getCount() {
        Event event = getEvent();
        if (event != null) {
            return event.onGetCount(this);
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Event event = getEvent();
        if (event != null) {
            return event.onGetView(this, position, convertView, parent);
        }
        return convertView;
    }

    // event
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Event event = getEvent();
        if (event != null) {
            event.onCellClicked(this, parent, view, position, id);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Event event = getEvent();
        if (event != null) {
            Boolean result = event.onCellLongClicked(this, parent, view, position, id);
            if (result != null) {
                return result;
            }
        }
        return false;   // default false not pass the event to the following handler
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event e) {
        event = e;
    }

    public static abstract class Event {

        public Event nestedEvent = null;

        public Object[] objects = null;

        public Event() {
        }

        public Event(Event nestedEvent) {
            this.nestedEvent = nestedEvent;
        }

        // !!! important, all method is empty implements !!!

        // in activity
        public void onCreate(WuKongListActivity activity) {
            if (nestedEvent != null) {
                nestedEvent.onCreate(activity);
            }
        }

        public void onResume(WuKongListActivity activity) {
            if (nestedEvent != null) {
                nestedEvent.onResume(activity);
            }
        }

        public void onStop(WuKongListActivity activity) {
            if (nestedEvent != null) {
                nestedEvent.onStop(activity);
            }
        }

        public void onDestroy(WuKongListActivity activity) {
            if (nestedEvent != null) {
                nestedEvent.onDestroy(activity);
            }
        }

        public Boolean onCreateMenu(WuKongListActivity activity, Menu menu) {
            Boolean result = null;
            if (nestedEvent != null) {
                result = nestedEvent.onCreateMenu(activity, menu);
            }
            return result;
        }

        public Boolean onMenuItemSelected(WuKongListActivity activity, MenuItem item) {
            Boolean result = null;
            if (nestedEvent != null) {
                result = nestedEvent.onMenuItemSelected(activity, item);
            }
            return result;
        }

        public Boolean onBackPressed(WuKongListActivity activity) {
            Boolean result = null;
            if (nestedEvent != null) {
                result = nestedEvent.onBackPressed(activity);
            }
            return result;
        }

        // in adapter
        public int onGetCount(WuKongListAdapter adapter) {
            int defCount = 0;
            if (nestedEvent != null) {
                defCount = nestedEvent.onGetCount(adapter);
            }
            return defCount;
        }

        public View onGetView(WuKongListAdapter adapter, int position, View convertView, ViewGroup parent) {
            if (nestedEvent != null) {
                convertView = nestedEvent.onGetView(adapter, position, convertView, parent);
            }
            return convertView;
        }

        public void onCellClicked(WuKongListAdapter adapter, AdapterView<?> parent, View view, int position, long id) {
            if (nestedEvent != null) {
                nestedEvent.onCellClicked(adapter, parent, view, position, id);
            }
        }

        public Boolean onCellLongClicked(WuKongListAdapter adapter, AdapterView<?> parent, View view, int position, long id) {
            Boolean isPassedEvent = null;  // default false not pass the event to the following handler
            if (nestedEvent != null) {
                isPassedEvent = nestedEvent.onCellLongClicked(adapter, parent, view, position, id);
            }
            return isPassedEvent;
        }

    }

}

