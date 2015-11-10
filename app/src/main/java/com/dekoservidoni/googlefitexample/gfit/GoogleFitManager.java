package com.dekoservidoni.googlefitexample.gfit;

import android.app.Activity;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;

/**
 * Manager responsible to group the API calls to make easy
 * the development and integration
 *
 * Created by DeKo on 05/11/2015.
 */
public class GoogleFitManager {

    /** Log tag */
    private static final String TAG = GoogleFitManager.class.getSimpleName();

    /** Request authentication constant */
    public static final int REQUEST_OAUTH = 1;

    /**
     *  Track whether an authorization activity is stacking over the current activity, i.e. when
     *  a known auth error is being resolved, such as showing the account chooser or presenting a
     *  consent dialog. This avoids common duplications as might happen on screen rotations, etc.
     */
    public static final String AUTH_PENDING = "auth_state_pending";

    /** Application activity */
    private Activity mActivity = null;

    /** Google fit callback */
    private GoogleFitCallback mCallback = null;

    /** Authentication progress flag */
    private boolean mAuthInProgress = false;

    /**
     * Callback responsible to communicate with the callers
     */
    public interface GoogleFitCallback {
        void connected(Bundle bundle);
        void connectionSuspended(int i);
    }

    /**
     * Constructor
     *
     * @param activity
     *          Application activity
     * @param callback
     *          Reference callback
     */
    public GoogleFitManager(Activity activity, GoogleFitCallback callback) {
        mActivity = activity;
        mCallback = callback;
    }

    /**
     * Build the SENSOR API client of google fit
     *
     * @return google api client object
     */
    public GoogleApiClient buildSensorClient() {
        return buildClient(Fitness.SENSORS_API, new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE));
    }

    /**
     * Build the HISTORY API client of google fit
     *
     * @return google api client object
     */
    public GoogleApiClient buildHistoryClient() {
        return buildClient(Fitness.HISTORY_API, new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE));
    }

    /**
     * Build the RECORDING API client of google fit
     *
     * @return google api client object
     */
    public GoogleApiClient buildRecordClient() {
        return buildClient(Fitness.RECORDING_API, new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE));
    }

    /**
     * Set the auth in progress flag
     *
     * @param authInProgress
     *          Flag value, true or false
     */
    public void setAuthInProgress(boolean authInProgress) {
        mAuthInProgress = authInProgress;
    }

    /**
     * Get the auth in progress flag
     *
     * @return true or false
     */
    public boolean getAuthInProgress() {
        return mAuthInProgress;
    }

    /**
     * Build the client of google fit
     *
     * @param api
     *          API requested to connect
     * @param scope
     *          API scopes of operation
     *
     * @return google api client object
     */
    private GoogleApiClient buildClient(Api<Api.ApiOptions.NoOptions> api, Scope scope) {
        return new GoogleApiClient.Builder(mActivity)
                .addApi(api)
                .addApi(Fitness.CONFIG_API)
                .addApi(Fitness.RECORDING_API)
                .addScope(scope)
                .addConnectionCallbacks(mConnectionCallbacks)
                .addOnConnectionFailedListener(mFailureListener).build();
    }

    /**
     * Google connection callback
     */
    private GoogleApiClient.ConnectionCallbacks mConnectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {

        @Override
        public void onConnected(Bundle bundle) {
            Log.e(TAG, "onConnected");

            if(mCallback != null) {
                mCallback.connected(bundle);
            }
        }

        @Override
        public void onConnectionSuspended(int i) {
            Log.e(TAG, "onConnectionSuspended");

            if(mCallback != null) {
                mCallback.connectionSuspended(i);
            }
        }
    };

    /**
     * Failure handler listener
     */
    private GoogleApiClient.OnConnectionFailedListener mFailureListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(ConnectionResult result) {
            Log.w(TAG, "onConnectionFailed");

            if (!result.hasResolution()) {
                // Show the localized error dialog
                GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), mActivity, 0).show();
                return;
            }
            // The failure has a resolution. Resolve it.
            // Called typically when the app is not yet authorized, and an
            // authorization dialog is displayed to the user.
            if (!mAuthInProgress) {
                try {
                    Log.d(TAG, "Attempting to resolve failed connection");
                    mAuthInProgress = true;
                    result.startResolutionForResult(mActivity, REQUEST_OAUTH);

                } catch (IntentSender.SendIntentException e) {
                    Log.e(TAG, " Exception while starting resolution activity", e);
                }
            }
        }
    };
}
