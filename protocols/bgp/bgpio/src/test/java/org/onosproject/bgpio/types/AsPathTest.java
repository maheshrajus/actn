/*
 * Copyright 2015-present Open Networking Laboratory
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
package org.onosproject.bgpio.types;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.google.common.testing.EqualsTester;

/**
 * Test for AsPath BGP Path Attribute.
 */
public class AsPathTest {
    //Two scenarios aspath set and sequence
    private final List<Integer> aspathSet1 = new ArrayList<>();
    private final List<Integer> aspathSet2 = new ArrayList<>();
    private final List<Integer> aspathSeq1 = new ArrayList<>();
    private final List<Integer> aspathSeq2 = new ArrayList<>();
    private final AsPath attr1 = new AsPath(aspathSet1, null, false);
    private final AsPath sameAsAttr1 = new AsPath(aspathSet1, null, false);
    private final AsPath attr2 = new AsPath(aspathSet2, null, false);
    private final AsPath attr3 = new AsPath(null, aspathSeq1, false);
    private final AsPath sameAsAttr3 = new AsPath(null, aspathSeq1, false);
    private final AsPath attr4 = new AsPath(null, aspathSeq2, false);

    @Test
    public void basics() {
        aspathSet1.add(100);
        aspathSet1.add(300);
        aspathSet2.add(200);
        aspathSeq2.add(400);
        aspathSeq1.add(300);
        new EqualsTester()
        .addEqualityGroup(attr1, sameAsAttr1)
        .addEqualityGroup(attr2)
        .addEqualityGroup(attr3, sameAsAttr3)
        .addEqualityGroup(attr4)
        .testEquals();
    }
}
