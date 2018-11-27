package com.example.caio.shoppinghelper.activities;



import com.bumptech.glide.Glide;
import com.example.caio.shoppinghelper.config.FirebaseConfig;
import android.app.Activity;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.design.widget.FloatingActionButton;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import com.example.caio.shoppinghelper.fragments.HomeFragment;
import com.example.caio.shoppinghelper.fragments.PaymentFragment;
import com.example.caio.shoppinghelper.fragments.SettingsFragment;
import com.example.caio.shoppinghelper.helper.Base64Custom;
import com.example.caio.shoppinghelper.helper.NetworkClient;
import com.example.caio.shoppinghelper.interfaces.ReloadAppInterface;
import com.example.caio.shoppinghelper.interfaces.UploadAPIs;
import com.example.caio.shoppinghelper.model.TotalProduct;
import com.example.caio.shoppinghelper.model.User;
import android.widget.TextView;
import android.widget.Toast;
import com.example.caio.shoppinghelper.R;
import com.example.caio.shoppinghelper.helper.Preferences;
import com.example.caio.shoppinghelper.interfaces.DataSenderInterFace;
import com.example.caio.shoppinghelper.model.Products;
import com.example.caio.shoppinghelper.model.RegisteredProducts;
import com.example.caio.shoppinghelper.monitor.NetworkBroadcast;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;


public class MainActivity extends AppCompatActivity implements DataSenderInterFace , ReloadAppInterface{


    private Toolbar toolbar;
    private FirebaseAuth FireBaseUser;
    private DatabaseReference firebaseTotalProducts;
    private FloatingActionButton fab;


    private BarcodeDetector detector;

    private static final int RC_BARCODE_CAPTURE = 9001;
    private final int GALLERY_PICTURE = 1;
    private final int CAMERA_PICTURE = 2;
    private static final String TAG = "BarcodeMain";
    private Uri imageToUploadUri;

    private SearchView searchView;

    private ValueEventListener valueEventListenerTotalProducts;
    private ChangeTab direction;

    private DatabaseReference firebase;
    private Products prodUser;
    private Fragment selectedFragment;

    SelectedBundle selectedBundle;
    public listviewFilter typedWord;

    private String codeProduct;

    // urls to load navigation header background image
    // and profile image
    private static final String urlNavHeaderBg = "https://api.androidhive.info/images/nav-menu-header-bg.jpg";

    // index to identify current nav menu item
    public static int navItemIndex = 0;

    // tags used to attach the fragments
    private static final String TAG_HOME = "home";
    private static final String TAG_PAY = "pay";
    private static final String TAG_FRUIT_RECOGNIZER = "fruits";
    private static final String TAG_SETTINGS = "settings";
    private static final String TAG_OUT = "out";
    public static String CURRENT_TAG = TAG_HOME;

    // toolbar titles respected to selected nav menu item
    private String[] activityTitles;

    // flag to load home fragment when user presses back key
    private boolean shouldLoadHomeFragOnBackPress = true;
    private Handler mHandler;

    private NavigationView navigationView;
    private DrawerLayout drawer;
    private View navHeader;
    private ImageView imgNavHeaderBg, imgProfile;
    private TextView txtName, txtWebsite;
    private Snackbar snackbar;

    private boolean totalValueBiggerthanZero;
    private Integer qtdProducts;

    private NetworkBroadcast networkBroadcast = new NetworkBroadcast();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        detector = new BarcodeDetector.Builder(getApplicationContext())
                .setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE)
                .build();
        if (!detector.isOperational()) {
            Toast.makeText(this ,  "Could not set up the detector!" , Toast.LENGTH_SHORT).show();
            return;
        }


        FireBaseUser = FirebaseConfig.getFirebaseAuth();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Home");
        setSupportActionBar(toolbar);


        //Loading slide menu
        mHandler = new Handler();

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        navHeader = navigationView.getHeaderView(0);
        txtName = (TextView) navHeader.findViewById(R.id.name);
        txtWebsite = (TextView) navHeader.findViewById(R.id.website);
        imgNavHeaderBg = (ImageView) navHeader.findViewById(R.id.img_header_bg);



        activityTitles = getResources().getStringArray(R.array.nav_item_activity_titles);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                snackbar = Snackbar.make(view, "No new messages", Snackbar.LENGTH_LONG)
                        .setAction("Action", null);
                snackbar.show();
                //snackbar.dismiss();

            }
        });

        // load nav menu header data
        loadNavHeader();

        // initializing navigation menu
        setUpNavigationView();

        if (savedInstanceState == null) {
            navItemIndex = 0;
            CURRENT_TAG = TAG_HOME;
            loadHomeFragment();
        }

        valueEventListenerTotalProducts = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                TotalProduct totalProduct = dataSnapshot.getValue(TotalProduct.class);

                if (totalProduct != null){

                    showSnackBar(totalProduct.getTotalProduct());

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    /***
     * Load navigation menu header information
     * like background image, profile image
     * name, website, notifications action view (dot)
     ***/
    private void loadNavHeader() {
        // name, website

        FirebaseAuth auth = FirebaseConfig.getFirebaseAuth();
        String emailUser = auth.getCurrentUser().getEmail();
        String encodeUserEmail = Base64Custom.encodeBase64(emailUser);


        DatabaseReference mPostReference = FirebaseConfig.getFirebase().child("user").child(encodeUserEmail);

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                String userString = user.getName();
                if (user.getSecondName() != null) {userString += " " + user.getSecondName();}

                txtName.setText(userString);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
            }
        };
        mPostReference.addValueEventListener(postListener);

        txtWebsite.setText("www.shoppinghelper.info");

        // loading header background image
        Glide.with(this).load(urlNavHeaderBg)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imgNavHeaderBg);


        // showing dot next to settings label
        navigationView.getMenu().getItem(3).setActionView(R.layout.menu_dot);
    }

    /***
     * Returns respected fragment that user
     * selected from navigation menu
     */
    private void loadHomeFragment() {
        // selecting appropriate nav menu item
        selectNavMenu();

        // set toolbar title
        setToolbarTitle();

        // if user select the current navigation menu again, don't do anything
        // just close the navigation drawer
        if (getSupportFragmentManager().findFragmentByTag(CURRENT_TAG) != null) {
            drawer.closeDrawers();

            // show or hide the fab button
            toggleFab();
            return;
        }


        /*
           Sometimes, when fragment has huge data, screen seems hanging
           when switching between navigation menus
           So using runnable, the fragment is loaded with cross fade effect
           This effect can be seen in GMail app
        */

        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                // update the main content by replacing fragments

                Fragment fragment = getHomeFragment();

                if (fragment != null){
                    android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                            android.R.anim.fade_out);
                    fragmentTransaction.replace(R.id.frame, fragment, CURRENT_TAG);
                    fragmentTransaction.commitAllowingStateLoss();
                }

            }
        };


        // If mPendingRunnable is not null, then add to the message queue
        if (mPendingRunnable != null) {
            mHandler.post(mPendingRunnable);
        }

        // show or hide the fab button
        toggleFab();

        //Closing drawer on item click
        drawer.closeDrawers();

        // refresh toolbar menu
        invalidateOptionsMenu();
    }

    private Fragment getHomeFragment() {

        selectedFragment = null;

        switch (navItemIndex) {
            case 0:

                // home
                selectedFragment = new HomeFragment();
                return selectedFragment;
            case 1:

                // payment fragment
                if  (totalValueBiggerthanZero){

                    selectedFragment = new PaymentFragment();
                }else{

                    selectedFragment = new  HomeFragment();}
                return selectedFragment;
            case 2:

                // settings fragment
                selectedFragment = new SettingsFragment();
                return selectedFragment;
            case 3:

                return selectedFragment;
            default:

                selectedFragment = new  HomeFragment();
                return selectedFragment;
        }
    }

    private void setToolbarTitle() {

        getSupportActionBar().setTitle(activityTitles[navItemIndex]);
    }

    private void selectNavMenu() {

        navigationView.getMenu().getItem(navItemIndex).setChecked(true);
    }

    private void setUpNavigationView() {

        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {
                    //Replacing the main content with ContentFragment Which is our Inbox View;
                    case R.id.nav_home:

                        navItemIndex = 0;
                        CURRENT_TAG = TAG_HOME;
                        break;
                    case R.id.pay_navigation:

                        if (totalValueBiggerthanZero){

                            navItemIndex = 1;
                            CURRENT_TAG = TAG_PAY;
                        }else{

                            Toast.makeText(MainActivity.this , "Product list is empty" , Toast.LENGTH_SHORT).show();
                            navItemIndex = 0;
                            CURRENT_TAG = TAG_HOME;}

                        break;
                    case R.id.nav_settings:

                        navItemIndex = 2;
                        CURRENT_TAG = TAG_SETTINGS;
                        break;
                    case R.id.recognize_fruits:

                        /* Image from gallery
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        startActivityForResult(intent, GALLERY_PICTURE);
                        */

                        // Image from camera
                        Intent chooserIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        File f = new File(Environment.getExternalStorageDirectory(), "POST_IMAGE.jpg");
                        chooserIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                        imageToUploadUri = Uri.fromFile(f);
                        startActivityForResult(chooserIntent, CAMERA_PICTURE);


                        break;
                    case R.id.sign_out:

                        if (totalValueBiggerthanZero) {

                            final AlertDialog.Builder alertDialog =
                                    new AlertDialog.Builder(MainActivity.this ,
                                            android.app.AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                            alertDialog.setTitle("You have unpaid items");
                            alertDialog.setMessage("Are you sure you want to log out ?");


                            alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    //Remove from database
                                    Preferences preferences = new Preferences(MainActivity.this);
                                    String userIdent = preferences.getIdentifier();


                                    firebase = FirebaseConfig.getFirebase();
                                    firebase.child("products_user")
                                            .child(userIdent)
                                            .removeValue();

                                    logOutUser();

                                }
                            });

                            alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    navItemIndex = 0;
                                    CURRENT_TAG = TAG_HOME;
                                }
                            });

                            alertDialog.show();

                        }else{

                            logOutUser();}

                        break;

                    case R.id.nav_about_us:

                        // launch new intent instead of loading fragment
                        startActivity(new Intent(MainActivity.this, AboutUsActivity.class));
                        drawer.closeDrawers();
                        return true;
                    case R.id.nav_privacy_policy:

                        // launch new intent instead of loading fragment
                        startActivity(new Intent(MainActivity.this, PrivacyPolicyActivity.class));
                        drawer.closeDrawers();
                        return true;
                    default:
                        navItemIndex = 0;
                }

                //Checking if the item is in checked state or not, if not make it in checked state
                if (menuItem.isChecked()) {
                    menuItem.setChecked(false);
                } else {
                    menuItem.setChecked(true);
                }
                menuItem.setChecked(true);

                loadHomeFragment();

                return true;
            }
        });


        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.openDrawer, R.string.closeDrawer) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank
                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawer.addDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawers();
            return;
        }

        // This code loads home fragment when back key is pressed
        // when user is in other fragment than home
        if (shouldLoadHomeFragOnBackPress) {

            // checking if user is on other navigation menu
            // rather than home
            if (navItemIndex != 0) {
                navItemIndex = 0;
                CURRENT_TAG = TAG_HOME;
                loadHomeFragment();
                return;
            }
        }

        super.onBackPressed();
    }

    // show or hide the fab
    private void toggleFab() {

        /*
        Maybe use this in the future

        if (navItemIndex == 0)
            fab.show();
        else
            fab.hide();
       */

        fab.hide();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        if (navItemIndex == 0) {

            inflater.inflate(R.menu.menu_main, menu);
        }


        if (navItemIndex == 3) {

            inflater.inflate(R.menu.notifications, menu);
        }

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem searchViewMenuItem  = menu.findItem(R.id.search);

        if (searchViewMenuItem != null) {

            searchView = (SearchView) searchViewMenuItem.getActionView();
            ImageView v = (ImageView) searchView.findViewById(android.support.v7.appcompat.R.id.search_button);
            v.setImageResource(R.drawable.ic_search_white);


            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {

                    direction.onTabSelected(1);

                    typedWord.onFilterSelect(newText);
                    return true;
                }
            });
        }

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.add_item:
                takePicture();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void logOutUser(){

        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.putExtra("SOURCE" , "MainActivity");
        startActivity(intent);
        finish();
    }

    private void takePicture(){

        Intent intent = new Intent(this, BarcodeCaptureActivity.class);
        intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
        intent.putExtra(BarcodeCaptureActivity.UseFlash, false);
        intent.putExtra(BarcodeCaptureActivity.AutoCapture, true);
        startActivityForResult(intent, RC_BARCODE_CAPTURE);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);

                    codeProduct = barcode.displayValue;


                    Preferences preferences = new Preferences(MainActivity.this);
                    String userIdent = preferences.getIdentifier();

                    //Check if user has already scanned this product
                    checkQtdProduct(userIdent);

                    /*
                      Check if the database has the product previously registered
                      In case the product is registered datasnapshot will not be null

                      This part of the app can be replaced by an API given by the customer
                      so the product will be checked directly at the source and not at the firebase
                    */

                    firebase = FirebaseConfig.getFirebase();
                    firebase.child("products_code")
                            .child(codeProduct)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.getValue() != null) {

                                        // Might be replaced by the customer's API

                                        prodUser = new Products();
                                        RegisteredProducts registeredProducts;
                                        registeredProducts = dataSnapshot.getValue(RegisteredProducts.class);

                                        Preferences preferences = new Preferences(MainActivity.this);
                                        String userIdent = preferences.getIdentifier();

                                        if (qtdProducts == 0) qtdProducts = 1;


                                        prodUser.setName(registeredProducts.getName());
                                        prodUser.setIdProduct(registeredProducts.getId_product());
                                        prodUser.setProductPrice(registeredProducts.getPrice());
                                        prodUser.setQtd(qtdProducts);


                                        firebase = FirebaseConfig.getFirebase();
                                        firebase = firebase.child("products_user")
                                                .child(userIdent)
                                                .child(codeProduct);
                                        firebase.setValue(prodUser);

                                    } else {
                                        Toast.makeText(MainActivity.this, "Product not found", Toast.LENGTH_SHORT).show();
                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });


                } else {
                    Toast.makeText(this, R.string.barcode_failure, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "No barcode captured, intent data is null");

                }
            } else {
                Toast.makeText(this, R.string.barcode_error, Toast.LENGTH_SHORT).show();
            }
        }

        //Image from camera
        if (requestCode == CAMERA_PICTURE && resultCode == Activity.RESULT_OK) {


            checkImage(imageToUploadUri.getPath());
        }


        //Image from gallery
        if (requestCode == GALLERY_PICTURE && resultCode == Activity.RESULT_OK) {

            InputStream inputStream = null;
            Bitmap bitmap = null;


            try {

                inputStream = getContentResolver().openInputStream(data.getData());
                bitmap = BitmapFactory.decodeStream(inputStream);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

                Uri picUri = data.getData();

                final String encode = Base64.encodeToString(stream.toByteArray() , Base64.NO_WRAP);
                //checkImage(encode);


                String filePath = getPath(picUri);
                checkImage(filePath);


            } catch (FileNotFoundException ex) {

                ex.printStackTrace();

            } finally {


                if (inputStream != null) {

                    try {

                        inputStream.close();

                    } catch (IOException ex) {

                        ex.printStackTrace();
                    }
                }
            }
        }
    }


    @Override
    public void sendData(Bundle bundle) {

        //Hide keyboard
        View viewKeyboard = MainActivity.this.getCurrentFocus();
        if (viewKeyboard != null) {

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            try {

                imm.hideSoftInputFromWindow(viewKeyboard.getWindowToken(), 0);
            }catch (java.lang.NullPointerException ex){

                Log.e("Keyboard error" , ex.toString());
            }
        }

        selectedBundle.onBundleSelect(bundle);
        direction.onTabSelected(View.FOCUS_RIGHT);
    }

    //Call Login activity and finish main activity
    @Override
    public void reloadApp() {

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    //Transfer data to description
    public interface SelectedBundle {

        void onBundleSelect(Bundle bundle);
    }

    public void setOnBundleSelected(SelectedBundle selectedBundle) {

        this.selectedBundle = selectedBundle;
    }

    //Search on listView
    public interface listviewFilter {

        void onFilterSelect(String typedWord);
    }

    public void setOnFilterSelected(listviewFilter typedWord) {
        this.typedWord = typedWord;
    }

    public interface ChangeTab{

        void onTabSelected(int direction);
    }

    public void setOnTabChanged(ChangeTab parmDirection){
        this.direction = parmDirection;
    }

    private void showSnackBar(String totalValue){

        CharSequence setTextString;
        Menu menu = navigationView.getMenu();
        MenuItem textViewPay = menu.findItem(R.id.pay_navigation);
        String floatValue = totalValue.replaceAll("," , ".");

        //String sumFloat = totalValue.substring(2 , totalValue.length());
        if (Float.parseFloat(floatValue) == 0){

            totalValueBiggerthanZero = false;
            setTextString = "Pay";
            textViewPay.setTitle(setTextString);

        }else{

            totalValueBiggerthanZero = true;
            //Show total of the bill
            setTextString = "Pay total : $" + totalValue;
            textViewPay.setTitle(setTextString);
        }

    }
                                                    //64 encoded string
    private void checkImage(final String imagePath /* final String fruit */ ){


        /*
           Call an AI developed in python to analyze images
           The AI was developed using a repository of 40000 images from github (https://github.com/Horea94/Fruit-Images-Dataset),
           but it seems to not be fullfilling the requirements for this application
           this dataset of images must be improved in the future in order to achieve the customer's requirements
           and the must be trained to recognise different fruits from different angles and in different environments

           @fruit is the image converted to base64 string
           @image is the name of the image create on the server to be analyzed (Here I am using the userId which is unique)
        */

        /* Implementation with the multipart framework Retrofit*/

        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        UploadAPIs uploadAPIs = retrofit.create(UploadAPIs.class);

        //Create a file object using file path
        File file = new File(imagePath);

        // Create a request body with file and image media type
        RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"), file);

        // Create MultipartBody.Part using file request-body,file name and part name
        MultipartBody.Part part = MultipartBody.Part.createFormData("image", file.getName(), fileReqBody);

        //Create request body with text description and text media type
        RequestBody description = RequestBody.create(MediaType.parse("text/plain"), "image-type");


        final retrofit2.Call<ResponseBody> request = uploadAPIs.uploadImage(part , description);

        request.enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(retrofit2.Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {


                if (response.isSuccessful()) {

                    try {


                        JSONObject jsonObject = new JSONObject(response.body().string());
                        final String fruit = jsonObject.getString("label");

                        final AlertDialog.Builder alertDialog =
                                new AlertDialog.Builder(MainActivity.this,
                                        android.app.AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                        alertDialog.setTitle("Fruit confirmation");
                        alertDialog.setMessage("The fruit \"" + fruit + "\" is correct ?");


                        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                prodUser = new Products();

                                //Test
                                prodUser.setName(fruit);
                                prodUser.setIdProduct("041331026628");
                                prodUser.setProductPrice("$3.99");
                                prodUser.setQtd(2);

                                Preferences preferences = new Preferences(MainActivity.this);
                                String userIdent = preferences.getIdentifier();

                                firebase = FirebaseConfig.getFirebase();
                                firebase = firebase.child("products_user")
                                        .child(userIdent)
                                        .child(prodUser.getIdProduct());
                                firebase.setValue(prodUser);

                            }
                        });

                        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                //Do something

                            }
                        });

                        alertDialog.show();


                    } catch (JSONException ex) {

                        Log.i("Json error", ex.toString());
                    } catch (IOException e) {

                        e.printStackTrace();
                    }

                }else{


                    Toast.makeText(getApplicationContext() , response.message() , Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) {

                Toast.makeText(getApplicationContext() , "Error: " + t.toString() , Toast.LENGTH_LONG).show();
            }
        });

        /* Implementation with the 64 image encode

        StringRequest stringRequest = new StringRequest(StringRequest.Method.POST, Configure.REQUEST_IMAGE_ANALYSIS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try{

                    JSONObject jsonObject = new JSONObject(response);
                    final String fruit = jsonObject.getString("label");

                    final AlertDialog.Builder alertDialog =
                            new AlertDialog.Builder(MainActivity.this ,
                                    android.app.AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                    alertDialog.setTitle("Fruit confirmation");
                    alertDialog.setMessage("The fruit \"" + fruit + "\" is correct ?");


                    alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            prodUser = new Products();

                            prodUser.setName(fruit);
                            prodUser.setIdProduct("041331026628");
                            prodUser.setProductPrice("$3.99");
                            prodUser.setQtd(2);

                            Preferences preferences = new Preferences(MainActivity.this);
                            String userIdent = preferences.getIdentifier();

                            firebase = FirebaseConfig.getFirebase();
                            firebase = firebase.child("products_user")
                                    .child(userIdent)
                                    .child(prodUser.getIdProduct());
                            firebase.setValue(prodUser);

                        }
                    });

                    alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            //Do something

                        }
                    });

                    alertDialog.show();


                }catch(JSONException ex){

                    Log.i("Json error" , ex.toString());
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(getApplicationContext() , "Error: " + error.toString() , Toast.LENGTH_LONG).show();

            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String , String> params = new HashMap<>();
                params.put("image_encoded" , fruit);

                return params;
            }
        };

        String myTag = "req_ai_fruit_reader";
        AndroidLoginController.getmInstance().addToRequestQueue(stringRequest, myTag);

        */

    }

    private String getPath(Uri contentUri) {

        String[] proj = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(getApplicationContext(),    contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

    private void checkQtdProduct(String userIdent){

        qtdProducts = 1;

        firebase = FirebaseConfig.getFirebase();
        firebase.child("products_user")
                .child(userIdent)
                .child(codeProduct)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.getValue() != null){

                            prodUser = new Products();
                            prodUser = dataSnapshot.getValue(Products.class);
                            qtdProducts += prodUser.getQtd();

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseAuth auth = FirebaseConfig.getFirebaseAuth();

        if( auth.getCurrentUser() != null) {

            Preferences preferences = new Preferences(this);
            String userIdent = preferences.getIdentifier();
            firebaseTotalProducts = FirebaseConfig.getFirebase();
            firebaseTotalProducts.child("products_user")
                    .child("total")
                    .child(userIdent)
                    .addValueEventListener(valueEventListenerTotalProducts);

        }

    }

    @Override
    protected void onStop() {
        super.onStop();

        FirebaseAuth auth = FirebaseConfig.getFirebaseAuth();

        if( auth.getCurrentUser() != null) {

            firebaseTotalProducts.removeEventListener(valueEventListenerTotalProducts);

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilterConnection =
                new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(networkBroadcast , intentFilterConnection);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkBroadcast);
    }
}
