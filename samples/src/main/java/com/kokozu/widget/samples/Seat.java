package com.kokozu.widget.samples;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @author wuzhen
 * @since 2017-04-24
 */
public class Seat {

    // 满天星状态： -1 不可售，0 可售，1 已售，3 锁定
    /** 座位可选。 */
    public static final int SEAT_STATE_AVAILABLE = 0;

    /** 座位已被锁定。 */
    public static final int SEAT_STATE_LOCKED = 1;

    /** 座位不可选。 */
    public static final int SEAT_STATE_NONE = -1;

    /** KOTA 分享的座位。 */
    public static final int SEAT_STATE_KOTA = 2;

    /** 用户自己选中的座位。 */
    public static final int SEAT_STATE_SELECTED = 200;

    /** mode: -1 = not sell; 0 = can be selected; 1 = has been selected; 2 = clicked; 3 = locked; */
    private int seatState;

    /** 1 情侣座。 */
    private int seatType; // 座位的类型

    @JSONField(name = "isLoverL")
    private boolean isLoverL;

    private String seatNo;

    private String seatRow;

    private String seatCol;

    private int graphRow;

    private int graphCol;

    /** 座位所在的区号。 */
    private String seatPieceNo;

    public Seat() {
        super();
    }

    public String getSeatInfo() {
        return seatRow + "排" + seatCol + "座";
    }

    public String getSeatKey() {
        return graphRow + "-" + graphCol;
    }

    public int getSeatState() {
        return seatState;
    }

    public void setSeatState(int state) {
        // seat is available
        if (state == SEAT_STATE_AVAILABLE) {
            this.seatState = SEAT_STATE_AVAILABLE;
        }
        // seat is from KOTA
        else if (state == SEAT_STATE_KOTA) {
            this.seatState = SEAT_STATE_KOTA;
        }
        // seat is selected by user
        else if (state == SEAT_STATE_SELECTED) {
            this.seatState = SEAT_STATE_SELECTED;
        }
        // seat is locked
        else {
            this.seatState = SEAT_STATE_LOCKED;
        }
    }

    public boolean isSelectable() {
        return (this.seatState == SEAT_STATE_AVAILABLE);
    }

    public boolean isSelected() {
        return (this.seatState == SEAT_STATE_SELECTED);
    }

    public boolean cancelSelected() {
        if (this.seatState == SEAT_STATE_SELECTED) {
            this.seatState = SEAT_STATE_AVAILABLE;
            return true;
        }
        return false;
    }

    public boolean selectSeat() {
        if (this.seatState == SEAT_STATE_AVAILABLE) {
            this.seatState = SEAT_STATE_SELECTED;
            return true;
        }
        return false;
    }

    public String getSeatNo() {
        return seatNo;
    }

    public void setSeatNo(String seatNo) {
        this.seatNo = seatNo;
    }

    public String getSeatRow() {
        return seatRow;
    }

    public void setSeatRow(String seatRow) {
        this.seatRow = seatRow;
    }

    public String getSeatCol() {
        return seatCol;
    }

    public void setSeatCol(String seatCol) {
        this.seatCol = seatCol;
    }

    public int getGraphRow() {
        return graphRow;
    }

    public void setGraphRow(int graphRow) {
        this.graphRow = graphRow;
    }

    public int getGraphCol() {
        return graphCol;
    }

    public void setGraphCol(int graphCol) {
        this.graphCol = graphCol;
    }

    public String getSeatPieceNo() {
        return seatPieceNo;
    }

    public void setSeatPieceNo(String seatPieceNo) {
        this.seatPieceNo = seatPieceNo;
    }

    public int getSeatType() {
        return seatType;
    }

    public void setSeatType(int seatType) {
        this.seatType = seatType;
    }

    public boolean isLoverL() {
        return isLoverL;
    }

    @JSONField(name = "isLoverL")
    public void setLoverL(boolean isLoverL) {
        this.isLoverL = isLoverL;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((seatNo == null) ? 0 : seatNo.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!(obj instanceof Seat)) return false;
        Seat other = (Seat) obj;
        if (seatNo == null) {
            if (other.seatNo != null) return false;
        } else if (!seatNo.equals(other.seatNo)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "SeatData{"
                + "seatState="
                + seatState
                + ", seatType="
                + seatType
                + ", isLoverL="
                + isLoverL
                + ", seatNo='"
                + seatNo
                + '\''
                + ", seatRow='"
                + seatRow
                + '\''
                + ", seatCol='"
                + seatCol
                + '\''
                + ", graphRow="
                + graphRow
                + ", graphCol="
                + graphCol
                + ", seatPieceNo='"
                + seatPieceNo
                + '\''
                + '}';
    }
}
