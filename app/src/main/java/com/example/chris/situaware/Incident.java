package com.example.chris.situaware;

import android.content.Context;

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
    private Context mContext;



    private String[] arr1 = {};

    public Incident(int icode, int dcode, String time, String lat, String lon, Context mContext) {
        this.incidentCode = icode;
        this.detailCode = dcode;
        this.incidentTime = time;
        this.latitude = lat;
        this.longitude = lon;
        this.mContext = mContext;
    }

    public int getIncidentType() {
        return incidentCode-1;
    }

    public String getIncidentDetail() {
        String[] details = {""};
        System.out.println("INCIDENT CODE: "+incidentCode);
        System.out.println("DETAIL CODE: "+detailCode);
        if(incidentCode==1) {
            details = mContext.getResources().getStringArray(R.array.accident_detail_array);
        }
        else if(incidentCode==2) {
            details = mContext.getResources().getStringArray(R.array.antisocial_detail_array);
        }
        else if(incidentCode==3) {
            details = mContext.getResources().getStringArray(R.array.burglary_detail_array);
        }
        else if(incidentCode==4) {
            details = mContext.getResources().getStringArray(R.array.explosion_detail_array);
        }
        else if(incidentCode==5) {
            details = mContext.getResources().getStringArray(R.array.fire_detail_array);
        }
        else if(incidentCode==6) {
            details = mContext.getResources().getStringArray(R.array.suspicious_detail_array);
        }
        else if(incidentCode==7) {
            details = mContext.getResources().getStringArray(R.array.theft_detail_array);
        }
        else if(incidentCode==8) {
            details = mContext.getResources().getStringArray(R.array.vandalism_detail_array);
        }
        else if(incidentCode==9) {
            details = mContext.getResources().getStringArray(R.array.violence_detail_array);
        }
        else if(incidentCode==10) {
            details = mContext.getResources().getStringArray(R.array.weapon_detail_array);
        }
        else if(incidentCode==11) {
            String[] strarr = {"first val", mContext.getString(R.string.app_name)};
            details = strarr;
        }
        return details[detailCode-1];
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
