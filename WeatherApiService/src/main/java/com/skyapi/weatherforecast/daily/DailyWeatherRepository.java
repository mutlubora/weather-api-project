package com.skyapi.weatherforecast.daily;

import com.skyapi.weatherforecast.common.DailyWeather;
import com.skyapi.weatherforecast.common.DailyWeatherId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DailyWeatherRepository extends JpaRepository<DailyWeather, DailyWeatherId> {

    @Query("""
           SELECT d FROM DailyWeather d WHERE d.id.location.code = ?1\s
           AND d.id.location.trashed = FALSE
            """)
    List<DailyWeather> findByLocationCode(String locationCode);
}
