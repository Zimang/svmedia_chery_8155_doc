package com.desaysv.libradio.utils;

import android.util.Log;

import java.util.Comparator;

/**
 * Created by uidp4219 on 2018/1/30.
 * 通过拼音的对比工具Comparator
 */

public class PinyinComparator implements Comparator<String> {

    public static String getPinYin(int c) {
        return PinYinDB.getPinYin(c);
    }

    public static char toHWC(char c) {
        if (c == 12288)
            return ' ';
        if (c > 65280 && c < 65375)
            return (char) (c - 65248);
        return c;
    }

    private static long toLong(String s, int index, int[] result) {
        int len = s.length();
        long r = 0;
        result[1] = 0;
        for (; index < len; ++index) {
            int a = Character.digit(s.charAt(index), 10);
            if (a < 0)
                break;
            if (r == 0 && a == 0)
                ++result[1];

            r = r * 10 + a;
        }
        result[0] = index;
        return r;
    }

    private static int compare(String l, int lStart, String r, int rStart, int dr) {

        int lEnd = l.length();
        int rEnd = r.length();
        int lPos = lStart;
        int rPos = rStart;
        int d = dr;
        int[] result = new int[2];
        while (lPos < lEnd && rPos < rEnd) {
            char lChar = l.charAt(lPos);
            char rChar = r.charAt(rPos);
            boolean lIsNum = Character.isDigit(lChar);
            boolean rIsNum = Character.isDigit(rChar);

            //数字和其它的比较
            if (lIsNum ^ rIsNum) {
                if (lIsNum)
                    return Character.isAlphabetic(rChar) ? 1 : -1;
                return Character.isAlphabetic(lChar) ? -1 : 1;
            }

            //数字和数字的比较
            if (lIsNum) {
//                long lNum = toLong(l, lPos, result);
//                long rNum = toLong(r, rPos, result);
//                lPos = result[0];
//                rPos = result[0];
//
//                if (rNum != lNum)
//                    return compareUnsigned(lNum, rNum);
//
//                return compare(l, lPos, r, rPos, d);

                int lNum = Character.digit(lChar,10);
                int rNum = Character.digit(rChar,10);
                if (lNum != rNum){
                    return lNum - rNum;//从 0--9，因此需要倒序一下
                }

            } else if (lChar != rChar) { // 拼音转成字母的比较
                //both lChar and rChar is not number
                String l_py = getPinYin(lChar);
                String r_py = getPinYin(rChar);
                if (l_py != null && r_py != null) {
                    int c = l_py.compareTo(r_py);
                    if (c == 0)
                        return lChar - rChar;
                    return c;
                }
                if (l_py == null && r_py == null) {
                    if (d == 0)
                        d = lChar - rChar;
                    char lTmp = Character.toLowerCase(toHWC(lChar));
                    char rTmp = Character.toLowerCase(toHWC(rChar));

                    if (lTmp != rTmp) {
                        return compareToNoNumber(lTmp, rTmp);
                    }

                } else
                    return l_py == null ? compareToPinYin(l, lPos, lEnd, r_py, 0, r_py.length() - 1, d)
                            : compareToPinYin(l_py, 0, l_py.length() - 1, r, rPos, rEnd, d);
            }
            ++lPos;
            ++rPos;
        }

        return d == 0 ? Integer.compare(lEnd, rEnd) : d;
    }

    private static int compareToPinYin(String l, int lStart, int lEnd, String r, int rStart, int rEnd, int dr) {
        int lPos = lStart;
        int rPos = rStart;
        int d = dr;
        while (lPos < lEnd && rPos < rEnd) {
            char lChar = l.charAt(lPos);
            char rChar = r.charAt(rPos);
            if (lChar != rChar) {
                if (d == 0)
                    d = lChar - rChar;
                char lTmp = Character.toLowerCase(toHWC(lChar));
                char rTmp = Character.toLowerCase(toHWC(rChar));
                if (lTmp != rTmp) {
                    return compareToNoNumber(lTmp, rTmp);
                }
            }
            ++lPos;
            ++rPos;
        }
        return d == 0 ? Integer.compare(lEnd, rEnd) : d;
    }

    private static int compareToNoNumber(char l, char r) {
        boolean lIsAlpha = Character.isAlphabetic(l);
        boolean rIsAlpha = Character.isAlphabetic(r);
        if (lIsAlpha ^ rIsAlpha) {
            if (lIsAlpha)
                return -1;
            return 1;
        }
        return l - r;
    }

    @Override
    public int compare(String s, String t1) {
        return compare(s, 0, t1, 0, 0);
    }


    public static int compareUnsigned(long x, long y) {
        Log.d("compareUnsigned","x: " + x + ",y: "+y);
        return Long.compare(x, y);
    }
}
