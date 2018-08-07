package com.kokozu.widget.seatview;

import java.util.List;

/**
 * 选座的监听事件。
 *
 * @author wuzhen
 * @since 2017-04-24
 */
public interface OnChooseSeatListener {

    /**
     * 选择座位时因为情侣座超出数量限制。
     *
     * @param maxSelectCount 可选的最大数量
     */
    void onPickLoverSeatOverMaxCount(int maxSelectCount);

    /**
     * 选择座位时超出数量限制。
     *
     * @param maxSelectCount 可选的最大数量
     */
    void onSelectedSeatOverMaxCount(int maxSelectCount);

    /**
     * 选择座位时不符合选座的规则。
     */
    void onSelectSeatNotMatchRegular();

    /**
     * 选择的座位变化的监听事件。
     *
     * @param selectedSeats 已选的座位
     */
    void onSelectedSeatChanged(List<SeatData> selectedSeats);

    /**
     * 选中的座位已被售出的监听事件。
     */
    void onSelectedSeatSold();
}
