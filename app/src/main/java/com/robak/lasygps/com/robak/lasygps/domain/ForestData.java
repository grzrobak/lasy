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
    private final int oddzial;
    private final String pododdzal;
    private final String areaSize;
    private final String treeCode;
    private final int treeAge;
    private final int dataAge;

    public ForestData(JSONObject json) throws JSONException {
        JSONObject attributes = json.getJSONArray("features").getJSONObject(0).getJSONObject("attributes");
        this.forestAddress = attributes.getString("adress_forest");
        oddzial = Integer.valueOf(forestAddress.split("-")[4]);
        pododdzal = forestAddress.split("-")[5];
        areaSize = attributes.getString("sub_area");
        treeCode = attributes.getString("species_cd_d");
        treeAge = attributes.getInt("species_age");
        dataAge = attributes.getInt("a_year");
    }

    public String getNadlesnictwo() {
        return nadlesnictwo;
    }

    public void setNadlesnictwo(String nadlesnictwo) {
        this.nadlesnictwo = nadlesnictwo;
    }

    public int getOddzial() {
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

    public int getTreeAge() {
        return treeAge;
    }

    public int getDataAge() {
        return dataAge;
    }

}
