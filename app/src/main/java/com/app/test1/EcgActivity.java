package com.app.test1;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.graphics.Color;
import android.view.View;

import com.app.test1.api.ApiClient;
import com.app.test1.api.ApiInterface;
import com.app.test1.model.Data;
import com.app.test1.model.ecg.Ecg;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EcgActivity extends AppCompatActivity {
    private ApiInterface apiInterface;
    double [] EcgData =new double[31];
    GraphView graph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecg);
        graph = ( GraphView ) findViewById(R.id.graph);
        getData();
    }
    private void getData() {
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<Ecg> call = apiInterface.getEcgData("UQBJ4DVO9AKDYYFX");
        call.enqueue(new Callback<Ecg>() {
            @Override
            public void onResponse(Call<Ecg> call, Response<Ecg> response) {
                if (response.code()==200) {
                    Ecg body=response.body();
                    for (int i=0;i<30;i++) {
                        EcgData[i]= Double.parseDouble(body.getFeeds().get(i).getField1());
                    }
                    showGraph();
                }
            }
            @Override
            public void onFailure(Call<Ecg> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
    private void showGraph(){
        //        Data for the graph
        double [] Data = EcgData;

        //form our data (curve) in X and Y
        LineGraphSeries < DataPoint > series = new LineGraphSeries < DataPoint >();
        for ( int i = 0 ; i < 30 ; i ++) {series.appendData ( new DataPoint ( i , Data [ i ]), true , 30 ); }
        //Set the appearance
        series.setColor ( Color . rgb ( 0 , 80 , 100 )); //set the color of the curve
        series.setTitle ( "Curve 1" ); // set the curve name for the legend
        series.setDrawDataPoints ( true ); // draw points
        series.setDataPointsRadius ( 5 ); // data point radius
        series.setThickness ( 2 ); //line thickness

        graph.addSeries ( series );

        //The name of the graph
        graph.setTitle ( "ECG" );
        graph.setTitleTextSize ( 50 );
        graph.setTitleColor ( Color . RED ); //Legend
        graph.getLegendRenderer (). setVisible ( true );
        graph.getLegendRenderer (). setAlign ( LegendRenderer . LegendAlign . TOP );

        //Increase/reduce and scroll // activate horizontal zooming and scrolling
        graph . getViewport (). setScalable ( true ); // activate horizontal scrolling
        graph . getViewport (). setScrollable ( true ); // activate horizontal and vertical zooming and scrolling
        graph . getViewport (). setScalableY ( true ); // activate vertical scrolling
        graph . getViewport (). setScrollableY ( true);

        //which part of the graph to display // set manual X bounds
        graph . getViewport (). setXAxisBoundsManual ( true );
        graph . getViewport (). setMinX ( 0 );
        graph . getViewport (). setMaxX ( 90 ); // set manual Y bounds
        graph . getViewport (). setYAxisBoundsManual ( true );
        graph . getViewport (). setMinY ( 0 );
        graph . getViewport (). setMaxY ( 200 );

        //Axis labels
        GridLabelRenderer gridLabel = graph . getGridLabelRenderer ();
        gridLabel . setHorizontalAxisTitle ( "POINT" );
        gridLabel . setVerticalAxisTitle ( "ECG" );
    }
}