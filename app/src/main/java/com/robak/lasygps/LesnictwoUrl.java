package com.robak.lasygps;

import android.net.Uri;
import android.os.Parcel;
import android.support.annotation.NonNull;

import com.robak.lasygps.domain.ForestData;

import java.net.URI;
import java.net.URLEncoder;
import java.util.List;

/**
 * Created by Grzesiek on 15-09-2015.
 */
public class LesnictwoUrl
{
    private final static String url = "http://www.bdl.lasy.gov.pl/portal/BULiGL.BDL.Reports/Report/StandDescriptionData?arodesIntNum=%s&aYear=%s&adress_forest=%s&jointOwnership=false";

    private final String arodesIntNum;
    private final String aYear;
    private final String forrestAddress;

    public LesnictwoUrl(ForestData forestData) {
        this.arodesIntNum = forestData.getArodes_int_num();
        this.aYear = forestData.getDataAge();
        this.forrestAddress = forestData.getForestAddress().getRawValue();
    }

    public String getUrl()
    {
        return String.format(url,arodesIntNum,aYear,forrestAddress).replace(" ", "%20" );
    }
}
