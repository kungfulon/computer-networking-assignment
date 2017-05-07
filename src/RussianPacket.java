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

public class RussianPacket extends IPPacket {
    public static final int OFFSET_SOURCE_PORT = 0;
    public static final int OFFSET_DESTINATION_PORT = 2;
    public static final int OFFSET_CHECKSUM = 4;
    public static final int OFFSET_DATA_LENGTH = 8;
    public static final int OFFSET_IS_ACK = 9;
    public static final int OFFSET_ID = 10;
    public static final int OFFSET_DATA = 12;

    public static final int PROTOCOL_NUMBER = 197;

    public RussianPacket (int dataLength) {
        super(OFFSET_DATA + dataLength);
    }

    public void computeChecksum() {
        _data_[OFFSET_CHECKSUM] = _data_[OFFSET_CHECKSUM + 1] = _data_[OFFSET_CHECKSUM + 2] = _data_[OFFSET_CHECKSUM + 3] = 0;
        int checkSum = _computeChecksum_(OFFSET_SOURCE_PORT, OFFSET_CHECKSUM - OFFSET_SOURCE_PORT, OFFSET_DATA - OFFSET_SOURCE_PORT + getDataLength(), 0, false);
        String cs = String.format("%02X%02X", checkSum >> 8, checkSum & 0xFF);
        System.arraycopy(cs.getBytes(), 0, _data_, OFFSET_CHECKSUM, 4);
    }

    public boolean verifyChecksum() {
        byte[] curChecksum = new byte[4];
        System.arraycopy(_data_, OFFSET_CHECKSUM, curChecksum, 0, 4);
        _data_[OFFSET_CHECKSUM] = _data_[OFFSET_CHECKSUM + 1] = _data_[OFFSET_CHECKSUM + 2] = _data_[OFFSET_CHECKSUM + 3] = 0;
        int checkSum = _computeChecksum_(OFFSET_SOURCE_PORT, OFFSET_CHECKSUM - OFFSET_SOURCE_PORT, OFFSET_DATA - OFFSET_SOURCE_PORT + getDataLength(), 0, false);
        System.arraycopy(curChecksum, 0, _data_, OFFSET_CHECKSUM, 4);

        return checkSum == getCheckSum();
    }

    public int getSourcePort() {
        return ((_data_[OFFSET_SOURCE_PORT] << 8 & 0xFFFF) | _data_[OFFSET_SOURCE_PORT + 1] & 0xFF);
    }

    public void setSourcePort(int iPort) {
        _data_[OFFSET_SOURCE_PORT] = (byte)(iPort >> 8);
        _data_[OFFSET_SOURCE_PORT + 1] = (byte)(iPort & 0xFF);
    }

    public int getDestinationPort() {
        return ((_data_[OFFSET_DESTINATION_PORT] << 8 & 0xFFFF) | _data_[OFFSET_DESTINATION_PORT + 1] & 0xFF);
    }

    public void setDestinationPort(int iPort) {
        _data_[OFFSET_DESTINATION_PORT] = (byte)(iPort >> 8);
        _data_[OFFSET_DESTINATION_PORT + 1] = (byte)(iPort & 0xFF);
    }

    public int getCheckSum() {
        return ((Integer.parseInt(new String(_data_, OFFSET_CHECKSUM, 2), 16) << 8) | Integer.parseInt(new String(_data_, OFFSET_CHECKSUM + 2, 2), 16));
    }

    public int getDataLength() {
        return _data_[OFFSET_DATA_LENGTH] & 0xFF;
    }

    public void setDataLength(int length) {
        _data_[OFFSET_DATA_LENGTH] = (byte)length;
    }

    public boolean isACK() {
        return _data_[OFFSET_IS_ACK] == 1;
    }

    public void setIsACK(boolean isACK) {
        _data_[OFFSET_IS_ACK] = isACK ? (byte)1 : (byte)0;
    }

    public void getData(byte[] data, int offset) {
        System.arraycopy(_data_, OFFSET_DATA, data, offset, getDataLength());
    }

    public void setData(byte[] data, int offset, int length) {
        _data_[OFFSET_DATA_LENGTH] = (byte)length;
        System.arraycopy(data, offset, _data_, OFFSET_DATA, length);
    }

    public int getID() {
        return ((_data_[OFFSET_ID] << 8 & 0xFFFF) | _data_[OFFSET_ID + 1] & 0xFF);
    }

    public void setID(int id) {
        _data_[OFFSET_ID] = (byte)(id >> 8);
        _data_[OFFSET_ID + 1] = (byte)(id & 0xFF);
    }
}
