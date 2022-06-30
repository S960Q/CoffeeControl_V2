package com.example.coffeecontrol2506;

import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_WRITE;
import static android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT;
import static com.welie.blessed.BluetoothBytesParser.FORMAT_FLOAT;
import static com.welie.blessed.BluetoothBytesParser.FORMAT_SINT16;
import static com.welie.blessed.BluetoothBytesParser.FORMAT_UINT16;
import static com.welie.blessed.BluetoothBytesParser.FORMAT_UINT8;
import static com.welie.blessed.BluetoothBytesParser.bytes2String;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.nfc.Tag;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.coffeecontrol2506.databinding.ActivityMainBinding;
import com.example.coffeecontrol2506.databinding.FragmentGalleryBinding;
import com.example.coffeecontrol2506.databinding.FragmentHomeBinding;
import com.example.coffeecontrol2506.ui.slideshow.SlideshowFragment;
import com.google.android.gms.common.util.ArrayUtils;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.welie.blessed.BluetoothBytesParser;
import com.welie.blessed.BluetoothCentralManager;
import com.welie.blessed.BluetoothCentralManagerCallback;
import com.welie.blessed.BluetoothPeripheral;
import com.welie.blessed.BluetoothPeripheralCallback;
import com.welie.blessed.BondState;
import com.welie.blessed.ConnectionPriority;
import com.welie.blessed.GattStatus;
import com.welie.blessed.HciStatus;
import com.welie.blessed.ScanFailure;
import com.welie.blessed.WriteType;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;

import kotlin.reflect.KClassesImplKt;

//import timber.log.Timber;

public class BluetoothHandler {


    private static final String TAG = "BLE";

    // Local variables
    public BluetoothCentralManager central;
    private FragmentHomeBinding homeBinding;
    private FragmentGalleryBinding galleryBinding;
    private static BluetoothHandler instance = null;
    private final Context context;
    private final Handler handler = new Handler();
    private int currentTimeCounter = 0;
    public double temp = 0.0;
    public double tempRef = 92.0;
    public double power = 0.0;
    public double Kp = 0.0;
    public double Ki = 0.0;
    public double Kd = 0.0;
    public boolean CONNECTED = false;
    private Button connectButton;
    public BluetoothPeripheral connectedPeripheral;
    private SlideshowFragment graphView;
    public LineGraphSeries<DataPoint> tempValues;
    public ArrayList<DataPoint> tmpValuesDataPoint = new ArrayList<DataPoint>(); // Create an ArrayList object
    public double graph2LastXValue = 5d;

    //private static final UUID COFFEE_SERVICE_UUID = UUID.fromString("25AE1441-05D3-4C5B-8281-93D4E07420CF");
    private static final UUID COFFEE_SERVICE_UUID = UUID.fromString("34881f02-1de5-ca8a-9043-0113c0d05807");
    //public static final UUID TEMP_UUID = UUID.fromString("25AE1443-05D3-4C5B-8281-93D4E07420CF");
    //public static final UUID POWER_UUID = UUID.fromString("25AE1442-05D3-4C5B-8281-93D4E07420CF");
    //public static final UUID POWER_UUID = UUID.fromString("f474541f-83be-4d6d-9358-84dd0882603d");
    public static final UUID POWER_UUID = UUID.fromString("3d608208-dd84-5893-6d4d-be831f5474f4");
    public static final UUID TEMP_UUID = UUID.fromString("783c510c-de26-2ea6-f748-215e7c2a1e7e");
    public static final UUID TEMP_REF_UUID = UUID.fromString("3f3dd517-e122-5a93-bd4d-f9b62d64c712");
    public static final UUID KP_UUID = UUID.fromString("2fa95ed2-6b0c-ffbd-b54d-a437d773bc4b");
    public static final UUID KI_UUID = UUID.fromString("7bd54ab2-f38f-8588-8140-d712b36842b0");
    public static final UUID KD_UUID = UUID.fromString("5e620d55-b8f3-8ab6-f44b-aefa801cd85b");

    // Callback for peripherals
    private final BluetoothPeripheralCallback peripheralCallback = new BluetoothPeripheralCallback() {
        @Override
        public void onServicesDiscovered(@NotNull BluetoothPeripheral peripheral) {
            // Request a higher MTU, iOS always asks for 185
            Log.d(TAG, "onServicesDiscovered: serviceDiscoverd********************");
            peripheral.requestMtu(185);

            // Request a new connection priority
            peripheral.requestConnectionPriority(ConnectionPriority.HIGH);

            peripheral.setNotify(COFFEE_SERVICE_UUID, POWER_UUID, true);
            peripheral.setNotify(COFFEE_SERVICE_UUID, TEMP_UUID, true);
            readControllerVals();



        }


        @Override
        public void onNotificationStateUpdate(@NotNull BluetoothPeripheral peripheral, @NotNull BluetoothGattCharacteristic characteristic, @NotNull GattStatus status) {
            if (status == GattStatus.SUCCESS) {
                final boolean isNotifying = peripheral.isNotifying(characteristic);
                Log.i(TAG,String.format("SUCCESS: Notify set to '%s' for %s", isNotifying, characteristic.getUuid()));
                //Log.i("BLE","SUCCESS: Notify set to '%s' for %s", isNotifying, characteristic.getUuid())
            } else {
                Log.e(TAG,String.format("ERROR: Changing notification state failed for %s (%s)", characteristic.getUuid(), status));
            }
        }

        @Override
        public void onCharacteristicWrite(@NotNull BluetoothPeripheral peripheral, @NotNull byte[] value, @NotNull BluetoothGattCharacteristic characteristic, @NotNull GattStatus status) {
            if (status == GattStatus.SUCCESS) {
                Log.i("BÖE",String.format("SUCCESS: Writing <%s> to <%s>", bytes2String(value), characteristic.getUuid()));
            } else {
                Log.i("BÖE",String.format("ERROR: Failed writing <%s> to <%s> (%s)", bytes2String(value), characteristic.getUuid(), status));
                //Timber.i("ERROR: Failed writing <%s> to <%s> (%s)", bytes2String(value), characteristic.getUuid(), status);
            }
        }

        @Override
        public void onCharacteristicUpdate(@NotNull BluetoothPeripheral peripheral, @NotNull byte[] value, @NotNull BluetoothGattCharacteristic characteristic, @NotNull GattStatus status) {
            //Log.i(TAG, "onCharacteristicUpdate: starting");
            if (status != GattStatus.SUCCESS) return;

            UUID characteristicUUID = characteristic.getUuid();
            ProgressBar bar = mainActivity.findViewById(R.id.tempBar);


                if (characteristicUUID.equals(TEMP_UUID)) {
                    byte[] charVal = characteristic.getValue();
                    BluetoothBytesParser parser = new BluetoothBytesParser(value);


                    int i = parser.getIntValue(FORMAT_UINT16);
                    temp = (float)i;


                    if(bar != null && homeBinding != null) {
                        bar = mainActivity.findViewById(R.id.tempBar);
                        TextView text = mainActivity.findViewById(R.id.tempText);

                        bar.setProgress((int) temp);
                        text.setText(String.format("%.2f °C", temp));
                        if(Math.abs(tempRef-temp) > 2) {
                            homeBinding.statusButton.setBackgroundColor(Color.GREEN);
                            homeBinding.statusButton.setText("Ready");
                        }
                        else {
                            homeBinding.statusButton.setBackgroundColor(Color.RED);
                            homeBinding.statusButton.setText("heating");
                        }
                    }
                    Log.i(TAG, "onCharacteristicUpdate: new Temp" + temp);


                }
                else if (characteristicUUID.equals(POWER_UUID)) {
                    byte[] buffer = characteristic.getValue();
                    BluetoothBytesParser parser = new BluetoothBytesParser(value);

                    int i = parser.getIntValue(FORMAT_UINT16);
                    power = (float)i;
                    //temp = bufferBytes.getInt();


                    Log.i(TAG, "onCharacteristicUpdate: " + String.valueOf(power) + "%%");


                    ProgressBar progress = mainActivity.findViewById(R.id.powerBar);
                    TextView progressText = mainActivity.findViewById(R.id.powerText);

                    if(bar != null) {
                    progress.setProgress((int) power);
                    progressText.setText(String.format("%.2f%%", power));
                    Log.i(TAG, "onCharacteristicUpdate: new power" + power);
                    //graph2LastXValue += 1d;
                    //tempValues.appendData(new DataPoint(graph2LastXValue, temp), true, 70);
                    //tmpValuesDataPoint.add(new DataPoint(graph2LastXValue, temp));
                    }

                }
                else if (characteristicUUID.equals(KP_UUID)) {
                    byte[] buffer = characteristic.getValue();
                    BluetoothBytesParser parser = new BluetoothBytesParser(value);

                    int i = parser.getIntValue(FORMAT_UINT16);
                    Kp = (double)i/100;
                    Log.i(TAG, "onCharacteristicUpdate: New Kp" + String.valueOf(Kp));

                }
                else if (characteristicUUID.equals(KI_UUID)) {
                    byte[] buffer = characteristic.getValue();
                    BluetoothBytesParser parser = new BluetoothBytesParser(value);

                    int i = parser.getIntValue(FORMAT_UINT16);
                    Ki = (double)i/100;
                    Log.i(TAG, "onCharacteristicUpdate: New Ki" + String.valueOf(Ki));
                }
                else if (characteristicUUID.equals(KD_UUID)) {
                    byte[] buffer = characteristic.getValue();
                    BluetoothBytesParser parser = new BluetoothBytesParser(value);
                    int i = parser.getIntValue(FORMAT_UINT16);
                    Kd = (double)i/100;
                    Log.i(TAG, "onCharacteristicUpdate: New Kd" + String.valueOf(Kd));
                }

                else if (characteristicUUID.equals(TEMP_REF_UUID)) {
                    byte[] buffer = characteristic.getValue();
                    BluetoothBytesParser parser = new BluetoothBytesParser(value);
                    int i = parser.getIntValue(FORMAT_UINT16);
                    tempRef = (double)i/100;
                    Log.i(TAG, "onCharacteristicUpdate: New tempRef" + String.valueOf(tempRef));
                }



        }

        @Override
        public void onMtuChanged(@NotNull BluetoothPeripheral peripheral, int mtu, @NotNull GattStatus status) {
            //Timber.i("new MTU set: %d", mtu);
        }

        /*private void sendMeasurement(@NotNull Intent intent, @NotNull BluetoothPeripheral peripheral ) {
            intent.putExtra(MEASUREMENT_EXTRA_PERIPHERAL, peripheral.getAddress());
            context.sendBroadcast(intent);
        }*/

    };

    // Callback for central
    private final BluetoothCentralManagerCallback bluetoothCentralManagerCallback = new BluetoothCentralManagerCallback() {

        @Override
        public void onConnectedPeripheral(@NotNull BluetoothPeripheral peripheral) {
            //Timber.i("connected to '%s'", peripheral.getName());
            Log.i(TAG, "onConnectedPeripheral: connected to: " + peripheral.getName());
            connectedPeripheral = peripheral;
            CONNECTED = true;

            connectButton = mainActivity.findViewById(R.id.searchButton);
            if(connectButton != null) {
                connectButton.setBackgroundColor(Color.GREEN);
                connectButton.setText("Connected");
            }
        }

        @Override
        public void onConnectionFailed(@NotNull BluetoothPeripheral peripheral, final @NotNull HciStatus status) {
            Log.e(TAG,String.format("connection '%s' failed with status %s", peripheral.getName(), status));
            CONNECTED = false;
            connectButton = mainActivity.findViewById(R.id.searchButton);
            if(connectButton != null) {
                connectButton.setBackgroundColor(Color.RED);
                connectButton.setText("Disconnected");
            }
        }

        @Override
        public void onDisconnectedPeripheral(@NotNull final BluetoothPeripheral peripheral, final @NotNull HciStatus status) {
            Log.i(TAG,String.format("disconnected '%s' with status %s", peripheral.getName(), status));
            CONNECTED = false;
            connectButton = mainActivity.findViewById(R.id.searchButton);
            if(connectButton != null) {
                connectButton.setBackgroundColor(Color.RED);
            }
            // Reconnect to this device when it becomes available again
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    central.autoConnectPeripheral(peripheral, peripheralCallback);
                }
            }, 5000);
        }

        @Override
        public void onDiscoveredPeripheral(@NotNull BluetoothPeripheral peripheral, @NotNull ScanResult scanResult) {
            //Timber.i("Found peripheral '%s'", peripheral.getName());
            //Log.i("BLE","Found Device!");
            Log.d("BLE",peripheral.getName());
            //

            if (peripheral.getName().contains("Contour") && peripheral.getBondState() == BondState.NONE) {
                // Create a bond immediately to avoid double pairing popups
                central.createBond(peripheral, peripheralCallback);
            }
            if(peripheral.getName().contains("CoffeeBam"))
            {

                central.stopScan();
                Log.d("BLE",peripheral.getName());
                Log.d("BLE",peripheral.getAddress());
                central.connectPeripheral(peripheral, peripheralCallback);
            }
            else {
                //central.connectPeripheral(peripheral, peripheralCallback);
                Log.i("BLE", "onDiscoveredPeripheral: No Device found");
                //Toast.makeText(mainActivity, "No Device Found", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onBluetoothAdapterStateChanged(int state) {
            //Timber.i("bluetooth adapter changed state to %d", state);
            if (state == BluetoothAdapter.STATE_ON) {
                // Bluetooth is on now, start scanning again
                // Scan for peripherals with a certain service UUIDs
                central.startPairingPopupHack();
                startScan();
            }
        }

        @Override
        public void onScanFailed(@NotNull ScanFailure scanFailure) {
            //Timber.i("scanning failed with error %s", scanFailure);
            Log.i(TAG, "onScanFailed: ScanFailed");
        }
    };

    public void readTemp()
    {
        connectedPeripheral.readCharacteristic(COFFEE_SERVICE_UUID, POWER_UUID);
    }

    public void readControllerVals()
    {
        Log.i(TAG, "readControllerVals: Reading KI");
        boolean sucess = connectedPeripheral.readCharacteristic(COFFEE_SERVICE_UUID, KI_UUID);
        Log.i(TAG, "readControllerVals: Reading KP");
        connectedPeripheral.readCharacteristic(COFFEE_SERVICE_UUID, KP_UUID);
        Log.i(TAG, "readControllerVals: Reading KD");
        connectedPeripheral.readCharacteristic(COFFEE_SERVICE_UUID, KD_UUID);
        connectedPeripheral.readCharacteristic(COFFEE_SERVICE_UUID, TEMP_REF_UUID);
        Log.i(TAG, "readControllerVals:" +String.valueOf(sucess));
    }



    private static byte[] intToBytes(final int data) {
        return new byte[] {
                //(byte)((data >> 24) & 0xff),
                //(byte)((data >> 16) & 0xff),
                (byte)((data >> 0) & 0xff),
                (byte)((data >> 8) & 0xff),
        };
    }

    public void writeValue(UUID characteristicUUID, int outData)
    {
        if(connectedPeripheral != null) connectedPeripheral.writeCharacteristic(COFFEE_SERVICE_UUID, characteristicUUID, intToBytes(outData),WriteType.WITH_RESPONSE);
        else{
            Log.e(TAG, "writeValue: could not write, nothing connected" );
        }
    }

    public void resetNotify()
    {
        connectedPeripheral.setNotify(COFFEE_SERVICE_UUID, POWER_UUID, true);
    }

    public static synchronized BluetoothHandler getInstance(Context context) {
        if (instance == null) {
            instance = new BluetoothHandler(context.getApplicationContext());
        }
        return instance;
    }

    private Activity mainActivity;
    public void setActivity(Activity _mainActiv)
    {
        mainActivity = _mainActiv;
    }
    private BluetoothHandler(Context context) {
        this.context = context;

        // Plant a tree
        //Timber.plant(new Timber.DebugTree());

        // Create BluetoothCentral
        central = new BluetoothCentralManager(context, bluetoothCentralManagerCallback, new Handler());

        // Scan for peripherals with a certain service UUIDs
        central.startPairingPopupHack();
        startScan();
    }

    public void startScan() {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //mainBinding binding = mainBinding.bind(rootView);
        //mainBinding = mainBinding.inflate(layoutInflater, viewGroup, false);
        tempValues = new LineGraphSeries<>();
        handler.postDelayed(new Runnable() {


            @Override
            public void run() {
                //central.scanForPeripheralsWithServices(new UUID[]{BLP_SERVICE_UUID, HTS_SERVICE_UUID, HRS_SERVICE_UUID, PLX_SERVICE_UUID, WSS_SERVICE_UUID, GLUCOSE_SERVICE_UUID});
                central.scanForPeripherals();
            }
        },3000);
    }

}
