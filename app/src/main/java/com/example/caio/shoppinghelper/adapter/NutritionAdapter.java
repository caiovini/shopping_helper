package com.example.caio.shoppinghelper.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.example.caio.shoppinghelper.R;
import com.example.caio.shoppinghelper.model.Nutrition;
import java.util.ArrayList;

public class NutritionAdapter extends ArrayAdapter<Nutrition> {

    private ArrayList<Nutrition> nutritions;
    private Context context;

    public NutritionAdapter(@NonNull Context c, ArrayList<Nutrition> objects) {

        super(c ,0 ,objects);
        this.nutritions = objects;
        this.context = c;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View view = null;

        if(nutritions != null){

            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);

            //View from xml
            view = layoutInflater.inflate(R.layout.list_nutrition , parent , false);


            TextView nutritionName = (TextView) view.findViewById(R.id.nutrition_name);
            TextView nutritionValue = (TextView) view.findViewById(R.id.nutritional_value);


            Nutrition nutrition = this.nutritions.get(position);
            nutritionName.setText(nutrition.getNameNutrition());
            nutritionValue.setText(nutrition.getValueNutrition());

        }

        return view;
    }
}
