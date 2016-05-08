package com.example.chris.situaware;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * Created by Chris on 08/05/2016.
 */
public class Incident {

    private int incidentCode;
    private int detailCode;
    private String incidentTime;
    private String latitude;
    private String longitude;

    public Incident(int icode, int dcode, String time, String lat, String lon) {
        this.incidentCode = icode;
        this.detailCode = dcode;
        this.incidentTime = time;
        this.latitude = lat;
        this.longitude = lon;
    }

    public int getIncidentType() {
        return incidentCode;
    }

    public String getIncidentDetail() {
        return "";
    }

    public String getIncidentTime() {
        return incidentTime;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }
}
