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

package org.onosproject.pcep.pcepio.protocol.ver1;

import java.util.LinkedList;
import java.util.ListIterator;

import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.pcep.pcepio.exceptions.PcepParseException;
import org.onosproject.pcep.pcepio.protocol.PcepAssociationObject;
import org.onosproject.pcep.pcepio.types.PcepErrorDetailInfo;
import org.onosproject.pcep.pcepio.types.PcepObjectHeader;
import org.onosproject.pcep.pcepio.types.PcepValueType;
import org.onosproject.pcep.pcepio.types.VirtualNetworkTlv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;

/**
 * Provides PCEP ASSOCIATION obejct.
 */
public class PcepAssociationObjectVer1 implements PcepAssociationObject {

    /*
     * ref : draft-leedhody-pce-vn-association, section : 4
     0                   1                   2                   3
       0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
      |         Reserved              |            Flags            |R|
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
      |      Association type         |      Association ID           |
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
      |              IPv4 Association Source                          |
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
      //                   Optional TLVs                             //
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

     */
    protected static final Logger log = LoggerFactory.getLogger(PcepAssociationObjectVer1.class);

    public static final byte ASSOCIATION_OBJ_TYPE = 1;
    public static final byte ASSOCIATION_OBJ_CLASS = (byte) 255;
    public static final byte ASSOCIATION_OBJECT_VERSION = 1;
    public static final short ASSOCIATION_OBJ_MINIMUM_LENGTH = 16;
    public static final int MINIMUM_COMMON_HEADER_LENGTH = 4;
    public static final boolean FLAG_DEFAULT_VALUE = false;

    static final PcepObjectHeader DEFAULT_ASSOCIATION_OBJECT_HEADER = new PcepObjectHeader(ASSOCIATION_OBJ_CLASS,
                                                                                           ASSOCIATION_OBJ_TYPE,
            PcepObjectHeader.REQ_OBJ_OPTIONAL_PROCESS, PcepObjectHeader.RSP_OBJ_PROCESSED,
                                                             ASSOCIATION_OBJ_MINIMUM_LENGTH);

    private PcepObjectHeader associationObjHeader;
    private static int flags;
    private boolean bRFlag;
    private int associationSource;
    private short associationID;
    private short associationType;

    //Optional TLV
    private LinkedList<PcepValueType> llOptionalTlv;
    public static final byte BBIT_SET = 1;
    public static final byte BBIT_RESET = 0;

    /**
     * Constructor to initialize member variables.
     *
     * @param associationObjHeader association object header
     * @param bRFlag R flag
     * @param associationSource association Source
     * @param associationID association ID
     * @param associationType association Type
     * @param llOptionalTlv list of optional tlv
     */
    public PcepAssociationObjectVer1(PcepObjectHeader associationObjHeader, boolean bRFlag, int associationSource,
            short associationID, short associationType, LinkedList<PcepValueType> llOptionalTlv) {

        this.associationObjHeader = associationObjHeader;
        this.bRFlag = bRFlag;
        this.associationType = associationType;
        this.associationID = associationID;
        this.associationSource = associationSource;
        this.llOptionalTlv = llOptionalTlv;
    }

    /**
     * sets the ASSOCIATION object header.
     *
     * @param obj association object header
     */
    public void setAssociationObjHeader(PcepObjectHeader obj) {
        this.associationObjHeader = obj;
    }

    @Override
    public void setAssociationID(short associationID) {
        this.associationID = associationID;
    }

    @Override
    public void setRFlag(boolean bRFlag) {
        this.bRFlag = bRFlag;
    }

    @Override
    public void setAssociationType(short associationType) {
        this.associationType = associationType;
    }

    @Override
    public void setAssociationSource(int associationSource) {
        this.associationSource = associationSource;
    }
    /**
     * Returns ASSOCIATION object header.
     *
     * @return associationObjHeader
     */
    public PcepObjectHeader getAssociationObjHeader() {
        return this.associationObjHeader;
    }

    @Override
    public short getAssociationID() {
        return this.associationID;
    }

    @Override
    public short getAssociationType() {
        return this.associationType;
    }

    @Override
    public int getAssociationSource() {
        return this.associationSource;
    }

    @Override
    public boolean getRFlag() {
        return this.bRFlag;
    }

    @Override
    public void setOptionalTlv(LinkedList<PcepValueType> llOptionalTlv) {
        this.llOptionalTlv = llOptionalTlv;

    }

    @Override
    public LinkedList<PcepValueType> getOptionalTlv() {
        return this.llOptionalTlv;
    }

    /**
     * Reads from channel buffer and returns instance of PCEP ASSOCIATION object.
     *
     * @param cb of channel buffer.
     * @return PCEP ASSOCIATION object
     * @throws PcepParseException when association object is not received in channel buffer
     */
    public static PcepAssociationObject read(ChannelBuffer cb) throws PcepParseException {

        log.debug("AssociationObject::read");
        PcepObjectHeader associationObjHeader;
        boolean bRFlag;
        short associationID;
        short associationType;

        int associationSource;
        short flags;
        short reserved;
        LinkedList<PcepValueType> llOptionalTlv = new LinkedList<>();

        associationObjHeader = PcepObjectHeader.read(cb);

        if (associationObjHeader.getObjClass() != ASSOCIATION_OBJ_CLASS) {
            throw new PcepParseException("ASSOCIATION object expected. " +
                                                 "But received " + associationObjHeader.getObjClass());
        }

        //take only AssociationObject buffer.
        ChannelBuffer tempCb = cb.readBytes(associationObjHeader.getObjLen() - MINIMUM_COMMON_HEADER_LENGTH);
        reserved = tempCb.readShort();
        flags = tempCb.readShort();
        bRFlag = ((flags & 0x1) != 0);

        associationType = tempCb.readShort();
        associationID = tempCb.readShort();
        associationSource = tempCb.readInt();

        llOptionalTlv = parseOptionalTlv(tempCb);

        return new PcepAssociationObjectVer1(associationObjHeader, bRFlag, associationSource,
                                             associationID, associationType, llOptionalTlv);
    }

    @Override
    public int write(ChannelBuffer cb) throws PcepParseException {

        int objStartIndex = cb.writerIndex();

        //write common header
        int objLenIndex = associationObjHeader.write(cb);

        //write Flags
        byte bFlag;

        bFlag = (bRFlag) ? BBIT_SET : BBIT_RESET;

        cb.writeInt(bFlag);

        //write association type
        cb.writeShort(associationType);

        //write association ID
        cb.writeShort(associationID);

        //write association source
        cb.writeInt(associationSource);

        // Add optional TLV
        if (!packOptionalTlv(cb)) {
            throw new PcepParseException("Failed to write association tlv to channel buffer.");
        }

        //now write ASSOCIATION Object Length
        cb.setShort(objLenIndex, (short) (cb.writerIndex() - objStartIndex));

        return cb.writerIndex();
    }

    /**
     * Parse Optional TLvs from the channel buffer.
     *
     * @param cb of type channel buffer
     * @return list of optional tlvs
     * @throws PcepParseException when unsupported tlv is received in association object
     */
    public static LinkedList<PcepValueType> parseOptionalTlv(ChannelBuffer cb) throws PcepParseException {

        LinkedList<PcepValueType> llOutOptionalTlv = new LinkedList<>();
        boolean bvirtualNetworkTlvPresent = false;

        while (MINIMUM_COMMON_HEADER_LENGTH <= cb.readableBytes()) {

            PcepValueType tlv;
            short hType = cb.readShort();
            short hLength = cb.readShort();

            switch (hType) {
            case VirtualNetworkTlv.TYPE:
                log.info("parsing VN Tlv, length: " + hLength);
                if (cb.readableBytes() < hLength) {
                    throw new PcepParseException("Length is not valid in VirtualNetworkTlv");
                }
                tlv = VirtualNetworkTlv.read(cb, hLength);
                bvirtualNetworkTlvPresent = true;
                break;
            default:
                throw new PcepParseException("Unsupported TLV received in Association Object.");
            }

            // Check for the padding
            int pad = hLength % 4;
            if (0 < pad) {
                pad = 4 - pad;
                if (pad <= cb.readableBytes()) {
                    cb.skipBytes(pad);
                }
            }
            llOutOptionalTlv.add(tlv);
        }

        if (!bvirtualNetworkTlvPresent) {
            throw new PcepParseException(PcepErrorDetailInfo.ERROR_TYPE_6,
                                         PcepErrorDetailInfo.ERROR_VALUE_255);
        }

        return llOutOptionalTlv;
    }

    /**
     * Writes optional tlvs to channel buffer.
     *
     * @param cb of type channel buffer
     * @return true if writing optional tlv to channel buffer is success.
     */
    protected boolean packOptionalTlv(ChannelBuffer cb) {

        ListIterator<PcepValueType> listIterator = llOptionalTlv.listIterator();

        while (listIterator.hasNext()) {
            PcepValueType tlv = listIterator.next();

            if (tlv == null) {
                log.debug("tlv is null from OptionalTlv list");
                continue;
            }
            tlv.write(cb);

            // need to take care of padding
            int pad = tlv.getLength() % 4;

            if (0 != pad) {
                pad = 4 - pad;
                for (int i = 0; i < pad; ++i) {
                    cb.writeByte((byte) 0);
                }
            }
        }

        return true;
    }

    /**
     * Builder class for PCEP association Object.
     */
    public static class Builder implements PcepAssociationObject.Builder {
        private boolean bIsHeaderSet = false;
        private boolean bIsAssociationIdSet = false;
        private boolean bIsAssociationSourceSet = false;
        private boolean bIsRFlagSet = false;
        private boolean bIsAssociationTypeSet = false;

        private PcepObjectHeader associationObjHeader;
        private int associationSource;
        private short associationId;
        private short associationType;
        private boolean bRFlag;
        LinkedList<PcepValueType> llOptionalTlv = new LinkedList<>();

        private boolean bIsPFlagSet = false;
        private boolean bPFlag;

        private boolean bIsIFlagSet = false;
        private boolean bIFlag;

        @Override
        public PcepAssociationObject build() throws PcepParseException {
            PcepObjectHeader associationObjHeader
                    = this.bIsHeaderSet ? this.associationObjHeader : DEFAULT_ASSOCIATION_OBJECT_HEADER;

            boolean bRFlag = this.bIsRFlagSet ? this.bRFlag : FLAG_DEFAULT_VALUE;

            if (!this.bIsAssociationTypeSet) {
                throw new PcepParseException("Association Type not set while building Association Object.");
            }

            if (!this.bIsAssociationIdSet) {
                throw new PcepParseException("Association ID not set while building Association Object.");
            }

            if (!this.bIsAssociationSourceSet) {
                throw new PcepParseException("IPV4 Association Source not set while building Association Object.");
            }

            if (bIsPFlagSet) {
                associationObjHeader.setPFlag(bPFlag);
            }

            if (bIsIFlagSet) {
                associationObjHeader.setIFlag(bIFlag);
            }

            return new PcepAssociationObjectVer1(associationObjHeader, bRFlag, this.associationSource,
                                                 this.associationId, this.associationType, this.llOptionalTlv);
        }

        @Override
        public PcepObjectHeader getAssociationObjHeader() {
            return this.associationObjHeader;
        }

        @Override
        public Builder setAssociationObjHeader(PcepObjectHeader obj) {
            this.associationObjHeader = obj;
            this.bIsHeaderSet = true;
            return this;
        }

        @Override
        public short getAssociationID() {
            return this.associationId;
        }

        @Override
        public Builder setAssociationID(short associationID) {
            this.associationId = associationID;
            this.bIsAssociationIdSet = true;
            return this;
        }

        @Override
        public short getAssociationType() {
            return this.associationType;
        }

        @Override
        public Builder setAssociationType(short associationType) {
            this.associationType = associationType;
            this.bIsAssociationTypeSet = true;
            return this;
        }

        @Override
        public int getAssociationSource() {
            return this.associationSource;
        }

        @Override
        public Builder setAssociationSource(int associationSource) {
            this.associationSource = associationSource;
            this.bIsAssociationSourceSet = true;
            return this;
        }

        @Override
        public boolean getRFlag() {
            return this.bRFlag;
        }

        @Override
        public Builder setRFlag(boolean bRFlag) {
            this.bRFlag = bRFlag;
            this.bIsRFlagSet = true;
            return this;
        }

        @Override
        public Builder setOptionalTlv(LinkedList<PcepValueType> llOptionalTlv) {
            this.llOptionalTlv = llOptionalTlv;
            return this;
        }

        @Override
        public LinkedList<PcepValueType> getOptionalTlv() {
            return this.llOptionalTlv;
        }

        @Override
        public Builder setPFlag(boolean value) {
            this.bPFlag = value;
            this.bIsPFlagSet = true;
            return this;
        }

        @Override
        public Builder setIFlag(boolean value) {
            this.bIFlag = value;
            this.bIsIFlagSet = true;
            return this;
        }

    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .add("RFlag", bRFlag)
                .add("AssociationType", associationType)
                .add("AssociationId", associationID)
                .add("AssociationSource", associationSource)
                .add("OptionalTlvList", llOptionalTlv)
                .toString();
    }
}
