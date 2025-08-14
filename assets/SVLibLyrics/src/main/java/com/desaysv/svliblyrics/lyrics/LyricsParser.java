package com.desaysv.svliblyrics.lyrics;

import android.text.TextUtils;
import android.util.Log;

import com.desaysv.svliblyrics.unicode.UniversalDetector;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author uidq1846
 * @desc lrc 格式歌词的解析
 * @time 2020-12-17 14:54
 */
public class LyricsParser {
    private static final String TAG = "LyricsParser";
    //采用正则表达式进行匹配解析,以[开头，匹配ar和时间等关系(根据LRC格式设置的匹配规则，后续可增加简易拓展)
    private static final String LINE_REGEX = "((\\[(((ar|ti|by|au|ve|re|al):.*)|(\\d{2}:\\d{2}(\\.\\d+)*))])+)(.*)";
    //时间分割表达式
    private static final String TIME_REGEX = "\\[(\\d{2}):(\\d{2})(\\.(\\d+))*]";

    private LyricsParser() {
    }

    /**
     * 将歌词文件里面的字符串 解析成一个List<LrcRow>
     */
    public static List<LyricsRow> getLyricsRows(InputStream inputStream) {
        return getLyricsRows(converfile(inputStream, "utf-8"));
    }

    /**
     * 将歌词文件里面的字符串 解析成一个List<LrcRow>
     */
    public static List<LyricsRow> getLyricsRows(File file) {
        return getLyricsRows(converfile(file));
    }

    /**
     * 获取List<LyricsRow>
     *
     * @param content content
     * @return List<LyricsRow>
     */
    public static List<LyricsRow> getLyricsRows(String content) {
        if (TextUtils.isEmpty(content)) {
            return new ArrayList<>();
        }
        List<LyricsRow> lrcRows = new ArrayList<>();
        BufferedReader br = null;
        String lrcLine;
        try {
            br = new BufferedReader(new StringReader(content));
            while ((lrcLine = br.readLine()) != null) {
                //获取每行分割的LyricsRow
                List<LyricsRow> rows = createRowsByMatcher(lrcLine);
                if (rows != null && rows.size() > 0) {
                    lrcRows.addAll(rows);
                }
            }
            final int size = lrcRows.size();
            if (size > 0) {//进行排序
                Collections.sort(lrcRows);
                //排完序之后计算每句歌词需要显示的时间，预留给歌词高亮准备
                for (int i = 0; i < size - 1; i++) {
                    LyricsRow l = lrcRows.get(i);
                    l.setTotalTime(lrcRows.get(i + 1).getTime() - l.getTime());
                }
                //最后一句默认5s
                lrcRows.get(size - 1).setTotalTime(5000);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("getLyricsRows", "getLyricsRows IOException = " + e.getMessage());
            return new ArrayList<>();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return lrcRows;
    }

    /**
     * 通过 InputStream 进行拼接
     *
     * @param inputStream inputStream
     * @param charsetName 编码格式
     * @return String
     */
    private static String converfile(InputStream inputStream, String charsetName) {
        BufferedInputStream bis = null;
        BufferedReader reader = null;
        String text = null;
        // 找到文档的前三个字节并自动判断文档类型
        try {
            bis = new BufferedInputStream(inputStream);
            bis.mark(4);
            if (TextUtils.isEmpty(charsetName)) {
                charsetName = getCharset(inputStream);
            }
            reader = new BufferedReader(new InputStreamReader(bis, charsetName));
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            text = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (bis != null) {
                    bis.close();
                }
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return text;
    }

    /**
     * file
     *
     * @param file file
     * @return String
     */
    private static String converfile(File file) {
        return converfile(file, null);
    }

    /**
     * file
     *
     * @param file        file
     * @param charsetName charsetName
     * @return String
     */
    private static String converfile(File file, String charsetName) {
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        BufferedReader reader;
        String text = null;
        try {
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            bis.mark(4);
            if (TextUtils.isEmpty(charsetName)) {
                charsetName = getCharset(file);
            }
            reader = new BufferedReader(new InputStreamReader(bis, charsetName));
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            text = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
                if (bis != null) {
                    bis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return text;
    }

    /**
     * 获取编码格式 这种方式不靠谱，待定
     *
     * @return String
     */
    private static String getCharset(File file) {
        String encoding = null;
        try {
            encoding = UniversalDetector.detectCharset(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "getCharset: encoding = " + encoding);
        if (encoding == null) {
            encoding = "GBK";
        }
        return encoding;
    }

    /**
     * 获取编码格式 这种方式不靠谱，待定
     *
     * @return String
     */
    private static String getCharset(InputStream fis) {
        UniversalDetector detector = null;
        String encoding = null;
        try {
            byte[] buf = new byte[4096];
            // (1)
            detector = new UniversalDetector();
            // (2)
            int nread;
            while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
                detector.handleData(buf, 0, nread);
            }
            // (3)
            detector.dataEnd();
            // (4)
            encoding = detector.getDetectedCharset();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // (5)
            if (detector != null) {
                detector.reset();
            }
        }
        Log.d(TAG, "getCharset: encoding = " + encoding);
        if (encoding == null) {
            encoding = "GBK";
        }
        return encoding;
    }

    /**
     * 将歌词文件中的某一行 解析成一个List<LrcRow>
     * 因为一行中可能包含了多个LrcRow对象
     * 比如  [00:33][01:36] xxxxxx 又或者更精细 [00:33.56][01:36.54] ，就包含了2个对象
     * 这里需要注意lrc格式需求，当前还未添加增强格式（每个词都规定了时间）
     * [al:本歌所在的唱片集]
     * <p>
     * [ar:演出者-歌手]
     * <p>
     * [au:歌詞作者-作曲家]
     * <p>
     * [by:此LRC文件的创建者]
     * <p>
     * [offset:+/- 以毫秒为单位加快或延後歌詞的播放] 这个先不提取，有这个的话，构建时，时间都需进行加减相同的时间数
     * <p>
     * [re:创建此LRC文件的播放器或编辑器]
     * <p>
     * [ti:歌词(歌曲)的标题]
     * <p>
     * [ve:程序的版本]
     * <p>
     * 简易格式：[mm:ss.xx]
     * 增强格式：更为精准
     * [mm:ss.xx] <mm:ss.xx> 第一行第一个词 <mm:ss.xx> 第一行第二个词 <mm:ss.xx> ... 第一行最后一个词 <mm:ss.xx>
     * <p>
     * 通过正则表达式匹配
     *
     * @param lrcLine lrcLine每行歌词
     * @return List<LyricsRow>
     */
    public static List<LyricsRow> createRowsByMatcher(String lrcLine) {
        if (lrcLine == null) {
            return null;
        }
        //是否匹配lrc格式要求
        Matcher matcher = Pattern.compile(LINE_REGEX).matcher(lrcLine);
        if (!matcher.matches()) {
            Log.d(TAG, "createRowsByMatcher: lrcLin Does not meet the rules, so return lrcLine = " + lrcLine);
            return null;
        }
        //匹配位置以规则当中()位置未值，如正则表达式发生改变，则此位置也发生改变
        String time = matcher.group(1);
        String content = matcher.group(8);
        List<LyricsRow> lyricsRows = new ArrayList<>();
        if (content == null || content.isEmpty()) {
            //则说明当前包含的是歌词状态
            String lrcMessage = matcher.group(4);
            if (lrcMessage != null && !lrcMessage.isEmpty()) {
                //这里需要拆分出歌唱家等信息，这里用于提取歌词信息，lrc当中没有时间位，有些不显示，但是这个可以在歌词加载完成后拼接显示，时间统一设置为0
                //这个可以不用硬编码，只是标识对应的意思，后续有需要可以进行更改
                if (lrcMessage.startsWith("al")) {
                    content = lrcMessage.replace("al", "专辑");
                } else if (lrcMessage.startsWith("ar")) {
                    content = lrcMessage.replace("ar", "歌手");
                } else if (lrcMessage.startsWith("au")) {
                    content = lrcMessage.replace("au", "专辑");
                } else if (lrcMessage.startsWith("by")) {
                    content = lrcMessage.replace("by", "制作");
                } else if (lrcMessage.startsWith("re")) {
                    content = lrcMessage.replace("re", "编译器");
                } else if (lrcMessage.startsWith("ti")) {
                    content = lrcMessage.replace("ti", "歌曲");
                } else if (lrcMessage.startsWith("ve")) {
                    content = lrcMessage.replace("ve", "版本");
                }
                LyricsRow lyricsRow = new LyricsRow("00:00", Integer.parseInt("0"), content);
                Log.d(TAG, "createRowsByMatcher: lyricsRow = " + lyricsRow.toString());
                lyricsRows.add(lyricsRow);
                return lyricsRows;
            }
            // 避免歌词文件中空行暂时返回null，如果后续需要显示空白行则屏蔽return
            return null;
        }
        //其实不会为null，time部分至少匹配一份才能够显示，避免报黄色
        assert time != null;
        Matcher timeMatcher = Pattern.compile(TIME_REGEX).matcher(time);
        while (timeMatcher.find()) {
            //这里至少匹配到2（可见匹配规则，不满足已经抛弃掉，为避免as显示黄色，所以增加为null校验），4可能匹配不到
            String tem = timeMatcher.group(1);
            String second = timeMatcher.group(2);
            String mill = timeMatcher.group(4);
            LyricsRow lyricsRow;
            assert tem != null;
            assert second != null;
            if (mill == null) {
                lyricsRow = new LyricsRow(tem + ":" + second,
                        Integer.parseInt(tem) * 60 * 1000
                                + Integer.parseInt(second) * 1000, content);
            } else {
                lyricsRow = new LyricsRow(tem + ":" + second + ":" + mill,
                        Integer.parseInt(tem) * 60 * 1000
                                + Integer.parseInt(second) * 1000
                                + Integer.parseInt(mill), content);
            }
            Log.d(TAG, "createRowsByMatcher: lyricsRow " + lyricsRow.toString());
            lyricsRows.add(lyricsRow);
        }
        return lyricsRows;
    }
}