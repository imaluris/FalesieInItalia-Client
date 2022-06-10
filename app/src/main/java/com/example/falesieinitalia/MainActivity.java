package com.example.falesieinitalia;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.Permission;
import java.util.ArrayList;
import java.util.List;
import java.lang.Object;

import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textOffset;
import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener, MapboxMap.OnMapClickListener {

    //inizializzazione variabili
    DrawerLayout drawerLayout;

    private static final String CALLOUT_LAYER_ID = "mapbox.poi.callout";

    private static final String ICON_ID = "ICON_ID";

    private static final String LAYER_ID = "LAYER_ID";

    private static final String SOURCE_ID = "SOURCE_ID";

    private static final String TAG = "falesia";

    public RequestQueue mQueue;

    private MapView mapView;

    private MapboxMap mapboxMap;

    private PermissionsManager permissionsManager;

    private String url = "https://boiling-gorge-96661.herokuapp.com/Falesia";

    public Boolean showCrags = true;

    public Boolean showGyms = true;

    private Toolbar mytoolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_main);

        //assegnamento variabile
        drawerLayout = findViewById(R.id.drawer_layout);

        mytoolbar = (Toolbar) findViewById(R.id.mytoolbar);
        setSupportActionBar(mytoolbar);
        getSupportActionBar().setTitle(null);
        mytoolbar.setOverflowIcon(ContextCompat.getDrawable(this, R.drawable.more1));

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            showCrags = extras.getBoolean("mostraFalesie");
            showGyms = extras.getBoolean("mostraPalestre");
        }

        checkPermission(Manifest.permission.ACCESS_FINE_LOCATION,1);
        checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION,2);
        gpsCheck();


    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        MainActivity.this.mapboxMap = mapboxMap;
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    List<Feature> symbolLayerIconFeatureList = new ArrayList<>();
                    
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject crag = jsonArray.getJSONObject(i);
                        String nome = crag.getString("nome");
                        String tipo = crag.getString("tipo");
                        double lng = crag.getDouble("longitudine");
                        double lat = crag.getDouble("latitudine");
                        String descrizione = crag.getString("descrizione");
                        String immagine = crag.getString("immagine");

                        symbolLayerIconFeatureList.add(Feature.fromGeometry(Point.fromLngLat(lng, lat)));
                        symbolLayerIconFeatureList.get(i).addStringProperty("NAME_PROPERTY_KEY", nome);
                        symbolLayerIconFeatureList.get(i).addStringProperty("TYPE_KEY", tipo);
                        symbolLayerIconFeatureList.get(i).addStringProperty("DESCRIPTION_KEY", descrizione);
                        symbolLayerIconFeatureList.get(i).addStringProperty("IMAGE_KEY", immagine);
                    }
                    mapboxMap.setStyle(new Style.Builder().fromUri(Style.MAPBOX_STREETS)

                            .withSource(new GeoJsonSource(SOURCE_ID, FeatureCollection.fromFeatures(symbolLayerIconFeatureList)))

                            .withLayer(new SymbolLayer(LAYER_ID, SOURCE_ID)
                                    .withProperties(
                                            iconImage(get("TYPE_KEY")),
                                            iconSize((float) 0.3),
                                            iconAllowOverlap(false),
                                            iconIgnorePlacement(false)
                                    )
                            ), new Style.OnStyleLoaded() {
                        @Override
                        public void onStyleLoaded(@NonNull Style style) {

                            enableLocationComponent(style);
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        mQueue = Volley.newRequestQueue(this);
        mQueue.add(request);

        mapboxMap.addOnMapClickListener(MainActivity.this);
        mapboxMap.getUiSettings().setLogoEnabled(true);
        mapboxMap.getUiSettings().setAttributionEnabled(false);


        mapView.addOnStyleImageMissingListener(new MapView.OnStyleImageMissingListener() {

            @Override
            public void onStyleImageMissing(@NonNull String id) {
                if (id.equals("Falesia") && showCrags) {
                    addImage(id, R.drawable.map_marker_falesia);
                }
                if (id.equals("Palestra") && showGyms) {
                    addImage(id, R.drawable.map_marker_gym);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_options_menu, menu);
        menu.getItem(0).setChecked(true);
        menu.getItem(1).setChecked(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.crag:
                if (item.isChecked()) {
                    // If item already checked then unchecked it
                    item.setChecked(false);
                    showCrags = false;
                } else {
                    // If item is unchecked then checked it
                    item.setChecked(true);
                    showCrags = true;
                }

                mapView.getMapAsync(this);

                return true;
            case R.id.gym:
                if (item.isChecked()) {
                    // If item already checked then unchecked it
                    item.setChecked(false);
                    showGyms = false;
                } else {
                    // If item is unchecked then checked it
                    item.setChecked(true);
                    showGyms = true;
                }

                mapView.getMapAsync(this);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean onMapClick(LatLng paramLatLng) {
        PointF pointF = this.mapboxMap.getProjection().toScreenLocation(paramLatLng);
        List<Feature> list = this.mapboxMap.queryRenderedFeatures(pointF, new String[]{"LAYER_ID"});
        if (!list.isEmpty()) {
            Feature feature = list.get(0);
            String str1 = feature.getStringProperty("NAME_PROPERTY_KEY");
            String str2 = feature.getStringProperty("DESCRIPTION_KEY");
            String str3 = feature.getStringProperty("IMAGE_KEY");
            Intent intent = new Intent((Context) this, DescrizioneFalesia.class);
            intent.putExtra("nome", str1);
            intent.putExtra("descrizione", str2);
            intent.putExtra("immagine", str3);
            intent.putExtra("mostraFalesie", showCrags);
            intent.putExtra("mostraPalestre", showGyms);

            startActivity(intent);
            //Toast.makeText((Context) this, str1, Toast.LENGTH_LONG).show();
            return true;
        }
        //Toast.makeText((Context) this, "Nada de nada", Toast.LENGTH_LONG).show();
        return true;
    }

    private void addImage(String paramString, int paramInt) {
        Style style = this.mapboxMap.getStyle();
        if (style != null)
            style.addImageAsync(paramString, BitmapUtils.getBitmapFromDrawable(getDrawable(paramInt)));
    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {

            LocationComponentOptions customLocationComponentOptions = LocationComponentOptions.builder(this)
                    .pulseEnabled(true)
                    .build();

            LocationComponent locationComponent = mapboxMap.getLocationComponent();

            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(this, loadedMapStyle)
                            .locationComponentOptions(customLocationComponentOptions)
                            .build());

            locationComponent.setLocationComponentEnabled(true);

            locationComponent.setCameraMode(CameraMode.TRACKING);

            locationComponent.setRenderMode(RenderMode.NORMAL);
    }

    // Function to check and request permission.
    public void checkPermission(String permission, int request)
    {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED ) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] { permission, Manifest.permission.ACCESS_COARSE_LOCATION}, request);
        } else {
            //Toast.makeText(this, "permessi gia'attivi", Toast.LENGTH_LONG).show();
        }

    }

    public void gpsCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            buildAlertMessageNoGps();

        }  else {

            //Toast.makeText(this, "gps attivo", Toast.LENGTH_LONG).show();

        }
    }

    private void buildAlertMessageNoGps() {
        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final androidx.appcompat.app.AlertDialog alert = builder.create();
        alert.show();

    }


    //CONTROLLARE ORIGINALE

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);
                }
            });
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    @SuppressWarnings( {"MissingPermission"})
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }


    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    public void ClickMenu(View view){
        //open drawer
        openDrawer(drawerLayout);

    }

    public static void openDrawer(DrawerLayout drawerLayout) {
        //open drawer layout
        drawerLayout.openDrawer(GravityCompat.START);
    }

    public void ClickLogo(View view){
        //Close drawer
        closeDrawer(drawerLayout);
    }

    public static void closeDrawer(DrawerLayout drawerLayout) {
        //Close drawer layout
        //chec condition
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            //WHEN DRAWER IS OPEN
            //CLODE DRAWER
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    public void ClickHome(View view){
        //Recreate activity
        closeDrawer(drawerLayout);
    }

    public void ClickDashboard(View view){
        //redirect activity to dashboard
        //redirectActivity(this,ListaFalesie.class);
        Intent intent = new Intent((Context)this, ListaFalesie.class);
        intent.putExtra("mostraFalesie", showCrags);
        intent.putExtra("mostraPalestre", showGyms);
        startActivity(intent);
    }

    public void ClickAboutUs(View view){
        //redirect activity to about us
        redirectActivity(this,Info.class);
    }

    public void ClickLogout(View view){
        //close app
        logout(this);
    }

    public static void logout(Activity activity) {
        //inizialize alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        //set title
        builder.setTitle("Logout");
        //set message
        builder.setMessage("Sicuro di voler uscire?");
        //positive yes button
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //finish activity
                activity.finishAffinity();
                //exit app
                System.exit(0);
            }
        });

        //negative button
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //dismiss dialog
                dialog.dismiss();
            }
        });

        //show dialod
        builder.show();
    }

    public static void redirectActivity(Activity activity, Class aClass) {
        //inizialize intent
        Intent intent = new Intent(activity,aClass);
        //set flag
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //start activity
        activity.startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        //close drawer
        closeDrawer(drawerLayout);
    }
}