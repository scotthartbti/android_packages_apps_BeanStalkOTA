/*
 * Copyright 2013 The Android Open Source Project
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

package com.beanstalk.beanstalkota;

import com.beanstalk.beanstalkota.helpers.DownloadHelper;
import com.beanstalk.beanstalkota.helpers.SettingsHelper;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DownloadReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
            SettingsHelper helper = new SettingsHelper(context);
            if (!helper.getDownloadFinished()) {
                DownloadHelper.clearDownload(id);
                return;
            }
        }
        Intent i = new Intent(context, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra(Utils.CHECK_DOWNLOADS_FINISHED, true);
        i.putExtra(Utils.CHECK_DOWNLOADS_ID, id);
        context.startActivity(i);
    }

}
