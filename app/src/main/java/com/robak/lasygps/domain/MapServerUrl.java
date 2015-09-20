package com.robak.lasygps.domain;

/**
 * Created by Grzesiek on 20-09-2015.
 */
public abstract class MapServerUrl
{
    private static final String MAPSERVER_URL = "http://mapserver.bdl.lasy.gov.pl/arcgis/rest/services/BDL_2_0/MapServer/";

    // Service details:
    // http://mapserver.bdl.lasy.gov.pl/arcgis/rest/services/BDL_2_0/MapServer
    private static final String LP_FORESTS_SERVICE = "16";
    private static final String NOT_LP_FORESTS_SERVICE = "17";

    private static final String QUERY = "/query?where=&text=&objectIds=&time=&geometry=%s,+%s&geometryType=esriGeometryPoint&inSR=4326&spatialRel=esriSpatialRelIntersects&relationParam=&outFields=subarea_id,+arodes_int_num,+adress_forest,+area_type_cd,+site_type_cd,+silviculture_cd,+forest_func_cd,+stand_struct_cd,+rotation_age,+sub_area,+prot_category_cd,+species_cd_d,+part_cd,+species_age,+a_year&returnGeometry=false&maxAllowableOffset=&geometryPrecision=&outSR=4326&returnIdsOnly=false&returnCountOnly=false&orderByFields=&groupByFieldsForStatistics=&outStatistics=&returnZ=false&returnM=false&gdbVersion=&returnDistinctValues=false&f=pjson";

    final protected String getLpForestsUrl()
    {
        return MAPSERVER_URL + LP_FORESTS_SERVICE + QUERY;
    }

    final protected String getNotLpForestsUrl()
    {
        return MAPSERVER_URL + NOT_LP_FORESTS_SERVICE + QUERY;
    }
}
