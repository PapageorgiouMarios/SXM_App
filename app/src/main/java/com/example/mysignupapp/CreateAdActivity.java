package com.example.mysignupapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.example.mysignupapp.Utility.NetworkChangeListener;
import com.example.mysignupapp.databinding.ActivityCreateAdBinding;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

public class CreateAdActivity extends DrawerBaseActivity implements GeocodingTask.GeocodingListener {
    FirebaseAuth mAuth;
    FirebaseUser currentUser;

    String[] items = {"Vehicles", "Men Clothing", "Women Clothing", "Music",
            "Sports", "Office", "Books", "Electronics", "Toys", "Movies", "Collectibles"};
    AutoCompleteTextView autoCompleteTxt;
    ArrayAdapter<String> adapterItems;

    private ImageSwitcher imageIs;
    private Button previousBtn, nextBtn, pickImagesBtn;
    private Button create_ad_button;

    private ImageButton delete_image;
    private ArrayList<Uri> imageUris;
    private ArrayList<String> myurls;
    StorageTask uploadTask;
    StorageReference storageReference;

    String title_input;
    String category_input;
    String description_input;
    String price_input, address_input = "";
    ArrayList<String> switch_inputs;

    TextView switch_selections;

    TextView image_number;
    boolean[] selected_switch;

    ArrayList<Integer> switch_list = new ArrayList<>();
    String[] category_array = {"Vehicles", "Men Clothing", "Women Clothing", "Music",
            "Sports", "Office", "Books", "Electronics", "Toys", "Movies", "Collectibles"};

    private static final int PICK_IMAGES_CODE = 0;

    int position = 0;

    TextInputLayout TITLE_textInputLayout;

    TextInputLayout PRICE_textInputLayout;

    TextInputLayout DESCRIPTION_textInputLayout;

    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    ActivityCreateAdBinding activityCreateAdBinding;

    LocationManager locationManager;
    LatLng currentLocation;

    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private CheckBox first_checkbox, getLocationAutomatically;
    private boolean boolean_location;

    Button btnGeoAddr;
    TextInputLayout ADDRESS_textInputLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setCurrentLocation(0, 0);
        super.onCreate(savedInstanceState);
        activityCreateAdBinding = ActivityCreateAdBinding.inflate(getLayoutInflater());
        setContentView(activityCreateAdBinding.getRoot());
        allocateActivityTitle("Ad Creation");


        ADDRESS_textInputLayout = findViewById(R.id.textfield_address);
        ADDRESS_textInputLayout.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {

                }else{
                    address_input = ADDRESS_textInputLayout.getEditText().getText().toString();
                    findGeocoding(address_input);
                    getLocationAutomatically.setChecked(false);

                }
            }
        });



        btnGeoAddr = findViewById(R.id.hahaha);
        btnGeoAddr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                address_input = ADDRESS_textInputLayout.getEditText().getText().toString();
                findGeocoding(address_input);
            }
        });



        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        first_checkbox = findViewById(R.id.checkbox);
        first_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Toast.makeText(getApplicationContext(), "ha", Toast.LENGTH_SHORT).show();
                if (isChecked) {
                    findViewById(R.id.textfield_address).setVisibility(View.VISIBLE);
                    findViewById(R.id.get_location_cbox).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.textfield_address).setVisibility(View.GONE);
                    findViewById(R.id.get_location_cbox).setVisibility(View.GONE);
                    getLocationAutomatically.setChecked(false);
                    ADDRESS_textInputLayout.getEditText().setText("");
                    setCurrentLocation(0, 0);
                }
            }
        });

        getLocationAutomatically = findViewById(R.id.get_location_cbox);
        getLocationAutomatically.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    getLocationCoordinates();
                    ADDRESS_textInputLayout.getEditText().setText("");
                }
            }
        });


        if (currentUser != null) {
            Toast.makeText(CreateAdActivity.this, "YOU EXIST", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(CreateAdActivity.this, "WHO ARE YOU", Toast.LENGTH_LONG).show();
        }

        switch_inputs = new ArrayList<>();

        storageReference = FirebaseStorage.getInstance().getReference("Ads");

        autoCompleteTxt = findViewById(R.id.select_category);

        adapterItems = new ArrayAdapter<String>(this, R.layout.list_item, items);
        autoCompleteTxt.setAdapter(adapterItems);

        TITLE_textInputLayout = (TextInputLayout) findViewById(R.id.title_textfield);

        PRICE_textInputLayout = (TextInputLayout) findViewById(R.id.price_textfield);

        DESCRIPTION_textInputLayout = (TextInputLayout) findViewById(R.id.description_textfield);

        autoCompleteTxt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                category_input = item;
                Toast.makeText(getApplicationContext(), "Item: " + item, Toast.LENGTH_SHORT).show();
            }
        });

        imageIs = findViewById(R.id.imagesIs);
        previousBtn = findViewById(R.id.previousBtn);
        nextBtn = findViewById(R.id.nextBtn);
        pickImagesBtn = findViewById(R.id.pickImagesBtn);
        delete_image = findViewById(R.id.deleteBtn);
        image_number = (TextView) findViewById(R.id.image_number);

        imageUris = new ArrayList<>();
        myurls = new ArrayList<>();

        String image_number_so_far = "Images: " + imageUris.size() + "/5";
        image_number.setText(image_number_so_far);

        imageIs.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                ImageView imageView = new ImageView(getApplicationContext());
                return imageView;
            }
        });

        pickImagesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageUris.size() >= 5) {
                    Toast.makeText(CreateAdActivity.this, "Max image input reached!", Toast.LENGTH_SHORT).show();
                } else {
                    pickImagesIntent();
                }
            }
        });

        previousBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position > 0) {
                    position--;
                    imageIs.setImageURI(imageUris.get(position));
                } else {
                    Toast.makeText(CreateAdActivity.this, "No Previous images", Toast.LENGTH_SHORT).show();
                }
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position < imageUris.size() - 1) {
                    position++;
                    imageIs.setImageURI(imageUris.get(position));
                } else {
                    Toast.makeText(CreateAdActivity.this, "No More images", Toast.LENGTH_SHORT).show();
                }
            }
        });

        delete_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageUris.size() == 1) {
                    imageIs.setImageURI(null);
                    imageUris.remove(position);
                    String image_number_now = "Images: " + imageUris.size() + "/5";
                    image_number.setText(image_number_now);
                }
                else if(imageUris.size() >= 2)
                {
                    imageUris.remove(position);
                    imageIs.setImageURI(imageUris.get(0));
                    String image_number_now = "Images: " + imageUris.size() + "/5";
                    image_number.setText(image_number_now);
                } else if (imageUris.isEmpty()) {
                    Toast.makeText(CreateAdActivity.this, "No images to delete", Toast.LENGTH_SHORT).show();
                }
            }
        });

        switch_selections = findViewById(R.id.switch_multiple_selector);

        selected_switch = new boolean[category_array.length];

        create_ad_button = findViewById(R.id.create_button);
        switch_selections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CreateAdActivity.this);
                builder.setTitle("Category");
                builder.setCancelable(false);
                builder.setMultiChoiceItems(category_array, selected_switch, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (isChecked) {
                            switch_list.add(which);
                            Collections.sort(switch_list);
                        } else {
                            switch_list.remove(Integer.valueOf(which));
                        }
                    }
                });

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        StringBuilder stringBuilder = new StringBuilder();

                        for (int j = 0; j < switch_list.size(); j++) {
                            stringBuilder.append(category_array[switch_list.get(j)]);
                            switch_inputs.add(category_array[switch_list.get(j)]);

                            if (j != switch_list.size() - 1) {
                                stringBuilder.append(", ");
                            }
                        }

                        switch_selections.setText(stringBuilder.toString());
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builder.setNeutralButton("Clear all", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for (int j = 0; j < selected_switch.length; j++) {
                            selected_switch[j] = false;
                            switch_list.clear();
                            switch_inputs.clear();
                            switch_selections.setText("");
                        }

                    }
                });

                builder.show();
            }
        });

        storageReference = FirebaseStorage.getInstance().getReference("Ads");
        create_ad_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                makeAd();
            }
        });

    }

    private void makeAd() {
        ProgressDialog progressDialog = new ProgressDialog(CreateAdActivity.this);
        progressDialog.setMessage("Creating ad...");
        progressDialog.show();

        title_input = TITLE_textInputLayout.getEditText().getText().toString();
        price_input = PRICE_textInputLayout.getEditText().getText().toString();
        description_input = DESCRIPTION_textInputLayout.getEditText().getText().toString();

        if (imageUris.size() > 0) {
            ArrayList<String> myurls = new ArrayList<>();

            for (int i = 0; i < imageUris.size(); i++) {

                StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                        + "." + getFileExtension(imageUris.get(i)));

                uploadTask = fileReference.putFile(imageUris.get(i));
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return fileReference.getDownloadUrl();
                    }
                }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {
                    if (task.isSuccessful()) {
                        Uri image = task.getResult();
                        String myurl = image.toString();
                        myurls.add(myurl);


                        if (myurls.size() == imageUris.size()) {
                            try {
                                Ad new_ad = new Ad(title_input, category_input, price_input, switch_inputs, myurls, description_input, getCurrentLocation());
                                Toast.makeText(CreateAdActivity.this, "Success!!!!!!!!!!!!!!!!!", Toast.LENGTH_LONG).show();
                                String user_id = mAuth.getCurrentUser().getUid();

                                FirebaseDatabase db = FirebaseDatabase.getInstance();

                                DatabaseReference user_ref = db.getReference("Users/" + user_id);
                                user_ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        User user_now = snapshot.getValue(User.class);
                                        user_now.getAds().add(new_ad);
                                        user_ref.setValue(user_now);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(CreateAdActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });

                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Ads");
                                String adId = reference.push().getKey();

                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("ID", adId);
                                hashMap.put("Images", myurls);
                                hashMap.put("Publisher", currentUser.getUid());
                                hashMap.put("Title", title_input);
                                hashMap.put("Coordinates", getCurrentLocation());

                                if (price_input.equals("0") || price_input.equals("")) {
                                    hashMap.put("Price", "Free");
                                } else {
                                    hashMap.put("Price", price_input + "$");
                                }

                                hashMap.put("Category", category_input);
                                hashMap.put("Description", description_input);
                                hashMap.put("Switch", switch_inputs);

                                reference.child(category_input + " " + title_input).setValue(hashMap);

                                progressDialog.dismiss();

                                startActivity(new Intent(CreateAdActivity.this, HomeActivity.class));
                                finish();

                            } catch (NullPointerException e) {
                                Toast.makeText(CreateAdActivity.this, "Success!!!!!!!!!!!!!!!!!", Toast.LENGTH_LONG).show();
                            }
                        }
                    } else {
                        Toast.makeText(CreateAdActivity.this, "Failed to post!", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CreateAdActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            Toast.makeText(CreateAdActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    public String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void pickImagesIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image(s)"), PICK_IMAGES_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGES_CODE) {

            if (resultCode == Activity.RESULT_OK) {

                if (data.getClipData() != null) {

                    int count = data.getClipData().getItemCount();

                    if (count > 5) {
                        count = 5;
                    }
                    for (int i = 0; i < count; i++) {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        imageUris.add(imageUri);
                    }

                    imageIs.setImageURI(imageUris.get(0));
                    position = 0;
                } else {
                    Uri imageUri = data.getData();
                    imageUris.add(imageUri);
                    imageIs.setImageURI(imageUris.get(0));
                    position = 0;
                }
                String image_number_so_far = "Images: " + imageUris.size() + "/5";
                image_number.setText(image_number_so_far);
            }
        }
    }

    @Override
    protected void onStart() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener, filter);
        super.onStart();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(networkChangeListener);
        super.onStop();
    }


    @Override
    public void onLocationChanged(Location location) {

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

    public void getGPS_Status(View view){
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            setBoolean_location(true);
        }else{
            setBoolean_location(false);
        }
    }


    public void setGPS_ON(){

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000/2);

        LocationSettingsRequest.Builder locationSettingsRequestBuilder = new LocationSettingsRequest.Builder();

        locationSettingsRequestBuilder.addLocationRequest(locationRequest);
        locationSettingsRequestBuilder.setAlwaysShow(true);

        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(locationSettingsRequestBuilder.build());
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                getLocationAutomatically.setChecked(false);
                setBoolean_location(false);

                if (e instanceof ResolvableApiException){
                    try {
                        ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                        resolvableApiException.startResolutionForResult(CreateAdActivity.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendIntentException) {
                        sendIntentException.printStackTrace();
                    }
                }
            }
        });
    }

    private void getLocationCoordinates() {

        if(isBoolean_location()){
        } else{
            setGPS_ON();
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Handle permissions if not granted
            return;
        }

        LocationServices.getFusedLocationProviderClient(this)
                .getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();

                            setCurrentLocation(latitude + getRandom(), longitude + getRandom());

                        } else {
                            setBoolean_location(false);
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }

    public void setCurrentLocation(double lat, double lon){
        this.currentLocation = new LatLng(lat, lon);
        System.out.println("latitude: " + currentLocation.latitude);
        System.out.println("longitude: " + currentLocation.longitude);
    }

    public LatLng getCurrentLocation(){ return this.currentLocation; }

    public boolean isBoolean_location() {
        return boolean_location;
    }

    public void setBoolean_location(boolean boolean_location) {
        this.boolean_location = boolean_location;
    }

    private double getRandom(){
        double rangeMin = 0.001, rangeMax = 0.003;
        Random r = new Random();
        int num = getInt();
        double randomValue = rangeMin + (rangeMax - rangeMin) * r.nextDouble();

        if(num==1){
            return randomValue * -1;
        }
        return randomValue;
    }

    private int getInt(){
        Random rand = new Random();
        int min = 0, max = 1;
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }

    public void findGeocoding(String address){
//        String address = "Λακεδαίμονος, Pl. Agiou Dimitriou &, Αμπελόκηποι 115 23";
        GeocodingTask geocodingTask = new GeocodingTask(this);
        geocodingTask.execute(address);
    }

    @Override
    public void onGeocodingSuccess(LatLng latLng) {
        // Handle successful geocoding here
        double latitude = latLng.latitude;
        double longitude = latLng.longitude;
        setCurrentLocation(latitude, longitude);

    }

    @Override
    public void onGeocodingFailure() {
        Toast.makeText(getApplicationContext(), "Fail to load your Address", Toast.LENGTH_SHORT).show();

    }

    @Override
    public CreateAdActivity getContext() {
        return this;
    }
}