package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.util.Log;

import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

  private static String LOG_TAG = Utils.class.getSimpleName();

  public static boolean showPercent = true;
  private static final String QUERY_KEY = "query";
  private static final String COUNT_KEY = "count";
  private static final String RESULTS_KEY = "results";
  private static final String QUOTE_KEY = "quote";
  private static final String BID_KEY = "Bid";
  private static final String CHANGE_KEY = "Change";
  private static final String SYMBOL_KEY = "Symbol";
  private static final String CHANGEINPERCENT_KEY = "ChangeinPercent";

  public static ArrayList quoteJsonToContentVals(String JSON){
    Log.d(LOG_TAG,"JSON: "+ JSON);
    ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
    JSONObject jsonObject = null;
    JSONArray resultsArray = null;
    try{
      jsonObject = new JSONObject(JSON);
      if (jsonObject != null && jsonObject.length() != 0){
        jsonObject = jsonObject.getJSONObject(QUERY_KEY);
        int count = Integer.parseInt(jsonObject.getString(COUNT_KEY));
        if (count == 1){
          jsonObject = jsonObject.getJSONObject(RESULTS_KEY)
              .getJSONObject(QUOTE_KEY);
          ContentProviderOperation checkObj = buildBatchOperation(jsonObject);
          if (checkObj != null)
              batchOperations.add(buildBatchOperation(jsonObject));
        } else{
          resultsArray = jsonObject.getJSONObject(RESULTS_KEY).getJSONArray(QUOTE_KEY);

          if (resultsArray != null && resultsArray.length() != 0){
            for (int i = 0; i < resultsArray.length(); i++){
              jsonObject = resultsArray.getJSONObject(i);
              batchOperations.add(buildBatchOperation(jsonObject));
            }
          }
        }
      }
    } catch (JSONException e){
      Log.e(LOG_TAG, "String to JSON failed: " + e);
    }
    return batchOperations;
  }

  public static String truncateBidPrice(String bidPrice){
    bidPrice = String.format("%.2f", Float.parseFloat(bidPrice));
    return bidPrice;
  }

  public static String truncateChange(String change, boolean isPercentChange){
    String weight = change.substring(0,1);
    String ampersand = "";
    if (isPercentChange){
      ampersand = change.substring(change.length() - 1, change.length());
      change = change.substring(0, change.length() - 1);
    }
    change = change.substring(1, change.length());
    double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
    change = String.format("%.2f", round);
    StringBuffer changeBuffer = new StringBuffer(change);
    changeBuffer.insert(0, weight);
    changeBuffer.append(ampersand);
    change = changeBuffer.toString();
    return change;
  }

  public static ContentProviderOperation buildBatchOperation(JSONObject jsonObject) {
    ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
            QuoteProvider.Quotes.CONTENT_URI);
    String bid = null;
    String change = null;
    try {
      bid = jsonObject.getString(BID_KEY);
    } catch (JSONException e) {
      e.printStackTrace();
    }

    try {
      change = jsonObject.getString(CHANGE_KEY);
    } catch (JSONException e) {
      e.printStackTrace();
    }

    //if ((change != null && change != "null") {
    if (bid!=null && bid!="null"){
      try {

        Log.d(LOG_TAG, "Inside change:"+bid);
        builder.withValue(QuoteColumns.SYMBOL, jsonObject.getString(SYMBOL_KEY));
        builder.withValue(QuoteColumns.BIDPRICE, truncateBidPrice(jsonObject.getString(BID_KEY)));
        builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(
                jsonObject.getString(CHANGEINPERCENT_KEY), true));
        builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
        builder.withValue(QuoteColumns.ISCURRENT, 1);
        if (change.charAt(0) == '-') {
          builder.withValue(QuoteColumns.ISUP, 0);
        } else {
          builder.withValue(QuoteColumns.ISUP, 1);
        }

      } catch (JSONException e) {
        e.printStackTrace();
      }
      return builder.build();
    }
    return null;
  }
}
