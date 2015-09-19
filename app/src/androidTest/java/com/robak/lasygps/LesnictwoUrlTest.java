package com.robak.lasygps;

import android.app.Application;
import android.test.ActivityUnitTestCase;
import android.test.AndroidTestCase;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Grzesiek on 15-09-2015.
 */
public class LesnictwoUrlTest extends AndroidTestCase {

    @Test
    public void testGetUrl() throws Exception {
        LesnictwoUrl lesnictwoUrl = new LesnictwoUrl("1123028162",
                "2014",
                "11-23-1-09-574   -b   -00");



        assertEquals("http://www.bdl.lasy.gov.pl/portal/BULiGL.BDL.Reports/Report/StandDescriptionData?arodesIntNum=1123028162&aYear=2014&adress_forest=11-23-1-09-574%20%20%20-b%20%20%20-00&jointOwnership=false",
                lesnictwoUrl.getUrl());
    }
}