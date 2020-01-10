
package com.deploygate.sample;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.deploygate.sdk.DeployGate;
import com.deploygate.sdk.DeployGateCallback;

public class SampleActivity extends Activity
        implements DeployGateCallback {

    private static final String TAG = "SampleActivity";

    private TextView mAvailableText;
    private TextView mManagedText;
    private TextView mAuthorizedText;
    private TextView mTitleText;
    private EditText mLogMessage;
    private Button mCrashButton;
    private Button mLogCatButton;
    private Button mUpdateButton;
    private LinearLayout mDistributionComments;

    private static final int[] sLogButtonIds = new int[] {
        R.id.logError, R.id.logWarn, R.id.logDebug, R.id.logInfo, R.id.logVerbose
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash_me);

        mAvailableText = (TextView) findViewById(R.id.available);
        mManagedText = (TextView) findViewById(R.id.managed);
        mAuthorizedText = (TextView) findViewById(R.id.authorized);
        mTitleText = (TextView) findViewById(R.id.title);
        mCrashButton = (Button) findViewById(R.id.button);
        mLogCatButton = (Button) findViewById(R.id.logcat);
        mUpdateButton = (Button) findViewById(R.id.updateButton);
        mLogMessage = (EditText) findViewById(R.id.message);
        mDistributionComments = (LinearLayout) findViewById(R.id.distributionComments);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // register for callback, also request refreshing (second argument)
        DeployGate.registerCallback(this, true);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // unregister to stop callback
        DeployGate.unregisterCallback(this);
    }


    /**
     * Called when the log buttons clicked. Each button has ID that can be used
     * to change log level.
     * 
     * @param v View instance of the button
     */
    public void onLogClick(View v) {
        String text = mLogMessage.getText().toString();
        switch (v.getId()) {
            case R.id.logError:
                DeployGate.logError(text);
                break;
            case R.id.logWarn:
                DeployGate.logWarn(text);
                break;
            case R.id.logDebug:
                DeployGate.logDebug(text);
                break;
            case R.id.logInfo:
                DeployGate.logInfo(text);
                break;
            case R.id.logVerbose:
                DeployGate.logVerbose(text);
                break;
            default:
                return;
        }
    }

    /**
     * Called when the crash button clicked
     * 
     * @param v View instance of the button
     */
    public void onCrashMeClick(View v) {
        // let's throw!
        throw new RuntimeException("CRASH TEST BUTTON CLICKED YAY!");
    }

    /**
     * Called when Capture and send LogCat button clicked
     *
     * @param v View instance of the button
     */
    public void onLogCatClick(View v) {
        DeployGate.requestLogCat();
        Toast.makeText(this, R.string.logcat_toast, Toast.LENGTH_LONG).show();
    }

    /**
     * Called when Capture and send LogCat button clicked
     *
     * @param v View instance of the button
     */
    public void onUpdateClick(View v) {
        DeployGate.installUpdate();
    }


    /**
     * Called when Comments button clicked
     *
     * @param view View instance of the button
     */
    public void onCommentsClick(View view) {
        DeployGate.openComments();
    }

    /**
     * Called when Compose button clicked
     *
     * @param view View instance of the button
     */
    public void onComposeCommentClick(View view) {
        DeployGate.composeComment("Hi from DeployGate SDK Sample");
    }

    @Override
    public void onInitialized(boolean isServiceAvailable) {
        // will be called to notify DeployGate SDK has initialized
        Log.d(TAG, "DeployGate SDK initialized, is DeployGate available? : " + isServiceAvailable);
        mAvailableText.setText(
            isServiceAvailable
                ? getString(R.string.available_yes, DeployGate.getDeployGateVersionCode())
                : getString(R.string.available_no)
        );
    }

    @Override
    public void onStatusChanged(boolean isManaged, boolean isAuthorized, String loginUsername,
            boolean isStopped) {
        // will be called when DeployGate status has changed, including this
        // activity starting and resuming.
        if (isManaged) {
            String distributionUrl = DeployGate.getDistributionUrl();
            if (! TextUtils.isEmpty(distributionUrl)) {
                mManagedText.setText(
                    Html.fromHtml(
                        getString(
                            R.string.managed_distribution,
                            DeployGate.getCurrentRevision(),
                            DeployGate.getDistributionTitle(),
                            distributionUrl
                        )
                    )
                );
                mManagedText.setMovementMethod(LinkMovementMethod.getInstance());
                mDistributionComments.setVisibility(View.VISIBLE);
            } else {
                mManagedText.setText(getString(R.string.managed_yes, DeployGate.getCurrentRevision()));
                mDistributionComments.setVisibility(View.GONE);
            }
        } else {
            mManagedText.setText(getString(R.string.managed_no));
            mDistributionComments.setVisibility(View.GONE);
        }

        String username = DeployGate.getDistributionUserName();
        if (username == null)
            username = loginUsername;

        mAuthorizedText.setText(
            isAuthorized
            ? getString(R.string.authorized_yes, username)
            : getString(R.string.authorized_no)
        );
        
        mCrashButton.setEnabled(isAuthorized);
        mLogCatButton.setEnabled(isAuthorized);
        mLogMessage.setEnabled(isAuthorized);
        for (int id : sLogButtonIds) {
            findViewById(id).setEnabled(isAuthorized);
        }
    }

    @Override
    public void onUpdateAvailable(int serial, String versionName, int versionCode) {
        // will be called on app update is available.
        mTitleText.setTextColor(Color.GREEN);

        String message = DeployGate.getUpdateMessage();
        if (message == null)
            message = "";

        mTitleText.setText(getString(
            R.string.update_available,
            serial,
            versionName,
            versionCode,
            message));
        mUpdateButton.setVisibility(View.VISIBLE);
    }
}
