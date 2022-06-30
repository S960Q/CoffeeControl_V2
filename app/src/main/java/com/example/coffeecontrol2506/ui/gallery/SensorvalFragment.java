package com.example.coffeecontrol2506.ui.gallery;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.coffeecontrol2506.databinding.FragmentGalleryBinding;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Random;

public class SensorvalFragment extends Fragment {

    private FragmentGalleryBinding binding;
    private SeekBar piSeekBar;
    private com.example.coffeecontrol2506.BluetoothHandler coffeeLeHandler;
    private float valKp = 0;
    private float valKi = 0;
    private float valKd = 0;
    private float valTemp = 0;
    public float[] kpArea = {0f,1.5f};
    public float[] kiArea = {0.5f,3.5f};
    public float[] kdArea = {0.5f,1.5f};



    private float seekBarArea(float area[],int progress, SeekBar tmpBar)
    {
        return (area[0] + (float)progress/(float)tmpBar.getMax()*(area[1]-area[0]));
    }

    private void seekBarSET(float area[],double valToSet, SeekBar tmpBar)
    {

        double test = tmpBar.getMax()/((area[1]-area[0])/(valToSet-area[0]));
        tmpBar.setProgress((int)test,true);
        //return (area[0] + (float)progress/(float)tmpBar.getMax()*(area[1]-area[0]));
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // perform seek bar change listener event used for getting the progress value
        binding.seekBarKp.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
                valKp =  seekBarArea(kiArea,progress,seekBar);
                //valKp = (float)progress/100;

                binding.textPi.setText(String.format("Kp: %.2f", valKp));
            }
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        binding.seekBarKi.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
                valKi = seekBarArea(kiArea,progress,seekBar);
                binding.textKi.setText(String.format("Ki: %.2f", valKi));
            }
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        binding.seekBarKd.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
                valKd = seekBarArea(kdArea,progress,seekBar);
                binding.textKd.setText(String.format("Kd: %.2f", valKd));
            }
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        binding.seekBarTemp.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress; //=-1000
                valTemp =  seekBarArea(new float[] {85,98},progress,seekBar);
                //valTemp = progress;
                binding.textTemp.setText(String.format("Temp: %.2f°C", valTemp));
            }
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        binding.commitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //String tp = String.valueOf((int)valKp);
                coffeeLeHandler.writeValue(coffeeLeHandler.KP_UUID,(int)valKp*100);
                coffeeLeHandler.writeValue(coffeeLeHandler.TEMP_REF_UUID,(int)valTemp*100);
                Log.d("TAG", "onClick send to KP: " +String.valueOf((int)valKp));
                //coffeeLeHandler.writeValue(coffeeLeHandler.KI_UUID,String.format("%f", (int)valKp*100));
                //coffeeLeHandler.writeValue(coffeeLeHandler.KD_UUID,String.format("%f", (int)valKp*100));
                //coffeeLeHandler.writeValue(coffeeLeHandler.TEMP_REF_UUID,String.format("%f", (int)valKp*100));
            }
        });

    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        GalleryViewModel galleryViewModel =
                new ViewModelProvider(this).get(GalleryViewModel.class);
        coffeeLeHandler =  com.example.coffeecontrol2506.BluetoothHandler.getInstance(this.getContext());
        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        seekBarSET(kpArea,coffeeLeHandler.Kp,binding.seekBarKp);
        binding.textPi.setText(String.format("Kp: %.2f",coffeeLeHandler.Kp));

        //coffeeLeHandler.readControllerVals();



        //final TextView textView = binding.textGallery;
        //galleryViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}