package com.example.coffeecontrol2506.ui.slideshow;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.coffeecontrol2506.databinding.FragmentSlideshowBinding;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Random;

public class GraphFragment extends Fragment {

    private FragmentSlideshowBinding binding;
    private final Handler mHandler = new Handler();
    private Runnable mTimer2;
    public LineGraphSeries<DataPoint> mSeries2 = new LineGraphSeries<>();
    private com.example.coffeecontrol2506.BluetoothHandler coffeeLeHandler;


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.graph.addSeries(coffeeLeHandler.powerValues2);
        binding.graph.getViewport().setXAxisBoundsManual(true);
        binding.graph.getViewport().setMinX(0);
        binding.graph.getViewport().setMaxX(500);

        binding.graph2.addSeries(coffeeLeHandler.tempValues);
        binding.graph2.getViewport().setXAxisBoundsManual(true);
        binding.graph2.getViewport().setMinX(0);
        binding.graph2.getViewport().setMaxX(500);

        }
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        GraphViewModel slideshowViewModel =
                new ViewModelProvider(this).get(GraphViewModel.class);
        coffeeLeHandler =  com.example.coffeecontrol2506.BluetoothHandler.getInstance(this.getContext());
        coffeeLeHandler.resetNotify();
        Log.i("TAG", new String(String.valueOf(coffeeLeHandler.CONNECTED)));
        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        //mSeries2.appendData(new DataPoint(1d, 1d), true, 40);





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