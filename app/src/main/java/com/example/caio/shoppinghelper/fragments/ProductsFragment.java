package com.example.caio.shoppinghelper.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import com.example.caio.shoppinghelper.R;
import com.example.caio.shoppinghelper.activities.MainActivity;
import com.example.caio.shoppinghelper.helper.Base64Custom;
import com.example.caio.shoppinghelper.interfaces.DataSenderInterFace;
import com.example.caio.shoppinghelper.adapter.ProductAdapter;
import com.example.caio.shoppinghelper.config.FirebaseConfig;
import com.example.caio.shoppinghelper.helper.Preferences;
import com.example.caio.shoppinghelper.model.Products;
import com.example.caio.shoppinghelper.model.TotalProduct;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProductsFragment extends Fragment {

    private ProductAdapter adapter;
    private ArrayList<Products> products;
    private ArrayList<Products> productsSort = null;
    private ArrayList<Products> auxList;
    private HashMap<String , Integer> productsID;
    private ListView listView;

    private Products productToBeRemoved;

    private DatabaseReference firebase;
    private ValueEventListener valueEventListenerProducts;

    private int idProductPosition;

    DataSenderInterFace send;

    int textlength = 0;

    public ProductsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        products = new ArrayList<>();
        productsID = new HashMap<>();

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_products, container, false);
        listView = (ListView) view.findViewById(R.id.lv_products);


        adapter = new ProductAdapter(getActivity(), products);
        listView.setDividerHeight(1);
        listView.setAdapter(adapter);


        valueEventListenerProducts = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                float totalToPay = 0;

                //products.clear();
                //products_sort.clear();

                for(DataSnapshot data : dataSnapshot.getChildren()){

                    Products product = data.getValue(Products.class);

                    int indexProducts = -1;

                    if(productsID.containsKey(product.getIdProduct())){

                        indexProducts = productsID.get(product.getIdProduct());}


                    if (indexProducts != -1){

                        if (products.get(indexProducts).getQtd().equals(product.getQtd())){
                            //Do nothing

                        }else {

                            products.set(indexProducts, product);
                        }

                    }else {

                        products.add(product);
                        productsID.put(product.getIdProduct() , products.indexOf(product));
                    }


                    String sumFloat = product.getProductPrice().substring(1, product.getProductPrice().length());
                    totalToPay += (Float.parseFloat(sumFloat) * product.getQtd());

                }

                updateTotal(totalToPay);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                ArrayList<Products> auxSelectedList;

                if (productsSort == null){

                    auxSelectedList = products;
                }else{

                    auxSelectedList = productsSort;
                }

                Bundle bundle = new Bundle();
                bundle.putString("name" , auxSelectedList.get(position).getName());
                bundle.putString("id_product" , auxSelectedList.get(position).getIdProduct());
                send.sendData(bundle);

            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           final int position, long arg3) {

                View spinView = getLayoutInflater().inflate(R.layout.dialog_spinner, null);
                View normalView = getLayoutInflater().inflate(R.layout.dialog_normal, null);
                View usedView;
                final Spinner mSpinner = (Spinner) spinView.findViewById(R.id.spinner);
                productToBeRemoved = null;
                idProductPosition = position;

                int productsQtd;
                auxList = new ArrayList<>();

                if (productsSort == null){

                    productsQtd = products.get(idProductPosition).getQtd();
                    auxList = products;
                }else{

                    productsQtd = productsSort.get(idProductPosition).getQtd();
                    auxList = productsSort;
                }

                if(productsQtd > 1) {

                    ArrayAdapter<String> adapter;
                    List<String> list = new ArrayList<>();
                    list.add("Choose how many items...");
                    list.add("1 item");

                    for (int i = 2 ; i <= productsQtd; i++){

                        list.add(i + " items");
                    }

                    adapter = new ArrayAdapter<>(getActivity(),
                            android.R.layout.simple_spinner_item,
                            list);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    mSpinner.setAdapter(adapter);
                    usedView = spinView;

                }else{

                    usedView = normalView;
                }

                final AlertDialog alertDialog = new AlertDialog.Builder(getActivity() , AlertDialog.THEME_DEVICE_DEFAULT_LIGHT )
                        .setView(usedView)
                        .setTitle("Delete item")
                        .setMessage("Are you sure you want to delete this item?")
                        .setIcon(R.drawable.ic_remove_shopping_cart)
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Do nothing here
                            }
                        }).setNegativeButton("NO", null).create();


                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(final DialogInterface dialogInterface) {


                        Button positive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        Button negative = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

                        positive.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {


                                if (mSpinner.getSelectedItem() != null &&
                                        mSpinner.getSelectedItem().toString().equalsIgnoreCase("Choose how many items...")){

                                    Toast.makeText(getContext() ,
                                            "You must choose how many items you want to delete",
                                            Toast.LENGTH_LONG).show();

                                }else{

                                    int productsQuantity = auxList.get(idProductPosition).getQtd();
                                    int productsSubtract = 0;

                                    if (mSpinner.getSelectedItem() != null) productsSubtract = mSpinner.getSelectedItemPosition();


                                    Preferences preferences = new Preferences(getActivity());
                                    String userIdent = preferences.getIdentifier();

                                    String idProduct = auxList.get(idProductPosition).getIdProduct();

                                    if(productsSubtract > 0 && productsSubtract < productsQuantity) {

                                        //Products object
                                        Products productsObject = new Products();
                                        productsObject.setIdProduct(auxList.get(idProductPosition).getIdProduct());
                                        productsObject.setName(auxList.get(idProductPosition).getName());
                                        productsObject.setProductPrice(auxList.get(idProductPosition).getProductPrice());

                                        productsQuantity -= productsSubtract;
                                        productsObject.setQtd(productsQuantity);
                                        auxList.get(idProductPosition).setQtd(productsQuantity);

                                        //Change value
                                        firebase = FirebaseConfig.getFirebase();
                                        firebase.child("products_user")
                                                .child(userIdent)
                                                .child(idProduct)
                                                .setValue(productsObject);
                                    }else{

                                        productToBeRemoved = auxList.get(idProductPosition);
                                        adapter.remove(auxList.get(idProductPosition));

                                        //Remove from database
                                        firebase = FirebaseConfig.getFirebase();
                                        firebase.child("products_user")
                                                .child(userIdent)
                                                .child(idProduct)
                                                .removeValue();

                                    }


                                    if (productsSort == null){

                                        products = auxList;
                                        productsID.clear();

                                        for (Products productsList : products) {

                                            productsID.put(productsList.getIdProduct(), products.indexOf(productsList));
                                        }

                                    }else{

                                        productsSort = auxList;

                                        if (productToBeRemoved != null) {

                                            products.remove(productToBeRemoved);
                                            productsID.clear();

                                            for (Products productsList : products) {

                                                productsID.put(productsList.getIdProduct(), products.indexOf(productsList));
                                            }
                                        }

                                    }

                                    alertDialog.dismiss();

                                }
                            }
                        });

                        negative.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                Toast.makeText(getActivity(), "You clicked on NO", Toast.LENGTH_SHORT).show();
                                dialogInterface.dismiss();
                            }
                        });

                    }
                });

                alertDialog.show();

                //Don't call onClick
                return true;

            }

        });


        ((MainActivity)getActivity()).setOnFilterSelected(new MainActivity.listviewFilter() {
            @Override
            public void onFilterSelect(String typedWord) {

                textlength = typedWord.length();
                productsSort = new ArrayList<>();

                for(int i=0; i < products.size() ; i++ ){
                    if(textlength <= products.get(i).getName().length()){
                        Log.d("Sort" , products.get(i).getName().toLowerCase().trim());
                        if(products.get(i).getName().toLowerCase().trim().contains(
                                typedWord.toLowerCase().trim())){

                            productsSort.add(products.get(i));
                        }
                    }
                }

                if (textlength == 0){


                    products = productsSort;
                    productsSort = null;
                    productsID.clear();


                    for (Products productsList : products){

                        productsID.put(productsList.getIdProduct() , products.indexOf(productsList));
                    }

                    adapter = new ProductAdapter(getActivity() , products);
                    listView.setAdapter(adapter);
                }else {

                    adapter = new ProductAdapter(getActivity(), productsSort);
                    listView.setAdapter(adapter);
                }

            }
        });

        return view;
    }

    public void updateTotal(Float totalBuy){

        //Preferences preferences = new Preferences(getActivity());
        //String userIdent = preferences.getIdentifier();

        FirebaseAuth auth;
        auth = FirebaseConfig.getFirebaseAuth();
        String emailUser = auth.getCurrentUser().getEmail();
        String encodeUserEmail = Base64Custom.encodeBase64(emailUser);

        String formatFloat = String.format("%.2f" , totalBuy);

        TotalProduct totalProduct = new TotalProduct();
        totalProduct.setTotalProduct(formatFloat);

        firebase = FirebaseConfig.getFirebase();
        firebase.child("products_user")
                .child("total")
                .child(encodeUserEmail)
                .setValue(totalProduct);
    }


    @Override
    public void onResume() {
        super.onResume();

        Preferences preferences = new Preferences(getActivity());
        String userIdent = preferences.getIdentifier();
        firebase = FirebaseConfig.getFirebase();
        firebase.child("products_user")
                .child(userIdent)
                .addValueEventListener( valueEventListenerProducts );
    }

    @Override
    public void onPause() {
        super.onPause();
        firebase.removeEventListener( valueEventListenerProducts );
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        send = (DataSenderInterFace) getActivity();
    }

}