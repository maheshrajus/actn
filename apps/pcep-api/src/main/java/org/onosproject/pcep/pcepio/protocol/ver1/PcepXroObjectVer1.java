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

import com.google.common.base.MoreObjects;
import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.pcep.pcepio.exceptions.PcepParseException;
import org.onosproject.pcep.pcepio.protocol.PcepXroObject;
import org.onosproject.pcep.pcepio.types.AutonomousSystemNumberSubObject;
import org.onosproject.pcep.pcepio.types.IPv4SubObject;
import org.onosproject.pcep.pcepio.types.IPv6SubObject;
import org.onosproject.pcep.pcepio.types.PcepObjectHeader;
import org.onosproject.pcep.pcepio.types.PcepValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Objects;

/**
 * Provides PCEP Xro Object.
 */
public class PcepXroObjectVer1 implements PcepXroObject {
    /*
     * rfc 5521
     0                   1                   2                   3
     0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     | Object-Class  |   OT  |Res|P|I|   Object Length (bytes)       |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |        Reserved               |   Flags                     |F|
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |                                                               |
     //                        (Subobjects)                         //
     |                                                               |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

     Subobjects: The XRO is made up of one or more subobject(s).  An XRO
     with no subobjects MUST NOT be sent and SHOULD be ignored on receipt.
     The subobjects are encoded as follows:

     IPv4 prefix Subobject

     0                   1                   2                   3
     0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |X|  Type = 1   |     Length    | IPv4 address (4 bytes)        |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     | IPv4 address (continued)      | Prefix Length |   Attribute   |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

     IPv6 prefix Subobject

     0                   1                   2                   3
     0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |X|  Type = 2   |     Length    | IPv6 address (16 bytes)       |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     | IPv6 address (continued)                                      |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     | IPv6 address (continued)                                      |
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     | IPv6 address (continued)                                      |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     | IPv6 address (continued)      | Prefix Length |   Attribute   |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

     Unnumbered Interface ID Subobject

     0                   1                   2                   3
     0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |X|  Type = 3   |     Length    |    Reserved   |  Attribute    |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |                        TE Router ID                           |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |                        Interface ID                           |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+


     Autonomous System Number Subobject

     0                   1                   2                   3
     0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |X|  Type = 4   |     Length    |      2-Octet AS Number        |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

     SRLG Subobject

     0                   1                   2                   3
     0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |X|  Type = 5   |     Length    |       SRLG Id (4 bytes)       |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |      SRLG Id (continued)      |    Reserved   |  Attribute    |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

    */

    protected static final Logger log = LoggerFactory.getLogger(PcepXroObjectVer1.class);

    public static final byte XRO_OBJ_TYPE = 1;
    public static final byte XRO_OBJ_CLASS = 17;
    public static final short XRO_OBJ_MINIMUM_LENGTH = 12;
    public static final int OBJECT_HEADER_LENGTH = 4;
    public static final boolean FLAG_DEFAULT_VALUE = false;
    public static final int TYPE_SHIFT_VALUE = 0x7F;

    public static final PcepObjectHeader DEFAULT_XRO_OBJECT_HEADER = new PcepObjectHeader(XRO_OBJ_CLASS, XRO_OBJ_TYPE,
            PcepObjectHeader.REQ_OBJ_OPTIONAL_PROCESS, PcepObjectHeader.RSP_OBJ_PROCESSED, XRO_OBJ_MINIMUM_LENGTH);

    private boolean bFflag;
    private PcepObjectHeader xroObjHeader;
    private LinkedList<PcepValueType> subObjectList = new LinkedList<>();

    public static final byte FBIT_SET = 1;
    public static final byte FBIT_RESET = 0;

    /**
     * reset variables.
     */
    public PcepXroObjectVer1() {
        this.xroObjHeader = null;
        this.subObjectList = null;
    }

    /**
     * Constructor to initialize parameters of XRO object.
     *
     * @param xroObjHeader  XRO object header
     * @param llSubObjects list of sub objects.
     */
    public PcepXroObjectVer1(PcepObjectHeader xroObjHeader, boolean bFflag, LinkedList<PcepValueType> llSubObjects) {

        this.bFflag = bFflag;
        this.xroObjHeader = xroObjHeader;
        this.subObjectList = llSubObjects;
    }

    /**
     * Returns XRO object header.
     *
     * @return xroObjHeader XRO object header
     */
    public PcepObjectHeader getXroObjHeader() {
        return this.xroObjHeader;
    }

    /**
     * Sets Object Header.
     *
     * @param obj XRO object header
     */
    public void setXroObjHeader(PcepObjectHeader obj) {
        this.xroObjHeader = obj;
    }

    @Override
    public LinkedList<PcepValueType> getSubObjects() {
        return this.subObjectList;
    }

    @Override
    public void setSubObjects(LinkedList<PcepValueType> llSubObjects) {
        this.subObjectList = llSubObjects;
    }

    @Override
    public boolean getFflag() {
        return this.bFflag;
    }

    @Override
    public void setFflag(boolean bFflag) {
        this.bFflag = bFflag;
    }

    /**
     * Reads from channel buffer and returns object of PcepXroObject.
     *
     * @param cb channel buffer.
     * @return  object of PcepEroObject
     * @throws PcepParseException when XRO object is not present in channel buffer
     */
    public static PcepXroObject read(ChannelBuffer cb) throws PcepParseException {

        log.debug("XroObject::read");
        PcepObjectHeader xroObjHeader;
        short reserved;
        short flags;
        int length;
        boolean bFflag;
        LinkedList<PcepValueType> subObjectList = new LinkedList<>();

        xroObjHeader = PcepObjectHeader.read(cb);

        if (xroObjHeader.getObjClass() != XRO_OBJ_CLASS) {
            throw new PcepParseException("XRO object expected. " +
                                                 "But received " + xroObjHeader.getObjClass());
        }

        //take only AssociationObject buffer.
        length = xroObjHeader.getObjLen() - OBJECT_HEADER_LENGTH;
        ChannelBuffer tempCb = cb.readBytes(length);
        reserved = tempCb.readShort();
        flags = tempCb.readShort();
        bFflag = ((flags & 0x1) != 0);
        length = length - OBJECT_HEADER_LENGTH;

        if (length > OBJECT_HEADER_LENGTH) {
            subObjectList = parseSubObjects(tempCb);
        }

        return new PcepXroObjectVer1(xroObjHeader, bFflag, subObjectList);
    }

    /**
     * Parse list of Sub Objects.
     *
     * @param cb channel buffer
     * @return list of Sub Objects
     * @throws PcepParseException when fails to parse sub object list
     */
    protected static LinkedList<PcepValueType> parseSubObjects(ChannelBuffer cb) throws PcepParseException {

        LinkedList<PcepValueType> subObjectList = new LinkedList<>();

        while (0 < cb.readableBytes()) {

            //check the Type of the TLV
            short type = cb.readByte();
            type = (short) (type & (TYPE_SHIFT_VALUE));
            byte hLength = cb.readByte();

            PcepValueType subObj;

            switch (type) {

                case IPv4SubObject.TYPE:
                    subObj = IPv4SubObject.read(cb);
                    break;
                case IPv6SubObject.TYPE:
                    byte[] ipv6Value = new byte[IPv6SubObject.VALUE_LENGTH];
                    cb.readBytes(ipv6Value, 0, IPv6SubObject.VALUE_LENGTH);
                    subObj = new IPv6SubObject(ipv6Value);
                    break;
                case AutonomousSystemNumberSubObject.TYPE:
                    subObj = AutonomousSystemNumberSubObject.read(cb);
                    break;
                default:
                    throw new PcepParseException("Unexpected sub object. Type: " + (int) type);
            }
            // Check for the padding
            int pad = hLength % 4;
            if (0 < pad) {
                pad = 4 - pad;
                if (pad <= cb.readableBytes()) {
                    cb.skipBytes(pad);
                }
            }

            subObjectList.add(subObj);
        }
        if (0 < cb.readableBytes()) {
            throw new PcepParseException("Subobject parsing error. Extra bytes received.");
        }
        return subObjectList;
    }

    @Override
    public int write(ChannelBuffer cb) throws PcepParseException {

        //write Object header
        int objStartIndex = cb.writerIndex();

        int objLenIndex = xroObjHeader.write(cb);

        if (objLenIndex <= 0) {
            throw new PcepParseException("Failed to write XRO object header. Index " + objLenIndex);
        }

        //write Flags
        byte bFlag;

        bFlag = (bFflag) ? FBIT_SET : FBIT_RESET;

        cb.writeInt(bFlag);

        ListIterator<PcepValueType> listIterator = subObjectList.listIterator();

        while (listIterator.hasNext()) {
            listIterator.next().write(cb);
        }

        //Update object length now
        int length = cb.writerIndex() - objStartIndex;
        cb.setShort(objLenIndex, (short) length);
        //will be helpful during print().
        xroObjHeader.setObjLen((short) length);

        //As per RFC the length of object should be multiples of 4
        int pad = length % 4;

        if (pad != 0) {
            pad = 4 - pad;
            for (int i = 0; i < pad; i++) {
                cb.writeByte((byte) 0);
            }
            length = length + pad;
        }

        objLenIndex = cb.writerIndex();
        return objLenIndex;
    }

    /**
     * Builder class for PCEP XRO object.
     */
    public static class Builder implements PcepXroObject.Builder {

        private boolean bIsHeaderSet = false;

        private PcepObjectHeader xroObjHeader;
        LinkedList<PcepValueType> subObjectList = new LinkedList<>();

        private boolean bIsPFlagSet = false;
        private boolean bPFlag;

        private boolean bIsIFlagSet = false;
        private boolean bIFlag;

        private boolean bIsFflagSet = false;
        private boolean bFflag;

        @Override
        public PcepXroObject build() {

            PcepObjectHeader eroObjHeader = this.bIsHeaderSet ? this.xroObjHeader : DEFAULT_XRO_OBJECT_HEADER;

            if (bIsPFlagSet) {
                eroObjHeader.setPFlag(bPFlag);
            }

            if (bIsIFlagSet) {
                eroObjHeader.setIFlag(bIFlag);
            }

            boolean bFflag = this.bIsFflagSet ? this.bFflag : FLAG_DEFAULT_VALUE;

            return new PcepXroObjectVer1(xroObjHeader, bFflag, this.subObjectList);
        }

        @Override
        public PcepObjectHeader getXroObjHeader() {
            return this.xroObjHeader;
        }

        @Override
        public Builder setXroObjHeader(PcepObjectHeader obj) {
            this.xroObjHeader = obj;
            this.bIsHeaderSet = true;
            return this;
        }

        @Override
        public boolean getFflag() {
            return this.bFflag;
        }

        @Override
        public Builder setFflag(boolean bFflag) {
            this.bFflag = bFflag;
            this.bIsFflagSet = true;
            return this;
        }

        @Override
        public LinkedList<PcepValueType> getSubObjects() {
            return this.subObjectList;
        }

        @Override
        public Builder setSubObjects(LinkedList<PcepValueType> llSubObjects) {
            this.subObjectList = llSubObjects;
            return this;
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
    public int hashCode() {
        return Objects.hash(xroObjHeader, subObjectList);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).omitNullValues()
                .add("EroObjHeader", xroObjHeader)
                .add("FFlag", bFflag)
                .add("SubObjects", subObjectList)
                .toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof PcepEroObjectVer1) {
            int countObjSubTlv = 0;
            int countOtherSubTlv = 0;
            boolean isCommonSubTlv = true;
            PcepXroObjectVer1 other = (PcepXroObjectVer1) obj;
            Iterator<PcepValueType> objListIterator = other.subObjectList.iterator();
            countOtherSubTlv = other.subObjectList.size();
            countObjSubTlv = subObjectList.size();
            if (countObjSubTlv != countOtherSubTlv) {
                return false;
            } else {
                while (objListIterator.hasNext() && isCommonSubTlv) {
                    PcepValueType subTlv = objListIterator.next();
                    if (subObjectList.contains(subTlv)) {
                        isCommonSubTlv = Objects.equals(subObjectList.get(subObjectList.indexOf(subTlv)),
                                         other.subObjectList.get(other.subObjectList.indexOf(subTlv)));
                    } else {
                        isCommonSubTlv = false;
                    }
                }
                return isCommonSubTlv && Objects.equals(xroObjHeader, other.xroObjHeader);
            }
        }
        return false;
    }
}
