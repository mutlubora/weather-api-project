package com.skyapi.weatherforecast.hourly;

import com.skyapi.weatherforecast.common.HourlyWeather;
import com.skyapi.weatherforecast.common.HourlyWeatherId;
import com.skyapi.weatherforecast.common.Location;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback(value = false)
public class HourlyWeatherRepositoryTests {

    @Autowired
    private HourlyWeaterRepository hourlyWeaterRepository;

    @Test
    public void testAddHourlyWeather() {
        String locationCode = "DELHI_IN";
        int hourOfDay = 12;

        Location location = new Location().code(locationCode);

        HourlyWeather forecast = new HourlyWeather()
                .id(location, hourOfDay)
                .temperature(13)
                .precipitation(70)
                .status("Cloudy");

        HourlyWeather updatedForecast = hourlyWeaterRepository.save(forecast);

        assertThat(updatedForecast.getId().getLocation().getCode()).isEqualTo(locationCode);
        assertThat(updatedForecast.getId().getHourOfDay()).isEqualTo(hourOfDay);

    }
    @Test
    public void testDeleteHourlyWeather() {
        Location location = new Location().code("DELHI_IN");
        HourlyWeatherId id = new HourlyWeatherId(10, location);
        hourlyWeaterRepository.deleteById(id);
        Optional<HourlyWeather> result = hourlyWeaterRepository.findById(id);

        assertThat(result).isNotPresent();

    }

    @Test
    public void testFindByLocationCodeFound() {
        String locationCode = "MBMH_IN";
        int currentHour = 7;

        List<HourlyWeather> hourlyForecast = hourlyWeaterRepository
                .findByLocationCode(locationCode, currentHour);

        assertThat(hourlyForecast).isNotEmpty();
    }
    @Test
    public void testFindByLocationCodeNotFound() {
        String locationCode = "DELHI_IN";
        int currentHour = 10;

        List<HourlyWeather> hourlyForecast = hourlyWeaterRepository
                .findByLocationCode(locationCode, currentHour);

        assertThat(hourlyForecast).isEmpty();
    }
}
