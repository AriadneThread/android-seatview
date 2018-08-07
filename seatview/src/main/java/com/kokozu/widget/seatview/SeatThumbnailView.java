package com.kokozu.widget.seatview;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 座位图的缩略图。
 *
 * @author wuzhen
 * @since 2017-04-21
 */
public class SeatThumbnailView extends View {

    private static final int AUTO_HIDE_DELAY = 3000;
    private static final int CENTER_LINE_COLOR_DEFAULT = Color.parseColor("#666666");

    /*
     * 座位不同状态的图片
     */
    private Drawable mSeatNormal;
    private Drawable mSeatSold;
    private Drawable mSeatSelected;
    private Drawable mSeatLoverNormalL;
    private Drawable mSeatLoverNormalR;
    private Drawable mSeatLoverSoldL;
    private Drawable mSeatLoverSoldR;
    private Drawable mSeatLoverSelectedL;
    private Drawable mSeatLoverSelectedR;

    private boolean isAutoHide;
    private Drawable mThumbnailBackground;

    /**
     * 所有的座位，key：Seat.seatKey()
     */
    private Map<String, SeatData> mSeatData = new HashMap<>();

    /**
     * 已选的座位
     */
    private List<SeatData> mSelectedSeats = new ArrayList<>(6);

    /**
     * 已售的座位
     */
    private List<SeatData> mSoldSeats = new ArrayList<>();

    private int mMaxRow, mMaxCol;
    private int mHeight;
    private int mSeatWidth, mSeatHeight;

    private RectF mRangeRect = new RectF();
    private Rect mDrawRect = new Rect();
    private Paint mSeatRangePaint = new Paint();

    private PaintFlagsDrawFilter mDrawFilter;

    private boolean mShowCenterLine;
    private Paint mCenterLinePaint;
    private float[] mCenterLineDash = new float[2];

    public SeatThumbnailView(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public SeatThumbnailView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public SeatThumbnailView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SeatThumbnailView(
            Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(
            Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray a =
                context.obtainStyledAttributes(
                        attrs, R.styleable.SeatView, defStyleAttr, defStyleRes);
        this.mSeatNormal = a.getDrawable(R.styleable.SeatView_seat_drawableNormal);
        this.mSeatSold = a.getDrawable(R.styleable.SeatView_seat_drawableSold);
        this.mSeatSelected = a.getDrawable(R.styleable.SeatView_seat_drawableSelected);
        this.mSeatLoverNormalL = a.getDrawable(R.styleable.SeatView_seat_drawableLoverLeftNormal);
        this.mSeatLoverNormalR = a.getDrawable(R.styleable.SeatView_seat_drawableLoverRightNormal);
        this.mSeatLoverSoldL = a.getDrawable(R.styleable.SeatView_seat_drawableLoverLeftSold);
        this.mSeatLoverSoldR = a.getDrawable(R.styleable.SeatView_seat_drawableLoverRightSold);
        this.mSeatLoverSelectedL =
                a.getDrawable(R.styleable.SeatView_seat_drawableLoverLeftSelected);
        this.mSeatLoverSelectedR =
                a.getDrawable(R.styleable.SeatView_seat_drawableLoverRightSelected);

        this.mShowCenterLine =
                a.getBoolean(R.styleable.SeatView_seat_thumbnailShowCenterLine, false);
        int centerLineColor =
                a.getColor(
                        R.styleable.SeatView_seat_thumbnailCenterLineColor,
                        CENTER_LINE_COLOR_DEFAULT);

        int storeWidth =
                a.getDimensionPixelOffset(
                        R.styleable.SeatView_seat_thumbnailRangeLineWidth, Utils.dp2px(context, 1));
        int lineColor = a.getColor(R.styleable.SeatView_seat_thumbnailRangeLineColor, Color.WHITE);
        this.mThumbnailBackground = a.getDrawable(R.styleable.SeatView_seat_thumbnailBackground);
        this.isAutoHide = a.getBoolean(R.styleable.SeatView_seat_thumbnailAutoHide, true);
        a.recycle();

        mDrawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        mHeight = Utils.dp2px(context, 80);

        if (isAutoHide) {
            setAlpha(0.f);
        }

        mCenterLineDash[0] = Utils.dp2px(context, 1.5f);
        mCenterLineDash[1] = Utils.dp2px(context, 1.5f);

        mSeatRangePaint.setAntiAlias(true);
        mSeatRangePaint.setStyle(Paint.Style.STROKE);
        mSeatRangePaint.setStrokeWidth(storeWidth);
        mSeatRangePaint.setColor(lineColor);

        mCenterLinePaint = new Paint();
        mCenterLinePaint.setAntiAlias(true);
        mCenterLinePaint.setStyle(Paint.Style.STROKE);
        mCenterLinePaint.setColor(centerLineColor);
        mCenterLinePaint.setStrokeWidth(2);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 座位数据为空
        if (mMaxRow <= 0 || mMaxCol <= 0 || mSeatData == null || mSeatData.size() == 0) {
            return;
        }

        // 画背景
        if (mThumbnailBackground != null) {
            mThumbnailBackground.setBounds(0, 0, getWidth(), getHeight());
            mThumbnailBackground.draw(canvas);
        }

        // 画座位图
        int drawStartX = mDrawRect.left;
        int drawStartY = mDrawRect.top;

        final float seatDrawWidth = mSeatWidth;
        final float seatDrawHeight = mSeatHeight;

        int minGraphRow = Integer.MAX_VALUE;
        int centerSeatX = mMaxCol / 2;

        canvas.setDrawFilter(mDrawFilter);
        canvas.save();
        for (Map.Entry<String, SeatData> entry : mSeatData.entrySet()) {
            SeatData seat = entry.getValue();
            int row = seat.point.x;
            int col = seat.point.y;
            minGraphRow = Math.min(minGraphRow, row);

            int left = (int) (drawStartX + seatDrawWidth * (col - 1));
            int top = (int) (drawStartY + seatDrawHeight * (row - 1));
            int right = (int) (left + seatDrawWidth);
            int bottom = (int) (top + seatDrawHeight);

            if (col > centerSeatX) {
                left += 4;
                right += 4;
            }

            // 画座位
            drawSeat(canvas, seat, left, top, right, bottom);

            // 中心线
            if (mShowCenterLine && col == centerSeatX) {
                drawCenterLine(canvas, right + 2, mDrawRect.top, mDrawRect.height());
            }
        }
        canvas.restore();

        // 画当前显示的座位范围
        float left = mDrawRect.left / 2 + (mDrawRect.width() + mDrawRect.left) * mRangeRect.left;
        float right = mDrawRect.left / 2 + (mDrawRect.width() + mDrawRect.left) * mRangeRect.right;
        float top = mDrawRect.top / 2 + (mDrawRect.height() + mDrawRect.top) * mRangeRect.top;
        float bottom = mDrawRect.top / 2 + (mDrawRect.height() + mDrawRect.top) * mRangeRect.bottom;
        canvas.drawRect(left, top, right, bottom, mSeatRangePaint);

        showThumbnailView();
    }

    private void drawSeat(Canvas canvas, SeatData seat, int left, int top, int right, int bottom) {
        Drawable drawable = null;
        if (seat.state == SeatData.STATE_NORMAL) {
            // 情侣座左边的座位
            if (seat.isLoverLeftSeat()) {
                drawable = mSeatLoverNormalL;
            }
            // 情侣座右边的座位
            else if (seat.isLoverRightSeat()) {
                drawable = mSeatLoverNormalR;
                left -= 1;
            }
            // 普通座位
            else {
                drawable = mSeatNormal;
            }
        } else if (seat.state == SeatData.STATE_SOLD) {
            // 情侣座左边的座位
            if (seat.isLoverLeftSeat()) {
                drawable = mSeatLoverSoldL;
            }
            // 情侣座右边的座位
            else if (seat.isLoverRightSeat()) {
                drawable = mSeatLoverSoldR;
                left -= 1;
            }
            // 普通座位
            else {
                drawable = mSeatSold;
            }
        } else if (seat.state == SeatData.STATE_SELECTED) {
            // 情侣座左边的座位
            if (seat.isLoverLeftSeat()) {
                drawable = mSeatLoverSelectedL;
            }
            // 情侣座右边的座位
            else if (seat.isLoverRightSeat()) {
                drawable = mSeatLoverSelectedR;
                left -= 1;
            }
            // 普通座位
            else {
                drawable = mSeatSelected;
            }
        }

        if (drawable != null) {
            drawable.setBounds(left, top, right, bottom);
            drawable.draw(canvas);
        }
    }

    private void drawCenterLine(Canvas canvas, float startX, float startY, float length) {
        int index = (int) ((length / (mCenterLineDash[0] + mCenterLineDash[1]))) + 1;
        float endY = startY;
        for (int i = 0; i < index; i++) {
            endY += mCenterLineDash[0];
            canvas.drawLine(startX, startY, startX, endY, mCenterLinePaint);
            endY += mCenterLineDash[1];
            startY = endY;
        }
    }

    private void showThumbnailView() {
        if (!isAutoHide) {
            return;
        }

        removeCallbacks(hideRunnable);
        postDelayed(hideRunnable, AUTO_HIDE_DELAY);

        if (getAlpha() == 0) {
            setAlpha(1.f);
        }
    }

    private final Runnable hideRunnable =
            new Runnable() {

                @Override
                public void run() {
                    startHideAnimation();
                }
            };

    private void startHideAnimation() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(this, "alpha", 1.f, 0.f);
        animator.setDuration(200);
        animator.start();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        invalidate();
        requestLayout();
    }

    private void updateSoldSeat() {
        if (Utils.isEmpty(mSoldSeats)) {
            return;
        }

        for (SeatData soldSeat : mSoldSeats) {
            mSeatData.put(soldSeat.seatKey(), soldSeat);
        }
    }

    /**
     * 设置座位的数据。
     *
     * @param seats 座位
     */
    public void setSeatData(List<SeatData> seats) {
        if (Utils.isEmpty(seats)) {
            return;
        }

        mSelectedSeats.clear();
        mSeatData.clear();

        mMaxRow = mMaxCol = 0;
        final int size = Utils.size(seats);
        for (int i = 0; i < size; i++) {
            SeatData seat = seats.get(i);
            mSeatData.put(seat.seatKey(), seat);

            mMaxRow = Math.max(seat.point.x, mMaxRow);
            mMaxCol = Math.max(seat.point.y, mMaxCol);
        }

        settingThumbnailViewSize();
        updateSoldSeat();
        invalidate();
    }

    public void setSoldData(List<SeatData> seats) {
        if (Utils.isEmpty(seats)) {
            return;
        }

        mSoldSeats = new ArrayList<>(seats);

        if (mSeatData.size() == 0) {
            invalidate();
            return;
        }

        updateSoldSeat();

        if (Utils.isEmpty(mSelectedSeats)) {
            invalidate();
            return;
        }

        // 判断选中座位是否已售
        // 更新已选中座位图
        boolean needUpdateSelected = false;
        Iterator<SeatData> iterator = mSelectedSeats.iterator();
        while (iterator.hasNext()) {
            SeatData seat = iterator.next();
            SeatData data = mSeatData.get(seat.seatKey());
            if (data != null) {
                // 已售
                if (data.state == SeatData.STATE_SOLD) {
                    needUpdateSelected = true;
                }
                if (needUpdateSelected) {
                    iterator.remove();
                }
            }
        }
        invalidate();
    }

    public void setSelectedSeats(List<SeatData> selectedSeats) {
        mSelectedSeats.clear();
        if (!Utils.isEmpty(selectedSeats)) {
            mSelectedSeats.addAll(selectedSeats);
        }
        invalidate();
    }

    private void settingThumbnailViewSize() {
        if (mMaxRow == 0 || mMaxCol == 0) {
            return;
        }

        final int dp8 = Utils.dp2px(getContext(), 8);
        mSeatHeight = (mHeight - dp8 * 2) / mMaxRow;
        mSeatWidth =
                mSeatHeight * mSeatNormal.getIntrinsicWidth() / mSeatNormal.getIntrinsicHeight();

        int width = mMaxCol * mSeatWidth + dp8 * 2 + (mShowCenterLine ? 4 : 0);
        getLayoutParams().width = width;
        getLayoutParams().height = mHeight;

        int left = (width - mSeatWidth * mMaxCol) / 2;
        int top = (mHeight - mSeatHeight * mMaxRow) / 2;
        int right = width - left;
        int bottom = mHeight - top;
        mDrawRect.set(left, top, right, bottom);

        requestLayout();
    }

    void updateSeatArea(RectF rect) {
        if (rect != null && !rect.isEmpty()) {
            this.mRangeRect = rect;
            invalidate();
        }
    }

    /** 清空座位信息。 */
    void clearSeatData() {
        mSeatData.clear();
        mSoldSeats.clear();
        mSelectedSeats.clear();
        mRangeRect.setEmpty();
        mDrawRect.setEmpty();
        invalidate();
    }
}
