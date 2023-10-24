package com.skyapi.weatherforecast.realtime;

import com.skyapi.weatherforecast.common.RealtimeWeather;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RealtimeWeatherRepository extends JpaRepository<RealtimeWeather, String> {

    @Query("SELECT r FROM RealtimeWeather r WHERE r.location.countryCode = ?1 AND r.location.cityName = ?2")
    RealtimeWeather findByCountryCodeAndCityName(String countryCode, String cityName);

    @Query("SELECT r FROM RealtimeWeather r WHERE r.location.code = ?1 AND r.location.trashed = FALSE")
    RealtimeWeather findByLocationCode(String locationCode);
}
