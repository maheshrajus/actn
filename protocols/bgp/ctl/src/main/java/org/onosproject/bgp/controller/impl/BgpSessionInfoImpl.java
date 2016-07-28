/*
 * Copyright 2015-present Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.onosproject.bgp.controller.impl;

import java.util.List;
import java.util.ListIterator;
import org.onosproject.bgp.controller.BgpCfg;
import org.onosproject.bgp.controller.BgpId;
import org.onosproject.bgp.controller.BgpSessionInfo;
import org.onosproject.bgpio.protocol.BgpVersion;
import org.onosproject.bgpio.types.BgpValueType;
import org.onosproject.bgpio.types.FourOctetAsNumCapabilityTlv;
import org.onosproject.bgpio.types.MultiProtocolExtnCapabilityTlv;
import org.onosproject.bgpio.types.RpdCapabilityTlv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class maintains BGP peer session info.
 */
public class BgpSessionInfoImpl implements BgpSessionInfo {

    protected final Logger log = LoggerFactory.getLogger(BgpSessionInfoImpl.class);
    private BgpId remoteBgpId;
    private BgpVersion remoteBgpVersion;
    private long remoteBgpASNum;
    private short remoteBgpholdTime;
    private int remoteBgpIdentifier;
    private short negotiatedholdTime;
    private boolean isIbgpSession;
    private List<BgpValueType> remoteBgpCapability;
    private BgpCfg bgpConfig;
    private Boolean is4octetCapable;

    /**
     * Initialize session info.
     *
     *@param remoteBgpId remote peer id
     *@param remoteBgpVersion remote peer version
     *@param remoteBgpASNum remote peer AS number
     *@param remoteBgpholdTime remote peer hold time
     *@param remoteBgpIdentifier remote peer identifier
     *@param negotiatedholdTime negotiated hold time
     *@param isIbgpSession session type ibgp/ebgp
     *@param remoteBgpCapability remote peer capabilities
     *@param bgpConfig BGP configurations
     */
    public BgpSessionInfoImpl(BgpId remoteBgpId, BgpVersion remoteBgpVersion, long remoteBgpASNum,
                              short remoteBgpholdTime, int remoteBgpIdentifier, short negotiatedholdTime,
                              boolean isIbgpSession, List<BgpValueType> remoteBgpCapability,
                              BgpCfg bgpConfig) {
        this.remoteBgpId = remoteBgpId;
        this.remoteBgpVersion = remoteBgpVersion;
        this.remoteBgpASNum = remoteBgpASNum;
        this.remoteBgpholdTime = remoteBgpholdTime;
        this.remoteBgpIdentifier = remoteBgpIdentifier;
        this.negotiatedholdTime = negotiatedholdTime;
        this.isIbgpSession = isIbgpSession;
        this.remoteBgpCapability = remoteBgpCapability;
        this.bgpConfig = bgpConfig;
    }

    @Override
    public boolean is4octetCapable() {
        if (is4octetCapable != null) {
            return is4octetCapable;
        }
        is4octetCapable = false;
        if (!bgpConfig.getLargeASCapability()) {
            return is4octetCapable;
        }
        for (BgpValueType attr : remoteBgpCapability) {
            if (attr instanceof FourOctetAsNumCapabilityTlv) {
                is4octetCapable = true;
                return true;
            }
        }
        return is4octetCapable;
    }

    @Override
    public final boolean isCapabilitySupported(short type, short afi, byte sAfi) {

        List<BgpValueType> capability = remoteBgpCapability;
        ListIterator<BgpValueType> listIterator = capability.listIterator();

        while (listIterator.hasNext()) {
            BgpValueType tlv = listIterator.next();

            if (tlv.getType() == type) {
                if (tlv.getType() == MultiProtocolExtnCapabilityTlv.TYPE) {
                    MultiProtocolExtnCapabilityTlv temp = (MultiProtocolExtnCapabilityTlv) tlv;
                    if ((temp.getAfi() == afi) && (temp.getSafi() == sAfi)) {
                        return true;
                    }
                } else if (tlv.getType() == RpdCapabilityTlv.TYPE) {
                    RpdCapabilityTlv temp = (RpdCapabilityTlv) tlv;
                    if ((temp.getAfi() == afi) && (temp.getSafi() == sAfi)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public List<BgpValueType> remoteBgpCapability() {
        return remoteBgpCapability;
    }

    @Override
    public boolean isIbgpSession() {
        return isIbgpSession;
    }

    @Override
    public short negotiatedholdTime() {
        return negotiatedholdTime;
    }

    @Override
    public BgpId remoteBgpId() {
        return remoteBgpId;
    }

    @Override
    public BgpVersion remoteBgpVersion() {
        return remoteBgpVersion;
    }

    @Override
    public long remoteBgpASNum() {
        return remoteBgpASNum;
    }

    @Override
    public short remoteBgpHoldTime() {
        return remoteBgpholdTime;
    }

    @Override
    public int remoteBgpIdentifier() {
        return remoteBgpIdentifier;
    }
}
