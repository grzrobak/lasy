package com.robak.lasygps.domain;

/**
 * Created by Grzesiek on 19-09-2015.
 */
public class ForestAddress
{
    private final String forestAddress;
    private final String[] distinctValues;

    private final int DIVISION_INDEX;
    private final int SUBDIVISION_INDEX;


    public ForestAddress(String forestAddress) {
        this.forestAddress = forestAddress;
        this.distinctValues = forestAddress.split("-");

        int lastIndex =  distinctValues.length - 1;
        DIVISION_INDEX = lastIndex-2;
        SUBDIVISION_INDEX = lastIndex-1;
    }

    public String getDivision()
    {
        return distinctValues[DIVISION_INDEX].trim();
    }

    public String getSubdivison()
    {
        return distinctValues[SUBDIVISION_INDEX].trim();
    }

    public String getRawValue()
    {
        return forestAddress;
    }

}
