package com.kokozu.widget.seatview;

import android.util.SparseArray;

import java.util.List;
import java.util.Map;

/**
 * 判断座位是否可选的规则。
 *
 * @author wuzhen
 * @since 2017-04-20
 */
class SeatSelectRegular {

    /**
     * 判断选择的座位是否合法。
     *
     * @param selectedSeat 已选中的座位
     * @return 是否合法
     */
    static boolean isSelectedSeatLegal(
            List<SeatData> selectedSeat, Map<String, SeatData> seats, int maxCol) {
        if (seats == null || seats.size() == 0) {
            return true;
        }
        if (Utils.isEmpty(selectedSeat)) {
            return true;
        }
        if (maxCol <= 0) {
            return true;
        }

        SparseArray<int[]> rowMap = new SparseArray<>();
        for (SeatData seat : selectedSeat) {
            int row = seat.point.x;
            if (rowMap.get(row) == null) {
                int rows[] = new int[maxCol];
                for (int i = 0; i < maxCol; i++) {
                    rows[i] = getSeatStateByKey(row, i, seats);
                }
                rowMap.put(row, rows);
            }
        }
        for (int i = 0; i < rowMap.size(); i++) {
            int row = rowMap.keyAt(i);
            if (!checkSeatRowAvailable(row, maxCol, seats)) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkSeatRowAvailable(int row, int maxCol, Map<String, SeatData> seats) {
        int l1, l2, r1, r2;
        for (int s = 0; s < maxCol; s++) {
            if (getSeatStateByKey(row, s, seats) != SeatData.STATE_SELECTED) {
                continue;
            }

            int i;
            for (i = s + 1; i < maxCol; i++) {
                if (getSeatStateByKey(row, i, seats) != SeatData.STATE_SELECTED) {
                    break;
                }
            }
            l1 = getSeatStateByKey(row, s - 1, seats);
            l2 = getSeatStateByKey(row, s - 2, seats);
            r1 = getSeatStateByKey(row, i, seats);
            r2 = getSeatStateByKey(row, i + 1, seats);

            // 同一排的座位 1 左或右挨着已选座位或者边界，ok
            // ！,左或右不可能挨着自选,左或右加1如果挨着自选，则中间隔的已选或者没座,2左右挨着空座，左右隔一个不挨着自选，已选，边界
            if (l1 == SeatData.STATE_SOLD || r1 == SeatData.STATE_SOLD) {
                if (l2 == SeatData.STATE_SELECTED && l1 != SeatData.STATE_SOLD) {
                    return false;
                }
                if (r2 == SeatData.STATE_SELECTED && r1 != SeatData.STATE_SOLD) {
                    return false;
                }
            } else {
                if (l2 != SeatData.STATE_NORMAL || r2 != SeatData.STATE_NORMAL) {
                    return false;
                }
            }
            s = i;
        }
        return true;
    }

    private static int getSeatStateByKey(int row, int col, Map<String, SeatData> seats) {
        SeatData seat = seats.get(row + "-" + col);
        return seat == null ? SeatData.STATE_SOLD : seat.state;
    }
}
