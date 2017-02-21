package in.indianstreets.refervender;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {
    private static final int LOCATION_REQUEST_CODE = 1;
    private static final int SETTINGS_LOCATION_REQUEST_CODE = 2;
    private static final int STORAGE_REQUEST_CODE = 3;
    EditText nameET, mobileET, cityET, streetET, pincodeET, stateET, messageET, latitudeET, logitudeEt, subCategoryEt;
    Spinner categorySpn;
    Button savebtn;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    String selectedCategory;
    LocationRequest locationRequest;
    ArrayList<GPSModel> venderModelArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init
        nameET = (EditText) findViewById(R.id.refer_vender_name);
        cityET = (EditText) findViewById(R.id.refer_vender_city);
        mobileET = (EditText) findViewById(R.id.refer_vender_mobile);
        subCategoryEt = (EditText) findViewById(R.id.refer_vender_subcategory);
        streetET = (EditText) findViewById(R.id.refer_vender_street);
        pincodeET = (EditText) findViewById(R.id.refer_vender_pincode);
        messageET = (EditText) findViewById(R.id.refer_vender_message);
        stateET = (EditText) findViewById(R.id.refer_vender_location);
        latitudeET = (EditText) findViewById(R.id.refer_vender_latitude);
        logitudeEt = (EditText) findViewById(R.id.refer_vender_longitude);
        categorySpn = (Spinner) findViewById(R.id.refere_vender_category_spinner);
        savebtn = (Button) findViewById(R.id.refer_vender_button);


        // connect google api client
        clientConnect();

        //spinner handle
        categorySpn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = String.valueOf(parent.getItemAtPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // save button
        savebtn.setOnClickListener(this);
    }

    private void clientConnect() {
        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
        Toast.makeText(this, "connected", Toast.LENGTH_SHORT).show();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST_CODE);
        } else {
            getLocation();
        }

    }

    private LocationRequest getLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1500L);
        locationRequest.setFastestInterval(1000L);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    private void getLocation() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // getting location form gp provider
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, getLocationRequest(), new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if (location != null) {
                        mLastLocation = location;
                    }
                }
            });
            if (mLastLocation != null) {

                double latitude = mLastLocation.getLatitude();
                double longitude = mLastLocation.getLongitude();
                Log.d("myapp",String.valueOf(latitude));
                Log.d("myapp",String.valueOf(longitude));
                latitudeET.setText(String.valueOf(latitude));
                logitudeEt.setText(String.valueOf(longitude));
                latitudeET.setEnabled(false);
                logitudeEt.setEnabled(false);
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses = null;
                try {
                    addresses = geocoder.getFromLocation(latitude, longitude, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Log.d(Const.TAG,addresses.toString());
                if(addresses != null) {
                    //get city name
                    String cityName = addresses.get(0).getLocality();
                    // get state name
                    String stateName = addresses.get(0).getAdminArea();
                    // get Pincode
                    String pincode = String.valueOf(addresses.get(0).getPostalCode());
                    // get known name
                    String knownName = addresses.get(0).getFeatureName();
                    // set city value
                    cityET.setText(cityName);
                    // set state name
                    stateET.setText(stateName);
                    // set pincode
                    pincodeET.setText(pincode);
                    // set known name
                    nameET.setText(knownName);
                }

            }
        }else{
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Location Settings")
                    .setMessage("GPS seems disable. Do you want to enable?")
                    .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(intent,SETTINGS_LOCATION_REQUEST_CODE);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create();
            dialog.show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == LOCATION_REQUEST_CODE){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getLocation();
            }else{
                Toast.makeText(this, "Location Permission is not granted", Toast.LENGTH_SHORT).show();
            }
        }else if(requestCode == STORAGE_REQUEST_CODE){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                venderModelArrayList = new ArrayList<>();
                new FetchValues().execute();
            }else{
                Toast.makeText(this, "Storage Permission is not granted", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_location,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_current_location){
            if(mGoogleApiClient.isConnected()){
                getLocation();
            }else{
                mGoogleApiClient.connect();
                getLocation();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},STORAGE_REQUEST_CODE);
        }else{
//            getValues();
            venderModelArrayList = new ArrayList<>();
            new FetchValues().execute();
        }

    }

    private void getValues() {
        String name = nameET.getText().toString();
        String subCategory = subCategoryEt.getText().toString();
        String mobile = mobileET.getText().toString();
        String state = stateET.getText().toString();
        String city = cityET.getText().toString();
        String pincode = pincodeET.getText().toString();
        String street = streetET.getText().toString();
        String latitude = latitudeET.getText().toString();
        String longitude = logitudeEt.getText().toString();
        String message = messageET.getText().toString();
        //check for validation

        GPSModel model = new GPSModel();
        model.setName(name);
        model.setCategory(selectedCategory);
        model.setSubCategory(subCategory);
        model.setMobile(mobile);
        model.setCity(city);
        model.setState(state);
        model.setPincode(pincode);
        model.setStreet(street);
        model.setLatitude(latitude);
        model.setLongitude(longitude);
        model.setMessage(message);
        venderModelArrayList.add(model);

        addOrCreateXmlFile();
    }

    private void addOrCreateXmlFile() {
        File xlsFolder = null;
        String filePath;
        if(!Utils.isDirExists()) {
            filePath = createNewFile(xlsFolder);
            getSharedPreferences(Const.SHAREDPREFERENCE_MAIN,MODE_PRIVATE).edit().putString(Const.Folder_PATH,filePath).apply();
            Log.d(Const.TAG,filePath);
            Log.d(Const.TAG,"New File Created");
            File xlsFile = new File(filePath);
            try {
                createNewXlsFile(xlsFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            SharedPreferences preferences = getSharedPreferences(Const.SHAREDPREFERENCE_MAIN,MODE_PRIVATE);
            String folderPath = preferences.getString(Const.Folder_PATH,null);
            String path = preferences.getString(Const.DIR_PATH,null);
            if(path != null) {
                File xlsFile = new File(path);
                //check file if exist or not
                if (!xlsFile.exists()) {
                    // if file not exist
                    try {
                        createNewXlsFile(xlsFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    // if file exists
                    Log.d(Const.TAG, "file exists");
                    openExistXls(xlsFile);
                }
            }
        }
    }

    private void openExistXls(File xlsFile) {
        // get workbook
        Workbook workbook;
        try {
            workbook = Workbook.getWorkbook(xlsFile.getAbsoluteFile());
            WritableWorkbook workbookCopy = Workbook.createWorkbook(xlsFile,workbook);
            Log.d(Const.TAG,"Workbook open");
            WritableSheet sheet = workbookCopy.getSheet(0);
            modifyRow(workbook,workbookCopy,sheet,venderModelArrayList);

        }catch (IOException | WriteException | BiffException e) {
            e.printStackTrace();
        }
    }

    private void createNewXlsFile(File xlsFile) throws IOException {
        xlsFile.createNewFile();
        WorkbookSettings wbSettings = new WorkbookSettings();
        wbSettings.setLocale(new Locale("en", "EN"));
        // create workbook
        WritableWorkbook workbook;
        try {
            workbook = Workbook.createWorkbook(xlsFile, wbSettings);
            WritableSheet sheet = workbook.createSheet("Vender Details Sheet", 0);
            Label categoryLabel = new Label(0,0, Const.CATEGORY);
            Label subCategoryLabel = new Label(1,0, Const.SUB_CATEGORY);
            Label nameLabel = new Label(2,0, Const.VENDER_NAME);
            Label mobileLabel = new Label(3,0, Const.VENDER_MOBILE);
            Label stateLabel = new Label(4,0, Const.VENDER_STATE);
            Label cityLabel = new Label(5,0, Const.VENDER_CITY);
            Label pincodeLabel = new Label(6,0, Const.VENDER_PINCODE);
            Label streetLabel = new Label(7,0, Const.VENDER_STREET);
            Label latitudeLabel = new Label(8,0, Const.VENDER_LATITUDE);
            Label longitudeLabel = new Label(9,0, Const.VENDER_LONGITUDE);
            Label messageLabel = new Label(10,0, Const.VENDER_MESSAGE);
            sheet.addCell(categoryLabel);
            sheet.addCell(subCategoryLabel);
            sheet.addCell(nameLabel);
            sheet.addCell(mobileLabel);
            sheet.addCell(stateLabel);
            sheet.addCell(cityLabel);
            sheet.addCell(pincodeLabel);
            sheet.addCell(streetLabel);
            sheet.addCell(latitudeLabel);
            sheet.addCell(longitudeLabel);
            sheet.addCell(messageLabel);
            addRow(workbook,sheet,venderModelArrayList);
        } catch (IOException | WriteException e) {
            e.printStackTrace();
        }
    }

    // creating for first time;
    private void addRow(WritableWorkbook workbook, WritableSheet sheet, ArrayList<GPSModel> venderModelArrayList) throws WriteException, IOException {
        SharedPreferences preferences = getSharedPreferences(Const.SHAREDPREFERENCE_MAIN,MODE_PRIVATE);
        int initRow = preferences.getInt(Const.LAST_ROW_NUMBER,1);
        Log.d(Const.TAG,"Row:"+initRow);
        for(int i = 0 ; i < venderModelArrayList.size() ; i++){
            sheet.addCell(new Label(0,initRow,venderModelArrayList.get(i).getCategory()));
            sheet.addCell(new Label(1,initRow,venderModelArrayList.get(i).getSubCategory()));
            sheet.addCell(new Label(2,initRow,venderModelArrayList.get(i).getName()));
            sheet.addCell(new Label(3,initRow,venderModelArrayList.get(i).getMobile()));
            sheet.addCell(new Label(4,initRow,venderModelArrayList.get(i).getState()));
            sheet.addCell(new Label(5,initRow,venderModelArrayList.get(i).getCity()));
            sheet.addCell(new Label(6,initRow,venderModelArrayList.get(i).getPincode()));
            sheet.addCell(new Label(7,initRow,venderModelArrayList.get(i).getStreet()));
            sheet.addCell(new Label(8,initRow,venderModelArrayList.get(i).getLatitude()));
            sheet.addCell(new Label(9,initRow,venderModelArrayList.get(i).getLongitude()));
            sheet.addCell(new Label(10,initRow,venderModelArrayList.get(i).getMessage()));
            initRow = initRow + 1;
            SharedPreferences.Editor editor = getSharedPreferences(Const.SHAREDPREFERENCE_MAIN,MODE_PRIVATE).edit();
            editor.putInt(Const.LAST_ROW_NUMBER,initRow);
            editor.apply();
        }
        workbook.write();
        workbook.close();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "Saved", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // creating for first time;
    private void modifyRow(Workbook workbook1, WritableWorkbook workbook, WritableSheet sheet, ArrayList<GPSModel> venderModelArrayList) throws WriteException, IOException {
        SharedPreferences preferences = getSharedPreferences(Const.SHAREDPREFERENCE_MAIN,MODE_PRIVATE);
        int initRow = preferences.getInt(Const.LAST_ROW_NUMBER,1);
        Log.d(Const.TAG,"Row:"+initRow);
        for(int i = 0 ; i < venderModelArrayList.size() ; i++){
            sheet.addCell(new Label(0,initRow,venderModelArrayList.get(i).getCategory()));
            sheet.addCell(new Label(1,initRow,venderModelArrayList.get(i).getSubCategory()));
            sheet.addCell(new Label(2,initRow,venderModelArrayList.get(i).getName()));
            sheet.addCell(new Label(3,initRow,venderModelArrayList.get(i).getMobile()));
            sheet.addCell(new Label(4,initRow,venderModelArrayList.get(i).getState()));
            sheet.addCell(new Label(5,initRow,venderModelArrayList.get(i).getCity()));
            sheet.addCell(new Label(6,initRow,venderModelArrayList.get(i).getPincode()));
            sheet.addCell(new Label(7,initRow,venderModelArrayList.get(i).getStreet()));
            sheet.addCell(new Label(8,initRow,venderModelArrayList.get(i).getLatitude()));
            sheet.addCell(new Label(9,initRow,venderModelArrayList.get(i).getLongitude()));
            sheet.addCell(new Label(10,initRow,venderModelArrayList.get(i).getMessage()));
            initRow = initRow + 1;
            SharedPreferences.Editor editor = getSharedPreferences(Const.SHAREDPREFERENCE_MAIN,MODE_PRIVATE).edit();
            editor.putInt(Const.LAST_ROW_NUMBER,initRow);
            editor.apply();
        }
        workbook.write();
        workbook.close();
        workbook1.close();
        Log.d(Const.TAG,"Workbook close");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "Saved", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private String createNewFile(File xlsFolder) {
        xlsFolder = Utils.createDirectory(this);
        String path = xlsFolder.getAbsolutePath() + "/VenderDetails.xls";
        SharedPreferences.Editor editor = (getSharedPreferences(Const.SHAREDPREFERENCE_MAIN,Context.MODE_PRIVATE)).edit();
        editor.putString(Const.DIR_PATH,path);
        editor.apply();
        return path;
    }

    private class FetchValues extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... params) {
            getValues();
            return null;
        }
    }

}
