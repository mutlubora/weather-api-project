package com.skyapi.weatherforecast.full;

import com.skyapi.weatherforecast.BadRequestException;
import com.skyapi.weatherforecast.CommonUtility;
import com.skyapi.weatherforecast.GeolocationService;
import com.skyapi.weatherforecast.common.Location;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/full")
public class FullWeatherController {
    private final FullWeatherService fullWeatherService;
    private final GeolocationService geolocationService;
    private final ModelMapper modelMapper;

    public FullWeatherController(FullWeatherService fullWeatherService, GeolocationService geolocationService, ModelMapper modelMapper) {
        this.fullWeatherService = fullWeatherService;
        this.geolocationService = geolocationService;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    public ResponseEntity<?> getFullWeatherByIPAddress(HttpServletRequest request) {
        String ipAddress = CommonUtility.getIPAddress(request);
        
        Location locationFromIP = geolocationService.getLocation(ipAddress);
        Location locationInDB = fullWeatherService.getByLocation(locationFromIP);

        return ResponseEntity.ok(entity2DTO(locationInDB));
    }

    @GetMapping("/{locationCode}")
    public ResponseEntity<?> getFullWeatherByLocationCode(@PathVariable("locationCode") String locationCode) {

        Location locationInDB = fullWeatherService.getByLocationCode(locationCode);

        return ResponseEntity.ok(entity2DTO(locationInDB));
    }

    @PutMapping("/{locationCode}")
    public ResponseEntity<?> updateFullWeather(@PathVariable("locationCode") String locationCode,
                                               @RequestBody @Valid FullWeatherDTO dto) throws BadRequestException {
        if (dto.getListHourlyWeather().isEmpty()) {
            throw new BadRequestException("Hourly weather data cannot be empty.");
        }
        if (dto.getListDailyWeather().isEmpty()) {
            throw new BadRequestException("Daily weather data cannot be empty.");
        }
        Location locationInRequest = dto2Entity(dto);

        Location updatedLocation = fullWeatherService.update(locationCode, locationInRequest);

        return ResponseEntity.ok(entity2DTO(updatedLocation));
    }

    private FullWeatherDTO entity2DTO(Location location) {
        FullWeatherDTO dto = modelMapper.map(location, FullWeatherDTO.class);

        // Hide the location field in the realtime_weather object
        dto.getRealtimeWeather().setLocation(null);

        return dto;
    }

    private Location dto2Entity(FullWeatherDTO dto) {
        return modelMapper.map(dto, Location.class);
    }


}
