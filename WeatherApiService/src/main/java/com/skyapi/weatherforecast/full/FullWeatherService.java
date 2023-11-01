package com.skyapi.weatherforecast.full;

import com.skyapi.weatherforecast.AbstractLocationService;
import com.skyapi.weatherforecast.common.DailyWeather;
import com.skyapi.weatherforecast.common.HourlyWeather;
import com.skyapi.weatherforecast.common.Location;
import com.skyapi.weatherforecast.common.RealtimeWeather;
import com.skyapi.weatherforecast.location.LocationNotFoundException;
import com.skyapi.weatherforecast.location.LocationRepository;
import com.skyapi.weatherforecast.realtime.RealtimeWeatherRepository;
import com.skyapi.weatherforecast.realtime.RealtimeWeatherService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class FullWeatherService extends AbstractLocationService {
    public FullWeatherService(LocationRepository locationRepository) {
        super();
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

    public Location update(String locationCode, Location locationInRequest) {
        Location locationInDB = locationRepository.findByCode(locationCode);

        if (locationInDB == null) {
            throw new LocationNotFoundException(locationCode);
        }

        setLocationForWeatherData(locationInRequest, locationInDB);

        saveRealtimeWeatherIfNotExistBefore(locationInRequest, locationInDB);

        locationInRequest.copyAllFieldsFrom(locationInDB);

        return locationRepository.save(locationInRequest);
    }

    private void saveRealtimeWeatherIfNotExistBefore(Location locationInRequest, Location locationInDB) {
        if (locationInDB.getRealtimeWeather() == null) {
            locationInDB.setRealtimeWeather(locationInRequest.getRealtimeWeather());
            locationRepository.save(locationInDB);
        }
    }

    private void setLocationForWeatherData(Location locationInRequest, Location locationInDB) {
        RealtimeWeather realtimeWeather = locationInRequest.getRealtimeWeather();
        realtimeWeather.setLocation(locationInDB);
        realtimeWeather.setLastUpdated(new Date());

        List<DailyWeather> listDailyWeather = locationInRequest.getListDailyWeather();
        listDailyWeather.forEach(dw -> dw.getId().setLocation(locationInDB));

        List<HourlyWeather> listHourlyWeather = locationInRequest.getListHourlyWeather();
        listHourlyWeather.forEach(hw -> hw.getId().setLocation(locationInDB));
    }
}
