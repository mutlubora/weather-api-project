package com.skyapi.weatherforecast.base;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.skyapi.weatherforecast.daily.DailyWeatherController;
import com.skyapi.weatherforecast.full.FullWeatherController;
import com.skyapi.weatherforecast.hourly.HourlyWeatherController;
import com.skyapi.weatherforecast.location.LocationController;
import com.skyapi.weatherforecast.realtime.RealtimeWeatherController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {
    @GetMapping("/")
    public ResponseEntity<RootEntity> handleBaseURI() {
        return ResponseEntity.ok(createRootEntity());
    }

    private RootEntity createRootEntity() {
        RootEntity entity = new RootEntity();

        String locationsUrl = linkTo(methodOn(LocationController.class).listLocations()).toString();
        entity.setLocationsUrl(locationsUrl);

        String locationByCodeUrl = linkTo(methodOn(LocationController.class).getLocation(null)).toString();
        entity.setLocationByCodeUrl(locationByCodeUrl);

        String realtimeWeatherByIpUrl = linkTo(methodOn(RealtimeWeatherController.class).getRealtimeWeatherByIPAddress(null)).toString();
        entity.setRealtimeWeatherByIpUrl(realtimeWeatherByIpUrl);

        String realtimeWeatherByCodeUrl = linkTo(methodOn(RealtimeWeatherController.class).getRealtimeWeatherByLocationCode(null)).toString();
        entity.setRealtimeWeatherByCodeUrl(realtimeWeatherByCodeUrl);

        String hourlyForecastByIpUrl = linkTo(methodOn(HourlyWeatherController.class).listHourlyForecastByIPAddress(null)).toString();
        entity.setHourlyForecastByIpUrl(hourlyForecastByIpUrl);

        String hourlyForecastByCodeUrl = linkTo(methodOn(HourlyWeatherController.class).listHourlyForecastByLocationCode(null, null)).toString();
        entity.setHourlyForecastByCodeUrl(hourlyForecastByCodeUrl);

        String dailyForecastByIpUrl = linkTo(methodOn(DailyWeatherController.class).listDailyForecastByIPAddress( null)).toString();
        entity.setDailyForecastByIpUrl(dailyForecastByIpUrl);

        String dailyForecastByCodeUrl = linkTo(methodOn(DailyWeatherController.class).listDailyForecastByLocationCode( null)).toString();
        entity.setDailyForecastByCodeUrl(dailyForecastByCodeUrl);

        String fullWeatherByIpUrl = linkTo(methodOn(FullWeatherController.class).getFullWeatherByIPAddress( null)).toString();
        entity.setFullWeatherByIpUrl(fullWeatherByIpUrl);

        String fullWeatherByCodeUrl = linkTo(methodOn(FullWeatherController.class).getFullWeatherByLocationCode( null)).toString();
        entity.setFullWeatherByCodeUrl(fullWeatherByCodeUrl);

        return entity;
    }
}
