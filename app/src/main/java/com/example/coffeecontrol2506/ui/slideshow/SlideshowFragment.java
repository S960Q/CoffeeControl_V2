package com.example.coffeecontrol2506.ui.slideshow;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.coffeecontrol2506.databinding.FragmentSlideshowBinding;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Random;

public class SlideshowFragment extends Fragment {

    private FragmentSlideshowBinding binding;
    private final Handler mHandler = new Handler();
    private Runnable mTimer1;
    private Runnable mTimer2;
    private LineGraphSeries<DataPoint> mSeries1;
    public LineGraphSeries<DataPoint> mSeries2;
    private com.example.coffeecontrol2506.BluetoothHandler coffeeLeHandler;
    public double graph2LastXValue = 5d;

    public void onResume() {
        super.onResume();


        mTimer2 = new Runnable() {
            @Override
            public void run() {
                graph2LastXValue += 1d;
                //mSeries2.appendData(new DataPoint(graph2LastXValue, getRandom()), true, 70);
                Object[] tmpValues = coffeeLeHandler.tmpValuesDataPoint.toArray();

                int len = coffeeLeHandler.tmpValuesDataPoint.size();
                DataPoint[] values = new DataPoint[len];
                for (int i=0; i<len; i++) {

                    DataPoint v = coffeeLeHandler.tmpValuesDataPoint.get(i);
                    values[i] = v;
                }
                //return values;
                mSeries2.resetData(values);
                mHandler.postDelayed(this, 200);
            }
        };
        mHandler.postDelayed(mTimer2, 1000);

    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SlideshowViewModel slideshowViewModel =
                new ViewModelProvider(this).get(SlideshowViewModel.class);
        coffeeLeHandler =  com.example.coffeecontrol2506.BluetoothHandler.getInstance(this.getContext());
        coffeeLeHandler.resetNotify();
        Log.i("TAG", new String(String.valueOf(coffeeLeHandler.CONNECTED)));
        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        mSeries2.appendData(new DataPoint(1d, 1d), true, 40);



        binding.graph2.addSeries(mSeries2);
        binding.graph2.getViewport().setXAxisBoundsManual(true);
        binding.graph2.getViewport().setMinX(0);
        binding.graph2.getViewport().setMaxX(70);

        return root;
    }


    private DataPoint[] generateData() {
        int count = 30;
        DataPoint[] values = new DataPoint[count];
        for (int i=0; i<count; i++) {
            double x = i;
            double f = mRand.nextDouble()*0.15+0.3;
            double y = Math.sin(i*f+2) + mRand.nextDouble()*0.3;
            DataPoint v = new DataPoint(x, y);
            values[i] = v;
        }
        return values;
    }

    double mLastRandom = 2;
    Random mRand = new Random();
    private double getRandom() {
        return mLastRandom += mRand.nextDouble()*0.5 - 0.25;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}