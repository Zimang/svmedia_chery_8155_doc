package com.desaysv.moduleusbvideo.util;

import android.text.TextUtils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 * 汉字转拼音
 * add by lzm 从KX63哪里移植过来的汉子转拼音的工具，用来将文件名，ID3信息转化为拼音，方便后续排序
 */
public class CharacterParser {

    /**
     * 将字符串里面的中文全部转化为拼英
     *
     * @param inputString 输入的字符串
     * @return 解析之后的字符串，可以直接使用数据库进行排序
     */
    public static String getPingYin(String inputString) {
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        format.setVCharType(HanyuPinyinVCharType.WITH_V);
        char[] input = inputString.trim().toCharArray();
        StringBuilder output = new StringBuilder();
        try {
            for (char curChar : input) {
                if (Character.toString(curChar).matches("[\u4e00-\u9fa5]+")) {//汉字
                    String[] temp = PinyinHelper.toHanyuPinyinStringArray(curChar, format);
                    //Logger.d(TAG,"getPingYin() temp is null ? "+(temp == null?"true":"false"));
                    if (temp != null) {//防止出现空指针异常,解决PR：掉电上电后，弹框
                        output.append(temp[0]);
                    }
                } else {
                    output.append(curChar);
                }
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            e.printStackTrace();
        }
        return getFirstLetter(output.toString().toUpperCase());
    }

    /**
     * 修改文件的首字母，将特殊字符全部归纳到最后，数字归纳到英文后面，用来排序
     * { 越多，排序越前
     * ~ 越多，排序越后
     *
     * @param input 拼英化之后的字符串
     * @return 加入特殊字符的字符串
     */
    public static String getFirstLetter(String input) {
        String str;
        if (TextUtils.isEmpty(input)) {
            str = "~~~"; //4
        } else {
            String pinyinAuthor = input.substring(0, 1);
            if (pinyinAuthor.matches("[A-Z]")) {
                str = "{{" + input; // 1
            } else if (pinyinAuthor.matches("[0-9]")) {
                str = "{" + autoZero(input, 8);// 2
            } else {
                str = "~~" + input; //3
            }
        }
        return str;
    }

    /**
     * 自动前补0
     *
     * @param input 字符
     * @param size  大小
     * @return 字符
     */
    public static String autoZero(String input, int size) {
        if (null == input || input.isEmpty()) {
            return input;
        }
        int dataLength = 0;
        for (int i = 0; i < input.length(); i++) {
            boolean isNumber = input.substring(i, i + 1).matches("[0-9]");
            if (isNumber) {
                dataLength += 1;
            } else {
                break;
            }
        }
        if (dataLength < size && dataLength != 0) {
            StringBuilder inputBuilder = new StringBuilder(input);
            int strSize = size - dataLength;
            for (int i = 0; i < strSize; i++) {
                inputBuilder.insert(0, "0");
            }
            input = inputBuilder.toString();
        }
        return input;
    }
}
