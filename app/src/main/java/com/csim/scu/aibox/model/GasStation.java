package com.csim.scu.aibox.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by kenny on 2018/10/24.
 */

public abstract class GasStation {

    @Expose
    @SerializedName("保養間時間")
    private String 保養間時間;
    @Expose
    @SerializedName("etag儲值時間")
    private String etag儲值時間;
    @Expose
    @SerializedName("洗車類別")
    private String 洗車類別;
    @Expose
    @SerializedName("營業時間")
    private String 營業時間;
    @Expose
    @SerializedName("緯度")
    private double 緯度;
    @Expose
    @SerializedName("經度")
    private double 經度;
    @Expose
    @SerializedName("HappyCash")
    private String HappyCash;
    @Expose
    @SerializedName("一卡通")
    private String 一卡通;
    @Expose
    @SerializedName("悠遊卡")
    private String 悠遊卡;
    @Expose
    @SerializedName("電子發票")
    private String 電子發票;
    @Expose
    @SerializedName("自助柴油站")
    private String 自助柴油站;
    @Expose
    @SerializedName("刷卡自助")
    private String 刷卡自助;
    @Expose
    @SerializedName("會員卡")
    private String 會員卡;
    @Expose
    @SerializedName("超柴")
    private String 超柴;
    @Expose
    @SerializedName("煤油")
    private int 煤油;
    @Expose
    @SerializedName("酒精汽油")
    private String 酒精汽油;
    @Expose
    @SerializedName("無鉛98")
    private String 無鉛98;
    @Expose
    @SerializedName("無鉛95")
    private String 無鉛95;
    @Expose
    @SerializedName("無鉛92")
    private String 無鉛92;
    @Expose
    @SerializedName("國道高速公路")
    private int 國道高速公路;
    @Expose
    @SerializedName("營業中")
    private int 營業中;
    @Expose
    @SerializedName("服務中心")
    private String 服務中心;
    @Expose
    @SerializedName("電話")
    private String 電話;
    @Expose
    @SerializedName("地址")
    private String 地址;
    @Expose
    @SerializedName("鄉鎮區")
    private String 鄉鎮區;
    @Expose
    @SerializedName("縣市")
    private String 縣市;
    @Expose
    @SerializedName("站名")
    private String 站名;
    @Expose
    @SerializedName("類別")
    private String 類別;
    @Expose
    @SerializedName("站代號")
    private String 站代號;
    @Expose
    @SerializedName("_id")
    private _id _id;

    public String get保養間時間() {
        return 保養間時間;
    }

    public void set保養間時間(String 保養間時間) {
        this.保養間時間 = 保養間時間;
    }

    public String getEtag儲值時間() {
        return etag儲值時間;
    }

    public void setEtag儲值時間(String etag儲值時間) {
        this.etag儲值時間 = etag儲值時間;
    }

    public String get洗車類別() {
        return 洗車類別;
    }

    public void set洗車類別(String 洗車類別) {
        this.洗車類別 = 洗車類別;
    }

    public String get營業時間() {
        return 營業時間;
    }

    public void set營業時間(String 營業時間) {
        this.營業時間 = 營業時間;
    }

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

    public String getHappyCash() {
        return HappyCash;
    }

    public void setHappyCash(String HappyCash) {
        this.HappyCash = HappyCash;
    }

    public String get一卡通() {
        return 一卡通;
    }

    public void set一卡通(String 一卡通) {
        this.一卡通 = 一卡通;
    }

    public String get悠遊卡() {
        return 悠遊卡;
    }

    public void set悠遊卡(String 悠遊卡) {
        this.悠遊卡 = 悠遊卡;
    }

    public String get電子發票() {
        return 電子發票;
    }

    public void set電子發票(String 電子發票) {
        this.電子發票 = 電子發票;
    }

    public String get自助柴油站() {
        return 自助柴油站;
    }

    public void set自助柴油站(String 自助柴油站) {
        this.自助柴油站 = 自助柴油站;
    }

    public String get刷卡自助() {
        return 刷卡自助;
    }

    public void set刷卡自助(String 刷卡自助) {
        this.刷卡自助 = 刷卡自助;
    }

    public String get會員卡() {
        return 會員卡;
    }

    public void set會員卡(String 會員卡) {
        this.會員卡 = 會員卡;
    }

    public String get超柴() {
        return 超柴;
    }

    public void set超柴(String 超柴) {
        this.超柴 = 超柴;
    }

    public int get煤油() {
        return 煤油;
    }

    public void set煤油(int 煤油) {
        this.煤油 = 煤油;
    }

    public String get酒精汽油() {
        return 酒精汽油;
    }

    public void set酒精汽油(String 酒精汽油) {
        this.酒精汽油 = 酒精汽油;
    }

    public String get無鉛98() {
        return 無鉛98;
    }

    public void set無鉛98(String 無鉛98) {
        this.無鉛98 = 無鉛98;
    }

    public String get無鉛95() {
        return 無鉛95;
    }

    public void set無鉛95(String 無鉛95) {
        this.無鉛95 = 無鉛95;
    }

    public String get無鉛92() {
        return 無鉛92;
    }

    public void set無鉛92(String 無鉛92) {
        this.無鉛92 = 無鉛92;
    }

    public int get國道高速公路() {
        return 國道高速公路;
    }

    public void set國道高速公路(int 國道高速公路) {
        this.國道高速公路 = 國道高速公路;
    }

    public int get營業中() {
        return 營業中;
    }

    public void set營業中(int 營業中) {
        this.營業中 = 營業中;
    }

    public String get服務中心() {
        return 服務中心;
    }

    public void set服務中心(String 服務中心) {
        this.服務中心 = 服務中心;
    }

    public String get電話() {
        return 電話;
    }

    public void set電話(String 電話) {
        this.電話 = 電話;
    }

    public String get地址() {
        return 地址;
    }

    public void set地址(String 地址) {
        this.地址 = 地址;
    }

    public String get鄉鎮區() {
        return 鄉鎮區;
    }

    public void set鄉鎮區(String 鄉鎮區) {
        this.鄉鎮區 = 鄉鎮區;
    }

    public String get縣市() {
        return 縣市;
    }

    public void set縣市(String 縣市) {
        this.縣市 = 縣市;
    }

    public String get站名() {
        return 站名;
    }

    public void set站名(String 站名) {
        this.站名 = 站名;
    }

    public String get類別() {
        return 類別;
    }

    public void set類別(String 類別) {
        this.類別 = 類別;
    }

    public String get站代號() {
        return 站代號;
    }

    public void set站代號(String 站代號) {
        this.站代號 = 站代號;
    }

    public _id get_id() {
        return _id;
    }

    public void set_id(_id _id) {
        this._id = _id;
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
