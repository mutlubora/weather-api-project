package com.skyapi.weatherforecast.realtime;

import com.skyapi.weatherforecast.GeolocationException;
import com.skyapi.weatherforecast.GeolocationService;
import com.skyapi.weatherforecast.common.Location;
import com.skyapi.weatherforecast.common.RealtimeWeather;
import com.skyapi.weatherforecast.daily.DailyWeatherController;
import com.skyapi.weatherforecast.full.FullWeatherController;
import com.skyapi.weatherforecast.hourly.HourlyWeatherController;
import com.skyapi.weatherforecast.location.LocationNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.skyapi.weatherforecast.CommonUtility.getIPAddress;

@RestController
@RequestMapping("/v1/realtime")
public class RealtimeWeatherController {
    private static final Logger LOGGER = LoggerFactory.getLogger(RealtimeWeatherController.class);
    private final RealtimeWeatherService realtimeWeatherService;
    private final GeolocationService geolocationService;
    private final ModelMapper modelMapper;
    public RealtimeWeatherController(RealtimeWeatherService realtimeWeatherService, GeolocationService geolocationService, ModelMapper modelMapper) {
        this.realtimeWeatherService = realtimeWeatherService;
        this.geolocationService = geolocationService;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    public ResponseEntity<?> getRealtimeWeatherByIPAddress(HttpServletRequest request) {
        String ipAddress = getIPAddress(request);
        try {
            Location locationFromIP = geolocationService.getLocation(ipAddress);
            RealtimeWeather realtimeWeather = realtimeWeatherService.getByLocation(locationFromIP);

            RealtimeWeatherDTO dto = entity2DTO(realtimeWeather);

            return ResponseEntity.ok(addLinksByIP(dto));

        } catch (GeolocationException e) {

            LOGGER.error(e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{locationCode}")
    public ResponseEntity<?> getRealtimeWeatherByLocationCode(
            @PathVariable("locationCode") String locationCode) {
        RealtimeWeather realtimeWeather = realtimeWeatherService.getByLocationCode(locationCode);

        RealtimeWeatherDTO dto = entity2DTO(realtimeWeather);

        return ResponseEntity.ok(addLinksByLocation(dto, locationCode));
    }

    @PutMapping("/{locationCode}")
    public ResponseEntity<?> updateRealtimeWeather(
            @PathVariable("locationCode") String locationCode,
            @RequestBody @Valid RealtimeWeatherDTO dto) {

        RealtimeWeather realtimeWeather = dto2Entity(dto);
        realtimeWeather.setLocationCode(locationCode);

        RealtimeWeather updatedRealtimeWeather = realtimeWeatherService
                .update(locationCode, realtimeWeather);
        RealtimeWeatherDTO updatedDto = entity2DTO(updatedRealtimeWeather);

        return ResponseEntity.ok(addLinksByLocation(updatedDto, locationCode));
    }

    private RealtimeWeatherDTO entity2DTO(RealtimeWeather updatedRealtimeWeather) {
        return modelMapper.map(updatedRealtimeWeather, RealtimeWeatherDTO.class);
    }

    private RealtimeWeather dto2Entity(RealtimeWeatherDTO dto) {
        return modelMapper.map(dto, RealtimeWeather.class);
    }

    private RealtimeWeatherDTO addLinksByIP(RealtimeWeatherDTO dto) {

        dto.add(linkTo(
                        methodOn(RealtimeWeatherController.class).getRealtimeWeatherByIPAddress(null))
                            .withSelfRel());
        dto.add(linkTo(
                        methodOn(HourlyWeatherController.class).listHourlyForecastByIPAddress(null))
                            .withRel("hourly_forecast"));
        dto.add(linkTo(
                methodOn(DailyWeatherController.class).listDailyForecastByIPAddress(null))
                .withRel("daily_forecast"));
        dto.add(linkTo(
                methodOn(FullWeatherController.class).getFullWeatherByIPAddress(null))
                .withRel("full_forecast"));
        return dto;
    }

    private RealtimeWeatherDTO addLinksByLocation(RealtimeWeatherDTO dto, String locationCode) {

        dto.add(linkTo(
                methodOn(RealtimeWeatherController.class).getRealtimeWeatherByLocationCode(locationCode))
                .withSelfRel());
        dto.add(linkTo(
                methodOn(HourlyWeatherController.class).listHourlyForecastByLocationCode(locationCode, null))
                .withRel("hourly_forecast"));
        dto.add(linkTo(
                methodOn(DailyWeatherController.class).listDailyForecastByLocationCode(locationCode))
                .withRel("daily_forecast"));
        dto.add(linkTo(
                methodOn(FullWeatherController.class).getFullWeatherByLocationCode(locationCode))
                .withRel("full_forecast"));
        return dto;
    }
}
