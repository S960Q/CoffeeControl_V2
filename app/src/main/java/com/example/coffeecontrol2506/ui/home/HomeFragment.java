package com.example.coffeecontrol2506.ui.home;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.coffeecontrol2506.BluetoothHandler;
import com.example.coffeecontrol2506.R;
import com.example.coffeecontrol2506.databinding.FragmentHomeBinding;

import java.util.Timer;
import java.util.TimerTask;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private com.example.coffeecontrol2506.BluetoothHandler coffeeLeHandler;


    Timer upDateTimer = new Timer();

    private class upDateBar extends TimerTask {

        public void run() {

            if(binding.powerBar != null) binding.powerBar.setProgress((int)coffeeLeHandler.temp);
            if(binding.powerText != null) binding.powerText.setText( String.format("%2f", coffeeLeHandler.temp));
        }
    }

    private TimerTask upDateBar = new upDateBar();


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //final TextView textView = binding.textHome;
        //homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        coffeeLeHandler =  com.example.coffeecontrol2506.BluetoothHandler.getInstance(this.getContext());

        //upDateTimer.scheduleAtFixedRate(upDateBar, 0, 1000/2);
        if(coffeeLeHandler.CONNECTED) {
            binding.searchButton.setText("Connected");
            binding.searchButton.setBackgroundColor(Color.GREEN);
        }
        if(!coffeeLeHandler.CONNECTED) {
            binding.searchButton.setText("Disconnected");
            binding.searchButton.setBackgroundColor(Color.RED);
        }
        //homeBinding = homeBinding.inflate(getLayoutInflater());
        binding.searchButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {


            if(!coffeeLeHandler.CONNECTED) coffeeLeHandler.startScan();
            else if(coffeeLeHandler.CONNECTED) coffeeLeHandler.startScan();
            //scanLeDevice();
            //FirstButton.setEnabled(false);
        }
    });

        binding.goToValsButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //coffeeLeHandler.writeValue();
            Log.i("TAG", "onClick: Still Wokring*******************************");

        }
    });

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}