package gregor.martin.unicorntracker;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.db.chart.model.BarSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.BarChartView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public final static String FILE = "gregor.martin.unicorntracker.file";
    DrawerLayout drawer;
    FloatingActionButton fab;
    NavigationView navigationView;
    //AccessTokenTracker accessTokenTracker;
    ListView list;
    private ArrayList<String> myList;
    private ArrayList<String> myListForChart;
    private ArrayAdapter<String> adapter;
    private BarChartView chartView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(getApplication());

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken newAccessToken) {
                updateWithToken(newAccessToken);
            }
        };*/

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        myList = new ArrayList<String>();
        myListForChart = new ArrayList<String>();

        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, myList);

        list = (ListView) findViewById(R.id.list);
        list.setAdapter(adapter);

        AdapterView.OnItemClickListener mMessageClickedHandler = new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra(FILE, String.valueOf(position));
                startActivity(intent);
            }
        };

        list.setOnItemClickListener(mMessageClickedHandler);

        chartView = (BarChartView) findViewById(R.id.barchart);
    }

    private void getFiles() {
        myList.clear();
        myListForChart.clear();
        File file;
        int counter = 0;
        while (true) {
            file = new File(getFilesDir(), String.valueOf(counter));
            if (file.length() != 0) {
                int length = (int) file.length();

                byte[] bytes = new byte[length];

                FileInputStream in = null;
                try {
                    in = new FileInputStream(file);
                    in.read(bytes);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                PolylineOptions rectOptions = new PolylineOptions();
                String contents = new String(bytes);
                String[] points = contents.split("'");
                for (int i = 0; i < points.length; i++) {
                    String[] latLng = points[i].split(":");
                    LatLng currentPosition = new LatLng(Double.parseDouble(latLng[0]), Double.parseDouble(latLng[1]));
                    rectOptions.add(currentPosition);
                }
                myList.add(counter + " tracking - " + String.valueOf(Math.round(calculateMiles(rectOptions))) + " metres");
                myListForChart.add(String.valueOf(Math.round(calculateMiles(rectOptions))));
            } else {
                break;
            }
            counter++;
        }

        BarSet setOfData = new BarSet();
        for (int i = 0; i < myListForChart.size(); i++) {
            setOfData.addBar(String.valueOf(i), Float.valueOf(myListForChart.get(i)));
        }

        setOfData.setColor(getResources().getColor((R.color.colorAccent)));

        if(setOfData.size() != 0) {
            chartView.setYAxis(false);
            chartView.setXAxis(true);
            chartView.setYLabels(AxisController.LabelPosition.OUTSIDE);
            chartView.setXLabels(AxisController.LabelPosition.OUTSIDE);
            chartView.dismiss();
            chartView.addData(setOfData);
            chartView.setStep(5);
            chartView.show();
        }
    }

    /*private void updateWithToken(AccessToken currentAccessToken) {
        if (currentAccessToken != null) {
            GraphRequest request = GraphRequest.newMeRequest(
                    currentAccessToken,
                    new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject object, GraphResponse response) {
                            //text.setText(object.toString());
                        }
                    });
            request.executeAsync();
        } else {
            Intent i = new Intent(this, FacebookLogin.class);
            startActivity(i);
        }
    }*/

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_dashboard) {
            drawer.closeDrawer(GravityCompat.START);
            return true;
        } else if (id == R.id.nav_account) {
            /*Intent i = new Intent(this, FacebookLogin.class);
            startActivity(i);*/
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getFiles();
        list.setAdapter(adapter);
    }

    /*protected void onStart() {
        super.onStart();
        updateWithToken(AccessToken.getCurrentAccessToken());
    }*/

    protected void onStop() {
        super.onStop();
    }

    protected float calculateMiles(PolylineOptions points) {
        float totalDistance = 0;

        for (int i = 1; i < points.getPoints().size(); i++) {
            Location currLocation = new Location("this");
            currLocation.setLatitude(points.getPoints().get(i).latitude);
            currLocation.setLongitude(points.getPoints().get(i).longitude);

            Location lastLocation = new Location("this");
            lastLocation.setLatitude(points.getPoints().get(i - 1).latitude);
            lastLocation.setLongitude(points.getPoints().get(i - 1).longitude);

            totalDistance += lastLocation.distanceTo(currLocation);
        }

        return totalDistance;
    }
}
