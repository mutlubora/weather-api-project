package com.skyapi.weatherforecast.full;

import com.skyapi.weatherforecast.common.DailyWeather;
import com.skyapi.weatherforecast.common.HourlyWeather;
import com.skyapi.weatherforecast.common.Location;
import com.skyapi.weatherforecast.common.RealtimeWeather;
import com.skyapi.weatherforecast.location.LocationNotFoundException;
import com.skyapi.weatherforecast.location.LocationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FullWeatherService {
    private final LocationRepository locationRepository;

    public FullWeatherService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public Location getByLocation(Location locationFromIP) {
        String countryCode = locationFromIP.getCountryCode();
        String cityName = locationFromIP.getCityName();

        Location locationInDB = locationRepository.findByCountryCodeAndCityName(countryCode, cityName);

        if (locationInDB == null) {
            throw new LocationNotFoundException(countryCode, cityName);
        }

        return locationInDB;
    }

    public Location getByLocationCode(String locationCode) {
        Location location = locationRepository.findByCode(locationCode);

        if (location == null) {
            throw new LocationNotFoundException(locationCode);
        }
        return location;
    }

    public Location update(String locationCode, Location locationInRequest) {
        Location locationInDB = locationRepository.findByCode(locationCode);

        if (locationInDB == null) {
            throw new LocationNotFoundException(locationCode);
        }

        RealtimeWeather realtimeWeather = locationInRequest.getRealtimeWeather();
        realtimeWeather.setLocation(locationInDB);

        List<DailyWeather> listDailyWeather = locationInRequest.getListDailyWeather();
        listDailyWeather.forEach(dw -> dw.getId().setLocation(locationInDB));

        List<HourlyWeather> listHourlyWeather = locationInRequest.getListHourlyWeather();
        listHourlyWeather.forEach(hw -> hw.getId().setLocation(locationInDB));

        locationInRequest.setCode(locationInDB.getCode());
        locationInRequest.setCityName(locationInDB.getCityName());
        locationInRequest.setRegionName(locationInDB.getRegionName());
        locationInRequest.setCountryCode(locationInDB.getCountryCode());
        locationInRequest.setCountryName(locationInDB.getCountryName());
        locationInRequest.setEnabled(locationInDB.isEnabled());
        locationInRequest.setTrashed(locationInDB.isTrashed());

        return locationRepository.save(locationInRequest);
    }
}
