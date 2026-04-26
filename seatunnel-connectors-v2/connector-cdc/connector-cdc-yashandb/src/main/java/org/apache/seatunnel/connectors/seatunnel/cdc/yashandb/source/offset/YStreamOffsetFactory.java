/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.seatunnel.connectors.seatunnel.cdc.yashandb.source.offset;

import org.apache.seatunnel.connectors.cdc.base.source.offset.Offset;
import org.apache.seatunnel.connectors.cdc.base.source.offset.OffsetFactory;
import org.apache.seatunnel.connectors.seatunnel.cdc.yashandb.config.YashanSourceConfigFactory;
import org.apache.seatunnel.connectors.seatunnel.cdc.yashandb.source.YashanDialect;

import java.util.Map;

/** Factory for creating {@link YStreamOffset} instances. */
public class YStreamOffsetFactory extends OffsetFactory {

    private final YashanSourceConfigFactory configFactory;
    private final YashanDialect dialect;

    public YStreamOffsetFactory(
        YashanSourceConfigFactory configFactory, YashanDialect dialect) {
        this.configFactory = configFactory;
        this.dialect = dialect;
    }

    @Override
    public Offset earliest() {
        return new YStreamOffset("0", "0");
    }

    @Override
    public Offset neverStop() {
        return null;
    }

    @Override
    public Offset latest() {
        return null;
    }

    @Override
    public Offset specific(Map<String, String> offset) {
        return null;
    }

    @Override
    public Offset specific(String filename, Long position) {
        return null;
    }

    @Override
    public Offset timestamp(long timestamp) {
        // TODO: Convert timestamp to approximate SCN
        return new YStreamOffset(Long.toString(timestamp), Long.toString(timestamp));
    }
}
