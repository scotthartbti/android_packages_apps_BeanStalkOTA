/*
 * Copyright 2013 BeanStalk Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.beanstalk.beanstalkota.updater;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;

import com.beanstalk.beanstalkota.http.HttpStringReader.HttpStringReaderListener;
import com.beanstalk.beanstalkota.http.URLStringReader.URLStringReaderListener;

public abstract class Updater implements URLStringReaderListener, HttpStringReaderListener  {

    public interface PackageInfo extends Serializable {

        public String getMd5();
        public String getFilename();
        public String getPath();
        public String getSize();
        public long getVersion();
        public boolean isDelta();
        public String getDeltaFilename();
        public String getDeltaPath();
        public String getDeltaMd5();
        public boolean isGapps();
    }

    public static final String PROPERTY_DEVICE = "ro.pa.device";
    public static final String PROPERTY_DEVICE_EXT = "ro.product.device";

//    public static final int ROM_NOTIFICATION_ID = 122303222;
//    public static final int GAPPS_NOTIFICATION_ID = 122303224;
    public static final int NOTIFICATION_ID = 122303225;

    public static interface UpdaterListener {

        public void startChecking(boolean isRom);

        public void versionFound(PackageInfo[] info, boolean isRom);

        public void checkError(boolean isRom);
    }

    private Context mContext;
    private PackageInfo[] mLastUpdates;
    private List<UpdaterListener> mListeners = new ArrayList<UpdaterListener>();

    public Updater(Context context) {
        mContext = context;
    }

    public abstract long getVersion();

    public abstract void check();

    public abstract boolean isScanning();

    public abstract boolean isRom();

    protected Context getContext() {
        return mContext;
    }

    public PackageInfo[] getLastUpdates() {
        return mLastUpdates;
    }

    public void setLastUpdates(PackageInfo[] infos) {
        mLastUpdates = infos;
    }

    public void addUpdaterListener(UpdaterListener listener) {
        mListeners.add(listener);
    }

    public void removeUpdaterListener(UpdaterListener listener) {
        mListeners.remove(listener);
    }

    protected void fireStartChecking() {
        if (mContext instanceof Activity) {
            ((Activity) mContext).runOnUiThread(new Runnable() {
    
                public void run() {
                    for (UpdaterListener listener : mListeners) {
                        listener.startChecking(isRom());
                    }
                }
            });
        }
    }

    protected void fireCheckCompleted(final PackageInfo[] info) {
        if (mContext instanceof Activity) {
            ((Activity) mContext).runOnUiThread(new Runnable() {
    
                public void run() {
                    for (UpdaterListener listener : mListeners) {
                        listener.versionFound(info, isRom());
                    }
                }
            });
        }
    }

    protected void fireCheckError(final boolean isRom) {
        if (mContext instanceof Activity) {
            ((Activity) mContext).runOnUiThread(new Runnable() {
    
                public void run() {
                    for (UpdaterListener listener : mListeners) {
                        listener.checkError(isRom);
                    }
                }
            });
        }
    }
}
