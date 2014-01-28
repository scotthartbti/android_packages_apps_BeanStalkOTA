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

package com.beanstalk.beanstalkota.updater.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.beanstalk.beanstalkota.Utils;
import com.beanstalk.beanstalkota.updater.Server;
import com.beanstalk.beanstalkota.updater.UpdatePackage;
import com.beanstalk.beanstalkota.updater.Updater.PackageInfo;

public class GooServer implements Server {

    private static final String URL = "http://goo.im/json2&path=/devs/beanstalk/%s&ro_board=%s";

    private String mDevice = null;
    private String mError = null;
    private long mVersion = 0L;
    private boolean mIsRom;

    public GooServer(boolean isRom) {
        mIsRom = isRom;
    }

    @Override
    public String getUrl(String device, long version) {
        mDevice = device;
        mVersion = version;
        return String.format(URL, new Object[] { device, device });
    }

    @Override
    public List<PackageInfo> createPackageInfoList(String buffer) throws Exception {
        List<PackageInfo> list = new ArrayList<PackageInfo>();
        mError = null;
        if (buffer != null && !buffer.isEmpty()) {
            JSONObject result = new JSONObject(buffer);
            JSONObject update = null;
            try {
                update = result.getJSONObject("update_info");
            } catch (JSONException ex) {
                update = result;
            }
            JSONArray updates = update.optJSONArray("list");
            if (updates == null) {
                mError = "Device not found";
            }
            for (int i = 0; updates != null && i < updates.length(); i++) {
                JSONObject file = updates.getJSONObject(i);
                String filename = file.optString("filename");
                if (filename != null && !filename.isEmpty() && filename.endsWith(".zip")) {
                    String stripped = filename.replace(".zip", "");
                    stripped = stripped.replace("-signed", "");
                    String[] parts = stripped.split("-");
                    int part = parts.length - 2;
                    if (parts[part].startsWith("RC")) {
                        part = parts.length - 1;
                    }
                    boolean isNew = parts[parts.length - 1].matches("[-+]?\\d*\\.?\\d+");
                    if (!isNew) {
                        continue;
                    }
                    long version = Utils.parseRomVersion(filename);
                    if (version > mVersion) {
                        list.add(new UpdatePackage(mDevice, filename, version, "0", "http://goo.im"
                                + file.getString("path"), file.getString("md5"), mIsRom));
                    }
                }
            }
        }
        Collections.sort(list, new Comparator<PackageInfo>() {

            @Override
            public int compare(PackageInfo lhs, PackageInfo rhs) {
                long v1 = lhs.getVersion();
                long v2 = rhs.getVersion();
                return v1 < v2 ? 1 : -1;
            }

        });
        return list;
    }

    @Override
    public String getError() {
        return mError;
    }

}
