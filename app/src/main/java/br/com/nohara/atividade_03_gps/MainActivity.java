package br.com.nohara.atividade_03_gps;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {
    private ToggleButton btnGPSPermission;
    private ToggleButton btnGPSOnOff;
    private ToggleButton btnDistance;
    private Button btnReset;
    private TextView textViewparkour;
    private ImageView btnSearch;
    private EditText inputText;

    private Chronometer chronometer;
    private long pause;
    private boolean running;

    private double latitude;
    private double longitude;
    private double TotalDistance = 0;
    private Location currentLocation;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private static final int REQUEST_PERMISSION_COD_GPS = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputText = findViewById(R.id.inputTextId);

        btnSearch = findViewById(R.id.searchButtonId);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String location = inputText.getText().toString();
                Uri uri = Uri.parse(getString(R.string.uri_mapa, latitude, longitude)+location);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setPackage("com.google.android.apps.maps");
                startActivity(intent);
            }
        });

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();

                if(!btnDistance.isChecked())
                    return;

                if(currentLocation == null)
                    currentLocation = location;

                TotalDistance += currentLocation.distanceTo(location);

                currentLocation = location;
                textViewparkour.setText(String.format("%.2f m", TotalDistance));
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        //Permissão para utilizar a localização

        btnGPSPermission = findViewById(R.id.btnGPSPermission);
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            btnGPSPermission.setChecked(true);
        }else{
            btnGPSPermission.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    ActivityCompat.requestPermissions(
                            MainActivity.this, new String[]
                                    {Manifest.permission.ACCESS_FINE_LOCATION},1001);

                }
            });
        }


        //Permissão para uso do GPS

        btnGPSOnOff = findViewById(R.id.btnGPSPermission);
        btnGPSOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (ActivityCompat.checkSelfPermission(MainActivity.this,Manifest.permission.ACCESS_FINE_LOCATION) ==
                            PackageManager.PERMISSION_GRANTED){
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                1000,
                                10,
                                locationListener);
                        Toast.makeText(getApplicationContext(), getString(R.string.enableGPS), Toast.LENGTH_LONG ).show();
                        //else nao ta funcionando
                    }else {
                        Toast.makeText(MainActivity.this,getString(R.string.getPermission),Toast.LENGTH_LONG).show();
                        btnGPSOnOff.setChecked(false);
                    }
                }else {
                    locationManager.removeUpdates(locationListener);
                    Toast.makeText(getApplicationContext(), getString(R.string.disableGPS), Toast.LENGTH_LONG ).show();
                }

            }
        });

        textViewparkour = findViewById(R.id.distPercValueId);
        chronometer = findViewById(R.id.tempPassValueId);
        btnDistance = findViewById(R.id.statusPercursoId);
        btnDistance.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && btnGPSOnOff.isChecked() && !running){
                    Toast.makeText(getApplicationContext(), getString(R.string.start), Toast.LENGTH_LONG ).show();

                    chronometer.setBase(SystemClock.elapsedRealtime() - pause);
                    chronometer.start();
                    running = true;


                    TotalDistance=0;

                }else if (isChecked && !btnGPSOnOff.isChecked()){
                    Toast.makeText(getApplicationContext(),getString(R.string.enableYourGPS), Toast.LENGTH_LONG ).show();
                    btnDistance.setChecked(false);
                }
                else {
                    Toast.makeText(getApplicationContext(),getString(R.string.finish), Toast.LENGTH_LONG ).show();
                    chronometer.stop();
                    pause = SystemClock.elapsedRealtime() - chronometer.getBase();
                    running = false;
                }

            }
        });


        btnReset = findViewById(R.id.btnReset);
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!running){
                    chronometer.setBase(SystemClock.elapsedRealtime());
                    pause = 0;
                    textViewparkour.setText("");
                }else{
                    Toast.makeText(MainActivity.this,getString(R.string.progress),Toast.LENGTH_LONG).show();
                }
            }
        });

    }

}
