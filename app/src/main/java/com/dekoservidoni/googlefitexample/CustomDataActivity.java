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
import com.google.android.gms.fitness.request.DataTypeCreateRequest;
import com.google.android.gms.fitness.result.DataReadResult;
import com.google.android.gms.fitness.result.DataTypeResult;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Activity responsible to create custom data on google fit
 *
 * Created by DeKo on 05/11/2015.
 */
public class CustomDataActivity extends AppCompatActivity implements GoogleFitManager.GoogleFitCallback {

    /** Log tag */
    private static final String TAG = CustomDataActivity.class.getSimpleName();

    /** Google fit client */
    private GoogleApiClient mClient = null;

    /** Google fit manager instance */
    private GoogleFitManager mManager = null;

    /** UI Component */
    private TextView mLogView = null;

    /** Connection flag */
    private boolean mIsConnected = false;

    /** Constant values */
    private static final String CUSTOM_DATA_TYPE = "com.dekoservidoni.gfitpoc.CUSTOM";
    private static final String CUSTOM_FIELD_VALUE = "value";

    /** Data type custom */
    private DataType mCustomType = null;

    /** Sample value increment */
    private static int mIncrement = 1;

    @Override
    protected void onStart() {
        super.onStart();
        // Connect to the Fitness API
        if(mClient != null) {
            mClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mClient != null && mClient.isConnected()) {
            mClient.disconnect();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_custom);

        mManager = new GoogleFitManager(this, this);

        mLogView = (TextView) findViewById(R.id.view_custom_information);
        mLogView.setMovementMethod(new ScrollingMovementMethod());

        Button clean = (Button) findViewById(R.id.clean_custom_button);
        clean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLogView.setText("");
            }
        });

        Button readRequest = (Button) findViewById(R.id.read_custom_button);
        readRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsConnected) {
                    read(mCustomType);
                }
            }
        });

        Button insertRequest = (Button) findViewById(R.id.insert_custom_button);
        insertRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsConnected) {
                    insert();
                }
            }
        });

        if (savedInstanceState != null) {
            mManager.setAuthInProgress(savedInstanceState.getBoolean(GoogleFitManager.AUTH_PENDING));
        }

        mClient = mManager.buildHistoryClient();
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(GoogleFitManager.AUTH_PENDING, mManager.getAuthInProgress());
    }

    /***********************************************/
    /**             CALLBACK METHODS              **/
    /***********************************************/

    @Override
    public void connected(Bundle bundle) {
        Utils.printMessage(mLogView, "Connected!");
        mIsConnected = true;

        Fitness.ConfigApi.readDataType(mClient, CUSTOM_DATA_TYPE)
                .setResultCallback(new ResultCallback<DataTypeResult>() {
                    @Override
                    public void onResult(DataTypeResult dataTypeResult) {

                        if (dataTypeResult.getDataType() != null) {
                            Utils.printMessage(mLogView, "Custom datatype found! [" + dataTypeResult.getDataType().getName() + "]");
                            mCustomType = dataTypeResult.getDataType();

                        } else {
                            Utils.printMessage(mLogView, "Custom datatype not found, let's create it...");

                            DataTypeCreateRequest request = new DataTypeCreateRequest.Builder()
                                    // The prefix of your data type name must match your app's package name
                                    .setName(CUSTOM_DATA_TYPE)
                                    // Add some custom fields, both int and float
                                    .addField(CUSTOM_FIELD_VALUE, Field.FORMAT_INT32)
                                    .build();

                            Fitness.ConfigApi.createCustomDataType(mClient, request).setResultCallback(new ResultCallback<DataTypeResult>() {
                                @Override
                                public void onResult(DataTypeResult dataTypeResult) {
                                    Utils.printMessage(mLogView, "created a custom data type with name: " + dataTypeResult.getDataType().getName());
                                    mCustomType = dataTypeResult.getDataType();
                                }
                            });

                        }
            }
        });
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
     * Insert the custom data on google fit
     */
    private void insert() {

        Calendar calendar2 = Calendar.getInstance();
        calendar2.set(Calendar.DAY_OF_MONTH, calendar2.get(Calendar.DAY_OF_MONTH) - 1);
        long end = calendar2.getTimeInMillis();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) - 2);
        long start = calendar.getTimeInMillis();

        // create the data source to insert (steps in this case)
        DataSource dataSource = new DataSource.Builder()
                .setAppPackageName(this)
                .setDataType(mCustomType)
                .setName("My Type")
                .setType(DataSource.TYPE_RAW)
                .build();

        // Set values for the data point
        // This data type has two custom fields (int, float)
        DataPoint dataPoint = DataPoint.create(dataSource);
        dataPoint.getValue(mCustomType.getFields().get(0)).setInt(1+mIncrement);
        dataPoint.setTimeInterval(start, end, TimeUnit.MILLISECONDS);

        // create the data set from the data source
        final DataSet dataSet = DataSet.create(dataSource);
        dataSet.add(dataPoint);

        Fitness.HistoryApi.insertData(mClient, dataSet).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {

                if(status.isSuccess()) {
                    Utils.printMessage(mLogView, "Custom data insert with success! [" + dataSet.getDataSource().getName() + "]");
                    mIncrement++;
                } else {
                    Utils.printMessage(mLogView, "Failed to insert custom data: [" + dataSet.getDataSource().getName() + "]");
                }
            }
        });
    }

    /**
     * Read a custom data type from HistoryAPI
     */
    private void read(DataType dataType) {

        // set the start and end of the period
        long end = Calendar.getInstance().getTimeInMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1);
        long start = calendar.getTimeInMillis();

        // create the read request
        DataReadRequest readRequest = new DataReadRequest.Builder()
                .read(dataType)
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
