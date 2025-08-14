package com.desaysv.svliblyrics.lyrics;

/**
 * @author uidq1846
 * @desc 歌词的行
 * @time 2020-12-17 14:55
 */
public class LyricsRow implements Comparable<LyricsRow> {
    /**
     * 开始时间 为00:10:00
     * 用于时间线显示
     */
    private String timeStr;

    /**
     * 开始时间 毫米数  00:10:00  为10000
     */
    private int time;

    /**
     * 歌词内容
     */
    private String content;

    /**
     * 该行歌词显示的总时间
     */
    private int totalTime;

    public LyricsRow() {
        super();
    }

    public LyricsRow(String timeStr, int time, String content) {
        super();
        this.timeStr = timeStr;
        this.time = time;
        this.content = content;
    }

    public String getTimeStr() {
        return timeStr;
    }

    public void setTimeStr(String timeStr) {
        this.timeStr = timeStr;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(int totalTime) {
        this.totalTime = totalTime;
    }

    @Override
    public int compareTo(LyricsRow anotherLyricsRow) {
        return this.time - anotherLyricsRow.time;
    }

    @Override
    public String toString() {
        return "LyricsRow [ timeStr=" + timeStr + ", time = " + time + ", content = " + content + ", totalTime = " + totalTime + " ]";
    }
}
