package com.sam_chordas.android.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

/**
 * Created by firej on 7/10/2016.
 */
public class QuoteWidgetRVS extends RemoteViewsService{
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new QuoteRemoteVF(this.getApplicationContext(), intent);
    }
}

class QuoteRemoteVF implements RemoteViewsService.RemoteViewsFactory {

    private static final String TAG = "QuoteRemoteVF";

    private Context mContext;
    private int mAppWidgetId;
    private Cursor mCursor;

    public QuoteRemoteVF (Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        // Refresh the cursor
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                new String[]{QuoteColumns._ID,
                        QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                QuoteColumns.ISCURRENT + " = ?",
                new String[]{"1"},
                null);
    }

    @Override
    public void onDestroy() {
        if (mCursor != null) {
            mCursor.close();
        }
    }

    @Override
    public int getCount() {
        return mCursor.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        // Get the data for this position from the content provider
        String id = null;
        String symbol = null;
        String bidPrice = null;
        String change = null;
        int isUp = 1;
        if (mCursor.moveToPosition(position)) {
            id = mCursor.getString(mCursor.getColumnIndex(QuoteColumns._ID));
            symbol = mCursor.getString(mCursor.getColumnIndex(QuoteColumns.SYMBOL));
            bidPrice = mCursor.getString(mCursor.getColumnIndex(QuoteColumns.BIDPRICE));
            change = mCursor.getString(mCursor.getColumnIndex(QuoteColumns.PERCENT_CHANGE));
            isUp = mCursor.getInt(mCursor.getColumnIndex(QuoteColumns.ISUP));
        }

        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_list_item_quote);
        rv.setTextViewText(R.id.stock_symbol, symbol);
        rv.setTextViewText(R.id.bid_price, bidPrice);
        rv.setTextViewText(R.id.change, change);
        if (isUp == 1) {
            rv.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
        } else {
            rv.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
        }

        // Set the click intent
        final Intent in = new Intent();
        Log.d(TAG, "getViewAt: " + id);
        in.putExtra("_id", id);
        in.putExtra("_symbol", symbol);
        //idk if the id is correct
        rv.setOnClickFillInIntent(R.id.listView_quotes, in);
        return rv;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}