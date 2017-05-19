/*
    srcPort: 2 bytes
    dstPort: 2 bytes
    checkSum: 4 bytes (would have been 2 bytes if it's short, not string)
    length: 1 byte
    isACK: 1 byte
    id: 2 bytes
    totalHeaderLength: 12 bytes
 */

import org.savarese.vserv.tcpip.IPPacket;

import java.util.Arrays;

class PutinPacket extends IPPacket {
    private static final int OFFSET_SOURCE_PORT = 0;
    private static final int OFFSET_DESTINATION_PORT = 2;
    private static final int OFFSET_CHECKSUM = 4;
    private static final int OFFSET_DATA_LENGTH = 8;
    private static final int OFFSET_IS_ACK = 9;
    private static final int OFFSET_ID = 10;
    static final int OFFSET_DATA = 12;

    static final int PROTOCOL_NUMBER = 197;

    PutinPacket(int dataLength) {
        super(OFFSET_DATA + dataLength);
    }

    private int getNewChecksum() {
        return _computeChecksum_(OFFSET_SOURCE_PORT,
                OFFSET_CHECKSUM - OFFSET_SOURCE_PORT,
                OFFSET_DATA - OFFSET_SOURCE_PORT + getDataLength(),
                0,
                false);
    }

    void computeChecksum() {
        Arrays.fill(_data_, OFFSET_CHECKSUM, OFFSET_CHECKSUM + 4, (byte)0);

        int checkSum = getNewChecksum();
        String cs = String.format("%02X%02X", checkSum >> 8, checkSum & 0xFF);
        ByteUtil.setBytes(_data_, OFFSET_CHECKSUM, cs.getBytes());
    }

    boolean verifyChecksum() {
        String curChecksum = getCheckSum();
        computeChecksum();

        boolean ok = curChecksum.equals(getCheckSum());
        ByteUtil.setBytes(_data_, OFFSET_CHECKSUM, curChecksum.getBytes());

        return ok;
    }

    int getSourcePort() {
        return ByteUtil.getUShort(_data_, OFFSET_SOURCE_PORT);
    }

    void setSourcePort(int iPort) {
        ByteUtil.setUShort(_data_, OFFSET_SOURCE_PORT, iPort);
    }

    int getDestinationPort() {
        return ByteUtil.getUShort(_data_, OFFSET_DESTINATION_PORT);
    }

    void setDestinationPort(int iPort) {
        ByteUtil.setUShort(_data_, OFFSET_DESTINATION_PORT, iPort);
    }

    private String getCheckSum() {
        return ByteUtil.getString(_data_, OFFSET_CHECKSUM, 4);
    }

    int getDataLength() {
        return _data_[OFFSET_DATA_LENGTH] & 0xFF;
    }

    void setDataLength(int length) {
        _data_[OFFSET_DATA_LENGTH] = (byte)length;
    }

    boolean isACK() {
        return _data_[OFFSET_IS_ACK] == 1;
    }

    void setIsACK(boolean isACK) {
        _data_[OFFSET_IS_ACK] = (byte)(isACK ? 1 : 0);
    }

    void setPutinData(byte[] data) {
        _data_[OFFSET_DATA_LENGTH] = (byte)data.length;
        ByteUtil.setBytes(_data_, OFFSET_DATA, data);
    }

    byte[] getPutinData() {
        return Arrays.copyOfRange(_data_, OFFSET_DATA, OFFSET_DATA + getDataLength());
    }

    int getID() {
        return ByteUtil.getUShort(_data_, OFFSET_ID);
    }

    void setID(int id) {
        ByteUtil.setUShort(_data_, OFFSET_ID, id);
    }
}
