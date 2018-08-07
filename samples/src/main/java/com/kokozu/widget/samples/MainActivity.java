package com.kokozu.widget.samples;

import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.kokozu.widget.seatview.OnChooseSeatListener;
import com.kokozu.widget.seatview.SeatData;
import com.kokozu.widget.seatview.SeatThumbnailView;
import com.kokozu.widget.seatview.SeatView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnChooseSeatListener {

    SeatView seatView;
    SeatThumbnailView thumbnailView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        seatView = findViewById(R.id.seat_view);
        thumbnailView = findViewById(R.id.thumbnail_view);
        seatView.attachThumbnailView(thumbnailView);
        seatView.setOnChooseSeatListener(this);
        seatView.setSeatState(SeatView.STATE_LOADING);

        loadSeats();

        findViewById(R.id.btn1).setOnClickListener(mRecommendClicked);
        findViewById(R.id.btn2).setOnClickListener(mRecommendClicked);
        findViewById(R.id.btn3).setOnClickListener(mRecommendClicked);
        findViewById(R.id.btn4).setOnClickListener(mRecommendClicked);

        findViewById(R.id.btn5).setOnClickListener(mRegularClicked);
    }

    private View.OnClickListener mRecommendClicked =
            new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    int recommendCount;
                    switch (v.getId()) {
                        case R.id.btn1:
                            recommendCount = 1;
                            break;

                        case R.id.btn2:
                            recommendCount = 2;
                            break;

                        case R.id.btn3:
                            recommendCount = 3;
                            break;

                        case R.id.btn4:
                            recommendCount = 4;
                            break;

                        default:
                            recommendCount = 1;
                            break;
                    }
                    List<SeatData> seats = seatView.selectRecommendSeats(recommendCount);
                    if (seats == null || seats.size() == 0) {
                        Toast.makeText(MainActivity.this, "未获取到推荐座位", Toast.LENGTH_SHORT).show();
                    }
                    seatView.setSelectedData(seats);
                }
            };

    private View.OnClickListener mRegularClicked =
            new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    boolean legal = seatView.isSelectedSeatLegal();
                    String message = legal ? "符合规则" : "不许留空座";
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            };

    private void loadSeats() {
        new Thread(
                new Runnable() {

                    @Override
                    public void run() {
                        try {
                            InputStream is = getAssets().open("seats.json");
                            String seatsText = convertStreamToString(is);
                            JSONObject object = JSON.parseObject(seatsText);
                            List<Seat> seats =
                                    JSON.parseArray(object.getString("seats"), Seat.class);

                            if (seats != null) {
                                final List<SeatData> seatList = new ArrayList<>();
                                for (Seat seat : seats) {
                                    SeatData seatData = new SeatData();
                                    seatData.state =
                                            seat.getSeatState() == 0
                                                    ? SeatData.STATE_NORMAL
                                                    : SeatData.STATE_SOLD;
                                    seatData.point =
                                            new Point(
                                                    seat.getGraphRow(), seat.getGraphCol());
                                    if (seat.getSeatType() == 1) {
                                        seatData.type =
                                                seat.isLoverL()
                                                        ? SeatData.TYPE_LOVER_LEFT
                                                        : SeatData.TYPE_LOVER_RIGHT;
                                    } else {
                                        seatData.type = SeatData.TYPE_NORMAL;
                                    }
                                    seatList.add(seatData);
                                }

                                try {
                                    Thread.sleep(3000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                runOnUiThread(
                                        new Runnable() {

                                            @Override
                                            public void run() {
                                                seatView.setSeatData(seatList);
                                            }
                                        });
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .start();

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {

                        try {
                            InputStream is = getAssets().open("sold_seat.json");
                            String seatsText = convertStreamToString(is);
                            JSONObject object = JSON.parseObject(seatsText);
                            List<Seat> seats =
                                    JSON.parseArray(object.getString("seats"), Seat.class);

                            if (seats != null) {
                                final List<SeatData> seatList = new ArrayList<>();
                                for (Seat seat : seats) {
                                    SeatData seatData = new SeatData();
                                    seatData.state =
                                            seat.getSeatState() == 0
                                                    ? SeatData.STATE_NORMAL
                                                    : SeatData.STATE_SOLD;
                                    seatData.point =
                                            new Point(
                                                    seat.getGraphRow(), seat.getGraphCol());
                                    if (seat.getSeatType() == 1) {
                                        seatData.type =
                                                seat.isLoverL()
                                                        ? SeatData.TYPE_LOVER_LEFT
                                                        : SeatData.TYPE_LOVER_RIGHT;
                                    } else {
                                        seatData.type = SeatData.TYPE_NORMAL;
                                    }
                                    seatList.add(seatData);
                                }

                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                runOnUiThread(
                                        new Runnable() {

                                            @Override
                                            public void run() {
                                                seatView.setSoldData(seatList);
                                            }
                                        });
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .start();
    }

    static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return sb.toString();
    }

    @Override
    public void onPickLoverSeatOverMaxCount(int maxSelectCount) {
        Toast.makeText(this, "情侣座超出座位数量限制", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSelectedSeatOverMaxCount(int maxSelectCount) {
        Toast.makeText(this, "最多选择" + maxSelectCount + "个座位", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSelectSeatNotMatchRegular() {
        Toast.makeText(this, "不能留空座", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSelectedSeatChanged(List<SeatData> selectedSeats) {
        if (selectedSeats == null) {
            return;
        }

        StringBuilder seats = new StringBuilder();
        for (SeatData seat : selectedSeats) {
            seats.append(seat.point.x);
            seats.append("-");
            seats.append(seat.point.y);
            seats.append(", ");
        }
        Toast.makeText(this, "已选座位： " + seats, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSelectedSeatSold() {
        Toast.makeText(this, "选择的座位已被售出", Toast.LENGTH_SHORT).show();
    }
}
