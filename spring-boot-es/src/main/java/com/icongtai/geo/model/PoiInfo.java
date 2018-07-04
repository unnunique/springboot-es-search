package com.icongtai.geo.model;

/**
 * 地理位置逆编码单个poi 信息，单个点的相关信息
 */
public class PoiInfo{
    // poi 点的基本信息
    private GeoReGeoBaseInfo geoReGeoBaseInfo;
    // 店铺数
    private int shops;
    // 好评分
    private int goodComments;
    // 星级
    private int level;
    // 休闲类型
    private String otherType;
    // 公司规模
    private int numbers;
    // 均价
    private Double avgPrice;

    public int getShops() {
        return shops;
    }

    public void setShops(int shops) {
        this.shops = shops;
    }

    public int getGoodComments() {
        return goodComments;
    }

    public void setGoodComments(int goodComments) {
        this.goodComments = goodComments;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getOtherType() {
        return otherType;
    }

    public void setOtherType(String otherType) {
        this.otherType = otherType;
    }

    public int getNumbers() {
        return numbers;
    }

    public void setNumbers(int numbers) {
        this.numbers = numbers;
    }

    public GeoReGeoBaseInfo getGeoReGeoBaseInfo() {
        return geoReGeoBaseInfo;
    }

    public void setGeoReGeoBaseInfo(GeoReGeoBaseInfo geoReGeoBaseInfo) {
        this.geoReGeoBaseInfo = geoReGeoBaseInfo;
    }

    public Double getAvgPrice() {
        return avgPrice;
    }

    public void setAvgPrice(Double avgPrice) {
        this.avgPrice = avgPrice;
    }

    @Override
    public String toString() {
        return "PoiInfo{" +
                "geoReGeoBaseInfo=" + geoReGeoBaseInfo +
                ", shops=" + shops +
                ", goodComments=" + goodComments +
                ", level=" + level +
                ", otherType='" + otherType + '\'' +
                ", numbers=" + numbers +
                ", avgPrice=" + avgPrice +
                '}';
    }
}
