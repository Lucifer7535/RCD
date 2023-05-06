package com.gvvp.roadcrackdetector;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class StatsActivity extends AppCompatActivity {

    private BarChart barChart;
    private ArrayList<BarEntry> barArraylist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        barChart = findViewById(R.id.stats_barchart);
        getData();
        BarDataSet barDataSet = new BarDataSet(barArraylist, "Crack Types");
        Description description = new Description();
        description.setText("My Chart Description");
        description.setPosition(999f, 999f);
        barChart.setDescription(description);
        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);
        XAxis xAxis = barChart.getXAxis();
        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        barDataSet.setValueTextColor(Color.BLACK);
        barDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getBarLabel(BarEntry barEntry) {
                String[] labels = {"Linear Crack", "Longitudinal Crack", "Alligator Crack", "Pothole"};
                return labels[(int)barEntry.getX()];
            }
        });
        barDataSet.setValueTextSize(7f);
        barChart.getDescription().setEnabled(true);
        YAxis yAxis = barChart.getAxisLeft();
        yAxis.setAxisMinimum(0f);
        yAxis.setAxisMaximum(100f);
        yAxis.setGranularity(10f);
    }

    private void getData() {
        barArraylist = new ArrayList();
        AtomicInteger linearCount = new AtomicInteger();
        AtomicInteger longitudinalCount = new AtomicInteger();
        AtomicInteger alligatorCount = new AtomicInteger();
        AtomicInteger potholeCount = new AtomicInteger();


        BarEntry linearEntry = new BarEntry(0f, 0);
        BarEntry longitudinalEntry = new BarEntry(1f, 0);
        BarEntry alligatorEntry = new BarEntry(2f, 0);
        BarEntry potholeEntry = new BarEntry(3f, 0);

        getCrackCountByType("Linear Crack", linearCount, linearEntry);
        getCrackCountByType("Longitudnal Crack", longitudinalCount, longitudinalEntry);
        getCrackCountByType("Alligator Crack", alligatorCount, alligatorEntry);
        getCrackCountByType("Pothole", potholeCount, potholeEntry);

        barArraylist.add(linearEntry);
        barArraylist.add(longitudinalEntry);
        barArraylist.add(alligatorEntry);
        barArraylist.add(potholeEntry);
    }


    private void getCrackCountByType(String crackType, AtomicInteger count, BarEntry entry) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        final String uid = currentUser.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference cracksRef = db.collection("Users").document(uid).collection("locations");

        cracksRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String label = document.getString("Label");

                    if (label.equalsIgnoreCase(crackType)) {
                        count.getAndIncrement();
                    }
                }
                // Update the bar entry with the count value
                entry.setY(count.get());
                barChart.invalidate();
            } else {
                Log.d(TAG, "Error getting documents: ", task.getException());
            }
        });
    }
}


//
//import static android.content.ContentValues.TAG;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import android.graphics.Color;
//import android.os.Bundle;
//import android.util.Log;
//
//import com.github.mikephil.charting.charts.BarChart;
//import com.github.mikephil.charting.components.XAxis;
//import com.github.mikephil.charting.data.BarData;
//import com.github.mikephil.charting.data.BarDataSet;
//import com.github.mikephil.charting.data.BarEntry;
//import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
//import com.github.mikephil.charting.utils.ColorTemplate;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.firestore.CollectionReference;
//import com.google.firebase.firestore.FirebaseFirestore;
//import com.google.firebase.firestore.QueryDocumentSnapshot;
//
//import java.util.ArrayList;
//import java.util.concurrent.atomic.AtomicInteger;
//
//public class StatsActivity extends AppCompatActivity {
//
//    private BarChart barChart;
//    private ArrayList barArrayList;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_stats);
//
//        barChart = findViewById(R.id.stats_barchart);
//        getData();
//        BarDataSet barDataSet = new BarDataSet(barArrayList, "Crack Types");
//        BarData barData = new BarData(barDataSet);
//        barChart.setData(barData);
//        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
//        barDataSet.setValueTextColor(Color.BLACK);
//        barDataSet.setValueTextSize(16f);
//        XAxis xAxis = barChart.getXAxis();
//        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"D00", "D10", "D20", "D40"}));
//        barChart.invalidate();
//    }
//    private void getData(){
//        barArrayList = new ArrayList();
//        barArrayList.add(new BarEntry(2f, getCrackCountByType("Linear Crack")));
//        barArrayList.add(new BarEntry(3f, getCrackCountByType("Longitudinal Crack")));
//        barArrayList.add(new BarEntry(4f, getCrackCountByType("Alligator Crack")));
//        barArrayList.add(new BarEntry(5f, getCrackCountByType("Pothole")));
//    }
//    private int getCrackCountByType(String crackType) {
//        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
//        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
//        final String uid = currentUser.getUid();
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        CollectionReference cracksRef = db.collection("Users").document(uid).collection("locations");
//
//        AtomicInteger count = new AtomicInteger();
//
//        cracksRef.get().addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                for (QueryDocumentSnapshot document : task.getResult()) {
//                    String label = document.getString("Label");
//
//                    if (label.equalsIgnoreCase(crackType)) {
//                        count.getAndIncrement();
//                    }
//                }
//            } else {
//                Log.d(TAG, "Error getting documents: ", task.getException());
//            }
//        });
//        return count.get();
//    }
//}