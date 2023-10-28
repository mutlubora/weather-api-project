package com.skyapi.weatherforecast.realtime;

import com.skyapi.weatherforecast.GeolocationException;
import com.skyapi.weatherforecast.GeolocationService;
import com.skyapi.weatherforecast.common.Location;
import com.skyapi.weatherforecast.common.RealtimeWeather;
import com.skyapi.weatherforecast.location.LocationNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

            return ResponseEntity.ok(entity2DTO(realtimeWeather));

        } catch (GeolocationException e) {

            LOGGER.error(e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{locationCode}")
    public ResponseEntity<?> getRealtimeWeatherByLocationCode(
            @PathVariable("locationCode") String locationCode) {
        RealtimeWeather realtimeWeather = realtimeWeatherService.getByLocationCode(locationCode);
        return ResponseEntity.ok(entity2DTO(realtimeWeather));
    }

    @PutMapping("/{locationCode}")
    public ResponseEntity<?> updateRealtimeWeather(
            @PathVariable("locationCode") String locationCode,
            @RequestBody @Valid RealtimeWeatherDTO dto) {

        RealtimeWeather realtimeWeather = dto2Entity(dto);
        realtimeWeather.setLocationCode(locationCode);

        RealtimeWeather updatedRealtimeWeather = realtimeWeatherService
                .update(locationCode, realtimeWeather);

        return ResponseEntity.ok(entity2DTO(updatedRealtimeWeather));
    }

    private RealtimeWeatherDTO entity2DTO(RealtimeWeather updatedRealtimeWeather) {
        return modelMapper.map(updatedRealtimeWeather, RealtimeWeatherDTO.class);
    }

    private RealtimeWeather dto2Entity(RealtimeWeatherDTO dto) {
        return modelMapper.map(dto, RealtimeWeather.class);
    }
}
