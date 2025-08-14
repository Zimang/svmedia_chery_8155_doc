package com.desaysv.mediacommonlib.utils;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 * Created by uidp5370 on 2019-6-3.
 * 网上抄的，一个用来转化byte和hex的工具类
 */
public class CharacterConversion {
    /**
     * Convert byte[] to hex
     * string.这里我们可以将byte转换成int，然后利用Integer.toHexString(int)来转换成16进制字符串
     *
     * @param src byte[] data
     * @return hex string
     */
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            stringBuilder.append(String.format("%02x", src[i]).toUpperCase());
        }
        return stringBuilder.toString();
    }

    public static String bytesToHexString(byte[] src, int len) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0 || len <= 0) {
            return null;
        }
        for (int i = 0; i < len; i++) {
            stringBuilder.append(String.format("%02x", src[i]).toUpperCase());
        }
        return stringBuilder.toString();
    }

    /**
     * Convert hex string to byte[]
     *
     * @param hexString the hex string
     * @return byte[]
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || "".equals(hexString)) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) ((charToByte(hexChars[pos]) & 0x0FF) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    /**
     * Convert hex string to char[]
     *
     * @param hexString the hex string
     * @return char[]
     */
    public static char[] hexStringToChars(String hexString) {
        byte[] d = hexStringToBytes(hexString);
        char[] c = new char[d.length];
        for (int i = 0; i < d.length; i++) {
            c[i] = (char) d[i];
        }
        return c;
    }

    /**
     * Convert char to byte
     *
     * @param c char
     * @return byte
     */
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    /**
     * Convert byte to HexString
     *
     * @param b byte
     * @return String
     */
    public static String byteToHexString(byte b) {
        StringBuffer buf = new StringBuffer();
        char[] hexChars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F'};
        int high = ((b & 0xf0) >> 4);
        int low = (b & 0x0f);
        buf.append(hexChars[high]);
        buf.append(hexChars[low]);
        return buf.toString();
    }

    /**
     * Convert int to HexString
     *
     * @param i int
     * @return String
     */
    public static String intByteToHexString(int i) {
        String hex = "0123456789ABCDEF";
        if (i > 255) {
            return "00";
        }
        if (i < 16) {
            return "0" + hex.charAt(i);
        } else {
            int two = (int) i / 16;
            int one = (int) i % 16;
            return hex.charAt(two) + "" + hex.charAt(one) + "";
        }
    }

    /**
     * Convert string to ASCII char[]
     *
     * @param String
     * @return char[] ,ASCII format
     */
    public static byte[] StringToASCIIBytes(String String) {
        if (String == null || "".equals(String)) {
            return null;
        }
        String newString = new String();

        newString = String;
        int length = newString.length();
        char[] Chars = newString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            d[i] = (byte) Chars[i];
            // Log.d(TAG, "StringToASCIIBytes :"+d[i]);
        }
        return d;
    }

    /**
     * 将byte数组的内容转换成String字符串，范围是ASCII的32到 127 遇到'\0'结束
     *
     * @param src 输入的byte数组
     * @return 返回ASCII字符串
     */
    public static String bytesToASCIIString(byte[] src) {
        if (src == null || src.length <= 0) {
            return null;
        }
        byte[] newByte = new byte[src.length];
        int newLength = 0;
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            char temp = (char) src[i];
            if ((temp >= 32) && (temp <= 127)) // 取ASCII表中的可打印字符范围
            {
                newByte[newLength] = src[i];
                newLength++;
            } else if (temp == 0) {
                break;
            }

        }
        return new String(newByte, 0, newLength);
    }

    /**
     * 将char数组的内容转换成byte数组
     *
     * @param chars   输入的char型数组
     * @param charset 转换的字符集，US-ASCII, ISO-8859-1, UTF-8, UTF-16BE, UTF-16LE, UTF-16
     * @return 返回byte型数组
     */
    public static byte[] getBytesFromChars(char[] chars, String charset) {
        if (chars == null || chars.length <= 0) {
            return null;
        }
        Charset cs = Charset.forName(charset);
        CharBuffer cb = CharBuffer.allocate(chars.length);
        cb.put(chars);
        cb.flip();
        ByteBuffer bb = cs.encode(cb);
        return bb.array();
    }

    /**
     * 将byte数组的内容转换成char数组
     *
     * @param bytes   输入的byte型数组
     * @param charset 转换的字符集，US-ASCII, ISO-8859-1, UTF-8, UTF-16BE, UTF-16LE, UTF-16
     * @return 返回char型数组
     */
    public static char[] getCharsFromBytes(byte[] bytes, String charset) {
        if (bytes == null || bytes.length <= 0) {
            return null;
        }
        Charset cs = Charset.forName(charset);
        ByteBuffer bb = ByteBuffer.allocate(bytes.length);
        bb.put(bytes);
        bb.flip();
        CharBuffer cb = cs.decode(bb);
        return cb.array();
    }
}
