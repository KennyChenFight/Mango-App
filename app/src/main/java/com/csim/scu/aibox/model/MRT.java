package com.csim.scu.aibox.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by kenny on 2018/10/24.
 */

public abstract class MRT {

    @Expose
    @SerializedName("geometry")
    private Geometry geometry;
    @Expose
    @SerializedName("properties")
    private Properties properties;
    @Expose
    @SerializedName("type")
    private String type;
    @Expose
    @SerializedName("_id")
    private _id _id;

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public _id get_id() {
        return _id;
    }

    public void set_id(_id _id) {
        this._id = _id;
    }

    public static class Geometry {
        @Expose
        @SerializedName("coordinates")
        private List<Double> coordinates;
        @Expose
        @SerializedName("type")
        private String type;

        public List<Double> getCoordinates() {
            return coordinates;
        }

        public void setCoordinates(List<Double> coordinates) {
            this.coordinates = coordinates;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    public static class Properties {
        @Expose
        @SerializedName("緯度")
        private double 緯度;
        @Expose
        @SerializedName("經度")
        private double 經度;
        @Expose
        @SerializedName("出入口編號")
        private String 出入口編號;
        @Expose
        @SerializedName("出入口名稱")
        private String 出入口名稱;
        @Expose
        @SerializedName("項次")
        private int 項次;

        public double get緯度() {
            return 緯度;
        }

        public void set緯度(double 緯度) {
            this.緯度 = 緯度;
        }

        public double get經度() {
            return 經度;
        }

        public void set經度(double 經度) {
            this.經度 = 經度;
        }

        public String get出入口編號() {
            return 出入口編號;
        }

        public void set出入口編號(String 出入口編號) {
            this.出入口編號 = 出入口編號;
        }

        public String get出入口名稱() {
            return 出入口名稱;
        }

        public void set出入口名稱(String 出入口名稱) {
            this.出入口名稱 = 出入口名稱;
        }

        public int get項次() {
            return 項次;
        }

        public void set項次(int 項次) {
            this.項次 = 項次;
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
