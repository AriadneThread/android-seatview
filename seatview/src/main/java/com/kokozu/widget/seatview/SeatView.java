package com.kokozu.widget.seatview;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 座位图控件。
 *
 * @author fushixiang
 * @since 2016-07-22
 */
public class SeatView extends View {

    private static final String TAG = "kkz.widget.SeatView";

    private static final int DEFAULT_MAX_SELECTED_COUNT = 4; // 默认可选的最大数量
    private static final float MAX_SCALE_UP_RANGE_TOP = 1.f; // 缩放的最大值

    public static final int STATE_NONE = 0;
    public static final int STATE_LOADING = 1;
    public static final int STATE_COMPLETED = 2;

    @SeatState private int mSeatState = STATE_NONE;

    private float mInitFingersDis = 1f;
    private float mInitScale = 1f;
    private float mScale = 1f;
    private float mLatestScale;
    private float mMinScale; // 座位图缩放的最小值

    private float mCurrentX, mCurrentY;
    private float mDrawStartX, mDrawStartY;
    private int mMaxRow, mMaxCol;
    private int mMaxSelectedCount;

    // 座位各种状态的图片
    private Drawable mSeatNormal;
    private Drawable mSeatSold;
    private Drawable mSeatSelected;
    private Drawable mSeatLoverNormalL;
    private Drawable mSeatLoverNormalR;
    private Drawable mSeatLoverSoldL;
    private Drawable mSeatLoverSoldR;
    private Drawable mSeatLoverSelectedL;
    private Drawable mSeatLoverSelectedR;

    // 座位的数据
    private List<SeatData> mSelectedSeats = new ArrayList<>(6);
    private List<SeatData> mSoldSeats = new ArrayList<>(50);
    private Map<String, SeatData> mSeatData = new HashMap<>(50);

    private PaintFlagsDrawFilter mDrawFilter;

    private int mSeatWidth, mSeatHeight;
    private int mSeatPaddingX;

    private boolean mSelectable;
    private boolean isCheckRegularWhilePickSeat;
    private boolean isCheckRegularWhileRecommend;

    // 座位图中轴线
    private boolean mShowCenterLine;
    private CenterLinePainter mCenterLinePainter;

    // 画座位号
    private boolean mShowSeatNo;
    private String[] mSeatNo;
    private SeatNoPainter mSeatNoPainter;

    // 座位缩略图
    private SeatThumbnailView mSeatThumbnailView;
    private RectF mScreenSeatRect = new RectF();

    // 推荐座位
    private BestSeatFinder mBestSeatFinder;

    public SeatView(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public SeatView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public SeatView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SeatView(
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

        this.mMaxSelectedCount =
                a.getInteger(
                        R.styleable.SeatView_seat_maxSelectedCount, DEFAULT_MAX_SELECTED_COUNT);
        this.isCheckRegularWhilePickSeat =
                a.getBoolean(R.styleable.SeatView_seat_checkRegularWhilePickSeat, false);
        this.isCheckRegularWhileRecommend =
                a.getBoolean(R.styleable.SeatView_seat_checkRegularWhileRecommend, false);

        this.mShowCenterLine = a.getBoolean(R.styleable.SeatView_seat_showCenterLine, true);
        this.mShowSeatNo = a.getBoolean(R.styleable.SeatView_seat_showSeatNo, false);
        a.recycle();

        mCenterLinePainter = new CenterLinePainter(context, attrs, defStyleAttr, defStyleRes);
        if (mShowSeatNo) {
            mSeatNoPainter = new SeatNoPainter(context, attrs, defStyleAttr, defStyleRes);
        }

        int drawableWidth, drawableHeight;
        if (mSeatNormal != null) {
            drawableWidth = mSeatNormal.getIntrinsicWidth();
            drawableHeight = mSeatNormal.getIntrinsicHeight();

            this.mSeatWidth = Utils.dp2px(context, 40);
            this.mSeatHeight = mSeatWidth * drawableHeight / drawableWidth;
        }
        this.mDrawFilter =
                new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

        this.mSeatPaddingX = mShowSeatNo ? mSeatNoPainter.getPaddingX() : 0;

        mSelectable = true;
        mScale = 1f;
        mCurrentX = mCurrentY = 0;
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        mBestSeatFinder = new BestSeatFinder();
    }

    @Override
    public void invalidate() {
        super.invalidate();

        updateThumbnailView();
    }

    @Override
    public void postInvalidate() {
        super.postInvalidate();

        post(
                new Runnable() {

                    @Override
                    public void run() {
                        updateThumbnailView();
                    }
                });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        invalidate();
        requestLayout();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 座位数据为空
        if (mSeatData == null || mSeatData.size() == 0) {
            Log.e(TAG, "mSeatData is Empty.");
            return;
        }

        // 座位数据为空
        if (mMaxRow <= 0 || mMaxCol <= 0) {
            Log.e(TAG, "mMaxRow or mMaxCol is less than zero.");
            return;
        }

        int width = getWidth();
        int height = getHeight();

        // 初始化缩放比例
        if (mScale == -1) {
            initSeatScale(width, height);
        }
        if (mScale <= 0) {
            Log.e(TAG, "mScale is less than zero.");
            return;
        }

        limitScaleRange(); // 限制缩放的比例
        limitCurrentXY(); // 限制座位图的位置

        // 座位的中心
        int centerSeatX = mMaxCol / 2;
        int centerSeatY = mMaxRow / 2;

        final float xRight = mCurrentX + mSeatWidth * mScale * mMaxCol;
        if (xRight < width) {
            mDrawStartX = mCurrentX + (width - xRight) / 2;
        } else {
            mDrawStartX = mCurrentX;
        }

        final float yBottom = mCurrentY + mSeatHeight * mScale * mMaxRow;
        if (yBottom < height) {
            mDrawStartY = mCurrentY + (height - yBottom) / 2;
        } else {
            mDrawStartY = mCurrentY;
        }

        float lineStartY = mDrawStartY;
        int minGraphRow = Integer.MAX_VALUE;

        canvas.setDrawFilter(mDrawFilter);
        canvas.save();
        final float seatDrawWidth = mSeatWidth * mScale;
        final float seatDrawHeight = mSeatHeight * mScale;

        float lineX = -1;
        float lineY = -1;

        for (Map.Entry<String, SeatData> stringSeatEntry : mSeatData.entrySet()) {
            SeatData seat = stringSeatEntry.getValue();
            int graphCol = seat.point.y;
            int graphRow = seat.point.x;

            minGraphRow = Math.min(minGraphRow, graphRow);

            int left = (int) (mDrawStartX + seatDrawWidth * (graphCol - 1));
            int top = (int) (mDrawStartY + seatDrawHeight * (graphRow - 1));
            int right = (int) (left + seatDrawWidth);
            int bottom = (int) (top + seatDrawHeight);
            if (lineX == -1 && graphCol == centerSeatX + 1) {
                lineX = left;
            }
            if (lineY == -1 && graphRow == centerSeatY + 1) {
                lineY = top;
            }

            // 画座位
            drawSeat(canvas, seat, left, top, right, bottom);
        }

        final float seatTotalWidth = seatDrawWidth * mMaxCol;
        final float seatTotalHeight = seatDrawHeight * mMaxRow;
        settingScreenSeatRect(seatTotalWidth, seatTotalHeight);

        canvas.restore();

        // 画座位的中心线
        if (lineX > 0 && lineY > 0 && mShowCenterLine) {
            boolean seatNotFill = (seatTotalHeight < getHeight());
            float length = (seatTotalHeight < getHeight() ? seatTotalHeight : getHeight());
            float startY;
            if (seatNotFill) {
                startY = lineStartY + seatDrawHeight * (minGraphRow - 1);
            } else {
                startY = 0;
            }
            mCenterLinePainter.drawLine(canvas, lineX, startY, length);
        }

        // 画座位的排号
        if (mShowSeatNo && mSeatNo != null) {
            float seatNoHeight = mMaxRow * mSeatHeight * mScale;
            mSeatNoPainter.drawSeatNo(mSeatNo, canvas, mMaxRow, mDrawStartY, seatNoHeight);
        }
    }

    private void settingScreenSeatRect(float totalWidth, float totalHeight) {
        float left = Math.min(mDrawStartX, 0);
        float top = Math.min(mDrawStartY, 0);

        float leftRange = Math.min(1, Math.abs(left) / totalWidth);
        float rightRange = Math.min(1, (Math.abs(left) + getWidth()) / totalWidth);
        float topRange = Math.min(1, Math.abs(top) / totalHeight);
        float bottomRange = Math.min(1, (Math.abs(top) + getHeight()) / totalHeight);

        mScreenSeatRect.set(leftRange, topRange, rightRange, bottomRange);

        if (mSeatThumbnailView != null) {
            mSeatThumbnailView.updateSeatArea(mScreenSeatRect);
        }
    }

    private void drawSeat(Canvas canvas, SeatData seat, int left, int top, int right, int bottom) {
        Rect bounds = new Rect(left, top, right, bottom);
        Drawable drawable;

        // 座位可选
        if (seat.state == SeatData.STATE_NORMAL) {
            // 情侣座左边的座位
            if (seat.isLoverLeftSeat()) {
                drawable = mSeatLoverNormalL;
            }
            // 情侣座右边的座位
            else if (seat.isLoverRightSeat()) {
                drawable = mSeatLoverNormalR;
                bounds.left -= 1;
            }
            // 普通座位
            else {
                drawable = mSeatNormal;
            }
        }
        // 座位已选
        else if (seat.state == SeatData.STATE_SELECTED) {
            // 情侣座左边的座位
            if (seat.isLoverLeftSeat()) {
                drawable = mSeatLoverSelectedL;
            }
            // 情侣座右边的座位
            else if (seat.isLoverRightSeat()) {
                drawable = mSeatLoverSelectedR;
                bounds.left -= 1;
            }
            // 普通座位
            else {
                drawable = mSeatSelected;
            }
        }
        // 座位已售
        else {
            // 情侣座左边的座位
            if (seat.isLoverLeftSeat()) {
                drawable = mSeatLoverSoldL;
            }
            // 情侣座右边的座位
            else if (seat.isLoverRightSeat()) {
                drawable = mSeatLoverSoldR;
                bounds.left -= 1;
            }
            // 普通座位
            else {
                drawable = mSeatSold;
            }
        }
        if (drawable != null) {
            drawable.setBounds(bounds);
            drawable.draw(canvas);
        }
    }

    private void limitScaleRange() {
        if (mScale > MAX_SCALE_UP_RANGE_TOP) {
            mScale = MAX_SCALE_UP_RANGE_TOP;
        }
        if (mScale < mMinScale) {
            mScale = mMinScale;
        }
    }

    private void limitCurrentXY() {
        //        float rangeX = getWidth() - seatWidth * newScale * mMaxCol;
        float rangeX = getWidth() - mSeatWidth * mScale * mMaxCol - mSeatPaddingX * 2;
        if (mCurrentX < rangeX) {
            mCurrentX = rangeX;
        }
        //    if (currentX > 0) {
        //      currentX = 0;
        //    }
        //FIXME
        if (mScale == mMinScale) {
            if (mCurrentX > 0) {
                mCurrentX = 0;
            }
        } else {
            if (mCurrentX > mSeatPaddingX) {
                mCurrentX = mSeatPaddingX;
            }
        }

        float rangeY = getHeight() - mSeatHeight * mScale * mMaxRow;
        if (mCurrentY < rangeY) {
            mCurrentY = rangeY;
        }
        if (mCurrentY > 0) {
            mCurrentY = 0;
        }
    }

    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mTouchMode = NONE;

    private int mTouchSlop;
    private long mTouchDownTime;
    private boolean isMoveMode;

    private PointF mTouchDownPoint = new PointF();
    private PointF mTouchDown2Point = new PointF();
    private PointF mTouchEventPoint = new PointF();

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        // 手指按下
        if (action == MotionEvent.ACTION_DOWN) {
            mTouchDownTime = SystemClock.uptimeMillis();
            mTouchDownPoint.set(event.getX(), event.getY());
            mTouchEventPoint.set(event.getX(), event.getY());
            mTouchMode = DRAG;
        }
        // 第二个手指按下
        else if (action == MotionEvent.ACTION_POINTER_DOWN) {
            mTouchDown2Point.set(event.getX(1), event.getY(1));
            mInitFingersDis = spacing(event);
            mInitScale = mScale;
            mLatestScale = mScale;
            if (mInitFingersDis > 10f) {
                mTouchMode = ZOOM;
            }
        }
        // 手指抬起
        else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            isMoveMode = false;
            if (mSelectable
                    && Math.abs(event.getX() - mTouchDownPoint.x) < mTouchSlop
                    && Math.abs(event.getY() - mTouchDownPoint.y) < mTouchSlop) {

                // 处理点击事件
                performClickSeat(event);
            }
        }
        // 第二个手指抬起
        else if (action == MotionEvent.ACTION_POINTER_UP) {
            mTouchMode = NONE;
        }
        // 手指移动
        else if (action == MotionEvent.ACTION_MOVE) {
            performMoveEvent(event);
        }
        return true;
    }

    private void performClickSeat(MotionEvent event) {
        int col = (int) ((event.getX() - mDrawStartX) / mSeatWidth / mScale) + 1;
        int row = (int) ((event.getY() - mDrawStartY) / mSeatHeight / mScale) + 1;

        if (col > mMaxCol || col <= 0 || row > mMaxRow || row <= 0) {
            return;
        }

        String key = row + "-" + col;
        SeatData seat = mSeatData.get(key);
        if (seat == null) {
            return;
        }

        boolean isMatchRegular = true;
        // 已选座位
        if (seat.state == SeatData.STATE_SELECTED) {
            seat.unSelectSeat();
            mSelectedSeats.remove(seat);

            if (seat.isLoverSeat()) { // 情侣座
                unSelectLoverSeat(seat);
            }
            isMatchRegular = checkSeatRegular(seat, false);
        } else if (seat.state == SeatData.STATE_NORMAL) {
            int selectedCount = Utils.size(mSelectedSeats);
            if (selectedCount >= mMaxSelectedCount) {
                if (mChooseSeatListener != null) {
                    mChooseSeatListener.onSelectedSeatOverMaxCount(mMaxSelectedCount);
                }
                return;
            }

            // 判断选座规则：先把座位放到已选中的座位列表中，然后判断规则
            seat.selectSeat();
            mSelectedSeats.add(seat);

            if (seat.isLoverSeat()) { // 情侣座
                isMatchRegular = selectLoverSeat(seat);
            } else { // 非情侣座
                isMatchRegular = checkSeatRegular(seat, true);
                if (isMatchRegular && selectedCount == 0) {
                    performZoomInMaxScale(row, col);
                }
            }
        }

        if (mChooseSeatListener != null && isMatchRegular) {
            mChooseSeatListener.onSelectedSeatChanged(mSelectedSeats);
        }
        invalidate();
    }

    private void performMoveEvent(MotionEvent event) {
        if (SystemClock.uptimeMillis() - mTouchDownTime < 50) {
            return;
        }

        if (mTouchMode == DRAG) {
            performDragSeat(event);
        } else if (mTouchMode == ZOOM) {
            performZoomSeat(event);
        }
    }

    private void performDragSeat(MotionEvent event) {
        final float x = event.getX();
        final float y = event.getY();

        if (!isMoveMode
                && ((Math.abs(x - mTouchDownPoint.x) > 10
                        || Math.abs(y - mTouchDownPoint.y) > 10))) {
            isMoveMode = true;
        }

        if (isMoveMode) {
            mCurrentX = mCurrentX + (x - mTouchEventPoint.x) * 1.02f;
            mCurrentY = mCurrentY + (y - mTouchEventPoint.y) * 1.02f;
            mTouchEventPoint.set(x, y);

            invalidate();
        }
    }

    private void performZoomSeat(MotionEvent event) {
        float newDist = spacing(event);
        if (newDist < 10) {
            return;
        }

        if (newDist > 10f) {
            mScale = newDist / mInitFingersDis * mInitScale;
            limitScaleRange();

            if (mScale != mLatestScale) {
                // 计算缩放时 currentX、currentY 的值
                float midX = (mTouchDownPoint.x + mTouchDown2Point.x) / 2;
                float midY = (mTouchDownPoint.y + mTouchDown2Point.y) / 2;

                mCurrentX = midX - (midX - mCurrentX) * mScale / mLatestScale;
                mCurrentY = midY - (midY - mCurrentY) * mScale / mLatestScale;

                invalidate();
                mLatestScale = mScale;
            }
        }
    }

    private boolean checkSeatRegular(SeatData seat, boolean selectSeat) {
        if (isCheckRegularWhilePickSeat) { // 选座时校验选座规则
            // 该座位不符合选座规则
            if (!isSelectedSeatLegal()) {
                if (selectSeat) {
                    seat.unSelectSeat();
                    mSelectedSeats.remove(seat);
                } else {
                    seat.selectSeat();
                    mSelectedSeats.add(seat);
                }
                if (mChooseSeatListener != null) {
                    mChooseSeatListener.onSelectSeatNotMatchRegular();
                }
                return false;
            }
        }
        return true;
    }

    private boolean selectLoverSeat(SeatData seat) {
        int seatRow = seat.point.x;
        int seatCol = seat.point.y + (seat.isLoverLeftSeat() ? 1 : -1);
        SeatData other = mSeatData.get(seatRow + "-" + seatCol);

        // 若选中另一个情侣座则超过最大数量
        if (Utils.size(mSelectedSeats) >= mMaxSelectedCount) {
            seat.unSelectSeat();
            mSelectedSeats.remove(seat);

            if (mChooseSeatListener != null) {
                mChooseSeatListener.onPickLoverSeatOverMaxCount(mMaxSelectedCount);
            }
            return false;
        } else if (other != null && other.state == SeatData.STATE_NORMAL) {
            other.selectSeat();
            mSelectedSeats.add(other);

            if (checkSeatRegular(seat, true)) {
                if (Utils.isEmpty(mSelectedSeats)) {
                    performZoomInMaxScale(seat.point.x, seat.point.y);
                }
                return true;
            } else {
                other.unSelectSeat();
                mSelectedSeats.remove(other);
            }
        }
        return false;
    }

    private void unSelectLoverSeat(SeatData seat) {
        int graphRow = seat.point.x;
        int graphCol = seat.point.y + (seat.isLoverLeftSeat() ? 1 : -1);

        SeatData loverOther = mSeatData.get(graphRow + "-" + graphCol);
        if (loverOther != null && loverOther.state == SeatData.STATE_SELECTED) {
            loverOther.unSelectSeat();
            mSelectedSeats.remove(loverOther);
        }
    }

    private void performZoomInMaxScale(final int row, final int col) {
        if (mScale >= MAX_SCALE_UP_RANGE_TOP) {
            return;
        }

        long duration = (long) Math.abs((MAX_SCALE_UP_RANGE_TOP - mScale) / 0.5 * 100);
        duration = Math.max(150, duration);

        final float initScale = mScale;

        ValueAnimator anim = ValueAnimator.ofFloat(mScale, MAX_SCALE_UP_RANGE_TOP);
        anim.setDuration(duration);
        anim.addUpdateListener(
                new ValueAnimator.AnimatorUpdateListener() {

                    float latestScale = initScale;

                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        mScale = (Float) animation.getAnimatedValue();
                        if (row > 0 && col > 0) {
                            zoomInSeatView(latestScale, row, col);
                            latestScale = mScale;
                        }
                        invalidate();
                    }
                });
        anim.setTarget(this);
        anim.start();
    }

    private void zoomInSeatView(float lastScale, int row, int col) {
        mCurrentX = mCurrentX - (mSeatWidth * (col - 0.5f) * (mScale - lastScale));
        mCurrentY = mCurrentY - (mSeatHeight * (row - 0.5f) * (mScale - lastScale));

        final float newSeatHeight = mSeatHeight * mScale;
        float topY = mCurrentY + newSeatHeight * (row - 1); // 点击行的顶点y坐标
        float bottomY = topY + newSeatHeight; // 点击行的底部y坐标
        final int factorY = 0;
        if (topY < factorY) {
            mCurrentY = factorY - newSeatHeight * (row - 1);
        }
        if (getHeight() > 0 && bottomY > getHeight()) {
            mCurrentY = getHeight() - newSeatHeight * row;
        }

        final float newSeatWidth = mSeatWidth * mScale;
        float leftX = mCurrentX + newSeatWidth * (col - 1); // 点击列的左点x坐标
        float rightX = leftX + newSeatWidth; // 点击列右边的x坐标
        final int factorX = 0;
        if (leftX < factorX) {
            mCurrentX = factorX - newSeatWidth * (col - 1);
        }
        if (getWidth() > 0 && rightX > getWidth()) {
            mCurrentX = getWidth() - newSeatWidth * col;
        }
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * 设置已售的座位数据。
     *
     * @param seats 已售的座位
     */
    public void setSoldData(List<SeatData> seats) {
        mSoldSeats.clear();
        if (Utils.isEmpty(seats) || isSeatEmpty()) {
            return;
        }

        mSoldSeats.addAll(seats);
        mBestSeatFinder.setSoldSeats(seats);

        if (!Utils.isEmpty(mSelectedSeats)) { // 判断选中座位是否已售
            boolean needUpdateSelected = false; // 更新已选中座位图
            for (SeatData seat : mSelectedSeats) {
                SeatData data = mSeatData.get(seat.seatKey());
                if (data != null && data.state == SeatData.STATE_SOLD) { // 已售
                    needUpdateSelected = true;
                }
            }

            if (needUpdateSelected) {
                for (SeatData seat : mSelectedSeats) {
                    SeatData data = mSeatData.get(seat.seatKey());
                    if (data != null) {
                        data.unSelectSeat(); // 重置已选座位
                    }
                }
                mSelectedSeats.clear(); // 清空已选座位图
                if (mChooseSeatListener != null) {
                    mChooseSeatListener.onSelectedSeatChanged(mSelectedSeats);
                }
                if (mChooseSeatListener != null) {
                    mChooseSeatListener.onSelectedSeatSold();
                }
            }
        }
        updateSoldSeat();
        invalidate();
    }

    private void updateSoldSeat() {
        if (isSeatEmpty() || Utils.isEmpty(mSoldSeats)) {
            return;
        }
        for (SeatData soldSeat : mSoldSeats) {
            mSeatData.put(soldSeat.seatKey(), soldSeat);
        }

        if (mSeatThumbnailView != null) {
            mSeatThumbnailView.setSoldData(mSoldSeats);
        }
    }

    /**
     * 绑定座位的缩略图。
     *
     * @param view 缩略图
     */
    public void attachThumbnailView(SeatThumbnailView view) {
        mSeatThumbnailView = view;
    }

    private void updateThumbnailView() {
        if (mSeatThumbnailView != null) {
            mSeatThumbnailView.updateSeatArea(mScreenSeatRect);
        }
    }

    /**
     * 设置座位图的数据。
     *
     * @param seats 座位列表
     */
    public void setSeatData(List<SeatData> seats) {
        mSelectedSeats.clear();
        mSeatData.clear();
        mBestSeatFinder.setSeats(seats);

        if (Utils.isEmpty(seats)) {
            return;
        }

        SparseIntArray rowMap = new SparseIntArray();

        mMaxRow = mMaxCol = 0;
        final int size = Utils.size(seats);
        for (int i = 0; i < size; i++) {
            SeatData seat = seats.get(i);
            mSeatData.put(seat.seatKey(), seat);
            mMaxRow = Math.max(seat.point.x, mMaxRow);
            mMaxCol = Math.max(seat.point.y, mMaxCol);

            int rowCount = rowMap.get(seat.point.x, 0);
            rowMap.put(seat.point.x, rowCount + 1);
        }

        updateSoldSeat();

        int width = getWidth();
        int height = getHeight();
        initSeatScale(width, height);

        if (mMaxRow > 0 && rowMap.size() > 0) {
            mSeatNo = new String[mMaxRow];
            int seatNo = 1; // 座位排号
            for (int i = 1; i <= mMaxRow; i++) {
                int count = rowMap.get(i, 0);
                mSeatNo[i - 1] = count > 0 ? String.valueOf(seatNo) : "";
                if (count > 0) {
                    seatNo++;
                }
            }
        }

        if (mSeatThumbnailView != null) {
            mSeatThumbnailView.setSeatData(seats);
        }
        invalidate();
    }

    /**
     * 设置已选的座位。
     *
     * @param seatData 已选的座位
     */
    public void setSelectedData(List<SeatData> seatData) {
        List<SeatData> datas = new ArrayList<>(mSelectedSeats);
        for (SeatData data : datas) {
            data.unSelectSeat();
        }
        datas.clear();
        datas.addAll(seatData);
        for (SeatData data : datas) {
            data.selectSeat();
        }
        this.mSelectedSeats = new ArrayList<>(datas);
        invalidate();
    }

    private void initSeatScale(int width, int height) {
        float scaleX = (width - mSeatPaddingX * 2) * 1.f / mMaxCol / mSeatWidth;
        float scaleY = height * 1.f / mMaxRow / mSeatHeight;
        mScale = mMinScale = Math.min(scaleX, scaleY);
    }

    /**
     * 判断座位是否无数据。
     *
     * @return 是否无数据
     */
    public boolean isSeatEmpty() {
        return mSeatData == null || mSeatData.size() <= 0;
    }

    /**
     * 是否无已售座位。
     *
     * @return 是否无已售座位
     */
    public boolean isSoldSeatEmpty() {
        return Utils.size(mSoldSeats) <= 0;
    }

    /**
     * 座位图是否处于放大显示。
     *
     * @return 是否放大显示
     */
    public boolean isScale() {
        return mScreenSeatRect.right != mMaxCol || mScreenSeatRect.bottom != mMaxRow;
    }

    /**
     * 获取已选的座位。
     *
     * @return 已选的座位
     */
    public List<SeatData> getSelectedSeat() {
        if (mSelectedSeats == null) {
            mSelectedSeats = new ArrayList<>();
        }
        return mSelectedSeats;
    }

    /**
     * 获取已选座位的数量。
     *
     * @return 已选座位的数量
     */
    public int getSelectedSeatCount() {
        return Utils.size(mSelectedSeats);
    }

    /**
     * 获取座位的数据。
     *
     * @return 座位的数据
     */
    public List<SeatData> getSeatData() {
        return new ArrayList<>(mSeatData.values());
    }

    /**
     * 获取已售的座位。
     *
     * @return 已售的座位
     */
    public List<SeatData> getSoldSeatData() {
        return mSoldSeats;
    }

    /** 清除所有已选的座位。 */
    public void removeAllSelectedSeats() {
        if (!Utils.isEmpty(mSelectedSeats)) {
            for (SeatData seat : mSelectedSeats) {
                seat.unSelectSeat();
            }
        }
        mSelectedSeats.clear();
        invalidate();
    }

    /**
     * 移除已选的座位。
     *
     * @param seat 座位
     */
    public void removeSelectedSeat(SeatData seat) {
        if (Utils.isEmpty(mSelectedSeats)) {
            return;
        }

        if (seat.isLoverSeat()) { // 情侣座
            seat.unSelectSeat();
            int graphRow = seat.point.x;
            int graphCol = seat.point.y + (seat.isLoverLeftSeat() ? 1 : -1);

            SeatData other = mSeatData.get(graphRow + "-" + graphCol);
            if (other != null && other.state == SeatData.STATE_SELECTED) {
                other.unSelectSeat();
                mSelectedSeats.remove(other);
            }
            mSelectedSeats.remove(seat);
        }
        List<SeatData> selectedSeats = new ArrayList<>(mSelectedSeats);
        selectedSeats.remove(seat);
        seat.unSelectSeat();

        checkSeatRegular(seat, false);
        mSelectedSeats.remove(seat);
        invalidate();
    }

    /** 清空座位数据。 */
    public void clearSeatData() {
        mSeatData.clear();
        mSoldSeats.clear();
        mSelectedSeats.clear();
        mBestSeatFinder.clear();

        if (mSeatThumbnailView != null) {
            mSeatThumbnailView.clearSeatData();
        }

        invalidate();
    }

    /**
     * 获取推荐的座位。
     *
     * @param recommendCount 推荐座位的数量
     * @return 推荐的座位
     */
    public List<SeatData> selectRecommendSeats(int recommendCount) {
        List<SeatData> seats = mBestSeatFinder.selectedRecommendSeat(recommendCount);
        if (!Utils.isEmpty(seats)) {
            if (!Utils.isEmpty(mSelectedSeats)) {
                for (SeatData seat : mSelectedSeats) {
                    seat.unSelectSeat();
                }
            }
            for (SeatData seat : seats) {
                seat.selectSeat();
            }

            mSelectedSeats.clear();
            mSelectedSeats.addAll(seats);

            // 检查推荐的座位是否合法
            if (isCheckRegularWhileRecommend && checkRecommendSeatRegular(recommendCount)) {
                invalidate();
                if (mChooseSeatListener != null) {
                    mChooseSeatListener.onSelectedSeatChanged(mSelectedSeats);
                }
            }
        }
        return seats;
    }

    private boolean checkRecommendSeatRegular(int recommendCount) {
        if (!isSelectedSeatLegal()) {
            mBestSeatFinder.addIgnoreSeats(recommendCount, mSelectedSeats);
            selectRecommendSeats(recommendCount);
            return false;
        }
        return true;
    }

    /**
     * 判断选择的座位是否符合规则。
     *
     * @return 是否符合规则
     */
    public boolean isSelectedSeatLegal() {
        return SeatSelectRegular.isSelectedSeatLegal(mSelectedSeats, mSeatData, mMaxCol);
    }

    private OnChooseSeatListener mChooseSeatListener;

    public void setOnChooseSeatListener(OnChooseSeatListener listener) {
        this.mChooseSeatListener = listener;
    }

    /**
     * 设置座位图的状态。
     *
     * @param state 状态
     */
    public void setSeatState(@SeatState int state) {
        this.mSeatState = state;
    }

    /**
     * 获取座位图的状态。
     *
     * @return 状态
     */
    @SeatState
    public int getSeatState() {
        return mSeatState;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({STATE_NONE, STATE_LOADING, STATE_COMPLETED})
    public @interface SeatState {}
}
