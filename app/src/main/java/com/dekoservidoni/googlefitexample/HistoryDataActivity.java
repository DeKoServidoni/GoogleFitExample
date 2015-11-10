package com.dekoservidoni.googlefitexample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dekoservidoni.googlefitexample.gfit.GoogleFitManager;
import com.dekoservidoni.googlefitexample.utils.Utils;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Activity responsible to read and insert data in the google fit using the HistoryAPI
 *
 * Created by DeKo on 05/11/2015.
 */
public class HistoryDataActivity extends AppCompatActivity implements GoogleFitManager.GoogleFitCallback {

    /** Log tag */
    private static final String TAG = HistoryDataActivity.class.getSimpleName();

    /** UI Components */
    private TextView mLogView = null;

    /** Google fit client */
    private GoogleApiClient mClient = null;

    /** Google fit manager instance */
    private GoogleFitManager mManager = null;

    /** Connection flag */
    private boolean mIsConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_history);

        mManager = new GoogleFitManager(this, this);
        mLogView = (TextView) findViewById(R.id.read_request_text_view);
        mLogView.setMovementMethod(new ScrollingMovementMethod());
        mLogView.requestFocus();

        Button clean = (Button) findViewById(R.id.clean_history_button);
        clean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLogView.setText("");
            }
        });

        Button readRequest = (Button) findViewById(R.id.read_request_button);
        readRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsConnected) {
                    // connection and auth is OK so let´s request the
                    // operation from the desired API
                    requestHistory();
                }
            }
        });

        Button insertRequest = (Button) findViewById(R.id.insert_request_button);
        insertRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsConnected) {
                    insertData();
                }
            }
        });

        if (savedInstanceState != null) {
            mManager.setAuthInProgress(savedInstanceState.getBoolean(GoogleFitManager.AUTH_PENDING));
        }

        mClient = mManager.buildHistoryClient();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(mClient != null) {
            mClient.connect();

            Fitness.RecordingApi.subscribe(mClient, DataType.TYPE_STEP_COUNT_DELTA)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            Utils.printMessage(mLogView, "Recording subscribed: " + status.isSuccess());
                        }
                    });
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mClient != null && mClient.isConnected()) {

            Fitness.RecordingApi.unsubscribe(mClient, DataType.TYPE_STEP_COUNT_DELTA)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            Utils.printMessage(mLogView, "Recording unsubscribed: " + status.isSuccess());
                        }
                    });

            mClient.disconnect();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(GoogleFitManager.AUTH_PENDING, mManager.getAuthInProgress());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GoogleFitManager.REQUEST_OAUTH) {
            mManager.setAuthInProgress(false);

            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!mClient.isConnecting() && !mClient.isConnected()) {
                    mClient.connect();
                }
            }
        }
    }

    /***********************************************/
    /**             CALLBACK METHODS              **/
    /***********************************************/

    @Override
    public void connected(Bundle bundle) {
        Utils.printMessage(mLogView, "Connected!");
        mIsConnected = true;
    }

    @Override
    public void connectionSuspended(int i) {
        // do something when the connection is suspended
        Utils.printMessage(mLogView, "Connection suspended!");
        mIsConnected = false;
    }

    /***********************************************/
    /**              PRIVATE METHODS              **/
    /***********************************************/

    /**
     * Insert data into Google Fit using HistoryAPI
     */
    private void insertData() {
        // create the data source to insert (steps in this case)
        DataSource dataSource = new DataSource.Builder()
                .setAppPackageName(this)
                .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .setName(TAG + " - step count")
                .setType(DataSource.TYPE_RAW)
                .build();

        // create the data set from the data source
        final DataSet dataSet = DataSet.create(dataSource);

        // For each data point, specify a start time, end time, and the data value -- in this case,
        // the number of new steps.
        Calendar cal2 = Calendar.getInstance();
        cal2.add(Calendar.DAY_OF_MONTH, -2);
        long endTime =  cal2.getTimeInMillis();

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -2);
        cal.add(Calendar.HOUR_OF_DAY, -1);
        long startTime = cal.getTimeInMillis();

        int steps = 100;

        DataPoint dataPoint = dataSet.createDataPoint();
        dataPoint.setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS);
        dataPoint.getValue(Field.FIELD_STEPS).setInt(steps);

        // add the datapoint to the data set
        dataSet.add(dataPoint);

        // create the result callback
        ResultCallback<Status> callback = new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {

                if(!status.isSuccess()) {
                    Utils.printMessage(mLogView, "Failed to insert DataSet: [" + dataSet.getDataSource().getName() + "]");
                } else {
                    Utils.printMessage(mLogView, "DataSet insert with success! [" + dataSet.getDataSource().getName() + "]");
                }
            }
        };

        // send the insert request to the HistoryAPI
        Fitness.HistoryApi.insertData(mClient, dataSet).setResultCallback(callback);
    }

    /**
     * Request a read from HistoryAPI
     */
    private void requestHistory() {

        // set the start and end of the period
        long end = Calendar.getInstance().getTimeInMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1);
        long start = calendar.getTimeInMillis();

        // create the read request
        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(5, TimeUnit.DAYS)
                //.read(DataType.TYPE_STEP_COUNT_DELTA)
                .setTimeRange(start, end, TimeUnit.MILLISECONDS)
                .enableServerQueries()
                .build();

        // set the callback to parse the result when it arrived
        ResultCallback<DataReadResult> callback = new ResultCallback<DataReadResult>() {
            @Override
            public void onResult(DataReadResult dataReadResult) {

                if (dataReadResult.getBuckets().size() > 0) {
                    Log.i(TAG, "Buckets size(): " + dataReadResult.getBuckets().size());
                    for (Bucket bucket : dataReadResult.getBuckets()) {
                        List<DataSet> dataSets = bucket.getDataSets();

                        for (DataSet dataSet : dataSets) {
                            Log.i(TAG, "dataSet.dataType: " + dataSet.getDataType().getName() +" [size: "+dataSets.size()+"]");

                            for (DataPoint dp : dataSet.getDataPoints()) {
                                String msg = Utils.describeDataPoint(dp);
                                Utils.printMessage(mLogView, msg);
                            }
                        }
                    }
                } else if (dataReadResult.getDataSets().size() > 0) {
                    Log.i(TAG, "dataSet.size(): " + dataReadResult.getDataSets().size());
                    for (DataSet dataSet : dataReadResult.getDataSets()) {
                        Log.i(TAG, "dataType: " + dataSet.getDataType().getName());

                        for (DataPoint dp : dataSet.getDataPoints()) {
                            String msg = Utils.describeDataPoint(dp);
                            Utils.printMessage(mLogView, msg);
                        }
                    }
                }
            }
        };

        // call the HistoryAPI and request a read
        Fitness.HistoryApi.readData(mClient, readRequest).setResultCallback(callback);
    }
}
