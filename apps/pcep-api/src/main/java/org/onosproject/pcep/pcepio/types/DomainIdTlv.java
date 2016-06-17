package org.onosproject.pcep.pcepio.types;

import org.jboss.netty.buffer.ChannelBuffer;
import org.onosproject.pcep.pcepio.protocol.PcepVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Created by root1 on 6/6/16.
 */
public class DomainIdTlv implements PcepValueType {
    /* Domain-ID TLV Format
     *
     * Reference : draft-ietf-pce-hierarchy-extensions-02

    0                   1                   2                   3
    0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    |           Domain Type         |            Reserved           |
    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    |                       Domain ID                               |
    //                                                             //
    |                                                               |
    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+

       Domain Type (8 bits): Indicates the domain type.  Two types of domain
       are currently defined:

       o  Type=1: the Domain ID field carries an IGP Area ID.

       o  Type=2: the Domain ID field carries an AS number.

       Domain ID (variable): Indicates an IGP Area ID or AS number.  It can
       be 2 bytes, 4 bytes or 8 bytes long depending on the domain
       identifier used.


        domainType
            1  ---> 2 byte As
            2  ---> 4 byte As
            3  ---> ospf area id (4 bytes)
            4  ---> isis area id (4 or 8)
    */

    protected static final Logger log = LoggerFactory.getLogger(DomainIdTlv.class);

    public static final short TYPE = (short) 65304;
    public static short length = 8;
    public short dmnType;
    public long  dmnId;

    public DomainIdTlv(short domainType, int domainId) {
        this.dmnType = domainType;
        this.dmnId = domainId;

        /* need to increment length if domain type is isis */
    }


    @Override
    public PcepVersion getVersion() {
        return PcepVersion.PCEP_1;
    }

    @Override
    public short getType()  {
        return TYPE;
    }

    @Override
    public short getLength() {
        return length;
    }

    @Override
    public int hashCode() {

        return Objects.hash(dmnType, dmnId);

    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof DomainIdTlv) {
            DomainIdTlv other = (DomainIdTlv) obj;
                return Objects.equals(this.dmnType, other.dmnType) && Objects.equals(this.dmnId, other.dmnId);
        }

        return false;
    }

    @Override
    public int write(ChannelBuffer c) {
        int iLenStartIndex = c.writerIndex();
        c.writeShort(TYPE);
        c.writeShort(length);

        c.writeShort(dmnType);
        c.writeShort(0);

        if (1 == dmnType) {
            c.writeShort((int) dmnId);
            c.writeShort(0);
        } else if (2 == dmnType) {
            c.writeInt((int) dmnId);
        } else {
            c.writeLong(dmnId);
        }

        return c.writerIndex() - iLenStartIndex;
    }

    public static PcepValueType read(ChannelBuffer c) {
        short domainType = c.readShort();
        c.readShort();
        long domainId = 0;

        if (1 == domainType) {
            domainId = c.readShort();
            c.readShort();
        } else if (2 == domainType) {
            domainId = c.readInt();
        }

        log.info("Domain ID tlv decode finished: AsNum: " + domainId);

        return new DomainIdTlv(domainType, (int) domainId);
    }
}
