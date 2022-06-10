package com.example.falesieinitalia;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class DescrizioneFalesia extends AppCompatActivity {

    DrawerLayout drawerLayout;
    boolean showCrags;
    boolean showGyms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_descrizione_falesia);
        drawerLayout = findViewById(R.id.drawer_layout);
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            String name = extras.getString("nome");
            String description = extras.getString("descrizione");
            String url = "https://boiling-gorge-96661.herokuapp.com//images/" + extras.getString("immagine") + ".jpg";
            Picasso.get().load(url).into((ImageView) findViewById(R.id.Foto));
            TextView  titolo = findViewById(R.id.Titolo);
            titolo.setText(name);
            TextView  descrizione = findViewById(R.id.Descrizione);
            descrizione.setText(description);
            descrizione.setMovementMethod(new ScrollingMovementMethod());
            showCrags = extras.getBoolean("mostraFalesie");
            showGyms = extras.getBoolean("mostraPalestre");
        }
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

    public void ClickMenu(View view){
        //open drawer
        DescrizioneFalesia.openDrawer(drawerLayout);
    }

    public void ClickLogo(View view){
        //close drawer
        DescrizioneFalesia.closeDrawer(drawerLayout);
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
        Intent intent = new Intent((Context)this, ListaFalesie.class);
        intent.putExtra("mostraFalesie", showCrags);
        intent.putExtra("mostraPalestre", showGyms);
        startActivity(intent);
    }

    public void ClickAboutUs(View view){
        //redirect activity to info
        Intent intent = new Intent((Context)this, Info.class);
        startActivity(intent);
        DescrizioneFalesia.closeDrawer(drawerLayout);
    }

    public void ClickLogout(View view){
        //close app
        DescrizioneFalesia.logout(this);
    }
}