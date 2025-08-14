package com.desaysv.moduleusbvideo.vr;


import android.os.Parcel;
import android.os.Parcelable;

public class VRVideoActionBean implements Parcelable {
    public String user;
    public SemanticBean semantic;

    protected VRVideoActionBean(Parcel in) {
        user = in.readString();
        semantic = in.readParcelable(SemanticBean.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(user);
        dest.writeParcelable(semantic, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<VRVideoActionBean> CREATOR = new Creator<VRVideoActionBean>() {
        @Override
        public VRVideoActionBean createFromParcel(Parcel in) {
            return new VRVideoActionBean(in);
        }

        @Override
        public VRVideoActionBean[] newArray(int size) {
            return new VRVideoActionBean[size];
        }
    };

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public SemanticBean getSemantic() {
        return semantic;
    }

    public void setSemantic(SemanticBean semantic) {
        this.semantic = semantic;
    }

    @Override
    public String toString() {
        return "VRVideoActionBean{" +
                "user='" + user + '\'' +
                ", semantic=" + semantic +
                '}';
    }

    public static class SemanticBean implements Parcelable {
        private String type;
        private String value;
        private String action;
        private String position;

        protected SemanticBean(Parcel in) {
            type = in.readString();
            value = in.readString();
            action = in.readString();
            position = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(type);
            dest.writeString(value);
            dest.writeString(action);
            dest.writeString(position);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<SemanticBean> CREATOR = new Creator<SemanticBean>() {
            @Override
            public SemanticBean createFromParcel(Parcel in) {
                return new SemanticBean(in);
            }

            @Override
            public SemanticBean[] newArray(int size) {
                return new SemanticBean[size];
            }
        };

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }

        @Override
        public String toString() {
            return "SemanticBean{" +
                    "type='" + type + '\'' +
                    ", value='" + value + '\'' +
                    ", action='" + action + '\'' +
                    ", position='" + position + '\'' +
                    '}';
        }
    }
}
