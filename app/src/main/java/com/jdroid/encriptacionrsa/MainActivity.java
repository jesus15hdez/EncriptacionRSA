package com.jdroid.encriptacionrsa;

import static android.os.Environment.getExternalStorageDirectory;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.Objects;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {
Button searchEncrypt, searchDecrypt;
ImageButton startEncrypt, cancelEncrypt, startDecrypt, cancelDecrypt;
ImageView statusEncrypt, statusDecrypt;
TextView textStatusEncrypt, textStatusDecrypt, selectedFileEncrypt, selectedFileDecrypt;
ProgressBar progressBarEncrypt, progressBarDecrypt;
DrawerLayout drawerLayout;
String str="", path="";
WifiManager wifiManager;

float bytes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //BARRA DE HERRAMIENTAS TOOLBAR
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        //DRAWERVIEW
        drawerLayout = findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, myToolbar, R.string.drawerOpen, R.string.drawerClose);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        //NavigationView
        final NavigationView navView = findViewById(R.id.navView);
        navView.setNavigationItemSelectedListener(this);

        //NOTIFICACIONES
        createNotificationChannel();

        //INICIALIZACION DE BOTONES DEL SERVICIO DE ENCRIPTACION
        searchEncrypt=findViewById(R.id.searchEncrypt);
        startEncrypt=findViewById(R.id.startEncrypt);
        startEncrypt.setEnabled(false);
        startEncrypt.setColorFilter(getColor(R.color.disableButton));
        cancelEncrypt=findViewById(R.id.cancelEncrypt);
        cancelEncrypt.setEnabled(false);
        cancelEncrypt.setColorFilter(getColor(R.color.disableButton));
        selectedFileEncrypt=findViewById(R.id.selectedFileEncrypt);
        statusEncrypt=findViewById(R.id.statusEncrypt);
        textStatusEncrypt=findViewById(R.id.textStatusEncrypt);
        progressBarEncrypt=findViewById(R.id.progressBarEncrypt);
        //GENERACION DE METODOS DE EVENTOS
        searchEncrypt.setOnClickListener(this);
        startEncrypt.setOnClickListener(this);
        cancelEncrypt.setOnClickListener(this);

        //INICIALIZACION DE BOTONES DEL SERVICIO DE DESENCRIPTADO
        searchDecrypt=findViewById(R.id.searchDecrypt);
        startDecrypt=findViewById(R.id.startDecrypt);
        startDecrypt.setEnabled(false);
        startDecrypt.setColorFilter(getColor(R.color.disableButton));
        cancelDecrypt=findViewById(R.id.cancelDecrypt);
        cancelDecrypt.setEnabled(false);
        cancelDecrypt.setColorFilter(getColor(R.color.disableButton));
        selectedFileDecrypt=findViewById(R.id.selectedFileDecrypt);
        statusDecrypt=findViewById(R.id.statusDecrypt);
        textStatusDecrypt=findViewById(R.id.textStatusDecrypt);
        progressBarDecrypt=findViewById(R.id.progressBarDecrypt);

        //GENERACION DE METODOS DE EVENTOS
        searchDecrypt.setOnClickListener(this);
        startDecrypt.setOnClickListener(this);
        cancelDecrypt.setOnClickListener(this);
    }

    private void scanSuccess() {
        List<ScanResult> results = wifiManager.getScanResults();
    }

    private void scanFailure() {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        List<ScanResult> results = wifiManager.getScanResults();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED) {
            startEncrypt.setEnabled(false);
            startEncrypt.setColorFilter(getColor(R.color.disableButton));
            cancelEncrypt.setEnabled(false);
            cancelEncrypt.setColorFilter(getColor(R.color.disableButton));
            startDecrypt.setEnabled(false);
            startDecrypt.setColorFilter(getColor(R.color.disableButton));
            cancelDecrypt.setEnabled(false);
            cancelDecrypt.setColorFilter(getColor(R.color.disableButton));
        }
        if ((resultCode == Activity.RESULT_OK) && (requestCode == 1)) {
            if (data != null) {
                statusEncrypt.setVisibility(View.INVISIBLE);
                Uri uri = data.getData();
                path=getNameFile(uri);
                selectedFileEncrypt.setText(path);
                String aux[]=path.split("\\.");
                if(aux[1].equals("java") || aux[1].equals("ino") || aux[1].equals("c") || aux[1].equals("cpp") || aux[1].equals("txt") || aux[1].equals("h")) {
                    textStatusEncrypt.setText("");
                    bytes = Float.parseFloat(getSizeFile(uri));
                    try (InputStream inputStream = getContentResolver().openInputStream(uri);
                         BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream)))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            str = str + line + "\r\n";
                        }
                        startEncrypt.setEnabled(true);
                        startEncrypt.setColorFilter(getColor(R.color.enableButton));
                        cancelEncrypt.setEnabled(true);
                        cancelEncrypt.setColorFilter(getColor(R.color.enableButton));
                        searchDecrypt.setEnabled(false);
                    } catch (IOException e) {
                        startEncrypt.setEnabled(false);
                        startEncrypt.setColorFilter(getColor(R.color.disableButton));
                        cancelEncrypt.setEnabled(false);
                        cancelEncrypt.setColorFilter(getColor(R.color.disableButton));
                        searchDecrypt.setEnabled(true);
                    }
                }
                else{
                    textStatusEncrypt.setText("Archivo seleccionado no valido");
                }
            }
            else{
                selectedFileEncrypt.setText("");
            }
        }
        else if ((resultCode == Activity.RESULT_OK) && (requestCode == 2)){
            if (data!=null){
                statusDecrypt.setVisibility(View.INVISIBLE);
                Uri uri = data.getData();
                path=getNameFile(uri);
                selectedFileDecrypt.setText(path);
                String aux[]=path.split("\\.");
                if(aux[1].equals("rsa")) {
                    textStatusDecrypt.setText("");
                    try (InputStream inputStream = getContentResolver().openInputStream(uri);
                         BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream)))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            str = line;
                        }
                        startDecrypt.setEnabled(true);
                        startDecrypt.setColorFilter(getColor(R.color.enableButton));
                        cancelDecrypt.setEnabled(true);
                        cancelDecrypt.setColorFilter(getColor(R.color.enableButton));
                        searchEncrypt.setEnabled(false);
                    } catch (IOException e) {
                        startDecrypt.setEnabled(false);
                        startDecrypt.setColorFilter(getColor(R.color.disableButton));
                        cancelDecrypt.setEnabled(false);
                        cancelDecrypt.setColorFilter(getColor(R.color.disableButton));
                        searchEncrypt.setEnabled(true);
                    }
                }
                else {
                    textStatusDecrypt.setText("Archivo no valido; Debe ser .rsa");
                }
            }
            else{
                selectedFileDecrypt.setText("");
            }
        }
    }

    private String getNameFile(Uri uri) {
        String displayName = "";

        if (uri.getScheme().equals("file")) {
            displayName = uri.getLastPathSegment();
        } else if (uri.getScheme().equals("content")) {

            Cursor cursor = null;
            cursor = getBaseContext().getContentResolver().query(uri, null, null, null, null, null);

            try {
                // moveToFirst() returns false if the cursor has 0 rows.
                if (cursor != null && cursor.moveToFirst()) {
                    displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return displayName;
    }

    private String getSizeFile(Uri uri) {
        String size = "";

        if (uri.getScheme().equals("file")) {
            size = uri.getLastPathSegment();
        } else if (uri.getScheme().equals("content")) {

            Cursor cursor = null;
            cursor = getBaseContext().getContentResolver().query(uri, null, null, null, null, null);
            try {
                // moveToFirst() returns false if the cursor has 0 rows.
                if (cursor != null && cursor.moveToFirst()) {
                    size = cursor.getString(cursor.getColumnIndex(OpenableColumns.SIZE));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return size;
    }

    public void showNotificationEncrypt(boolean encrypt){
        if(encrypt) {
            // Create an explicit intent for an Activity in your app
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "main")
                    .setSmallIcon(R.drawable.notify)
                    .setContentTitle(getString(R.string.titleEncryptNotification))
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(getString(R.string.bodyTextEncryptNotification)))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    // Set the intent that will fire when the user taps the notification
                    //.setContentIntent(pendingIntent)
                    .setAutoCancel(false)
                    .setOngoing(true);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            // Issue the initial notification with zero progress
            builder.setProgress(0, 0, true);
            // notificationId is a unique int for each notification that you must define
            notificationManager.notify(0, builder.build());
        }
        else{
            // Create an explicit intent for an Activity in your app
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "main")
                    .setSmallIcon(R.drawable.notify)
                    .setContentTitle(getString(R.string.titleDecryptNotification))
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(getString(R.string.bodyTextDecryptNotification)))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    // Set the intent that will fire when the user taps the notification
                    //.setContentIntent(pendingIntent)
                    .setAutoCancel(false)
                    .setOngoing(true);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            // Issue the initial notification with zero progress
            builder.setProgress(0, 0, true);
            // notificationId is a unique int for each notification that you must define
            notificationManager.notify(0, builder.build());
        }
    }

    public void updateNotificationEncrypt(boolean encrypt){
        if (encrypt) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "main")
                    .setSmallIcon(R.drawable.notify)
                    .setContentTitle(getString(R.string.encriptado))
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(getString(R.string.finishEncrypt)))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setOngoing(false);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            // Issue the initial notification with zero progress
            builder.setProgress(0, 0, false);
            // notificationId is a unique int for each notification that you must define
            notificationManager.notify(0, builder.build());
        }
        else{
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "main")
                    .setSmallIcon(R.drawable.notify)
                    .setContentTitle(getString(R.string.desencriptado))
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(getString(R.string.finishDecrypt)))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setOngoing(false);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            // Issue the initial notification with zero progress
            builder.setProgress(0, 0, false);
            // notificationId is a unique int for each notification that you must define
            notificationManager.notify(0, builder.build());
        }
    }

    public void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("main", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                Intent i=new Intent(this, about.class);
                startActivity(i);
                return true;
            case R.id.themeMode:
                int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                switch (currentNightMode) {
                    case Configuration.UI_MODE_NIGHT_NO:
                        getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        break;
                    case Configuration.UI_MODE_NIGHT_YES:
                        getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        break;
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.searchEncrypt:
                str="";
                path="";
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("*/*");
                startActivityForResult(Intent.createChooser(intent, "Choose File"), 1);
                break;
            case R.id.startEncrypt:
                showNotificationEncrypt(true);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        startEncrypt.post(new Runnable() {
                            @Override
                            public void run() {
                                startEncrypt.setEnabled(false);
                                startEncrypt.setColorFilter(getColor(R.color.disableButton));
                                cancelEncrypt.setEnabled(false);
                                cancelEncrypt.setColorFilter(getColor(R.color.disableButton));
                                statusEncrypt.setImageResource(R.drawable.time);
                                statusEncrypt.setVisibility(View.VISIBLE);
                                textStatusEncrypt.setText(R.string.waitEncrypt);
                                searchEncrypt.setEnabled(false);
                                progressBarEncrypt.setIndeterminate(true);
                            }
                        });

                        RSA rsa = new RSA();
                        try {
                            rsa.genKeyPair(512 * (int) Math.ceil(bytes / 53));
                            String aux[] = path.split("\\.");
                            File ruta_sd = new File(getExternalStorageDirectory()+"/Download/", aux[0]);
                            ruta_sd.mkdirs();
                            rsa.saveToDiskPrivateKey(ruta_sd, aux);
                            rsa.saveToDiskPublicKey(ruta_sd, aux[0]);
                            String secure = rsa.Encrypt(str);

                            File f = new File(getExternalStorageDirectory()+"/Download/"+aux[0]+"/", aux[0] + ".rsa");
                            OutputStreamWriter fout = new OutputStreamWriter(new FileOutputStream(f));
                            fout.write(secure);
                            fout.close();
                            updateNotificationEncrypt(true);
                        } catch (Exception ex) {
                            statusEncrypt.post(new Runnable() {
                                @Override
                                public void run() {
                                    statusEncrypt.setImageResource(R.drawable.error);
                                    searchDecrypt.setEnabled(true);
                                }
                            });
                        }
                        startEncrypt.post(new Runnable() {
                            @Override
                            public void run() {
                                progressBarEncrypt.setIndeterminate(false);
                                progressBarEncrypt.setProgress(100, true);
                                textStatusEncrypt.setText(R.string.encriptado);
                                statusEncrypt.setImageResource(R.drawable.notify);
                                searchDecrypt.setEnabled(true);
                                searchEncrypt.setEnabled(true);
                            }
                        });
                    }
                }).start();
                break;
            case R.id.cancelEncrypt:
                textStatusEncrypt.setText("");
                selectedFileEncrypt.setText("");
                startEncrypt.setEnabled(false);
                startEncrypt.setColorFilter(getColor(R.color.disableButton));
                cancelEncrypt.setEnabled(false);
                cancelEncrypt.setColorFilter(getColor(R.color.disableButton));
                searchDecrypt.setEnabled(true);
                break;
            case R.id.searchDecrypt:
                path="";
                str="";
                Intent ID = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                ID.setType("*/*");
                startActivityForResult(Intent.createChooser(ID, "Choose File"), 2);
                break;
            case R.id.startDecrypt:
                showNotificationEncrypt(false);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        startDecrypt.post(new Runnable() {
                            @Override
                            public void run() {
                                startDecrypt.setEnabled(false);
                                startDecrypt.setColorFilter(getColor(R.color.disableButton));
                                cancelDecrypt.setEnabled(false);
                                cancelDecrypt.setColorFilter(getColor(R.color.disableButton));
                                searchDecrypt.setEnabled(false);
                                searchEncrypt.setEnabled(false);
                                progressBarDecrypt.setIndeterminate(true);
                                statusDecrypt.setVisibility(View.VISIBLE);
                                statusDecrypt.setImageResource(R.drawable.time);
                                textStatusDecrypt.setText(R.string.waitDecrypt);
                            }
                        });

                        RSA rsa = new RSA();
                        String aux[]=path.split("\\.");
                        String extension= null;
                        try {
                            File directory = new File(getExternalStorageDirectory()+"/Download/"+aux[0]+"/");
                            extension = rsa.openFromDiskPrivateKey(directory,aux[0]+"Key.pri");
                            rsa.openFromDiskPublicKey(directory,aux[0]+"Key.pub");
                            String unsecure = rsa.Decrypt(str);
                            File f = new File(directory, aux[0]+"."+extension);
                            OutputStreamWriter fout = new OutputStreamWriter(new FileOutputStream(f));
                            fout.write(unsecure);
                            fout.close();
                            updateNotificationEncrypt(false);
                        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
                            //statusDecrypt.setVisibility(View.VISIBLE);
                            statusDecrypt.setImageResource(R.drawable.error);
                            textStatusDecrypt.setText("Error al desencriptar");
                            searchEncrypt.setEnabled(true);
                        }
                        statusDecrypt.post(new Runnable() {
                            @Override
                            public void run() {
                                //statusDecrypt.setVisibility(View.VISIBLE);
                                statusDecrypt.setImageResource(R.drawable.notify);
                                textStatusDecrypt.setText(getString(R.string.desencriptado));
                                searchEncrypt.setEnabled(true);
                                searchDecrypt.setEnabled(true);
                                progressBarDecrypt.setIndeterminate(false);
                                progressBarDecrypt.setProgress(100, true);
                            }
                        });
                    }
                }).start();
                break;
            case R.id.cancelDecrypt:
                textStatusDecrypt.setText("");
                selectedFileDecrypt.setText("");
                startDecrypt.setEnabled(false);
                startDecrypt.setColorFilter(getColor(R.color.disableButton));
                cancelDecrypt.setEnabled(false);
                cancelDecrypt.setColorFilter(getColor(R.color.disableButton));
                searchEncrypt.setEnabled(true);
                break;
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int itemId = menuItem.getItemId();
        menuItem.setChecked(true);
        Toast.makeText(getApplicationContext(), menuItem.getTitle(), Toast.LENGTH_SHORT).show();
        return false;
    }
}
