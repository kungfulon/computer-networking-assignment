class ByteUtil
{
    static int getUShort(byte[] bytes, int offset) {
        return ((bytes[offset] & 0xff) << 8) | (bytes[offset + 1] & 0xff);
    }

    static void setUShort(byte[] bytes, int offset, int value) {
        bytes[offset] = (byte)(value >> 8);
        bytes[offset + 1] = (byte)(value & 0xff);
    }

    static String getString(byte[] bytes, int offset, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; ++i) sb.append((char)bytes[offset + i]);
        return sb.toString();
    }

    static String getHexString(byte[] bytes, int startIndex) {
        StringBuilder sb = new StringBuilder();
        for (int i = startIndex; i < bytes.length; ++i)
            sb.append(String.format("%02x", bytes[i]));
        return sb.toString();
    }

    static void setBytes(byte[] bytes, int offset, byte[] val) {
        System.arraycopy(val, 0, bytes, offset, val.length);
    }
}
