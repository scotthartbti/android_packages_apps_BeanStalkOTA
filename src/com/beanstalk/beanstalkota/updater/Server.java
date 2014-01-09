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

import java.util.List;

import com.beanstalk.beanstalkota.updater.Updater.PackageInfo;

public interface Server {

    public String getUrl(String device, long version);

    public List<PackageInfo> createPackageInfoList(String buffer) throws Exception;

    public String getError();
}
