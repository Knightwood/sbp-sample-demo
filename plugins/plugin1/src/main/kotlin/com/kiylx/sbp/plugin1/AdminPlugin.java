/*
 * Copyright (C) 2019-present the original author or authors.
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
package com.kiylx.sbp.plugin1;

import org.laxture.sbp.SpringBootPlugin;
import org.laxture.sbp.spring.boot.SharedDataSourceSpringBootstrap;
import org.laxture.sbp.spring.boot.SpringBootstrap;
import org.pf4j.PluginWrapper;

/**
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public class AdminPlugin extends SpringBootPlugin {

    public AdminPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    protected SpringBootstrap createSpringBootstrap() {
        SpringBootstrap bootstrap = new SharedDataSourceSpringBootstrap(this, AdminPluginStarter.class);
//        if (getMainApplicationContext().containsBean(SecurityConfig.class.getName())) {
//            bootstrap.addPresetProperty("sbp.security.enabled", true);
//        }
        return bootstrap;
    }

}