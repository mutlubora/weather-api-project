package com.skyapi.weatherforecast.hourly;

import com.skyapi.weatherforecast.BadRequestException;
import com.skyapi.weatherforecast.CommonUtility;
import com.skyapi.weatherforecast.GeolocationException;
import com.skyapi.weatherforecast.GeolocationService;
import com.skyapi.weatherforecast.common.HourlyWeather;
import com.skyapi.weatherforecast.common.Location;
import com.skyapi.weatherforecast.daily.DailyWeatherController;
import com.skyapi.weatherforecast.full.FullWeatherController;
import com.skyapi.weatherforecast.location.LocationNotFoundException;
import com.skyapi.weatherforecast.realtime.RealtimeWeatherController;
import com.skyapi.weatherforecast.realtime.RealtimeWeatherDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/v1/hourly")
@Validated
public class HourlyWeatherController {

    private final HourlyWeatherService hourlyWeatherService;
    private final GeolocationService geolocationService;
    private final ModelMapper modelMapper;
    public HourlyWeatherController(HourlyWeatherService hourlyWeatherService, GeolocationService geolocationService, ModelMapper modelMapper) {
        this.hourlyWeatherService = hourlyWeatherService;
        this.geolocationService = geolocationService;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    public ResponseEntity<?> listHourlyForecastByIPAddress(HttpServletRequest request) {
        String ipAddress = CommonUtility.getIPAddress(request);
        try {
            int currentHour = Integer.parseInt(request.getHeader("X-Current-Hour"));

            Location locationFromIP = geolocationService.getLocation(ipAddress);

            List<HourlyWeather> hourlyForecast = hourlyWeatherService.getByLocation(locationFromIP, currentHour);

            if (hourlyForecast.isEmpty()) {
               return ResponseEntity.noContent().build();
            }

            HourlyWeatherListDTO dto = listEntity2DTO(hourlyForecast);

            return ResponseEntity.ok(addLinksByIP(dto));

        } catch (NumberFormatException | GeolocationException e) {
            return ResponseEntity.badRequest().build();
        } catch (LocationNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{locationCode}")
    public ResponseEntity<?> listHourlyForecastByLocationCode(
            @PathVariable("locationCode") String locationCode,
            HttpServletRequest request) {

        try {
            int currentHour = Integer.parseInt(request.getHeader("X-Current-Hour"));

            List<HourlyWeather> hourlyForecast = hourlyWeatherService.getByLocationCode(locationCode, currentHour);

            if (hourlyForecast.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            HourlyWeatherListDTO dto = listEntity2DTO(hourlyForecast);

            return ResponseEntity.ok(addLinksByLocation(dto, locationCode));

        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        } catch (LocationNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{locationCode}")
    public ResponseEntity<?> updateHourlyForecast(@PathVariable("locationCode") String locationCode,
                                                  @RequestBody @Valid List<HourlyWeatherDTO> listDTO)
                                                  throws BadRequestException {
        if (listDTO.isEmpty()) {
            throw new BadRequestException("Hourly forecast data cannot be empty.");
        }

        List<HourlyWeather> listHourlyWeathers = listDTO2ListEntity(listDTO);

        try {
            List<HourlyWeather> updatedHourlyWeather =
                    hourlyWeatherService.updateByLocationCode(locationCode, listHourlyWeathers);

            HourlyWeatherListDTO updatedDto = listEntity2DTO(updatedHourlyWeather);

            return ResponseEntity.ok(addLinksByLocation(updatedDto, locationCode));

        } catch (LocationNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private List<HourlyWeather> listDTO2ListEntity(List<HourlyWeatherDTO> listDTO) {
        List<HourlyWeather> listEntity = new ArrayList<>();

        listDTO.forEach(dto -> {
            HourlyWeather hourlyForecast = modelMapper.map(dto, HourlyWeather.class);
            listEntity.add(hourlyForecast);
        });

        return listEntity;
    }

    private HourlyWeatherListDTO listEntity2DTO(List<HourlyWeather> hourlyForecast) {
        Location location = hourlyForecast.get(0).getId().getLocation();

        HourlyWeatherListDTO listDTO = new HourlyWeatherListDTO();
        listDTO.setLocation(location.toString());

        hourlyForecast.forEach(hourlyWeather -> {
            HourlyWeatherDTO dto = modelMapper.map(hourlyWeather, HourlyWeatherDTO.class);
            listDTO.addWeatherHourlyDTO(dto);
        });

        return listDTO;
    }

    private HourlyWeatherListDTO addLinksByIP(HourlyWeatherListDTO dto) {

        dto.add(linkTo(
                methodOn(HourlyWeatherController.class).listHourlyForecastByIPAddress(null))
                .withSelfRel());
        dto.add(linkTo(
                methodOn(RealtimeWeatherController.class).getRealtimeWeatherByIPAddress(null))
                .withRel("realtime_weather"));
        dto.add(linkTo(
                methodOn(DailyWeatherController.class).listDailyForecastByIPAddress(null))
                .withRel("daily_forecast"));
        dto.add(linkTo(
                methodOn(FullWeatherController.class).getFullWeatherByIPAddress(null))
                .withRel("full_forecast"));
        return dto;
    }

    private HourlyWeatherListDTO addLinksByLocation(HourlyWeatherListDTO dto, String locationCode) {

        dto.add(linkTo(
                methodOn(HourlyWeatherController.class).listHourlyForecastByLocationCode(locationCode, null))
                .withSelfRel());
        dto.add(linkTo(
                methodOn(RealtimeWeatherController.class).getRealtimeWeatherByLocationCode(locationCode))
                .withRel("realtime_weather"));
        dto.add(linkTo(
                methodOn(DailyWeatherController.class).listDailyForecastByLocationCode(locationCode))
                .withRel("daily_forecast"));
        dto.add(linkTo(
                methodOn(FullWeatherController.class).getFullWeatherByLocationCode(locationCode))
                .withRel("full_forecast"));
        return dto;
    }
}
