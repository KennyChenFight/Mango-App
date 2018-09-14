package com.csim.scu.aibox.view.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.csim.scu.aibox.R;
import com.csim.scu.aibox.log.Logger;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by kenny on 2018/8/19.
 */

public class BloodFragment extends Fragment {

    private List<Map<String, String>> concerList;
    private Button btShare;
    private LineChart lineChart;
    private ImageButton ibBack;
    private XAxis xAxis;                //X軸
    private YAxis leftYAxis;            //左側Y軸
    private YAxis rightYaxis;           //右側Y軸
    private Legend legend;              //圖例
    private LimitLine limitLine;        //限制線
    private String[] week = new String[]{"週一", "週二", "週三", "週四", "週五", "週六", "週日"};
    private boolean[] weekStatus = new boolean[]{false, false, false, false, false, false, false};

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_blood, container, false);
        concerList = (List<Map<String, String>>) this.getArguments().getSerializable("concernList");
        findViews(view);
        initLineChart();
        setShareListener();
        return view;
    }

    private void findViews(View view) {
        btShare = view.findViewById(R.id.btShare);
        ibBack = view.findViewById(R.id.ibBack);
        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().popBackStackImmediate();
            }
        });
        lineChart = view.findViewById(R.id.lineChart);
        for (int i = 0; i < concerList.size(); i++) {
            Map<String, String> map = concerList.get(i);
            Logger.d(Integer.parseInt(map.get("dateWeekNum")) + "");
            weekStatus[Integer.parseInt(map.get("dateWeekNum"))] = true;
        }
        weekStatus[0] = true;
    }

    private void initLineChart() {
        /**
         * lineChart設定
         */
        // 無資料的情況
        lineChart.setNoDataText("尚無血壓資料");
        // 是否展示網格線
        lineChart.setDrawGridBackground(false);
        // 是否顯示邊界
        lineChart.setDrawBorders(true);
        // 是否可以拖動
        lineChart.setDragEnabled(false);
        // 是否有觸控事件
        lineChart.setTouchEnabled(false);
        // 設定關於此圖表的敘述
        lineChart.getDescription().setText("血壓圖");
        // 設定此圖表的顏色
        lineChart.setBackgroundColor(Color.WHITE);
        // 是否顯示邊界
        lineChart.setDrawBorders(false);

        /**
         * XY軸設定
         */
        // 取得X軸物件
        xAxis = lineChart.getXAxis();
        // X軸設定顯示位置在底部
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        // 是否有X軸的網格線
        xAxis.setDrawGridLines(false);
        //xAxis.setAxisMinimum(0f);
        xAxis.setGranularity(1);
        //xAxis.setLabelCount(6, true);
        // 設定X軸的座標格式
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                Logger.d(value + "");
                int index = (int) value;
//                if (index == 0) {
//                    return week[index];
//                }
//                else {
//                    return "";
//                }
                if (index < 0 || index >= week.length) {
                    return "";
                } else {
                    if (weekStatus[index]) {
                        return week[index];
                    }
                    else {
                        return "";
                    }
                }
            }
        });
        // 取得左邊Y軸物件
        leftYAxis = lineChart.getAxisLeft();
        // 取得右邊Y軸物件
        rightYaxis = lineChart.getAxisRight();
        // 是否有右邊Y軸的網格線
        rightYaxis.setDrawGridLines(false);
        // 是否有左邊Y軸的網格線
        leftYAxis.setDrawGridLines(false);
        // 是否啟用右邊Y軸
        rightYaxis.setEnabled(false);

        /**
         *折線圖例 標籤 設定
         */
        // 取得圖例物件
        legend = lineChart.getLegend();
        // 設定圖例顯示型別，LINE CIRCLE SQUARE EMPTY 等等多種方式
        legend.setForm(Legend.LegendForm.LINE);
        // 設定圖例文字大小
        legend.setTextSize(12f);
        // 顯示圖利位置在左下方
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        // 是否繪製在圖表裡面
        legend.setDrawInside(false);
        setLineChartData();
        // 繪製圖表
        lineChart.invalidate();
    }

    private void setLineChartData() {
        List<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(getTelePressureLineDataSet());
        dataSets.add(getDiasPressureLineDataSet());
        LineData lineData = new LineData(dataSets);
        lineChart.setData(lineData);
        Logger.d(lineData.getDataSets().toString());
    }

    private LineDataSet getTelePressureLineDataSet() {
        List<Entry> values = new ArrayList<>();
        for (int i = 0; i < concerList.size(); i++) {
            Map<String, String> map = concerList.get(i);
            values.add(new Entry(Float.parseFloat(map.get("dateWeekNum")), Float.parseFloat(map.get("diastolic"))));
        }
//        values.add(new Entry(0, 115));
//        values.add(new Entry(1, 100));
//        values.add(new Entry(2, 100));
//        values.add(new Entry(3, 105));
//        values.add(new Entry(4, 115));
//        values.add(new Entry(5, 90));
//        values.add(new Entry(6, 95));

        LineDataSet lineDataSet = new LineDataSet(values, "收縮壓");
        // 設定線條的樣式
        lineDataSet.setMode(LineDataSet.Mode.LINEAR);
        // 設定線條的顏色
        lineDataSet.setColor(Color.RED);
        // 設定線條的寬度
        lineDataSet.setLineWidth(3f);
        // 顯示座標點的小圓點
        lineDataSet.setDrawCircles(true);
        // 設定小圓點的顏色
        lineDataSet.setCircleColor(Color.RED);
        // 設定小圓點的半徑
        lineDataSet.setCircleRadius(5f);
        // 設定小圓點為實心
        lineDataSet.setDrawCircleHole(false);
        // 設定座標點的值文字大小
        lineDataSet.setValueTextSize(10f);
        // 不顯示定位線
        lineDataSet.setHighlightEnabled(false);
        return lineDataSet;
    }

    private LineDataSet getDiasPressureLineDataSet() {
        List<Entry> values = new ArrayList<>();
        for (int i = 0; i < concerList.size(); i++) {
            Map<String, String> map = concerList.get(i);
            values.add(new Entry(Float.parseFloat(map.get("dateWeekNum")), Float.parseFloat(map.get("systolic"))));
        }
//        values.add(new Entry(0, 60));
//        values.add(new Entry(1, 50));
//        values.add(new Entry(2, 40));
//        values.add(new Entry(3, 50));
//        values.add(new Entry(4, 60));
//        values.add(new Entry(5, 70));
//        values.add(new Entry(6, 80));

        LineDataSet lineDataSet = new LineDataSet(values, "舒張壓");
        // 設定線條的樣式
        lineDataSet.setMode(LineDataSet.Mode.LINEAR);
        // 設定線條的顏色
        lineDataSet.setColor(Color.BLUE);
        // 設定線條的寬度
        lineDataSet.setLineWidth(3f);
        // 顯示座標點的小圓點
        lineDataSet.setDrawCircles(true);
        // 設定小圓點的顏色
        lineDataSet.setCircleColor(Color.BLUE);
        // 設定小圓點的半徑
        lineDataSet.setCircleRadius(5f);
        // 設定小圓點為實心
        lineDataSet.setDrawCircleHole(false);
        // 設定座標點的值文字大小
        lineDataSet.setValueTextSize(10f);
        // 不顯示定位線
        lineDataSet.setHighlightEnabled(false);
        return lineDataSet;
    }

    private void setShareListener() {
        btShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bitmap = getLineChartBitmap();
                Uri uri = Uri.parse(MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), bitmap, null,null));
                Intent shareIntent = new Intent();

                shareIntent.setAction(Intent.ACTION_SEND);
                // 設置分享内容的類型
                shareIntent.setType("image/*");
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                // 創建分享的Dialog
                shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "分享");
                startActivity(Intent.createChooser(shareIntent, "選擇分享APP"));
            }
        });
    }

    public Bitmap getLineChartBitmap() {
        lineChart.setDrawingCacheEnabled(true);
        // 啟用DrawingCache
        lineChart.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(lineChart.getDrawingCache());
        // 停止DrawingCache
        lineChart.setDrawingCacheEnabled(false);
        return bitmap;
    }

}
