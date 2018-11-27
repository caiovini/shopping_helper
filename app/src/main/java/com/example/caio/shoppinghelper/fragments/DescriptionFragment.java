package com.example.caio.shoppinghelper.fragments;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.example.caio.shoppinghelper.R;
import com.example.caio.shoppinghelper.activities.MainActivity;
import com.example.caio.shoppinghelper.adapter.NutritionAdapter;
import com.example.caio.shoppinghelper.model.Nutrition;
import com.google.common.base.Splitter;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;


public class DescriptionFragment extends Fragment {


    /*
       Maybe show expiry date in the future
       private TextView descriptionText;
       private StringBuilder concatenateString;

    */
    private String productName;
    private Context context;

    private NutritionAdapter adapter;
    private ArrayList<Nutrition> nutritions;
    private ListView listView;
    private ProgressBar progressBar;


    private Bundle bundle;

    public DescriptionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        context = getActivity();

        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_description, container, false);

        listView = (ListView) view.findViewById(R.id.lv_nutrition);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBarNutrition);


        nutritions = new ArrayList<>();
        adapter = new NutritionAdapter(getActivity() , nutritions);
        listView.setDividerHeight(1);
        listView.setAdapter(adapter);

        ((MainActivity) getActivity()).setOnBundleSelected(new MainActivity.SelectedBundle() {
            @Override
            public void onBundleSelect(Bundle bundleParameters) {

                bundle = bundleParameters;
                productName = bundle.getString("name");
                String idProduct = bundle.getString("id_product");

                //Ideally could be replaced by customer's API
                String sURL = "https://world.openfoodfacts.org/api/v0/product/" + idProduct + ".json";
                nutritions.clear();


                LoadListView loadListView = new LoadListView();
                loadListView.execute(sURL);
            }
        });

        return view;
    }

    class LoadListView extends AsyncTask<String, String, String> {


        @Override
        protected String doInBackground(String... strings) {


            DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());
            HttpPost httppost = new HttpPost(strings[0]);

            httppost.setHeader("Content-type", "application/json");

            InputStream inputStream = null;
            String jsonString = "";
            try {
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();

                inputStream = entity.getContent();

                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();

                String line;
                while ((line = reader.readLine()) != null)
                {
                    sb.append(line + "\n");
                }
                jsonString = sb.toString();
            } catch (Exception e) {

            }
            finally {
                try{if(inputStream != null)inputStream.close();}catch(Exception squish){}
            }

            JSONObject jsonObject;
            String productList = "";
            try {

                jsonObject = new JSONObject(jsonString);
                productList = jsonObject.getJSONObject("product").getString("nutriments");
            } catch (JSONException e) {

                e.printStackTrace();
            }

            return productList;
        }


        @Override
        protected void onPostExecute(String s) {

            if (s.isEmpty() || s.equals("{}")){

                Toast.makeText(getContext()  , "Product not found " , Toast.LENGTH_LONG).show();
            }else {

                Map<String, String> map = Splitter.on(",").withKeyValueSeparator(":").split(s);

                for (Map.Entry<String, String> entry : map.entrySet()) {

                    Nutrition nutrition = new Nutrition();
                    nutrition.setNameNutrition(entry.getKey());
                    nutrition.setValueNutrition(entry.getValue());
                    nutritions.add(nutrition);
                }

                adapter.notifyDataSetChanged();
            }

            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            progressBar.setVisibility(View.GONE);
        }

        @Override
        protected void onPreExecute() {


            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            progressBar.setVisibility(View.VISIBLE);

        }
    }
}
