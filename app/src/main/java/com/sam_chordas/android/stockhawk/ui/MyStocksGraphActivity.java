package com.sam_chordas.android.stockhawk.ui;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

/**
 * Created by rmenezes on 4/1/2016.
 */
public class MyStocksGraphActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    GraphView graph;
    private static final int CURSOR_ID= 0;
    Cursor mCursor;
    String symbolName; //Symbol string for args and graph title

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);
        graph = (GraphView) findViewById(R.id.linechart);
        Bundle args = new Bundle();
        Intent getGraphIntent = getIntent();
        symbolName = getGraphIntent.getStringExtra(getResources().getString(R.string.string_symbol));
        args.putString(getResources().getString(R.string.string_symbol),
                symbolName);
        getLoaderManager().initLoader(CURSOR_ID,args,this);

    }

    @Override public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, QuoteProvider.Quotes.CONTENT_URI,
                new String[]{ QuoteColumns.BIDPRICE},
                QuoteColumns.SYMBOL + " = ?",
                new String[]{args.getString(getResources().getString(R.string.string_symbol))},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    mCursor = data;
       
        DataPoint[] datapoints = new DataPoint[mCursor.getCount()];
        mCursor.moveToFirst();

        for (int i = 0; i < mCursor.getCount(); i++) {
            float bidPrice = Float.parseFloat(mCursor.getString(mCursor.getColumnIndex(QuoteColumns.BIDPRICE)));
            datapoints[i]= new DataPoint(i,bidPrice);
        mCursor.moveToNext();
        }
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(datapoints);
        series.setDrawDataPoints(true);
        series.setDataPointsRadius(10);
        series.setThickness(1);

        graph.setTitle(symbolName);
        graph.addSeries(series);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}
}
