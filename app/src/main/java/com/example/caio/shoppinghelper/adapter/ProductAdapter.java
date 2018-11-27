package com.example.caio.shoppinghelper.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import com.example.caio.shoppinghelper.R;
import com.example.caio.shoppinghelper.model.Products;

public class ProductAdapter extends ArrayAdapter<Products>  {

    private ArrayList<Products> products;
    private Context context;

    //private int lastPosition = 0;


    public ProductAdapter(@NonNull Context c, ArrayList<Products> objects) {

        super(c ,0 ,objects);
        this.products = objects;
        this.context = c;

    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View view = null;

        //Check if list is empty
        if(products != null){

            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);

            //View from xml
            view = layoutInflater.inflate(R.layout.list_products , parent , false);

            TextView productName = (TextView) view.findViewById(R.id.pr_name);
            TextView productPrice = (TextView) view.findViewById(R.id.pr_price);
            TextView qtdProducts = (TextView) view.findViewById(R.id.pr_qtd_products);
            TextView productEach = (TextView) view.findViewById(R.id.pr_qtd_products_each);


            Products product = this.products.get(position);
            productName.setText(product.getName());
            String multFloat = product.getProductPrice().substring(1 , product.getProductPrice().length());
            Float eachTotal = Float.parseFloat(multFloat) * product.getQtd();

            String formatFloat = String.format("%.2f" ,eachTotal);

            productPrice.setText("$" + formatFloat);
            qtdProducts.setText("Qtd : " + product.getQtd().toString());
            productEach.setText("Each : " + product.getProductPrice());


            /*
            if (position == lastPosition){

                Animation animation = AnimationUtils.loadAnimation(getContext(),R.anim.slide_right_in);
                view.startAnimation(animation);

                if (convertView == null) lastPosition = this.products.size();
            }
            */

        }

        return view;
    }

    @Override
    public void remove(@Nullable Products object) {

        //lastPosition = this.products.size();
        super.remove(object);

    }

}
