package com.robak.lasygps.com.robak.lasygps.domain;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Grzesiek on 19-09-2015.
 */
public class ForestData
{
    private String forestAddress;

    private String nadlesnictwo;
    private String lesnictwo;
    private final String oddzial;
    private final String pododdzal;
    private final String areaSize;
    private final String treeCode;
    private final String treeAge;
    private final String dataAge;

    private final String arodes_int_num;

    public ForestData(JSONObject json) throws JSONException {
        JSONObject attributes = json.getJSONArray("features").getJSONObject(0).getJSONObject("attributes");
        this.forestAddress = attributes.getString("adress_forest");
        oddzial = forestAddress.split("-")[4].trim();
        pododdzal = forestAddress.split("-")[5].trim();
        areaSize = attributes.getString("sub_area");
        treeCode = attributes.getString("species_cd_d");
        treeAge = attributes.getString("species_age");
        dataAge = attributes.getString("a_year");
        arodes_int_num = attributes.getString("arodes_int_num");
    }

    public void setLesnictwo(String lesnictwo) {
        this.lesnictwo = lesnictwo.trim();
    }

    public String getForestAddress() {
        return forestAddress;
    }

    public String getLesnictwo() {
        return lesnictwo;
    }

    public String getArodes_int_num() {
        return arodes_int_num;
    }

    public String getNadlesnictwo() {
        return nadlesnictwo;
    }

    public void setNadlesnictwo(String nadlesnictwo) {
        this.nadlesnictwo = nadlesnictwo.trim();
    }

    public String getOddzial() {
        return oddzial;
    }

    public String getPododdzal() {
        return pododdzal;
    }

    public String getAreaSize() {
        return areaSize;
    }

    public String getTreeCode() {
        return treeCode;
    }

    public String getTreeAge() {
        return treeAge;
    }

    public String getDataAge() {
        return dataAge;
    }

}
