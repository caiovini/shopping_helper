package com.example.caio.shoppinghelper.model;
import org.apache.commons.lang3.StringUtils;

public class Nutrition {

    private String nameNutrition;
    private String valueNutrition;

    public String getNameNutrition() {
        return nameNutrition;
    }

    public void setNameNutrition(String nameNutrition) {

        String  format = nameNutrition.replace("_"  , " " )
                .replace("{"  , ""  )
                .replace("\"" , ""  );

        this.nameNutrition = StringUtils.capitalize(format);
    }

    public String getValueNutrition() {
        return valueNutrition;
    }

    public void setValueNutrition(String valueNutrition) {

        this.valueNutrition = valueNutrition.replace("_" , " " )
                .replace("\"" , "");
    }
}
