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

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * YStream offset based on SCN (System Change Number).
 *
 * <p>Stores the current SCN position for YStream incremental capture. Similar to Oracle's
 * RedoLogOffset.
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class YStreamOffset extends Offset {

    private static final long serialVersionUID = 1L;

    private final String scn;
    private final String commitScn;

    public YStreamOffset(String scn, String commitScn) {
        this.scn = scn;
        this.commitScn = commitScn;
    }

    @Override
    public Map<String, String> getOffset() {
        Map<String, String> offset = new HashMap<>();
        offset.put("scn", scn);
        offset.put("commit_scn", commitScn);
        return offset;
    }

    @Override
    public int compareTo(Offset o) {
        YStreamOffset that = (YStreamOffset) o;
        return Long.compare(Long.parseLong(this.scn), Long.parseLong(that.scn));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof YStreamOffset)) return false;
        if (!super.equals(o)) return false;
        YStreamOffset that = (YStreamOffset) o;
        return scn.equals(that.scn) && commitScn.equals(that.commitScn);
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + scn.hashCode();
    }
}
