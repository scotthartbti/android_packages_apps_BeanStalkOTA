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

package com.beanstalk.beanstalkota.helpers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.beanstalk.beanstalkota.IOUtils;
import com.beanstalk.beanstalkota.R;

public class RecoveryHelper {

    public class RecoveryInfo {

        private int id;
        private String name = null;
        private String internalSdcard = null;
        private String externalSdcard = null;

        public RecoveryInfo(int id, String name, String internalSdcard, String externalSdcard) {
            this.id = id;
            this.name = name;
            this.internalSdcard = internalSdcard;
            this.externalSdcard = externalSdcard;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getInternalSdcard() {
            return internalSdcard;
        }

        public void setInternalSdcard(String sdcard) {
            this.internalSdcard = sdcard;
        }

        public String getExternalSdcard() {
            return externalSdcard;
        }

        public void setExternalSdcard(String sdcard) {
            this.externalSdcard = sdcard;
        }
    }

    private static final String SDCARD = Environment.getExternalStorageDirectory()
            .getAbsolutePath();

    private SparseArray<RecoveryInfo> recoveries = new SparseArray<RecoveryInfo>();
    private SettingsHelper mSettings;
    private Context mContext;

    public RecoveryHelper(Context context) {

        mContext = context;
        mSettings = new SettingsHelper(context);

        recoveries.put(R.id.cwmbased, new RecoveryInfo(R.id.cwmbased, "cwmbased", "sdcard",
                "external_sd"));
        recoveries.put(R.id.twrp, new RecoveryInfo(R.id.twrp, "twrp", "sdcard", "external_sd"));
        recoveries.put(R.id.stock, new RecoveryInfo(R.id.stock, "stock", "sdcard", "external_sd"));

        if (!mSettings.existsRecovery()) {
            test();
        }
    }

    public void selectRecovery() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.selection_recovery,
                (ViewGroup) ((Activity) mContext).findViewById(R.id.recovery_layout));

        RadioButton cbCwmbased = (RadioButton) view.findViewById(R.id.cwmbased);
        RadioButton cbTwrp = (RadioButton) view.findViewById(R.id.twrp);
        RadioButton cbStock = (RadioButton) view.findViewById(R.id.stock);

        final RadioGroup mGroup = (RadioGroup) view.findViewById(R.id.recovery_radio_group);

        RecoveryInfo info = getRecovery();
        if (info == null) {
            cbCwmbased.setChecked(true);
        } else {
            switch (info.getId()) {
                case R.id.stock:
                    cbStock.setChecked(true);
                    break;
                case R.id.twrp:
                    cbTwrp.setChecked(true);
                    break;
                default:
                    cbCwmbased.setChecked(true);
                    break;
            }
        }

        new AlertDialog.Builder(mContext).setTitle(R.string.recovery_select_alert_title)
                .setCancelable(false).setMessage(R.string.recovery_select_alert_summary)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {

                        int id = mGroup.getCheckedRadioButtonId();

                        setRecovery(id);

                        dialog.dismiss();
                    }
                }).show();
    }

    public void selectSdcard(final boolean internal) {

        final EditText input = new EditText(mContext);
        input.setText(internal ? mSettings.getInternalStorage() : mSettings.getExternalStorage());

        new AlertDialog.Builder(mContext)
                .setTitle(R.string.recovery_select_sdcard_alert_title)
                .setMessage(R.string.recovery_select_sdcard_alert_summary)
                .setView(input)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        String value = input.getText().toString();

                        if (value == null || "".equals(value.trim())) {
                            Toast.makeText(mContext, R.string.recovery_select_sdcard_alert_error,
                                    Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            return;
                        }

                        if (value.startsWith("/")) {
                            value = value.substring(1);
                        }

                        if (internal) {
                            mSettings.setInternalStorage(value);
                        } else {
                            mSettings.setExternalStorage(value);
                        }
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                }).show();
    }

    public RecoveryInfo getRecovery() {
        String recovery = mSettings.getRecovery();
        for (int i = 0; i < recoveries.size(); i++) {
            int key = recoveries.keyAt(i);
            RecoveryInfo info = recoveries.get(key);
            if (info.getName().equals(recovery)) {
                return info;
            }
        }
        return null;
    }

    public void setRecovery(int id) {
        RecoveryInfo info = recoveries.get(id);
        mSettings.setRecovery(info.getName());
        mSettings.setInternalStorage(info.getInternalSdcard());
        mSettings.setExternalStorage(info.getExternalSdcard());
    }

    public String getCommandsFile() {

        RecoveryInfo info = getRecovery();

        switch (info.getId()) {
            case R.id.stock:
                return "command";
            case R.id.twrp:
                return "openrecoveryscript";
            default:
                return "extendedcommand";
        }
    }

    public String getRecoveryFilePath(String filePath) {

        String internalStorage = mSettings.getInternalStorage();
        String externalStorage = mSettings.getExternalStorage();

        String primarySdcard = IOUtils.getPrimarySdCard();
        String secondarySdcard = IOUtils.getSecondarySdCard();

        String[] internalNames = new String[] {
                primarySdcard,
                "/mnt/sdcard",
                "/sdcard",
                "/storage/sdcard0",
                "/storage/emulated/0" };
        String[] externalNames = new String[] {
                secondarySdcard == null ? " " : secondarySdcard,
                "/mnt/extSdCard",
                "/extSdCard",
                "/storage/sdcard1",
                "/storage/emulated/1" };
        for (int i = 0; i < internalNames.length; i++) {
            String internalName = internalNames[i];
            String externalName = externalNames[i];
            if (filePath.startsWith(externalName)) {
                filePath = filePath.replace(externalName, "/" + externalStorage);
                break;
            } else if (filePath.startsWith(internalName)) {
                filePath = filePath.replace(internalName, "/" + internalStorage);
                break;
            }
        }

        return filePath;
    }

    public String[] getCommands(String[] items, String[] originalItems, boolean wipeSystem,
            boolean wipeData, boolean wipeCaches, String backupFolder, String backupOptions)
            throws Exception {
        List<String> commands = new ArrayList<String>();

        int size = items.length, i = 0;

        RecoveryInfo info = getRecovery();

        String internalStorage = mSettings.getInternalStorage();

        switch (info.getId()) {
            default:

                if (backupFolder != null) {
                    commands.add("backup_rom(\"/" + internalStorage + "/clockworkmod/backup/"
                            + backupFolder + "\");");
                }

                if (wipeSystem) {
                    commands.add("format(\"/system\");");
                }

                if (wipeData) {
                    commands.add("format(\"/data\");");
                    commands.add("format(\"/" + internalStorage + "/.android_secure\");");
                }
                if (wipeCaches) {
                    commands.add("format(\"/cache\");");
                    commands.add("format(\"/data/dalvik-cache\");");
                    commands.add("format(\"/cache/dalvik-cache\");");
                    commands.add("format(\"/sd-ext/dalvik-cache\");");
                }

                if (size > 0) {
                    for (; i < size; i++) {
                        commands.add("assert(install_zip(\"" + items[i] + "\"));");
                    }
                }

                break;

            case R.id.twrp:

                boolean hasAndroidSecure = hasAndroidSecure();
                boolean hasSdExt = hasSdExt();

                if (backupFolder != null) {
                    String str = "backup ";
                    if (backupOptions != null && backupOptions.indexOf("S") >= 0) {
                        str += "S";
                    }
                    if (backupOptions != null && backupOptions.indexOf("D") >= 0) {
                        str += "D";
                    }
                    if (backupOptions != null && backupOptions.indexOf("C") >= 0) {
                        str += "C";
                    }
                    if (backupOptions != null && backupOptions.indexOf("R") >= 0) {
                        str += "R";
                    }
                    str += "123";
                    if (backupOptions != null && backupOptions.indexOf("B") >= 0) {
                        str += "B";
                    }
                    if (backupOptions != null && backupOptions.indexOf("A") >= 0
                            && hasAndroidSecure) {
                        str += "A";
                    }
                    if (backupOptions != null && backupOptions.indexOf("E") >= 0 && hasSdExt) {
                        str += "E";
                    }
                    commands.add(str + "O " + backupFolder);
                }

                if (wipeSystem) {
                    commands.add("mount system");
                    commands.add("cmd /sbin/busybox rm -r /system/*");
                    commands.add("unmount system");
                }

                if (wipeData) {
                    commands.add("wipe data");
                }
                if (wipeCaches) {
                    commands.add("wipe cache");
                    commands.add("wipe dalvik");
                }

                for (; i < size; i++) {
                    commands.add("install " + items[i]);
                }

                break;

            case R.id.stock:

                if (wipeData) {
                    commands.add("--wipe_data\n");
                }

                if (wipeCaches) {
                    commands.add("--wipe_cache\n");
                }

                if (size > 0) {
                    for (; i < size; i++) {
                        File file = new File(originalItems[i]);
                        IOUtils.copyOrRemoveCache(file, true);
                        commands.add("--update_package=CACHE:" + file.getName() + "\n");
                    }
                }

                break;
        }

        return commands.toArray(new String[commands.size()]);
    }

    public boolean hasAndroidSecure() {
        return folderExists(SDCARD + "/.android-secure");
    }

    public boolean hasSdExt() {
        return folderExists("/sd-ext");
    }

    private void test() {

        File folderTwrp = new File(SDCARD + "/TWRP/");
        File folderCwm = new File(SDCARD + "/clockworkmod/");

        if (folderTwrp.exists() && folderCwm.exists()) {
            selectRecovery();
        } else if (!folderTwrp.exists() && !folderCwm.exists()) {
            setRecovery(R.id.stock);
            Toast.makeText(
                    mContext,
                    mContext.getString(R.string.recovery_changed,
                            mContext.getString(R.string.recovery_stock)), Toast.LENGTH_LONG).show();
        } else if (folderTwrp.exists()) {
            setRecovery(R.id.twrp);
            Toast.makeText(
                    mContext,
                    mContext.getString(R.string.recovery_changed,
                            mContext.getString(R.string.recovery_twrp)), Toast.LENGTH_LONG).show();
        } else if (folderCwm.exists()) {
            setRecovery(R.id.cwmbased);
            Toast.makeText(
                    mContext,
                    mContext.getString(R.string.recovery_changed,
                            mContext.getString(R.string.recovery_cwm)), Toast.LENGTH_LONG).show();
        }
    }

    private boolean folderExists(String path) {
        File f = new File(path);
        return f.exists() && f.isDirectory();
    }
}
