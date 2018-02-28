package com.kokozu.widget.seatview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;

/**
 * 画座位图排号。
 *
 * @author wuzhen
 * @since 2017-04-20
 */
class SeatNoPainter {

    private static final int BACKGROUND_COLOR_DEFAULT = Color.parseColor("#4C000000");

    private Paint mSeatNoPaint = new Paint(); //画座位号
    private RectF mSeatNoRectF = new RectF(); //画座位号背景

    private int mSeatNoWidth;
    private int mSeatNoTopMargin;
    private int mSeatNoLeftMargin;
    private int mSeatNoTextColor;
    private int mSeatNoBackgroundColor;

    private Context mContext;

    SeatNoPainter(
            Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this.mContext = context;
        TypedArray a =
                context.obtainStyledAttributes(
                        attrs, R.styleable.SeatView, defStyleAttr, defStyleRes);
        this.mSeatNoWidth =
                a.getDimensionPixelOffset(
                        R.styleable.SeatView_seat_seatNoWidth, Utils.dp2px(context, 14));
        this.mSeatNoTopMargin =
                a.getDimensionPixelOffset(
                        R.styleable.SeatView_seat_seatNoTopMargin, Utils.dp2px(context, 15));
        this.mSeatNoLeftMargin =
                a.getDimensionPixelOffset(
                        R.styleable.SeatView_seat_seatNoLeftMargin, Utils.dp2px(context, 4));
        this.mSeatNoBackgroundColor =
                a.getColor(
                        R.styleable.SeatView_seat_seatNoBackgroundColor, BACKGROUND_COLOR_DEFAULT);

        float textSize =
                a.getDimension(R.styleable.SeatView_seat_seatNoTextSize, mSeatNoWidth * 0.7f);
        mSeatNoTextColor = a.getColor(R.styleable.SeatView_seat_seatNoTextColor, Color.WHITE);
        a.recycle();

        mSeatNoPaint.setTextSize(textSize);
        mSeatNoPaint.setColor(mSeatNoTextColor);
        mSeatNoPaint.setAntiAlias(true);
        mSeatNoPaint.setTextAlign(Paint.Align.CENTER);
    }

    int getPaddingX() {
        return mSeatNoLeftMargin + mSeatNoWidth + Utils.dp2px(mContext, 5);
    }

    void drawSeatNo(String[] seatNo, Canvas canvas, int maxRow, float drawTop, float height) {
        canvas.save();
        float h = height + mSeatNoTopMargin * 2;
        float left = mSeatNoLeftMargin;
        float top = drawTop - mSeatNoTopMargin;
        mSeatNoRectF.set(left, top, left + mSeatNoWidth, top + h);
        mSeatNoPaint.setColor(mSeatNoBackgroundColor);
        canvas.drawRoundRect(mSeatNoRectF, mSeatNoTopMargin, mSeatNoTopMargin, mSeatNoPaint);

        mSeatNoPaint.setColor(mSeatNoTextColor);
        float x = left + mSeatNoWidth / 2;
        float noHeight = height / maxRow;
        Paint.FontMetricsInt fontMetrics = mSeatNoPaint.getFontMetricsInt();
        for (int i = 0; i < seatNo.length; i++) {
            float textStartY = mSeatNoRectF.top + mSeatNoTopMargin + noHeight * i;
            float textEndY = textStartY + noHeight;
            float baseline = (textStartY + textEndY - fontMetrics.bottom - fontMetrics.top) / 2;
            if (!TextUtils.isEmpty(seatNo[i])) {
                canvas.drawText(seatNo[i], x, baseline, mSeatNoPaint);
            }
        }
        canvas.restore();
    }
}
