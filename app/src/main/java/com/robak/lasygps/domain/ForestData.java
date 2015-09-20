package com.robak.lasygps.domain;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Grzesiek on 19-09-2015.
 */
public class ForestData
{
    private ForestAddress forestAddress;

    private String inspectorate;
    private String forestry;
    private final String division;
    private final String subdivision;
    private final String areaSize;
    private final String treeCode;
    private final String treeAge;
    private final String dataAge;

    private final String arodes_int_num;

    public ForestData(JSONObject json) throws JSONException {
        JSONObject attributes = json.getJSONArray("features").getJSONObject(0).getJSONObject("attributes");
        this.forestAddress = new ForestAddress(attributes.getString("adress_forest"));
        division = forestAddress.getDivision();
        subdivision = forestAddress.getSubdivison();
        areaSize = attributes.getString("sub_area");
        treeCode = attributes.getString("species_cd_d");
        treeAge = attributes.getString("species_age");
        dataAge = attributes.getString("a_year");
        arodes_int_num = attributes.getString("arodes_int_num");
    }

    public void setForestry(String forestry) {
        this.forestry = forestry.trim();
    }

    public ForestAddress getForestAddress() {
        return forestAddress;
    }

    public String getForestry() {
        return forestry;
    }

    public String getArodes_int_num() {
        return arodes_int_num;
    }

    public String getInspectorate() {
        return inspectorate;
    }

    public void setInspectorate(String inspectorate) {
        this.inspectorate = inspectorate.trim();
    }

    public String getDivision() {
        return division;
    }

    public String getSubdivision() {
        return subdivision;
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
