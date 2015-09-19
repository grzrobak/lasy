package com.robak.lasygps.domain;

/**
 * Created by Grzesiek on 19-09-2015.
 */
public class ForestAddress
{
    private final String forestAddress;
    private final String[] distinctValues;

    private final int ODDZIAL_INDEX;
    private final int PODODDZIAL_INDEX;


    public ForestAddress(String forestAddress) {
        this.forestAddress = forestAddress;
        this.distinctValues = forestAddress.split("-");

        int lastIndex =  distinctValues.length - 1;
        ODDZIAL_INDEX = lastIndex-2;
        PODODDZIAL_INDEX = lastIndex-1;
    }

    public String getOddzial()
    {
        return distinctValues[ODDZIAL_INDEX].trim();
    }

    public String getPododdzial()
    {
        return distinctValues[PODODDZIAL_INDEX].trim();
    }

    public String getRawValue()
    {
        return forestAddress;
    }

}
