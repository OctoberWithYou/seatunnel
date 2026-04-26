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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

/** Test for {@link YStreamOffset}. */
public class YStreamOffsetTest {

    @Test
    public void testConstructor() {
        YStreamOffset offset = new YStreamOffset("1000", "1001");
        Assertions.assertEquals("1000", offset.getScn());
        Assertions.assertEquals("1001", offset.getCommitScn());
    }

    @Test
    public void testGetOffset() {
        YStreamOffset offset = new YStreamOffset("12345", "12346");
        Map<String, String> offsetMap = offset.getOffset();

        Assertions.assertNotNull(offsetMap);
        Assertions.assertEquals(2, offsetMap.size());
        Assertions.assertEquals("12345", offsetMap.get("scn"));
        Assertions.assertEquals("12346", offsetMap.get("commit_scn"));
    }

    @Test
    public void testCompareToGreater() {
        YStreamOffset offset1 = new YStreamOffset("1000", "1001");
        YStreamOffset offset2 = new YStreamOffset("500", "501");

        Assertions.assertTrue(offset1.compareTo(offset2) > 0);
    }

    @Test
    public void testCompareToLess() {
        YStreamOffset offset1 = new YStreamOffset("500", "501");
        YStreamOffset offset2 = new YStreamOffset("1000", "1001");

        Assertions.assertTrue(offset1.compareTo(offset2) < 0);
    }

    @Test
    public void testCompareToEqual() {
        YStreamOffset offset1 = new YStreamOffset("1000", "1001");
        YStreamOffset offset2 = new YStreamOffset("1000", "1002");

        Assertions.assertEquals(0, offset1.compareTo(offset2));
    }

    @Test
    public void testEqualsSame() {
        YStreamOffset offset = new YStreamOffset("1000", "1001");
        Assertions.assertEquals(offset, offset);
    }

    @Test
    public void testEqualsEqual() {
        YStreamOffset offset1 = new YStreamOffset("1000", "1001");
        YStreamOffset offset2 = new YStreamOffset("1000", "1001");

        Assertions.assertEquals(offset1, offset2);
    }

    @Test
    public void testEqualsNotEqualScn() {
        YStreamOffset offset1 = new YStreamOffset("1000", "1001");
        YStreamOffset offset2 = new YStreamOffset("1001", "1001");

        Assertions.assertNotEquals(offset1, offset2);
    }

    @Test
    public void testEqualsNotEqualCommitScn() {
        YStreamOffset offset1 = new YStreamOffset("1000", "1001");
        YStreamOffset offset2 = new YStreamOffset("1000", "1002");

        Assertions.assertNotEquals(offset1, offset2);
    }

    @Test
    public void testEqualsNull() {
        YStreamOffset offset = new YStreamOffset("1000", "1001");
        Assertions.assertNotEquals(offset, null);
    }

    @Test
    public void testEqualsDifferentClass() {
        YStreamOffset offset = new YStreamOffset("1000", "1001");
        Assertions.assertNotEquals(offset, "string");
    }

    @Test
    public void testHashCode() {
        YStreamOffset offset1 = new YStreamOffset("1000", "1001");
        YStreamOffset offset2 = new YStreamOffset("1000", "1001");

        Assertions.assertEquals(offset1.hashCode(), offset2.hashCode());
    }

    @Test
    public void testHashCodeDifferent() {
        YStreamOffset offset1 = new YStreamOffset("1000", "1001");
        YStreamOffset offset2 = new YStreamOffset("1001", "1001");

        Assertions.assertNotEquals(offset1.hashCode(), offset2.hashCode());
    }

    @Test
    public void testLargeScn() {
        String largeScn = "999999999999999";
        YStreamOffset offset = new YStreamOffset(largeScn, largeScn);

        Assertions.assertEquals(largeScn, offset.getScn());
        Assertions.assertEquals(largeScn, offset.getCommitScn());
    }

    @Test
    public void testZeroScn() {
        YStreamOffset offset = new YStreamOffset("0", "0");

        Assertions.assertEquals("0", offset.getScn());
        Assertions.assertEquals("0", offset.getCommitScn());
    }

    @Test
    public void testGetScn() {
        YStreamOffset offset = new YStreamOffset("12345", "12346");
        Assertions.assertEquals("12345", offset.getScn());
    }

    @Test
    public void testGetCommitScn() {
        YStreamOffset offset = new YStreamOffset("12345", "12346");
        Assertions.assertEquals("12346", offset.getCommitScn());
    }

    @Test
    public void testCompareToWithLargeNumbers() {
        YStreamOffset offset1 = new YStreamOffset("999999999999999", "999999999999999");
        YStreamOffset offset2 = new YStreamOffset("1", "1");

        Assertions.assertTrue(offset1.compareTo(offset2) > 0);
    }

    @Test
    public void testOffsetMapNotNull() {
        YStreamOffset offset = new YStreamOffset("100", "101");
        Assertions.assertNotNull(offset.getOffset());
    }

    @Test
    public void testOffsetMapContainsScn() {
        YStreamOffset offset = new YStreamOffset("100", "101");
        Assertions.assertTrue(offset.getOffset().containsKey("scn"));
    }

    @Test
    public void testOffsetMapContainsCommitScn() {
        YStreamOffset offset = new YStreamOffset("100", "101");
        Assertions.assertTrue(offset.getOffset().containsKey("commit_scn"));
    }
}