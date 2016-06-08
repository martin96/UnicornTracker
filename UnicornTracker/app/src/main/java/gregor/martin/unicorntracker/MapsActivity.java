package gregor.martin.unicorntracker;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private LatLng currentPosition;
    private FloatingActionButton fab;
    private PolylineOptions rectOptions;

    private boolean mRequestingLocationUpdates = false;
    private boolean fabStatus = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fabStatus == false) {
                    if (ContextCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        final OvershootInterpolator interpolator = new OvershootInterpolator();
                        ViewCompat.animate(fab).rotation(135f).withLayer().setDuration(300).setInterpolator(interpolator).start();
                        mRequestingLocationUpdates = true;
                        startLocationUpdates();
                        fabStatus = true;
                    }
                } else {
                    final OvershootInterpolator interpolator = new OvershootInterpolator();
                    ViewCompat.animate(fab).rotation(0f).withLayer().setDuration(300).setInterpolator(interpolator).start();
                    mRequestingLocationUpdates = false;
                    stopLocationUpdates();

                    String string = "";
                    List<LatLng> list = rectOptions.getPoints();
                    for (int i = 0; i < list.size(); i++) {
                        string += list.get(i).latitude + ":" + list.get(i).longitude + "'";
                    }
                    Log.i("location", string);

                    File file;
                    int counter = 0;
                    while (true) {
                        file = new File(MapsActivity.this.getFilesDir(), String.valueOf(counter));
                        if (file.length() == 0) {
                            FileOutputStream stream = null;
                            try {
                                stream = new FileOutputStream(file);
                                stream.write(string.getBytes());
                                stream.close();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                        counter++;
                    }

                    fabStatus = false;
                    finish();
                }
            }
        });

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setScrollGesturesEnabled(false);
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if (mCurrentLocation != null) {
                currentPosition = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                rectOptions = new PolylineOptions();
                mMap.moveCamera(CameraUpdateFactory.zoomTo(18));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(currentPosition));
            }

            if (mRequestingLocationUpdates) {
                startLocationUpdates();
            }
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    100);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    protected void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, createLocationRequest(), this);
        }
    }

    protected LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(2000);
        mLocationRequest.setFastestInterval(500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
        if (fabStatus) {
            String string = "";
            List<LatLng> list = rectOptions.getPoints();
            for (int i = 0; i < list.size(); i++) {
                string += list.get(i).latitude + ":" + list.get(i).longitude + "'";
            }
            Log.i("location", string);

            File file;
            int counter = 0;
            while (true) {
                file = new File(MapsActivity.this.getFilesDir(), String.valueOf(counter));
                if (file.length() == 0) {
                    FileOutputStream stream = null;
                    try {
                        stream = new FileOutputStream(file);
                        stream.write(string.getBytes());
                        stream.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                counter++;
            }
            fabStatus = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        currentPosition = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        rectOptions.add(currentPosition);
        Polyline polyline = mMap.addPolyline(rectOptions);
        mMap.animateCamera(CameraUpdateFactory.newLatLng(currentPosition));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];

                if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED) {
                            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(
                                    mGoogleApiClient);
                            if (mCurrentLocation != null) {
                                currentPosition = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                                rectOptions = new PolylineOptions();
                                mMap.moveCamera(CameraUpdateFactory.zoomTo(18));
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(currentPosition));
                            }
                        }
                    } else {
                        ActivityCompat.requestPermissions(
                                this,
                                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                100);
                    }
                }
            }
        }
    }
}
