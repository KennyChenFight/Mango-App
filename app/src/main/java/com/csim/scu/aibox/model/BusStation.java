package com.csim.scu.aibox.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by kenny on 2018/10/24.
 */

public abstract class BusStation {

    @Expose
    @SerializedName("VersionID")
    private int VersionID;
    @Expose
    @SerializedName("UpdateTime")
    private String UpdateTime;
    @Expose
    @SerializedName("LocationCityCode")
    private String LocationCityCode;
    @Expose
    @SerializedName("CityCode")
    private String CityCode;
    @Expose
    @SerializedName("City")
    private String City;
    @Expose
    @SerializedName("StationID")
    private String StationID;
    @Expose
    @SerializedName("Bearing")
    private String Bearing;
    @Expose
    @SerializedName("StopAddress")
    private String StopAddress;
    @Expose
    @SerializedName("StopPosition")
    private StopPosition StopPosition;
    @Expose
    @SerializedName("StopName")
    private StopName StopName;
    @Expose
    @SerializedName("AuthorityID")
    private String AuthorityID;
    @Expose
    @SerializedName("StopID")
    private String StopID;
    @Expose
    @SerializedName("StopUID")
    private String StopUID;
    @Expose
    @SerializedName("_id")
    private _id _id;

    public BusStation () {

    }

    public BusStation(int versionID, String updateTime, String locationCityCode, String cityCode, String city, String stationID, String bearing, String stopAddress, BusStation.StopPosition stopPosition, BusStation.StopName stopName, String authorityID, String stopID, String stopUID, BusStation._id _id) {
        VersionID = versionID;
        UpdateTime = updateTime;
        LocationCityCode = locationCityCode;
        CityCode = cityCode;
        City = city;
        StationID = stationID;
        Bearing = bearing;
        StopAddress = stopAddress;
        StopPosition = stopPosition;
        StopName = stopName;
        AuthorityID = authorityID;
        StopID = stopID;
        StopUID = stopUID;
        this._id = _id;
    }

    public int getVersionID() {
        return VersionID;
    }

    public void setVersionID(int VersionID) {
        this.VersionID = VersionID;
    }

    public String getUpdateTime() {
        return UpdateTime;
    }

    public void setUpdateTime(String UpdateTime) {
        this.UpdateTime = UpdateTime;
    }

    public String getLocationCityCode() {
        return LocationCityCode;
    }

    public void setLocationCityCode(String LocationCityCode) {
        this.LocationCityCode = LocationCityCode;
    }

    public String getCityCode() {
        return CityCode;
    }

    public void setCityCode(String CityCode) {
        this.CityCode = CityCode;
    }

    public String getCity() {
        return City;
    }

    public void setCity(String City) {
        this.City = City;
    }

    public String getStationID() {
        return StationID;
    }

    public void setStationID(String StationID) {
        this.StationID = StationID;
    }

    public String getBearing() {
        return Bearing;
    }

    public void setBearing(String Bearing) {
        this.Bearing = Bearing;
    }

    public String getStopAddress() {
        return StopAddress;
    }

    public void setStopAddress(String StopAddress) {
        this.StopAddress = StopAddress;
    }

    public StopPosition getStopPosition() {
        return StopPosition;
    }

    public void setStopPosition(StopPosition StopPosition) {
        this.StopPosition = StopPosition;
    }

    public StopName getStopName() {
        return StopName;
    }

    public void setStopName(StopName StopName) {
        this.StopName = StopName;
    }

    public String getAuthorityID() {
        return AuthorityID;
    }

    public void setAuthorityID(String AuthorityID) {
        this.AuthorityID = AuthorityID;
    }

    public String getStopID() {
        return StopID;
    }

    public void setStopID(String StopID) {
        this.StopID = StopID;
    }

    public String getStopUID() {
        return StopUID;
    }

    public void setStopUID(String StopUID) {
        this.StopUID = StopUID;
    }

    public _id get_id() {
        return _id;
    }

    public void set_id(_id _id) {
        this._id = _id;
    }

    public static class StopPosition {
        @Expose
        @SerializedName("PositionLon")
        private double PositionLon;
        @Expose
        @SerializedName("PositionLat")
        private double PositionLat;

        public double getPositionLon() {
            return PositionLon;
        }

        public void setPositionLon(double PositionLon) {
            this.PositionLon = PositionLon;
        }

        public double getPositionLat() {
            return PositionLat;
        }

        public void setPositionLat(double PositionLat) {
            this.PositionLat = PositionLat;
        }
    }

    public static class StopName {
        @Expose
        @SerializedName("En")
        private String En;
        @Expose
        @SerializedName("Zh_tw")
        private String Zh_tw;

        public String getEn() {
            return En;
        }

        public void setEn(String En) {
            this.En = En;
        }

        public String getZh_tw() {
            return Zh_tw;
        }

        public void setZh_tw(String Zh_tw) {
            this.Zh_tw = Zh_tw;
        }
    }

    public static class _id {
        @Expose
        @SerializedName("$oid")
        private String $oid;

        public String get$oid() {
            return $oid;
        }

        public void set$oid(String $oid) {
            this.$oid = $oid;
        }
    }
}
