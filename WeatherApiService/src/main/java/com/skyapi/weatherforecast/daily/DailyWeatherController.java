package com.skyapi.weatherforecast.daily;

import com.skyapi.weatherforecast.BadRequestException;
import com.skyapi.weatherforecast.CommonUtility;
import com.skyapi.weatherforecast.GeolocationService;
import com.skyapi.weatherforecast.common.DailyWeather;
import com.skyapi.weatherforecast.common.Location;
import com.skyapi.weatherforecast.full.FullWeatherController;
import com.skyapi.weatherforecast.hourly.HourlyWeatherController;
import com.skyapi.weatherforecast.realtime.RealtimeWeatherController;
import com.skyapi.weatherforecast.realtime.RealtimeWeatherDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/v1/daily")
@Validated
public class DailyWeatherController {
    private final DailyWeatherService dailyWeatherService;
    private final GeolocationService geolocationService;
    private final ModelMapper modelMapper;

    public DailyWeatherController(DailyWeatherService dailyWeatherService,
                                  GeolocationService geolocationService,
                                  ModelMapper modelMapper) {
        this.dailyWeatherService = dailyWeatherService;
        this.geolocationService = geolocationService;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    public ResponseEntity<?> listDailyForecastByIPAddress(HttpServletRequest request) {
        String ipAddress = CommonUtility.getIPAddress(request);

        Location locationFromIP = geolocationService.getLocation(ipAddress);
        List<DailyWeather> dailyForecast = dailyWeatherService.getByLocation(locationFromIP);

        if (dailyForecast.isEmpty()) {
           return ResponseEntity.noContent().build();
        }

        DailyWeatherListDTO dto = listEntity2DTO(dailyForecast);

        return ResponseEntity.ok(addLinksByIP(dto));
    }

    @GetMapping("/{locationCode}")
    public ResponseEntity<?> listDailyForecastByLocationCode(@PathVariable("locationCode") String locationCode) {
        List<DailyWeather> dailyForecast = dailyWeatherService.getByLocationCode(locationCode);

        if (dailyForecast.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        DailyWeatherListDTO dto = listEntity2DTO(dailyForecast);
        return ResponseEntity.ok(addLinksByLocation(dto, locationCode));
    }

    @PutMapping("/{locationCode}")
    public ResponseEntity<?> updateDailyForecast(@PathVariable("locationCode") String locationCode,
                                                 @RequestBody @Valid List<DailyWeatherDTO> listDTO)
            throws BadRequestException {

        if (listDTO.isEmpty()) {
            throw new BadRequestException("Daily forecast data cannot be empty.");
        }

        List<DailyWeather> dailyForecast = listDTO2listEntity(listDTO);
        List<DailyWeather> updatedDailyForecast = dailyWeatherService
               .updateByLocationCode(locationCode, dailyForecast);

        DailyWeatherListDTO dto = listEntity2DTO(updatedDailyForecast);
        return ResponseEntity.ok(addLinksByLocation(dto, locationCode));
    }

    private DailyWeatherListDTO listEntity2DTO(List<DailyWeather> dailyForecast) {
        Location location = dailyForecast.get(0).getId().getLocation();

        DailyWeatherListDTO listDTO = new DailyWeatherListDTO();
        listDTO.setLocation(location.toString());

        dailyForecast.forEach(dailyWeather -> {
                    DailyWeatherDTO dto = modelMapper.map(dailyWeather, DailyWeatherDTO.class);
                    listDTO.addDailyWeatherDTO(dto);
                });

        return listDTO;
    }

    private List<DailyWeather> listDTO2listEntity(List<DailyWeatherDTO> dtoList) {

        List<DailyWeather> listEntity  = new ArrayList<>();

        dtoList.forEach(dto -> {
            DailyWeather dailyForecast = modelMapper.map(dto, DailyWeather.class);
            listEntity .add(dailyForecast);
        });

        return listEntity ;
    }

    private EntityModel<DailyWeatherListDTO> addLinksByIP(DailyWeatherListDTO dto) {
        EntityModel<DailyWeatherListDTO> entityModel = EntityModel.of(dto);

        entityModel.add(linkTo(
                methodOn(DailyWeatherController.class).listDailyForecastByIPAddress(null))
                .withSelfRel());
        entityModel.add(linkTo(
                methodOn(HourlyWeatherController.class).listHourlyForecastByIPAddress(null))
                .withRel("hourly_forecast"));
        entityModel.add(linkTo(
                methodOn(RealtimeWeatherController.class).getRealtimeWeatherByIPAddress(null))
                .withRel("realtime_weather"));
        entityModel.add(linkTo(
                methodOn(FullWeatherController.class).getFullWeatherByIPAddress(null))
                .withRel("full_forecast"));

        return entityModel;
    }

    private EntityModel<DailyWeatherListDTO> addLinksByLocation(DailyWeatherListDTO dto, String locationCode) {

        return EntityModel.of(dto)
                .add(linkTo(
                    methodOn(DailyWeatherController.class).listDailyForecastByLocationCode(locationCode))
                    .withSelfRel())
                .add(linkTo(
                    methodOn(HourlyWeatherController.class).listHourlyForecastByLocationCode(locationCode, null))
                    .withRel("hourly_forecast"))
                .add(linkTo(
                        methodOn(RealtimeWeatherController.class).getRealtimeWeatherByLocationCode(locationCode))
                        .withRel("realtime_weather"))
                .add(linkTo(
                        methodOn(FullWeatherController.class).getFullWeatherByLocationCode(locationCode))
                        .withRel("full_forecast"));
    }
}
