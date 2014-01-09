/*
 * Copyright (C) 2013 BeanStalk Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use mContext file except in compliance with the License.
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

package com.beanstalk.beanstalkota.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.beanstalk.beanstalkota.R;
import com.beanstalk.beanstalkota.Utils;
import com.beanstalk.beanstalkota.updater.GappsUpdater;
import com.beanstalk.beanstalkota.updater.RomUpdater;
import com.beanstalk.beanstalkota.updater.Updater.PackageInfo;
import com.beanstalk.beanstalkota.updater.Updater.UpdaterListener;

public class UpdateFragment extends Fragment implements UpdaterListener {

    private RomUpdater mRomUpdater;
    private GappsUpdater mGappsUpdater;
    private TextView mStatusView;
    private TextView mRomView;
    private TextView mGappsView;

    public void setUpdaters(RomUpdater romUpdater, GappsUpdater gappsUpdater) {
        mRomUpdater = romUpdater;
        mGappsUpdater = gappsUpdater;
        mRomUpdater.addUpdaterListener(this);
        mGappsUpdater.addUpdaterListener(this);
        updateText(mRomUpdater.getLastUpdates(), mGappsUpdater.getLastUpdates());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_updates, container,
                false);

        mStatusView = (TextView) rootView.findViewById(R.id.status);
        mRomView = (TextView) rootView.findViewById(R.id.rom);
        mGappsView = (TextView) rootView.findViewById(R.id.gapps);

        if (mRomUpdater != null && mGappsUpdater != null) {
            updateText(mRomUpdater.getLastUpdates(), mGappsUpdater.getLastUpdates());
        }

        return rootView;
    }

    @Override
    public void versionFound(PackageInfo[] info, boolean isRom) {
        if (info == null || info.length == 0) {
            updateText(mRomUpdater.getLastUpdates(), mGappsUpdater.getLastUpdates());
        } else if (info[0].isGapps()) {
            updateText(mRomUpdater.getLastUpdates(), info);
        } else {
            updateText(info, mGappsUpdater.getLastUpdates());
        }
    }

    @Override
    public void startChecking(boolean isRom) {
        updateText(null, null);
    }

    private void updateText(PackageInfo[] roms, PackageInfo[] gapps) {
        Context context = getActivity();
        if (mStatusView == null || context == null) {
            return;
        }
        boolean weAreInAospa = Utils.weAreInAospa();
        Resources resources = context.getResources();
        if (mRomUpdater.isScanning() || mGappsUpdater.isScanning()) {
            mStatusView.setText(R.string.rom_scanning);
            mRomView.setText(Utils.getProp(Utils.MOD_VERSION));
            if (mGappsUpdater.getVersion() == -1) {
                mGappsView.setText(resources.getString(R.string.no_gapps_installed));
            } else {
                mGappsView.setText(resources.getString(R.string.gapps_version,
                        new Object[] {
                                mGappsUpdater.getVersion()
                        }));
            }
        } else {
            PackageInfo rom = roms != null && roms.length > 0 ? roms[0] : null;
            PackageInfo gapp = gapps != null && gapps.length > 0 ? gapps[0] : null;
            if (weAreInAospa) {
                mStatusView.setText(rom != null && gapp != null ? R.string.rom_gapps_new_version
                        : (rom != null ? R.string.rom_new_version
                                : (gapp != null ? R.string.gapps_new_version
                                        : R.string.all_up_to_date)));
            } else {
                mStatusView.setText(R.string.update_to_aospa);
            }
            if (rom != null) {
                mRomView.setText(rom.getFilename());
            } else {
                mRomView.setText(Utils.getProp(Utils.MOD_VERSION));
            }
            if (gapp != null) {
                mGappsView.setText(gapp.getFilename());
            } else {
                if (mGappsUpdater.getVersion() == -1) {
                    mGappsView.setText(resources.getString(R.string.no_gapps_installed));
                } else {
                    mGappsView.setText(resources.getString(R.string.gapps_version,
                            new Object[] {
                                    mGappsUpdater.getVersion()
                            }));
                }
            }
        }
    }
}
