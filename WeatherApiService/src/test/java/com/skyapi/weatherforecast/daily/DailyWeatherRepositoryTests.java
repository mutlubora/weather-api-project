package com.skyapi.weatherforecast.daily;

import com.skyapi.weatherforecast.common.DailyWeather;
import com.skyapi.weatherforecast.common.DailyWeatherId;
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
public class DailyWeatherRepositoryTests {

    @Autowired
    private DailyWeatherRepository dailyWeatherRepository;

    @Test
    public void testAdd() {
        String locationCode = "DANA_VN";

        Location location = new Location()
                .code(locationCode);

        DailyWeather forecast = new DailyWeather()
                .location(location)
                .dayOfMonth(16)
                .month(7)
                .minTemp(23)
                .maxTemp(32)
                .precipitation(40)
                .status("Cloudy");

        DailyWeather addedForecast = dailyWeatherRepository.save(forecast);

        assertThat(addedForecast.getId().getLocation().getCode()).isEqualTo(locationCode);
    }

    @Test
    public void testDelete() {
        String locationCode = "DELHI_IN";

        Location location = new Location()
                .code(locationCode);

        DailyWeatherId id = new DailyWeatherId(16, 7, location);

        dailyWeatherRepository.deleteById(id);

        Optional<DailyWeather> result = dailyWeatherRepository.findById(id);

        assertThat(result).isNotPresent();
    }

    @Test
    public void testFindByLocationCodeFound() {
        String locationCode = "DELHI_IN";

        List<DailyWeather> dailyWeathers = dailyWeatherRepository.findByLocationCode(locationCode);

        assertThat(dailyWeathers).isNotEmpty();
    }
    @Test
    public void testFindByLocationCodeNotFound() {
        String locationCode = "ABC";

        List<DailyWeather> dailyWeathers = dailyWeatherRepository.findByLocationCode(locationCode);

        assertThat(dailyWeathers).isEmpty();
    }
}
