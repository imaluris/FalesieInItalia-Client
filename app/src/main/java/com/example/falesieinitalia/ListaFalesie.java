package com.example.falesieinitalia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ListaFalesie extends AppCompatActivity {
    //inizialize variable
    DrawerLayout drawerLayout;
    ListView listView;
    private String url = "https://boiling-gorge-96661.herokuapp.com/Falesia";
    public RequestQueue mQueue;
    JSONArray jsonArray;
    ArrayList<Crag> crags = new ArrayList<>();
    public Boolean showCrags;
    public Boolean showGyms;
    public TextView sortByName;
    public TextView sortByCity;
    public TextView sortByRegion;
    CragListAdapter adapter;

    private Toolbar mytoolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this,getString(R.string.mapbox_access_token));

        Bundle extras = getIntent().getExtras();
        if(extras != null){
            showCrags = extras.getBoolean("mostraFalesie");
            showGyms = extras.getBoolean("mostraPalestre");
        }

        setContentView(R.layout.activity_lista_falesie);
        //assign variable
        drawerLayout = findViewById(R.id.drawer_layout);

        mytoolbar = (Toolbar) findViewById(R.id.mytoolbar);
        setSupportActionBar(mytoolbar);
        getSupportActionBar().setTitle(null);
        mytoolbar.setOverflowIcon(ContextCompat.getDrawable(this,R.drawable.more1));

        listView = findViewById(R.id.listview);

        OnResponseSaveData();

        SearchCrag();
    }

    public void setSortOnClick(){
        sortByName = (TextView) findViewById(R.id.nameTitle);

        sortByName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Collections.sort(crags, new Comparator<Crag>() {
                    @Override
                    public int compare(Crag o1, Crag o2) {
                        return  o1.getName().compareToIgnoreCase(o2.getName());
                    }
                });
                setupListView();
            }
        });

        sortByCity = (TextView) findViewById(R.id.cityTitle);

        sortByCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Collections.sort(crags, new Comparator<Crag>() {
                    @Override
                    public int compare(Crag o1, Crag o2) {
                        return  o1.getCity().compareToIgnoreCase(o2.getCity());
                    }
                });
                setupListView();
            }
        });

        sortByRegion = (TextView) findViewById(R.id.regionTitle);

        sortByRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Collections.sort(crags, new Comparator<Crag>() {
                    @Override
                    public int compare(Crag o1, Crag o2) {
                        return  o1.getRegion().compareToIgnoreCase(o2.getRegion());
                    }
                });
                setupListView();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_options_menu,menu);
        if(showCrags){
            menu.getItem(0).setChecked(true);
        } else {
            menu.getItem(0).setChecked(false);
        }

        if(showGyms){
            menu.getItem(1).setChecked(true);
        } else {
            menu.getItem(1).setChecked(false);
        }
        return true;
    }

    public void SearchCrag(){
        SearchView searchView = findViewById(R.id.searchBox);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                adapter.filter(newText);
                if(TextUtils.isEmpty(newText)){
                    adapter.filter("");
                    listView.clearTextFilter();
                }
                else{
                    adapter.filter(newText);
                }
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.crag:
                if(item.isChecked()){
                    // If item already checked then unchecked it
                    item.setChecked(false);
                    showCrags = false;
                }else{
                    // If item is unchecked then checked it
                    item.setChecked(true);
                    showCrags = true;
                }
                crags.clear();
                OnResponseSaveData();

                return true;
            case R.id.gym:
                if(item.isChecked()){
                    // If item already checked then unchecked it
                    item.setChecked(false);
                    showGyms = false;
                }else {
                    // If item is unchecked then checked it
                    item.setChecked(true);
                    showGyms = true;
                }
                crags.clear();
                OnResponseSaveData();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void OnResponseSaveData() {

        StringRequest request = new StringRequest(url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {

                    jsonArray = new JSONArray(response);

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject crag = jsonArray.getJSONObject(i);

                        String tipo = crag.getString("tipo");
                        String nome = crag.getString("nome");
                        String citta = crag.getString("citta");
                        String regione = crag.getString("regione");
                        String descrizione = crag.getString("descrizione");
                        String immagine = crag.getString("immagine");

                        int index = 0;

                        if (!showGyms & tipo.equals("Falesia")) {
                            //Log.d("tipo","mostra palestre e'falso");
                            crags.add(index, new Crag(nome, citta, regione, descrizione, immagine, tipo));
                            index++;
                        } else if(!showCrags & tipo.equals("Palestra")){
                            //Log.d("tipo","mostra falesie e'falso");
                            crags.add(index, new Crag(nome, citta, regione, descrizione, immagine, tipo));
                            index++;
                        } else if(showGyms & showCrags) {
                            crags.add(index, new Crag(nome, citta, regione, descrizione, immagine, tipo));
                            index++;
                        }

                        setupListView();

                        setSortOnClick();

                        setupClickItem();

                    }
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
    }

    public void setupListView(){

        adapter = new CragListAdapter(this, R.layout.itemlayout, crags);
        listView.setAdapter(adapter);

    }

    public void setupClickItem(){
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(ListaFalesie.this,"Hai selezionato la falesia:" + adapter.getItem(position).getName(),Toast.LENGTH_LONG).show();
                for (int i=0; i<crags.size();i++) {
                    if(crags.get(i).getName().compareTo(adapter.getItem(position).getName()) == 0) {
                        startActivityDescription(crags.get(i).getName(), crags.get(i).getDescription(), crags.get(i).getImage(), showCrags, showGyms);
                    }
                }
            }
        });
    }

    public void startActivityDescription(String name, String description, String image, boolean showCrags, boolean showGyms){
        Intent intent = new Intent((Context)this, DescrizioneFalesia.class);
        intent.putExtra("nome", name);
        intent.putExtra("descrizione", description);
        intent.putExtra("immagine", image);
        intent.putExtra("mostraFalesie", showCrags);
        intent.putExtra("mostraPalestre", showGyms);
        startActivity(intent);
    }

    public static void openDrawer(DrawerLayout drawerLayout) {
        //open drawer layout
        drawerLayout.openDrawer(GravityCompat.START);
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

    public static void redirectActivity(Activity activity, Class aClass) {
        //inizialize intent
        Intent intent = new Intent(activity,aClass);
        //set flag
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //start activity
        activity.startActivity(intent);
    }

    public void ClickMenu(View view){
        //open drawer
        ListaFalesie.openDrawer(drawerLayout);
    }

    public void ClickLogo(View view){
        //close drawer
        ListaFalesie.closeDrawer(drawerLayout);
    }

    public void ClickHome(View view){
        //redirect activity to home
        Intent intent = new Intent((Context)this, MainActivity.class);
        intent.putExtra("mostraFalesie", showCrags);
        intent.putExtra("mostraPalestre", showGyms);
        startActivity(intent);
    }

    public void ClickDashboard(View view){
        //Recreate activity
        ListaFalesie.closeDrawer(drawerLayout);
    }

    public void ClickAboutUs(View view){
        //redirect activity to info
        ListaFalesie.redirectActivity(this, Info.class);
    }

    public void ClickLogout(View view){
        //close app
        MainActivity.logout(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Close drawer
        MainActivity.closeDrawer(drawerLayout);
    }
}