# 座位图控件

**最低版本支持：API Level 16**

[![](https://www.jitpack.io/v/AriadneThread/android-seatview.svg)](https://www.jitpack.io/#AriadneThread/android-seatview)

## Dependencies

```
compile 'com.android.support:support-annotations:26.+' // support annotations
```

## Usage

### 自定义属性

```
<!-- 可选座位的图片 -->
<attr name="seat_drawableNormal" format="reference" />

<!-- 已售座位的图片 -->
<attr name="seat_drawableSold" format="reference" />

<!-- 已选座位的图片 -->
<attr name="seat_drawableSelected" format="reference" />

<!-- 可选的情侣座左边座位的图片 -->
<attr name="seat_drawableLoverLeftNormal" format="reference" />

<!-- 可选的情侣座右边座位的图片 -->
<attr name="seat_drawableLoverRightNormal" format="reference" />

<!-- 已售的情侣座左边座位的图片 -->
<attr name="seat_drawableLoverLeftSold" format="reference" />

<!-- 已售的情侣座右边座位的图片 -->
<attr name="seat_drawableLoverRightSold" format="reference" />

<!-- 已选的情侣座左边座位的图片 -->
<attr name="seat_drawableLoverLeftSelected" format="reference" />

<!-- 已选的情侣座右边座位的图片 -->
<attr name="seat_drawableLoverRightSelected" format="reference" />

<!-- 座位可选的最大数量 -->
<attr name="seat_maxSelectedCount" format="integer" />

<!-- 在选座时是否限制座位的规则 -->
<attr name="seat_checkRegularWhilePickSeat" format="boolean" />

<!-- 是否显示座位图的中轴线 -->
<attr name="seat_showCenterLine" format="boolean" />

<!-- 座位图中轴线的颜色 -->
<attr name="seat_centerLineColor" format="color|reference" />

<!-- 座位图中轴线的颜色 -->
<attr name="seat_centerLineWidth" format="dimension|reference" />

<!-- 是否显示座位的排号 -->
<attr name="seat_showSeatNo" format="boolean" />

<!-- 排号的宽度 -->
<attr name="seat_seatNoWidth" format="dimension|reference" />

<!-- 排号的字体大小 -->
<attr name="seat_seatNoTextSize" format="dimension|reference" />

<!-- 排号的字体颜色 -->
<attr name="seat_seatNoTextColor" format="color|reference" />

<!-- 排号文字距离顶部的边距 -->
<attr name="seat_seatNoTopMargin" format="dimension|reference" />

<!-- 排号文字距离左边的边距 -->
<attr name="seat_seatNoLeftMargin" format="dimension|reference" />

<!-- 排号文字的背景色 -->
<attr name="seat_seatNoBackgroundColor" format="color|reference" />

<!-- 座位缩略图的背景 -->
<attr name="seat_thumbnailBackground" format="reference" />

<!-- 座位缩略图是否显示中轴线 -->
<attr name="seat_thumbnailShowCenterLine" format="boolean"/>

<!-- 座位缩略图中轴线的颜色 -->
<attr name="seat_thumbnailCenterLineColor" format="color|reference" />

<!-- 座位缩略图是否可以自动隐藏 -->
<attr name="seat_thumbnailAutoHide" format="boolean"/>

<!-- 座位缩略图中座位范围线条的宽度 -->
<attr name="seat_thumbnailRangeLineWidth" format="dimension|reference" />

<!-- 座位缩略图中座位范围线条的颜色 -->
<attr name="seat_thumbnailRangeLineColor" format="color|reference" />
```

### 座位图控件

`com.kokozu.widget.seatview.SeatView`

### 座位缩略图控件

`com.kokozu.widget.seatview.SeatThumbnailView`


