/*
 * Copyright (c) 2026 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.utils.redis;

import redis.clients.jedis.commands.ProtocolCommand;
import redis.clients.jedis.util.SafeEncoder;

/**
 * RediSearch (vector search) commands not present in Jedis 3.3's typed API.
 * Sent via {@code Jedis#sendCommand}/{@code JedisCluster#sendCommand}.
 */
public enum RediSearchCommand implements ProtocolCommand {

    FT_CREATE("FT.CREATE"),
    FT_SEARCH("FT.SEARCH"),
    FT_INFO("FT.INFO");

    private final byte[] raw;

    RediSearchCommand(String command) {
        this.raw = SafeEncoder.encode(command);
    }

    @Override
    public byte[] getRaw() {
        return raw;
    }
}
