package com.kokozu.widget.seatview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

/**
 * 绘制座位图中轴线的类。
 *
 * @author wuzhen
 * @since 2017-04-20
 */
class CenterLinePainter {

    private static final int DEFAULT_CENTER_LINE_WIDTH_DP = 1;
    private static final int DEFAULT_CENTER_LINE_COLOR = Color.argb(255, 136, 136, 136);

    private Paint mCenterLinePaint = new Paint();
    private float[] dashLine = new float[2];

    CenterLinePainter(Context context, @Nullable AttributeSet attrs,
                      int defStyleAttr, int defStyleRes) {
        final int defLineWidth = Utils.dp2px(context, DEFAULT_CENTER_LINE_WIDTH_DP);

        TypedArray a =
                context.obtainStyledAttributes(
                        attrs, R.styleable.SeatView, defStyleAttr, defStyleRes);
        int centerLineWidth =
                a.getDimensionPixelSize(R.styleable.SeatView_seat_centerLineWidth, defLineWidth);
        int centerLineColor =
                a.getColor(R.styleable.SeatView_seat_centerLineColor, DEFAULT_CENTER_LINE_COLOR);
        a.recycle();

        int dp4 = Utils.dp2px(context, 4);
        dashLine[0] = dp4;
        dashLine[1] = dp4;

        mCenterLinePaint.setAntiAlias(true);
        mCenterLinePaint.setStyle(Paint.Style.STROKE);
        mCenterLinePaint.setColor(centerLineColor);
        mCenterLinePaint.setStrokeWidth(centerLineWidth);
    }

    void drawLine(Canvas canvas, float startX, float startY, float length) {
        int index = (int) ((length / (dashLine[0] + dashLine[1]))) + 1;
        float endY = startY;
        for (int i = 0; i < index; i++) {
            endY += dashLine[0];
            canvas.drawLine(startX, startY, startX, endY, mCenterLinePaint);
            endY += dashLine[1];
            startY = endY;
        }
    }
}
