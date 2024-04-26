package com.skyapi.weatherforecast.full;

import com.skyapi.weatherforecast.BadRequestException;
import com.skyapi.weatherforecast.CommonUtility;
import com.skyapi.weatherforecast.GeolocationService;
import com.skyapi.weatherforecast.common.Location;
import com.skyapi.weatherforecast.daily.DailyWeatherController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/v1/full")
public class FullWeatherController {
    private final FullWeatherService fullWeatherService;
    private final GeolocationService geolocationService;
    private final ModelMapper modelMapper;
    private final FullWeatherModelAssembler modelAssembler;

    public FullWeatherController(FullWeatherService fullWeatherService,
                                 GeolocationService geolocationService,
                                 ModelMapper modelMapper,
                                 FullWeatherModelAssembler modelAssembler) {
        this.fullWeatherService = fullWeatherService;
        this.geolocationService = geolocationService;
        this.modelMapper = modelMapper;
        this.modelAssembler = modelAssembler;
    }


    @GetMapping
    public ResponseEntity<?> getFullWeatherByIPAddress(HttpServletRequest request) {
        String ipAddress = CommonUtility.getIPAddress(request);
        
        Location locationFromIP = geolocationService.getLocation(ipAddress);
        Location locationInDB = fullWeatherService.getByLocation(locationFromIP);
        FullWeatherDTO dto = entity2DTO(locationInDB);
        return ResponseEntity.ok(modelAssembler.toModel(dto));
    }

    @GetMapping("/{locationCode}")
    public ResponseEntity<?> getFullWeatherByLocationCode(@PathVariable("locationCode") String locationCode) {

        Location locationInDB = fullWeatherService.get(locationCode);
        FullWeatherDTO dto = entity2DTO(locationInDB);

        return ResponseEntity.ok(addLinksByLocation(dto, locationCode));
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
        FullWeatherDTO dtoResponse = entity2DTO(updatedLocation);
        return ResponseEntity.ok(addLinksByLocation(dtoResponse, locationCode));
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

    private EntityModel<FullWeatherDTO> addLinksByLocation(FullWeatherDTO dto, String locationCode) {
        return EntityModel.of(dto)
                .add(linkTo(
                        methodOn(FullWeatherController.class).getFullWeatherByLocationCode(locationCode))
                        .withSelfRel());
    }

}
