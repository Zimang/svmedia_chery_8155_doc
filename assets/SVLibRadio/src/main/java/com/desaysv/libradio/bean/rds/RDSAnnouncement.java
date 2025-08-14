package com.desaysv.libradio.bean.rds;

import android.os.Parcel;
import android.os.Parcelable;

public class RDSAnnouncement implements Parcelable {
    /**
     * 显示
     */
    public static final int ANNOUNCEMENT_TYPE_SHOW = 1;
    /**
     * 隐藏
     */
    public static final int ANNOUNCEMENT_TYPE_HIDE = 0;


    public static final int ANNOUNCEMENT_TYPE_ALARM = 1;
    public static final int ANNOUNCEMENT_TYPE_TRAFFIC = 2;
    public static final int ANNOUNCEMENT_TYPE_OTHER = 3;


    /*参数说明	类型	说明	缺省值
    status	int	0：hide 1：show

    annoucetype int 0:无效; 1:alarm; 2:traffic; 3:other;

*/

    private int status;
    private int announceType;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getAnnounceType() {
        return announceType;
    }

    public void setAnnounceType(int announceType) {
        this.announceType = announceType;
    }

    protected RDSAnnouncement(Parcel in) {
        status = in.readInt();
        announceType = in.readInt();
    }

    public RDSAnnouncement() {
    }

    public static final Creator<RDSAnnouncement> CREATOR = new Creator<RDSAnnouncement>() {
        @Override
        public RDSAnnouncement createFromParcel(Parcel in) {
            return new RDSAnnouncement(in);
        }

        @Override
        public RDSAnnouncement[] newArray(int size) {
            return new RDSAnnouncement[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(status);
        dest.writeInt(announceType);
    }
}
