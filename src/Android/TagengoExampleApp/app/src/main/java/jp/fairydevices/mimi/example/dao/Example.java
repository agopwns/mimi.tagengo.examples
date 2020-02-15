package jp.fairydevices.mimi.example.dao;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Example {

    @SerializedName("boundingPoly")
    @Expose
    private BoundingPoly boundingPoly;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("locale")
    @Expose
    private String locale;

    public BoundingPoly getBoundingPoly() {
        return boundingPoly;
    }

    public void setBoundingPoly(BoundingPoly boundingPoly) {
        this.boundingPoly = boundingPoly;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

}