package com.kokozu.widget.seatview;

import android.graphics.Point;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 选择最佳座位。座位图分析 1.普通座位情况 2.全是情侣座情况 3.普通座位和情侣座混合的情况
 *
 * @author fushixiang
 * @since 2017-04-20
 */
class BestSeatFinder {

    private int mMaxRow, mMaxCol;

    private SeatData[][] mSeatArray; // 座位图数组

    private List<SeatData> mSeats = new ArrayList<>(); // 厅图
    private List<SeatData> mSoldSeats = new ArrayList<>(); // 已售座位图
    private List<Point> mPoints = new ArrayList<>(); // 用于定位

    private SparseArray<List<List<SeatData>>> mIgnoreSeats = new SparseArray<>();

    void setSeats(List<SeatData> seats) {
        mSeats.clear();
        if (seats != null && seats.size() > 0) {
            this.mSeats.addAll(seats);
        }
        final int size = Utils.size(mSeats);
        int maxCol = 0;
        int maxRow = 0;
        for (int i = 0; i < size; i++) {
            SeatData seat = mSeats.get(i);
            if (seat.point.y > maxCol) {
                maxCol = seat.point.y;
            }
            if (seat.point.x > maxRow) {
                maxRow = seat.point.x;
            }
        }
        mMaxRow = maxRow;
        mMaxCol = maxCol;

        if (maxRow > 0 && maxCol > 0) {
            mSeatArray = new SeatData[maxRow + 1][maxCol + 1]; // 初始化座位数组
            mPoints.clear();
            for (SeatData seat : mSeats) {
                int row = seat.point.x;
                int col = seat.point.y;
                mSeatArray[row][col] = seat;
            }
            for (int row = 0; row < mSeatArray.length; row++) {
                for (int col = 0; col < mSeatArray[row].length; col++) {
                    mPoints.add(new Point(col, row));
                }
            }
            int centerX = (maxCol / 2) + (maxCol % 2);
            int centerY = maxRow / 2 + (maxRow % 2);
            Collections.sort(mPoints, new PointComparator(new Point(centerX, centerY)));

            updateSeatArray();
        } else {
            mSeatArray = null;
        }
    }

    void setSoldSeats(List<SeatData> seats) {
        if (!Utils.isEmpty(seats)) {
            mSoldSeats.clear();
            mSoldSeats.addAll(seats);
            if (!Utils.isEmpty(mSeats)) {
                updateSeatArray();
            }
        }
    }

    List<SeatData> selectedRecommendSeat(int recommendCount) {
        List<SeatData> bestSeat = new ArrayList<>();
        if (mSeatArray != null) {
            for (int i = 0; i < mPoints.size(); i++) { // 查找最佳座位
                Point point = mPoints.get(i);
                SeatData seat = mSeatArray[point.y][point.x];
                if (seat == null || seat.state == SeatData.STATE_SOLD) {
                    continue;
                }

                int col = point.x;
                if (recommendCount > 1) {
                    col -= recommendCount / 2;
                    if (mMaxCol % 2 == 0) {
                        col += 1;
                    }
                    if (col < 0) {
                        col = 0;
                    }
                }
                for (int j = 0; j < recommendCount; j++) {
                    SeatData isGood = mSeatArray[point.y][col + j];
                    if (isGood != null) {
                        bestSeat.add(isGood);
                    }
                }
                boolean checkAvailable = checkSeatChooseAvailable(recommendCount, bestSeat);
                if (checkAvailable) {
                    break;
                } else {
                    bestSeat.clear();
                }
            }
        }
        return bestSeat;
    }

    void clear() {
        mSoldSeats.clear();
        mPoints.clear();
        mSeats.clear();
    }

    private void updateSeatArray() {
        if (mSeatArray != null && !Utils.isEmpty(mSoldSeats)) {
            for (SeatData soldSeat : mSoldSeats) {
                int row = soldSeat.point.x;
                int col = soldSeat.point.y;
                if (row <= mMaxRow && col <= mMaxCol) {
                    mSeatArray[row][col] = soldSeat;
                }
            }
        }
    }

    private boolean checkSeatChooseAvailable(int recommendCount, List<SeatData> selectedSeat) {
        if (Utils.size(selectedSeat) == recommendCount) {
            boolean available = true;
            boolean hasLover = false;
            for (SeatData seat : selectedSeat) {
                if (seat.isLoverSeat()) {
                    hasLover = true;
                }
                if (seat.state == SeatData.STATE_SOLD) {
                    available = false;
                    break;
                }
            }
            // 情侣座的情况
            if (hasLover) {
                if (recommendCount % 2 != 0) { // 不是2的倍数
                    available = false;
                } else {
                    if (!selectedSeat.get(0).isLoverLeftSeat()) { // 第一个座位不是left
                        available = false;
                    }
                }
            }

            // 如果有忽略的座位
            if (mIgnoreSeats.size() > 0 && !Utils.isEmpty(mIgnoreSeats.get(recommendCount))) {
                List<List<SeatData>> ignoreSeats = mIgnoreSeats.get(recommendCount);
                for (List<SeatData> seats : ignoreSeats) {
                    if (selectedSeat.equals(seats)) {
                        available = false;
                        break;
                    }
                }
            }
            return available;
        }
        return false;
    }

    void addIgnoreSeats(int recommendCount, List<SeatData> ignoreSeats) {
        if (Utils.isEmpty(ignoreSeats)) {
            return;
        }

        List<List<SeatData>> seats = mIgnoreSeats.get(recommendCount);
        if (seats == null) {
            seats = new ArrayList<>();
        }
        seats.add(new ArrayList<>(ignoreSeats));
        mIgnoreSeats.put(recommendCount, seats);
    }

    /** 用于查找推荐座位 */
    private class PointComparator implements Comparator<Point> {

        private Point point;

        PointComparator(Point point) {
            this.point = point;
        }

        @Override
        public int compare(Point lhs, Point rhs) {
            int lx = Math.abs(point.x - lhs.x);
            int ly = Math.abs(point.y - lhs.y);
            int rx = Math.abs(point.x - rhs.x);
            int ry = Math.abs(point.y - rhs.y);
            int result = Double.compare(sqrt(lx, ly), sqrt(rx, ry)); // 点之间的距离排序
            if (result == 0) {
                int dX = lx - rx;
                int dY = ly - ry;
                if (dX < dY) { // 距离小的排前面
                    return -1;
                } else {
                    if (dX == 0) {
                        result = -Double.compare(lhs.y, rhs.y);
                        if (result != 0) {
                            return result;
                        }
                    }
                    if (dY == 0) {
                        result = -Double.compare(lhs.x, rhs.x);
                    }
                }
            }
            return result;
        }

        private double sqrt(int x, int y) {
            return Math.sqrt(x * x + y * y);
        }
    }
}
