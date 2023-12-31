package com.skyapi.weatherforecast.common;

import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class DailyWeatherId implements Serializable {
    private int dayOfMonth;
    private int month;

    @ManyToOne
    @JoinColumn(name = "location_code")
    private Location location;

    public DailyWeatherId() {
    }

    public DailyWeatherId(int dayOfMonth, int month, Location location) {
        this.dayOfMonth = dayOfMonth;
        this.month = month;
        this.location = location;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDayOfMonth(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DailyWeatherId that)) return false;
        return getDayOfMonth() == that.getDayOfMonth() && getMonth() == that.getMonth() && Objects.equals(getLocation(), that.getLocation());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDayOfMonth(), getMonth(), getLocation());
    }
}
