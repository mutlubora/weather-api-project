package com.skyapi.weatherforecast.common;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "locations")
public class Location {

    @Id
    @Column(length = 12, nullable = false, unique = true)
    private String code;

    @Column(length = 128, nullable = false)
    private String cityName;

    @Column(length = 128)
    private String regionName;

    @Column(length = 64, nullable = false)
    private String countryName;

    @Column(length = 2, nullable = false)
    private String countryCode;

    private boolean enabled;
    private boolean trashed;

    @OneToOne(mappedBy = "location", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private RealtimeWeather realtimeWeather;

    @OneToMany(mappedBy = "id.location", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HourlyWeather> listHourlyWeather = new ArrayList<>();

    @OneToMany(mappedBy = "id.location", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DailyWeather> listDailyWeather = new ArrayList<>();

    public Location() {}

    public Location(String cityName, String regionName, String countryName, String countryCode) {
        this.cityName = cityName;
        this.regionName = regionName;
        this.countryName = countryName;
        this.countryCode = countryCode;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isTrashed() {
        return trashed;
    }

    public void setTrashed(boolean trashed) {
        this.trashed = trashed;
    }

    public RealtimeWeather getRealtimeWeather() {
        return realtimeWeather;
    }

    public void setRealtimeWeather(RealtimeWeather realtimeWeather) {
        this.realtimeWeather = realtimeWeather;
    }

    public List<HourlyWeather> getListHourlyWeather() {
        return listHourlyWeather;
    }

    public void setListHourlyWeather(List<HourlyWeather> listHourlyWeather) {
        this.listHourlyWeather = listHourlyWeather;
    }

    public List<DailyWeather> getListDailyWeather() {
        return listDailyWeather;
    }

    public void setListDailyWeather(List<DailyWeather> listDailyWeather) {
        this.listDailyWeather = listDailyWeather;
    }

    public void copyFieldsFrom(Location another) {
        setCountryCode(another.getCountryCode());
        setCountryName(another.getCountryName());
        setRegionName(another.getRegionName());
        setCityName(another.getCityName());
        setEnabled(another.isEnabled());
    }

    public void copyAllFieldsFrom(Location another) {
        copyFieldsFrom(another);
        setCode(another.getCode());
        setTrashed(another.isTrashed());
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Location location)) return false;
        return Objects.equals(getCode(), location.getCode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCode());
    }

    @Override
    public String toString() {
        return cityName  + (regionName != null ?  ", " + regionName : "") + ", " + countryName;
    }

    public Location code(String code) {
        setCode(code);
        return this;
    }


}
