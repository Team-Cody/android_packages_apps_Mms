/*
 * Copyright (C) 2007-2008 Esmertec AG.
 * Copyright (C) 2007-2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.mms.ui;

import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.R;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.SearchRecentSuggestions;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;

import com.android.mms.templates.TemplatesListActivity;
import com.android.mms.util.Recycler;

/**
 * With this activity, users can set preferences for MMS and SMS and
 * can access and manipulate SMS messages stored on the SIM.
 */
public class MessagingPreferenceActivity extends PreferenceActivity
            implements OnPreferenceChangeListener {
    // Symbolic names for the keys used for preference lookup
    public static final String MMS_DELIVERY_REPORT_MODE = "pref_key_mms_delivery_reports";
    public static final String EXPIRY_TIME              = "pref_key_mms_expiry";
    public static final String PRIORITY                 = "pref_key_mms_priority";
    public static final String READ_REPORT_MODE         = "pref_key_mms_read_reports";
    public static final String SMS_DELIVERY_REPORT_MODE = "pref_key_sms_delivery_reports";
    public static final String SMS_SPLIT_COUNTER        = "pref_key_sms_split_counter";
    public static final String SMS_SEND_COUNTDOWN       = "pref_key_sms_send_countdown";
    public static final String SMS_SEND_COUNTDOWN_VALUE = "pref_key_sms_send_countdown_value";
    public static final String NOTIFICATION_ENABLED     = "pref_key_enable_notifications";
    public static final String NOTIFICATION_VIBRATE     = "pref_key_vibrate";
    public static final String NOTIFICATION_VIBRATE_WHEN= "pref_key_vibrateWhen";
    public static final String NOTIFICATION_RINGTONE    = "pref_key_ringtone";
    public static final String AUTO_RETRIEVAL           = "pref_key_mms_auto_retrieval";
    public static final String RETRIEVAL_DURING_ROAMING = "pref_key_mms_retrieval_during_roaming";
    public static final String AUTO_DELETE              = "pref_key_auto_delete";
    public static final String MANAGE_TEMPLATES         = "pref_key_templates_manage";
    public static final String SHOW_GESTURE             = "pref_key_templates_show_gesture";
    public static final String GESTURE_SENSITIVITY      = "pref_key_templates_gestures_sensitivity";
    public static final String GESTURE_SENSITIVITY_VALUE= "pref_key_templates_gestures_sensitivity_value";
    public static final String MESSAGE_FONT_SIZE        = "pref_key_mms_message_font_size";
    public static final String STRIP_UNICODE            = "pref_key_strip_unicode";
    public static final String ENABLE_EMOJIS            = "pref_key_enable_emojis";
    public static final String FULL_TIMESTAMP           = "pref_key_mms_full_timestamp";
    public static final String SENT_TIMESTAMP           = "pref_key_mms_use_sent_timestamp";
    public static final String SIGNATURE                = "pref_key_mms_signature";
    public static final String SIGNATURE_AUTO_APPEND    = "pref_key_mms_signature_auto_append";
    public static final String NOTIFICATION_VIBRATE_PATTERN = "pref_key_mms_notification_vibrate_pattern";
    public static final String NOTIFICATION_VIBRATE_PATTERN_CUSTOM = "pref_key_mms_notification_vibrate_pattern_custom";
    public static final String NOTIFICATION_VIBRATE_CALL ="pre_key_mms_notification_vibrate_call";
    public static final String INPUT_TYPE               = "pref_key_mms_input_type";
    public static final String MMS_BREATH               = "mms_breath";
    public static final String DISPLAY_HIDESENDERNAME   = "pref_key_notification_hidesendername";
    public static final String DISPLAY_HIDEMESSAGE      = "pref_key_notification_hidemessage";


    // Menu entries
    private static final int MENU_RESTORE_DEFAULTS    = 1;

    private Preference mSmsLimitPref;
    private Preference mSmsDeliveryReportPref;
    private CheckBoxPreference mSmsSplitCounterPref;
    private Preference mMmsLimitPref;
    private Preference mMmsDeliveryReportPref;
    private Preference mMmsReadReportPref;
    private Preference mManageSimPref;
    private Preference mClearHistoryPref;
    private CheckBoxPreference mMmsAutoRetrieval;
    private CheckBoxPreference mMmsRetrievalDuringRoaming;
    private CheckBoxPreference mEnableMultipartSMS;
    private Preference mSmsToMmsTextThreshold;
    private ListPreference mVibrateWhenPref;
    private CheckBoxPreference mEnableNotificationsPref;
    private CheckBoxPreference mMMSBreath;
    private Recycler mSmsRecycler;
    private Recycler mMmsRecycler;
    private Preference mManageTemplate;
    private ListPreference mGestureSensitivity;
    private static final int CONFIRM_CLEAR_SEARCH_HISTORY_DIALOG = 3;
    private CharSequence[] mVibrateEntries;
    private CharSequence[] mVibrateValues;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.preferences);

        mManageSimPref = findPreference("pref_key_manage_sim_messages");
        mSmsLimitPref = findPreference("pref_key_sms_delete_limit");
        mSmsDeliveryReportPref = findPreference("pref_key_sms_delivery_reports");
        mSmsSplitCounterPref = (CheckBoxPreference) findPreference("pref_key_sms_split_counter");
        mMmsDeliveryReportPref = findPreference("pref_key_mms_delivery_reports");
        mMmsReadReportPref = findPreference("pref_key_mms_read_reports");
        mMmsLimitPref = findPreference("pref_key_mms_delete_limit");
        mClearHistoryPref = findPreference("pref_key_mms_clear_history");
        mEnableNotificationsPref = (CheckBoxPreference) findPreference(NOTIFICATION_ENABLED);
        mVibrateWhenPref = (ListPreference) findPreference(NOTIFICATION_VIBRATE_WHEN);
        mManageTemplate = findPreference(MANAGE_TEMPLATES);
        mGestureSensitivity = (ListPreference) findPreference(GESTURE_SENSITIVITY);

        // Get the MMS retrieval settings. Defaults to enabled with roaming disabled
        ContentResolver resolver = getContentResolver();
        mMmsAutoRetrieval = (CheckBoxPreference) findPreference(AUTO_RETRIEVAL);
        mMmsAutoRetrieval.setChecked(Settings.System.getInt(resolver,
                Settings.System.MMS_AUTO_RETRIEVAL, 1) == 1);
        mMmsRetrievalDuringRoaming = (CheckBoxPreference) findPreference(RETRIEVAL_DURING_ROAMING);
        mMmsRetrievalDuringRoaming.setChecked(Settings.System.getInt(resolver,
                Settings.System.MMS_AUTO_RETRIEVAL_ON_ROAMING, 0) == 1);

        mMMSBreath = (CheckBoxPreference) findPreference(MMS_BREATH);
        mMMSBreath.setChecked(mMMSBreath.isChecked());

        mEnableMultipartSMS = (CheckBoxPreference)findPreference("pref_key_sms_EnableMultipartSMS");
        mSmsToMmsTextThreshold = findPreference("pref_key_sms_SmsToMmsTextThreshold");

        mVibrateEntries = getResources().getTextArray(R.array.prefEntries_vibrateWhen);
        mVibrateValues = getResources().getTextArray(R.array.prefValues_vibrateWhen);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        setMessagePreferences();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Since the enabled notifications pref can be changed outside of this activity,
        // we have to reload it whenever we resume.
        setEnabledNotificationsPref();
        registerListeners();
    }

    private void setMessagePreferences() {
        if (!MmsApp.getApplication().getTelephonyManager().hasIccCard()) {
            // No SIM card, remove the SIM-related prefs
            PreferenceCategory smsCategory =
                (PreferenceCategory)findPreference("pref_key_sms_settings");
            smsCategory.removePreference(mManageSimPref);
        }

        if (!MmsConfig.getSMSDeliveryReportsEnabled()) {
            PreferenceCategory smsCategory =
                (PreferenceCategory)findPreference("pref_key_sms_settings");
            smsCategory.removePreference(mSmsDeliveryReportPref);
        }

        if (!MmsConfig.getSplitSmsEnabled()) {
            // SMS Split disabled, remove SplitCounter pref
            PreferenceCategory smsCategory =
                (PreferenceCategory)findPreference("pref_key_sms_settings");
            smsCategory.removePreference(mSmsSplitCounterPref);
        }

        if (!MmsConfig.getMmsEnabled()) {
            // No Mms, remove all the mms-related preferences
            PreferenceCategory mmsOptions =
                (PreferenceCategory)findPreference("pref_key_mms_settings");
            getPreferenceScreen().removePreference(mmsOptions);

            PreferenceCategory storageOptions =
                (PreferenceCategory)findPreference("pref_key_storage_settings");
            storageOptions.removePreference(findPreference("pref_key_mms_delete_limit"));
        } else {
            if (!MmsConfig.getMMSDeliveryReportsEnabled()) {
                PreferenceCategory mmsOptions =
                    (PreferenceCategory)findPreference("pref_key_mms_settings");
                mmsOptions.removePreference(mMmsDeliveryReportPref);
            }
            if (!MmsConfig.getMMSReadReportsEnabled()) {
                PreferenceCategory mmsOptions =
                    (PreferenceCategory)findPreference("pref_key_mms_settings");
                mmsOptions.removePreference(mMmsReadReportPref);
            }
        }

        mEnableMultipartSMS.setChecked(!MmsConfig.getMultipartSmsEnabled());
        mSmsToMmsTextThreshold.setDefaultValue(MmsConfig.getSmsToMmsTextThreshold()-1);

        setEnabledNotificationsPref();

        // If needed, migrate vibration setting from a previous version
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (!sharedPreferences.contains(NOTIFICATION_VIBRATE_WHEN) &&
                sharedPreferences.contains(NOTIFICATION_VIBRATE)) {
            int stringId = sharedPreferences.getBoolean(NOTIFICATION_VIBRATE, false) ?
                    R.string.prefDefault_vibrate_true :
                    R.string.prefDefault_vibrate_false;
            mVibrateWhenPref.setValue(getString(stringId));
        }

        mManageTemplate.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(MessagingPreferenceActivity.this,
                        TemplatesListActivity.class);
                startActivity(intent);
                return false;
            }
        });

        String gestureSensitivity = String.valueOf(sharedPreferences.getInt(GESTURE_SENSITIVITY_VALUE, 3));

        mGestureSensitivity.setSummary(gestureSensitivity);
        mGestureSensitivity.setValue(gestureSensitivity);
        mGestureSensitivity.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int value = Integer.parseInt((String) newValue);
                sharedPreferences.edit().putInt(GESTURE_SENSITIVITY_VALUE, value).commit();
                mGestureSensitivity.setSummary(String.valueOf(value));
                return true;
            }
        });

        mSmsRecycler = Recycler.getSmsRecycler();
        mMmsRecycler = Recycler.getMmsRecycler();

        // Fix up the recycler's summary with the correct values
        setSmsDisplayLimit();
        setMmsDisplayLimit();

        // Fix up the sms to mms treshold
        setSmsToMmsTextThreshold();

        adjustVibrateSummary(mVibrateWhenPref.getValue());
    }

    private void setEnabledNotificationsPref() {
        // The "enable notifications" setting is really stored in our own prefs. Read the
        // current value and set the checkbox to match.
        mEnableNotificationsPref.setChecked(getNotificationEnabled(this));
    }

    private void setSmsDisplayLimit() {
        mSmsLimitPref.setSummary(
                getString(R.string.pref_summary_delete_limit,
                        mSmsRecycler.getMessageLimit(this)));
    }

    private void setMmsDisplayLimit() {
        mMmsLimitPref.setSummary(
                getString(R.string.pref_summary_delete_limit,
                        mMmsRecycler.getMessageLimit(this)));
    }

    private void setSmsToMmsTextThreshold() {
        mSmsToMmsTextThreshold.setSummary(
                getString(R.string.pref_summary_sms_SmsToMmsTextThreshold,
                        MmsConfig.getSmsToMmsTextThreshold()-1));
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.clear();
        menu.add(0, MENU_RESTORE_DEFAULTS, 0, R.string.restore_default);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESTORE_DEFAULTS:
                restoreDefaultPreferences();
                return true;

            case android.R.id.home:
                // The user clicked on the Messaging icon in the action bar. Take them back from
                // wherever they came from
                finish();
                return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mSmsLimitPref) {
            new NumberPickerDialog(this,
                    mSmsLimitListener,
                    mSmsRecycler.getMessageLimit(this),
                    mSmsRecycler.getMessageMinLimit(),
                    mSmsRecycler.getMessageMaxLimit(),
                    R.string.pref_title_sms_delete).show();
        } else if (preference == mMmsLimitPref) {
            new NumberPickerDialog(this,
                    mMmsLimitListener,
                    mMmsRecycler.getMessageLimit(this),
                    mMmsRecycler.getMessageMinLimit(),
                    mMmsRecycler.getMessageMaxLimit(),
                    R.string.pref_title_mms_delete).show();
        } else if (preference == mSmsToMmsTextThreshold) {
            new NumberPickerDialog(this,
                    mSmsToMmsTextThresholdListener,
                    MmsConfig.getSmsToMmsTextThreshold()-1,
                    MmsConfig.getSmsToMmsTextThresholdMin()-1,
                    MmsConfig.getSmsToMmsTextThresholdMax()-1,
                    R.string.pref_title_sms_SmsToMmsTextThreshold).show();
        } else if (preference == mManageSimPref) {
            startActivity(new Intent(this, ManageSimMessages.class));
        } else if (preference == mClearHistoryPref) {
            showDialog(CONFIRM_CLEAR_SEARCH_HISTORY_DIALOG);
            return true;
        } else if (preference == mEnableNotificationsPref) {
            // Update the actual "enable notifications" value that is stored in secure settings.
            enableNotifications(mEnableNotificationsPref.isChecked(), this);
        } else if (preference == mEnableMultipartSMS) {
            //should be false when the checkbox is checked
            MmsConfig.setEnableMultipartSMS(!mEnableMultipartSMS.isChecked());
        } else if (preference == mMmsAutoRetrieval) {
            // Update the value in Settings.System
            Settings.System.putInt(getContentResolver(), Settings.System.MMS_AUTO_RETRIEVAL,
                    mMmsAutoRetrieval.isChecked() ? 1 : 0);
        } else if (preference == mMmsRetrievalDuringRoaming) {
            // Update the value in Settings.System
            Settings.System.putInt(getContentResolver(), Settings.System.MMS_AUTO_RETRIEVAL_ON_ROAMING,
                    mMmsRetrievalDuringRoaming.isChecked() ? 1 : 0);
        } else if (preference == mMMSBreath) {
            mMMSBreath.setChecked(mMMSBreath.isChecked());
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }


    private void restoreDefaultPreferences() {
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit().clear().apply();
        setPreferenceScreen(null);
        addPreferencesFromResource(R.xml.preferences);
        setMessagePreferences();
    }

    NumberPickerDialog.OnNumberSetListener mSmsLimitListener =
        new NumberPickerDialog.OnNumberSetListener() {
            public void onNumberSet(int limit) {
                mSmsRecycler.setMessageLimit(MessagingPreferenceActivity.this, limit);
                setSmsDisplayLimit();
            }
    };

    NumberPickerDialog.OnNumberSetListener mMmsLimitListener =
        new NumberPickerDialog.OnNumberSetListener() {
            public void onNumberSet(int limit) {
                mMmsRecycler.setMessageLimit(MessagingPreferenceActivity.this, limit);
                setMmsDisplayLimit();
            }
    };

    NumberPickerDialog.OnNumberSetListener mSmsToMmsTextThresholdListener =
            new NumberPickerDialog.OnNumberSetListener() {
                public void onNumberSet(int limit) {
                    SharedPreferences.Editor editPrefs =
                            PreferenceManager.getDefaultSharedPreferences(MessagingPreferenceActivity.this).edit();
                    editPrefs.putInt("pref_key_sms_SmsToMmsTextThreshold", limit);
                    editPrefs.apply();
                    MmsConfig.setSmsToMmsTextThreshold(limit+1);
                    setSmsToMmsTextThreshold();
                }
        };

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case CONFIRM_CLEAR_SEARCH_HISTORY_DIALOG:
                return new AlertDialog.Builder(MessagingPreferenceActivity.this)
                    .setTitle(R.string.confirm_clear_search_title)
                    .setMessage(R.string.confirm_clear_search_text)
                    .setPositiveButton(android.R.string.ok, new AlertDialog.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            SearchRecentSuggestions recent =
                                ((MmsApp)getApplication()).getRecentSuggestions();
                            if (recent != null) {
                                recent.clearHistory();
                            }
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .setIconAttribute(android.R.attr.alertDialogIcon)
                    .create();
        }
        return super.onCreateDialog(id);
    }

    public static boolean getNotificationEnabled(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean notificationsEnabled =
            prefs.getBoolean(MessagingPreferenceActivity.NOTIFICATION_ENABLED, true);
        return notificationsEnabled;
    }

    public static void enableNotifications(boolean enabled, Context context) {
        // Store the value of notifications in SharedPreferences
        SharedPreferences.Editor editor =
            PreferenceManager.getDefaultSharedPreferences(context).edit();

        editor.putBoolean(MessagingPreferenceActivity.NOTIFICATION_ENABLED, enabled);

        editor.apply();
    }

    private void registerListeners() {
        mVibrateWhenPref.setOnPreferenceChangeListener(this);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean result = false;
        if (preference == mVibrateWhenPref) {
            adjustVibrateSummary((String)newValue);
            result = true;
        }
        return result;
    }

    private void adjustVibrateSummary(String value) {
        int len = mVibrateValues.length;
        for (int i = 0; i < len; i++) {
            if (mVibrateValues[i].equals(value)) {
                mVibrateWhenPref.setSummary(mVibrateEntries[i]);
                return;
            }
        }
        mVibrateWhenPref.setSummary(null);
    }
    
    public static boolean getHideSenderNameEnabled(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean hideSenderName =
            prefs.getBoolean(MessagingPreferenceActivity.DISPLAY_HIDESENDERNAME, false);
        return hideSenderName;
    }
    
    public static boolean getHideMessageEnabled(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean hideMessageEnabled =
            prefs.getBoolean(MessagingPreferenceActivity.DISPLAY_HIDEMESSAGE, false);
        return hideMessageEnabled;
    }
    
}
