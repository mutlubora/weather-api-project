package com.skyapi.weatherforecast.full;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyapi.weatherforecast.GeolocationException;
import com.skyapi.weatherforecast.GeolocationService;
import com.skyapi.weatherforecast.common.DailyWeather;
import com.skyapi.weatherforecast.common.HourlyWeather;
import com.skyapi.weatherforecast.common.Location;
import com.skyapi.weatherforecast.common.RealtimeWeather;
import com.skyapi.weatherforecast.daily.DailyWeatherDTO;
import com.skyapi.weatherforecast.hourly.HourlyWeatherDTO;
import com.skyapi.weatherforecast.location.LocationNotFoundException;
import com.skyapi.weatherforecast.realtime.RealtimeWeatherDTO;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FullWeatherController.class)
public class FullWeatherControllerTests {
    private static final String END_POINT_PATH = "/v1/full";
    private static final String RESPONSE_CONTENT_TYPE = "application/hal+json";
    private static final String REQUEST_CONTENT_TYPE = "application/json";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FullWeatherService fullWeatherService;
    @MockBean
    private GeolocationService geolocationService;
    @SpyBean
    private FullWeatherModelAssembler modelAssembler;

    @Test
    public void testGetByIPShouldReturn400BadRequestBecauseGeolocationException() throws Exception {
        GeolocationException ex = new GeolocationException("Geolocation error.");

        when(geolocationService.getLocation(Mockito.anyString())).thenThrow(ex);

        mockMvc.perform(get(END_POINT_PATH))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.error", is(ex.getMessage())))
                .andDo(print());
    }

    @Test
    public void testGetByIPShouldReturn404NotFound() throws Exception {
        Location location = new Location().code("DELHI_IN");

        when(geolocationService.getLocation(Mockito.anyString())).thenReturn(location);

        LocationNotFoundException ex = new LocationNotFoundException(location.getCode());
        when(fullWeatherService.getByLocation(location)).thenThrow(ex);

        mockMvc.perform(get(END_POINT_PATH))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors.error", is(ex.getMessage())))
                .andDo(print());
    }

    @Test
    public void testGetByIPShouldReturn200OK() throws Exception {
        Location location = new Location();
        location.setCode("NYC_USA");
        location.setCityName("New York City");
        location.setRegionName("New York");
        location.setCountryCode("US");
        location.setCountryName("United States Of America");
        location.setEnabled(true);

        DailyWeather dailyForecast1 = new DailyWeather()
                .location(location)
                .dayOfMonth(16)
                .month(7)
                .minTemp(23)
                .maxTemp(32)
                .precipitation(40)
                .status("Cloudy");

        DailyWeather dailyForecast2 = new DailyWeather()
                .location(location)
                .dayOfMonth(17)
                .month(7)
                .minTemp(25)
                .maxTemp(34)
                .precipitation(30)
                .status("Sunny");

        RealtimeWeather realtimeWeather = new RealtimeWeather();
        realtimeWeather.setStatus("Cloudy");
        realtimeWeather.setPrecipitation(88);
        realtimeWeather.setHumidity(32);
        realtimeWeather.setTemperature(12);
        realtimeWeather.setWindSpeed(5);
        realtimeWeather.setLastUpdated(new Date());

        HourlyWeather hourlyForecast1 = new HourlyWeather()
                .id(location, 10)
                .temperature(13)
                .precipitation(70)
                .status("Cloudy");


        HourlyWeather hourlyForecast2 = new HourlyWeather()
                .id(location, 11)
                .temperature(15)
                .precipitation(60)
                .status("Sunny");

        location.setListDailyWeather(List.of(dailyForecast1, dailyForecast2));
        location.setRealtimeWeather(realtimeWeather);
        location.setListHourlyWeather(List.of(hourlyForecast1, hourlyForecast2));


        when(geolocationService.getLocation(Mockito.anyString())).thenReturn(location);
        when(fullWeatherService.getByLocation(location)).thenReturn(location);

        String expectedLocation = location.toString();

        mockMvc.perform(get(END_POINT_PATH))
                .andExpect(status().isOk())
                .andExpect(content().contentType(RESPONSE_CONTENT_TYPE))
                .andExpect(jsonPath("$.location", is(expectedLocation)))
                .andExpect(jsonPath("$.daily_forecast[1].day_of_month", is(17)))
                .andExpect(jsonPath("$.realtime_weather.temperature", is(12)))
                .andExpect(jsonPath("$.hourly_forecast[0].precipitation", is(70)))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/v1/full")))
                .andDo(print());
    }

    @Test
    public void testGetByLocationCodeShouldReturn404NotFound() throws Exception {
        String locationCode = "ABC";
        String requestURI = END_POINT_PATH + "/" + locationCode;


        LocationNotFoundException ex = new LocationNotFoundException(locationCode);
        when(fullWeatherService.get(locationCode)).thenThrow(ex);

        mockMvc.perform(get(requestURI))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors.error", is(ex.getMessage())))
                .andDo(print());
    }

    @Test
    public void testGetByLocationCodeShouldReturn200OK() throws Exception {
        String locationCode = "NYC_USA";
        String requestURI = END_POINT_PATH + "/" + locationCode;

        Location location = new Location();
        location.setCode("NYC_USA");
        location.setCityName("New York City");
        location.setRegionName("New York");
        location.setCountryCode("US");
        location.setCountryName("United States Of America");

        DailyWeather dailyForecast1 = new DailyWeather()
                .location(location)
                .dayOfMonth(16)
                .month(7)
                .minTemp(23)
                .maxTemp(32)
                .precipitation(40)
                .status("Cloudy");

        DailyWeather dailyForecast2 = new DailyWeather()
                .location(location)
                .dayOfMonth(17)
                .month(7)
                .minTemp(25)
                .maxTemp(34)
                .precipitation(30)
                .status("Sunny");

        RealtimeWeather realtimeWeather = new RealtimeWeather();
        realtimeWeather.setStatus("Cloudy");
        realtimeWeather.setPrecipitation(88);
        realtimeWeather.setHumidity(32);
        realtimeWeather.setTemperature(12);
        realtimeWeather.setWindSpeed(5);
        realtimeWeather.setLastUpdated(new Date());

        HourlyWeather hourlyForecast1 = new HourlyWeather()
                .id(location, 10)
                .temperature(13)
                .precipitation(70)
                .status("Cloudy");


        HourlyWeather hourlyForecast2 = new HourlyWeather()
                .id(location, 11)
                .temperature(15)
                .precipitation(60)
                .status("Sunny");

        location.setListDailyWeather(List.of(dailyForecast1, dailyForecast2));
        location.setRealtimeWeather(realtimeWeather);
        location.setListHourlyWeather(List.of(hourlyForecast1, hourlyForecast2));


        when(fullWeatherService.get(locationCode)).thenReturn(location);

        String expectedLocation = location.toString();

        mockMvc.perform(get(requestURI))
                .andExpect(status().isOk())
                .andExpect(content().contentType(RESPONSE_CONTENT_TYPE))
                .andExpect(jsonPath("$.location", is(expectedLocation)))
                .andExpect(jsonPath("$.daily_forecast[1].day_of_month", is(17)))
                .andExpect(jsonPath("$.realtime_weather.temperature", is(12)))
                .andExpect(jsonPath("$.hourly_forecast[0].precipitation", is(70)))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/v1/full/" + locationCode)))
                .andDo(print());
    }

    @Test
    public void testUpdateShouldReturn400BadRequestBecauseNoHourlyWeather() throws Exception {
        String locationCode = "NYC_USA";
        String requestURI = END_POINT_PATH + "/" + locationCode;

        FullWeatherDTO fullWeatherDTO = new FullWeatherDTO();


        RealtimeWeatherDTO realtimeWeather = new RealtimeWeatherDTO();
        realtimeWeather.setStatus("Cloudy");
        realtimeWeather.setPrecipitation(88);
        realtimeWeather.setHumidity(32);
        realtimeWeather.setTemperature(12);
        realtimeWeather.setWindSpeed(5);
        realtimeWeather.setLastUpdated(new Date());

        fullWeatherDTO.setRealtimeWeather(realtimeWeather);


        String requestBody = objectMapper.writeValueAsString(fullWeatherDTO);

        mockMvc.perform(put(requestURI).contentType(REQUEST_CONTENT_TYPE).content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.error", is("Hourly weather data cannot be empty.")))
                .andDo(print());
    }

    @Test
    public void testUpdateShouldReturn400BadRequestBecauseNoDailyWeather() throws Exception {
        String locationCode = "NYC_USA";
        String requestURI = END_POINT_PATH + "/" + locationCode;

        FullWeatherDTO fullWeatherDTO = new FullWeatherDTO();

        HourlyWeatherDTO hourlyForecast1 = new HourlyWeatherDTO()
                .hourOfDay(12)
                .temperature(13)
                .precipitation(70)
                .status("Cloudy");

        fullWeatherDTO.getListHourlyWeather().add(hourlyForecast1);

        RealtimeWeatherDTO realtimeWeather = new RealtimeWeatherDTO();
        realtimeWeather.setStatus("Cloudy");
        realtimeWeather.setPrecipitation(88);
        realtimeWeather.setHumidity(32);
        realtimeWeather.setTemperature(12);
        realtimeWeather.setWindSpeed(5);
        realtimeWeather.setLastUpdated(new Date());

        fullWeatherDTO.setRealtimeWeather(realtimeWeather);


        String requestBody = objectMapper.writeValueAsString(fullWeatherDTO);

        mockMvc.perform(put(requestURI).contentType(REQUEST_CONTENT_TYPE).content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.error", is("Daily weather data cannot be empty.")))
                .andDo(print());
    }

    @Test
    public void testUpdateShouldReturn400BadRequestBecauseInvalidRealtimeWeather() throws Exception {
        String locationCode = "NYC_USA";
        String requestURI = END_POINT_PATH + "/" + locationCode;

        FullWeatherDTO fullWeatherDTO = new FullWeatherDTO();

        HourlyWeatherDTO hourlyForecast1 = new HourlyWeatherDTO()
                .hourOfDay(12)
                .temperature(13)
                .precipitation(70)
                .status("Cloudy");

        fullWeatherDTO.getListHourlyWeather().add(hourlyForecast1);

        DailyWeatherDTO dailyForecast1 = new DailyWeatherDTO()
                .dayOfMonth(16)
                .month(7)
                .minTemp(23)
                .maxTemp(32)
                .precipitation(40)
                .status("Cloudy");

        fullWeatherDTO.getListDailyWeather().add(dailyForecast1);

        RealtimeWeatherDTO realtimeWeather = new RealtimeWeatherDTO();
        realtimeWeather.setStatus("Cloudy");
        realtimeWeather.setPrecipitation(88);
        realtimeWeather.setHumidity(32);
        realtimeWeather.setTemperature(122);
        realtimeWeather.setWindSpeed(5);
        realtimeWeather.setLastUpdated(new Date());

        fullWeatherDTO.setRealtimeWeather(realtimeWeather);

        String requestBody = objectMapper.writeValueAsString(fullWeatherDTO);

        mockMvc.perform(put(requestURI).contentType(REQUEST_CONTENT_TYPE).content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[\"realtimeWeather.temperature\"]", is("Temperature must be in range of -50 to 50 Celsius degree")))
                .andDo(print());
    }

    @Test
    public void testUpdateShouldReturn400BadRequestBecauseInvalidHourlyWeatherData() throws Exception {
        String locationCode = "NYC_USA";
        String requestURI = END_POINT_PATH + "/" + locationCode;

        FullWeatherDTO fullWeatherDTO = new FullWeatherDTO();

        HourlyWeatherDTO hourlyForecast1 = new HourlyWeatherDTO()
                .hourOfDay(122)
                .temperature(33)
                .precipitation(70)
                .status("Cloudy");

        fullWeatherDTO.getListHourlyWeather().add(hourlyForecast1);

        DailyWeatherDTO dailyForecast1 = new DailyWeatherDTO()
                .dayOfMonth(16)
                .month(7)
                .minTemp(23)
                .maxTemp(32)
                .precipitation(40)
                .status("Cloudy");

        fullWeatherDTO.getListDailyWeather().add(dailyForecast1);

        RealtimeWeatherDTO realtimeWeather = new RealtimeWeatherDTO();
        realtimeWeather.setStatus("Cloudy");
        realtimeWeather.setPrecipitation(88);
        realtimeWeather.setHumidity(32);
        realtimeWeather.setTemperature(12);
        realtimeWeather.setWindSpeed(5);
        realtimeWeather.setLastUpdated(new Date());

        fullWeatherDTO.setRealtimeWeather(realtimeWeather);

        String requestBody = objectMapper.writeValueAsString(fullWeatherDTO);

        mockMvc.perform(put(requestURI).contentType(REQUEST_CONTENT_TYPE).content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[\"listHourlyWeather[0].hourOfDay\"]", is("Hour of day must be in between 0-23")))
                .andDo(print());
    }

    @Test
    public void testUpdateShouldReturn400BadRequestBecauseInvalidDailyWeatherData() throws Exception {
        String locationCode = "NYC_USA";
        String requestURI = END_POINT_PATH + "/" + locationCode;

        FullWeatherDTO fullWeatherDTO = new FullWeatherDTO();

        HourlyWeatherDTO hourlyForecast1 = new HourlyWeatherDTO()
                .hourOfDay(12)
                .temperature(33)
                .precipitation(70)
                .status("Cloudy");

        fullWeatherDTO.getListHourlyWeather().add(hourlyForecast1);

        DailyWeatherDTO dailyForecast1 = new DailyWeatherDTO()
                .dayOfMonth(16)
                .month(7)
                .minTemp(23)
                .maxTemp(32)
                .precipitation(40)
                .status("");

        fullWeatherDTO.getListDailyWeather().add(dailyForecast1);

        RealtimeWeatherDTO realtimeWeather = new RealtimeWeatherDTO();
        realtimeWeather.setStatus("Cloudy");
        realtimeWeather.setPrecipitation(88);
        realtimeWeather.setHumidity(32);
        realtimeWeather.setTemperature(12);
        realtimeWeather.setWindSpeed(5);
        realtimeWeather.setLastUpdated(new Date());

        fullWeatherDTO.setRealtimeWeather(realtimeWeather);

        String requestBody = objectMapper.writeValueAsString(fullWeatherDTO);

        mockMvc.perform(put(requestURI).contentType(REQUEST_CONTENT_TYPE).content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[\"listDailyWeather[0].status\"]", containsString("Status must be in between")))
                .andDo(print());
    }

    @Test
    public void testUpdateShouldReturn404NotFound() throws Exception {
        String locationCode = "NYC_USA";
        String requestURI = END_POINT_PATH + "/" + locationCode;

        Location location = new Location();
        location.setCode(locationCode);

        FullWeatherDTO fullWeatherDTO = new FullWeatherDTO();

        HourlyWeatherDTO hourlyForecast1 = new HourlyWeatherDTO()
                .hourOfDay(12)
                .temperature(33)
                .precipitation(70)
                .status("Cloudy");

        fullWeatherDTO.getListHourlyWeather().add(hourlyForecast1);

        DailyWeatherDTO dailyForecast1 = new DailyWeatherDTO()
                .dayOfMonth(16)
                .month(7)
                .minTemp(23)
                .maxTemp(32)
                .precipitation(40)
                .status("Cloudy");

        fullWeatherDTO.getListDailyWeather().add(dailyForecast1);

        RealtimeWeatherDTO realtimeWeather = new RealtimeWeatherDTO();
        realtimeWeather.setStatus("Cloudy");
        realtimeWeather.setPrecipitation(88);
        realtimeWeather.setHumidity(32);
        realtimeWeather.setTemperature(12);
        realtimeWeather.setWindSpeed(5);
        realtimeWeather.setLastUpdated(new Date());

        fullWeatherDTO.setRealtimeWeather(realtimeWeather);

        String requestBody = objectMapper.writeValueAsString(fullWeatherDTO);

        LocationNotFoundException ex = new LocationNotFoundException(locationCode);
        when(fullWeatherService.update(Mockito.eq(locationCode), Mockito.any())).thenThrow(ex);

        mockMvc.perform(put(requestURI).contentType(REQUEST_CONTENT_TYPE).content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors.error", is(ex.getMessage())))
                .andDo(print());
    }

    @Test
    public void testUpdateShouldReturn200OK() throws Exception {
        String locationCode = "NYC_USA";
        String requestURI = END_POINT_PATH + "/" + locationCode;

        Location location = new Location();
        location.setCode(locationCode);
        location.setCityName("New York City");
        location.setRegionName("New York");
        location.setCountryCode("US");
        location.setCountryName("United States Of America");

        DailyWeather dailyForecast1 = new DailyWeather()
                .location(location)
                .dayOfMonth(16)
                .month(7)
                .minTemp(23)
                .maxTemp(32)
                .precipitation(40)
                .status("Cloudy");

        RealtimeWeather realtimeWeather = new RealtimeWeather();
        realtimeWeather.setStatus("Cloudy");
        realtimeWeather.setPrecipitation(88);
        realtimeWeather.setHumidity(32);
        realtimeWeather.setTemperature(12);
        realtimeWeather.setWindSpeed(5);
        realtimeWeather.setLastUpdated(new Date());

        HourlyWeather hourlyForecast1 = new HourlyWeather()
                .id(location, 10)
                .temperature(13)
                .precipitation(70)
                .status("Cloudy");

        location.setListDailyWeather(List.of(dailyForecast1));
        location.setRealtimeWeather(realtimeWeather);
        location.setListHourlyWeather(List.of(hourlyForecast1));

        FullWeatherDTO fullWeatherDTO = new FullWeatherDTO();

        HourlyWeatherDTO hourlyForecastDTO1 = new HourlyWeatherDTO()
                .hourOfDay(10)
                .temperature(13)
                .precipitation(70)
                .status("Cloudy");

        fullWeatherDTO.getListHourlyWeather().add(hourlyForecastDTO1);

        DailyWeatherDTO dailyForecastDTO1 = new DailyWeatherDTO()
                .dayOfMonth(16)
                .month(7)
                .minTemp(23)
                .maxTemp(32)
                .precipitation(40)
                .status("Cloudy");

        fullWeatherDTO.getListDailyWeather().add(dailyForecastDTO1);

        RealtimeWeatherDTO realtimeWeatherDTO = new RealtimeWeatherDTO();
        realtimeWeatherDTO.setStatus("Cloudy");
        realtimeWeatherDTO.setPrecipitation(88);
        realtimeWeatherDTO.setHumidity(32);
        realtimeWeatherDTO.setTemperature(12);
        realtimeWeatherDTO.setWindSpeed(5);
        realtimeWeatherDTO.setLastUpdated(new Date());

        fullWeatherDTO.setRealtimeWeather(realtimeWeatherDTO);

        String requestBody = objectMapper.writeValueAsString(fullWeatherDTO);
        when(fullWeatherService.update(Mockito.eq(locationCode), Mockito.any())).thenReturn(location);

        mockMvc.perform(put(requestURI).contentType(REQUEST_CONTENT_TYPE).content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(RESPONSE_CONTENT_TYPE))
                .andExpect(jsonPath("$.daily_forecast[0].day_of_month", is(16)))
                .andExpect(jsonPath("$.realtime_weather.temperature", is(12)))
                .andExpect(jsonPath("$.hourly_forecast[0].precipitation", is(70)))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/v1/full/" + locationCode)))
                .andDo(print());
    }
}
