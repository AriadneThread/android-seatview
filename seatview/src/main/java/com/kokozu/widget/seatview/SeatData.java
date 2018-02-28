package com.kokozu.widget.seatview;

import android.graphics.Point;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 座位。
 *
 * @author wuzhen
 * @since 2017-04-20
 */
public class SeatData implements Parcelable {

    /** 可选的座位。 */
    public static final int STATE_NORMAL = 0;

    /** 已售出的座位。 */
    public static final int STATE_SOLD = 1;

    /** 选中的座位。 */
    public static final int STATE_SELECTED = 2;

    /** 普通座位。 */
    public static final int TYPE_NORMAL = 0;

    /** 情侣座左边的座位。 */
    public static final int TYPE_LOVER_LEFT = 1;

    /** 情侣座右边的座位。 */
    public static final int TYPE_LOVER_RIGHT = 2;

    /** 座位的坐标。 */
    public Point point;

    /** 座位的状态。 */
    @SeatState public int state;

    /** 座位的类型。 */
    @SeatType public int type;

    public String seatRow;

    public String seatCol;

    public String seatNo;

    public String pieceNo;

    public String extra;

    /**
     * 判断是否情侣座。
     *
     * @return 是否情侣座
     */
    boolean isLoverSeat() {
        return type == TYPE_LOVER_LEFT || type == TYPE_LOVER_RIGHT;
    }

    /**
     * 判断是否为情侣座左边的座位。
     *
     * @return 是否情侣座左边的座位
     */
    boolean isLoverLeftSeat() {
        return type == TYPE_LOVER_LEFT;
    }

    /**
     * 判断是否为情侣座右边的座位。
     *
     * @return 是否情侣座右边的座位
     */
    boolean isLoverRightSeat() {
        return type == TYPE_LOVER_RIGHT;
    }

    /**
     * 选中座位。
     *
     * @return 是否选中成功
     */
    boolean selectSeat() {
        if (state == STATE_NORMAL) {
            state = STATE_SELECTED;
            return true;
        }
        return false;
    }

    /**
     * 取消选中的座位。
     *
     * @return 是否取消成功
     */
    boolean unSelectSeat() {
        if (state == STATE_SELECTED) {
            state = STATE_NORMAL;
            return true;
        }
        return false;
    }

    String seatKey() {
        return point.x + "-" + point.y;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({STATE_NORMAL, STATE_SOLD, STATE_SELECTED})
    public @interface SeatState {}

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TYPE_NORMAL, TYPE_LOVER_LEFT, TYPE_LOVER_RIGHT})
    public @interface SeatType {}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.point, flags);
        dest.writeInt(this.state);
        dest.writeInt(this.type);
        dest.writeString(this.seatRow);
        dest.writeString(this.seatCol);
        dest.writeString(this.seatNo);
        dest.writeString(this.pieceNo);
        dest.writeString(this.extra);
    }

    public SeatData() {}

    protected SeatData(Parcel in) {
        this.point = in.readParcelable(Point.class.getClassLoader());
        this.state = in.readInt();
        this.type = in.readInt();
        this.seatRow = in.readString();
        this.seatCol = in.readString();
        this.seatNo = in.readString();
        this.pieceNo = in.readString();
        this.extra = in.readString();
    }

    public static final Creator<SeatData> CREATOR =
            new Creator<SeatData>() {

                @Override
                public SeatData createFromParcel(Parcel source) {
                    return new SeatData(source);
                }

                @Override
                public SeatData[] newArray(int size) {
                    return new SeatData[size];
                }
            };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SeatData seatData = (SeatData) o;

        if (point != null ? !point.equals(seatData.point) : seatData.point != null) return false;
        if (seatRow != null ? !seatRow.equals(seatData.seatRow) : seatData.seatRow != null)
            return false;
        if (seatCol != null ? !seatCol.equals(seatData.seatCol) : seatData.seatCol != null)
            return false;
        return seatNo != null ? seatNo.equals(seatData.seatNo) : seatData.seatNo == null;
    }

    @Override
    public int hashCode() {
        int result = point != null ? point.hashCode() : 0;
        result = 31 * result + (seatRow != null ? seatRow.hashCode() : 0);
        result = 31 * result + (seatCol != null ? seatCol.hashCode() : 0);
        result = 31 * result + (seatNo != null ? seatNo.hashCode() : 0);
        return result;
    }
}
