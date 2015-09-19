package com.robak.lasygps.domain;

/**
 * Created by Grzesiek on 19-09-2015.
 */
public class OddzialUrl
{
    private static final String url = "http://mapserver.bdl.lasy.gov.pl/arcgis/rest/services/BDL_2_0/MapServer/16/query?where=&text=&objectIds=&time=&geometry=%s,+%s&geometryType=esriGeometryPoint&inSR=4326&spatialRel=esriSpatialRelIntersects&relationParam=&outFields=subarea_id,+arodes_int_num,+adress_forest,+area_type_cd,+site_type_cd,+silviculture_cd,+forest_func_cd,+stand_struct_cd,+rotation_age,+sub_area,+prot_category_cd,+species_cd_d,+part_cd,+species_age,+a_year&returnGeometry=false&maxAllowableOffset=&geometryPrecision=&outSR=4326&returnIdsOnly=false&returnCountOnly=false&orderByFields=&groupByFieldsForStatistics=&outStatistics=&returnZ=false&returnM=false&gdbVersion=&returnDistinctValues=false&f=pjson";

    private final String latitude;
    private final String longitude;

    public OddzialUrl(String latitude, String longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getUrl()
    {
        return String.format(url, latitude, longitude);
    }
}
