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

package org.onosproject.pcep.pcepio.types;

import java.util.Objects;

import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.pcep.pcepio.protocol.PcepVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;

/**
 * Provides VirtualNetworkTlv.
 */
public class VirtualNetworkTlv implements PcepValueType {

    /*
     *    VIRTUAL-NETWORK TLV format
     *    Reference :PCEP Extensions for Stateful PCE draft-leedhody-pce-vn-association
     *
        0                   1                   2                   3

        0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1

        +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
        |           Type=[TBD2]         |       Length (variable)       |
        +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
        |                                                               |
        //                   Virtual Network Name                      //
        |                                                               |
        +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

     */
    protected static final Logger log = LoggerFactory.getLogger(VirtualNetworkTlv.class);

    public static final short TYPE = (short) 65288;
    private short hLength;

    private final byte[] rawValue;

    /**
     * Constructor to initialize raw Value.
     *
     * @param rawValue Virtual.network.name
     */
    public VirtualNetworkTlv(byte[] rawValue) {
        this.rawValue = rawValue;
        this.hLength = (short) rawValue.length;
    }

    /**
     * Constructor to initialize raw Value.
     *
     * @param rawValue Virtual network name
     * @param hLength length of Virtual network name
     */
    public VirtualNetworkTlv(byte[] rawValue, short hLength) {
        this.rawValue = rawValue;
        if (0 == hLength) {
            this.hLength = (short) rawValue.length;
        } else {
            this.hLength = hLength;
        }
    }

    /**
     * Creates an object of VirtualNetworkTlv.
     *
     * @param raw Virtual Network name
     * @param hLength length of Virtual Network name
     * @return object of VirtualNetworkTlv
     */
    public static VirtualNetworkTlv of(final byte[] raw, short hLength) {
        return new VirtualNetworkTlv(raw, hLength);
    }

    /**
     * Returns Virtual Network name.
     *
     * @return Virtual Network name byte array
     */
    public byte[] getValue() {
        return rawValue;
    }

    @Override
    public PcepVersion getVersion() {
        return PcepVersion.PCEP_1;
    }

    @Override
    public short getType() {
        return TYPE;
    }

    @Override
    public short getLength() {
        return hLength;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rawValue);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof VirtualNetworkTlv) {
            VirtualNetworkTlv other = (VirtualNetworkTlv) obj;
            return Objects.equals(this.rawValue, other.rawValue);
        }
        return false;
    }

    @Override
    public int write(ChannelBuffer c) {
        int iLenStartIndex = c.writerIndex();
        c.writeShort(TYPE);
        c.writeShort(hLength);
        c.writeBytes(rawValue);
        return c.writerIndex() - iLenStartIndex;
    }

    /**
     * Reads channel buffer and returns object of VirtualNetworkTlv.
     *
     * @param c of type channel buffer
     * @param hLength length of bytes to read
     * @return object of VirtualNetworkTlv
     */
    public static VirtualNetworkTlv read(ChannelBuffer c, short hLength) {
        byte[] virtualNetworkName = new byte[hLength];
        c.readBytes(virtualNetworkName, 0, hLength);
        return new VirtualNetworkTlv(virtualNetworkName, hLength);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .add("VirtualNetworkName ", rawValue)
                .toString();
    }
}
