package com.skyapi.weatherforecast.hourly;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyapi.weatherforecast.GeolocationException;
import com.skyapi.weatherforecast.GeolocationService;
import com.skyapi.weatherforecast.common.HourlyWeather;
import com.skyapi.weatherforecast.common.Location;
import com.skyapi.weatherforecast.location.LocationNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HourlyWeatherController.class)
public class HourlyWeatherControllerTests {
    private static final String END_POINT_PATH = "/v1/hourly";
    public static final String X_CURRENT_HOUR = "X-Current-Hour";
    private static final String RESPONSE_CONTENT_TYPE = "application/hal+json";
    private static final String REQUEST_CONTENT_TYPE = "application/json";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private HourlyWeatherService hourlyWeatherService;
    @MockBean
    private GeolocationService geolocationService;


    @Test
    public void testGetByIpShouldReturn400BadRequestBecauseNoHeaderXCurrentHour() throws Exception {

        mockMvc.perform(get(END_POINT_PATH))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    public void testGetByIpShouldReturn400BadRequestBecauseGeoLocationException() throws Exception {

        when(geolocationService.getLocation(Mockito.anyString()))
                .thenThrow(GeolocationException.class);

        mockMvc.perform(get(END_POINT_PATH).header("X-Current-Hour", "9"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    public void testGetByIpShouldReturn204NoContent() throws Exception {

        int currentHour = 9;
        Location location = new Location().code("DELHI_IN");

        when(geolocationService.getLocation(Mockito.anyString()))
                .thenReturn(location);

        when(hourlyWeatherService.getByLocation(location, currentHour))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get(END_POINT_PATH).header(X_CURRENT_HOUR, String.valueOf(currentHour)))
                .andExpect(status().isNoContent())
                .andDo(print());
    }

    @Test
    public void testGetByIpShouldReturn200OK() throws Exception {

        int currentHour = 9;

        Location location = new Location();
        location.setCode("NYC_USA");
        location.setCityName("New York City");
        location.setRegionName("New York");
        location.setCountryCode("US");
        location.setCountryName("United States Of America");
        location.setEnabled(true);

        HourlyWeather forecast1 = new HourlyWeather()
                .id(location, 10)
                .temperature(13)
                .precipitation(70)
                .status("Cloudy");


        HourlyWeather forecast2 = new HourlyWeather()
                .id(location, 11)
                .temperature(15)
                .precipitation(60)
                .status("Sunny");

        when(geolocationService.getLocation(Mockito.anyString()))
                .thenReturn(location);

        when(hourlyWeatherService.getByLocation(location, currentHour))
                .thenReturn(List.of(forecast1, forecast2));

        String expectedLocation = location.toString();

        mockMvc.perform(get(END_POINT_PATH).header(X_CURRENT_HOUR, String.valueOf(currentHour)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(RESPONSE_CONTENT_TYPE))
                .andExpect(jsonPath("$.location", is(expectedLocation)))
                .andExpect(jsonPath("$.hourly_forecast[0].hour_of_day", is(10 )))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/v1/hourly")))
                .andExpect(jsonPath("$._links.realtime_weather.href", is("http://localhost/v1/realtime")))
                .andExpect(jsonPath("$._links.daily_forecast.href", is("http://localhost/v1/daily")))
                .andExpect(jsonPath("$._links.full_forecast.href", is("http://localhost/v1/full")))
                .andDo(print());
    }

    @Test
    public void testGetByLocationCodeShouldReturn400BadRequest() throws Exception {
        String locationCode = "DELHI_IN";
        String requestURI = END_POINT_PATH + "/" + locationCode;

        mockMvc.perform(get(requestURI))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    public void testGetByLocationCodeShouldReturn404NotFound() throws Exception {
        String locationCode = "DELHI_IN";
        int currentHour = 9;
        String requestURI = END_POINT_PATH + "/" + locationCode;

        when(hourlyWeatherService.getByLocationCode(locationCode, currentHour))
                .thenThrow(LocationNotFoundException.class);

        mockMvc.perform(get(requestURI).header(X_CURRENT_HOUR, String.valueOf(currentHour)))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    public void testGetByLocationCodeShouldReturn204NoContent() throws Exception {
        String locationCode = "DELHI_IN";
        int currentHour = 9;
        String requestURI = END_POINT_PATH + "/" + locationCode;

        when(hourlyWeatherService.getByLocationCode(locationCode, currentHour))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get(requestURI).header(X_CURRENT_HOUR, String.valueOf(currentHour)))
                .andExpect(status().isNoContent())
                .andDo(print());
    }

    @Test
    public void testGetByLocationCodeShouldReturn200OK() throws Exception {

        int currentHour = 9;
        String locationCode = "NYC_USA";
        String requestURI = END_POINT_PATH + "/" + locationCode;

        Location location = new Location();
        location.setCode("NYC_USA");
        location.setCityName("New York City");
        location.setRegionName("New York");
        location.setCountryCode("US");
        location.setCountryName("United States Of America");
        location.setEnabled(true);

        HourlyWeather forecast1 = new HourlyWeather()
                .id(location, 10)
                .temperature(13)
                .precipitation(70)
                .status("Cloudy");


        HourlyWeather forecast2 = new HourlyWeather()
                .id(location, 11)
                .temperature(15)
                .precipitation(60)
                .status("Sunny");


        when(hourlyWeatherService.getByLocationCode(locationCode, currentHour))
                .thenReturn(List.of(forecast1, forecast2));

        String expectedLocation = location.toString();

        mockMvc.perform(get(requestURI).header(X_CURRENT_HOUR, String.valueOf(currentHour)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(RESPONSE_CONTENT_TYPE))
                .andExpect(jsonPath("$.location", is(expectedLocation)))
                .andExpect(jsonPath("$.hourly_forecast[0].hour_of_day", is(10 )))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/v1/hourly/" + locationCode)))
                .andExpect(jsonPath("$._links.realtime_weather.href", is("http://localhost/v1/realtime/" + locationCode)))
                .andExpect(jsonPath("$._links.daily_forecast.href", is("http://localhost/v1/daily/" + locationCode)))
                .andExpect(jsonPath("$._links.full_forecast.href", is("http://localhost/v1/full/" + locationCode)))
                .andDo(print());
    }

    @Test
    public void testUpdateShouldReturn400BadRequestBecauseNoData() throws Exception {
        String requestURI = END_POINT_PATH + "/NYC_USA";

        List<HourlyWeatherDTO> listDTO = Collections.emptyList();

        String requestBody = objectMapper.writeValueAsString(listDTO);
        mockMvc.perform(put(requestURI).contentType(REQUEST_CONTENT_TYPE).content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.error", is("Hourly forecast data cannot be empty.")))
                .andDo(print());
    }

    @Test
    public void testUpdateShouldReturn400BadRequestBecauseInvalidData() throws Exception {
        String requestURI = END_POINT_PATH + "/NYC_USA";

        HourlyWeatherDTO dto1 = new HourlyWeatherDTO()
                .hourOfDay(10)
                .temperature(133)
                .precipitation(70)
                .status("Cloudy");

        HourlyWeatherDTO dto2 = new HourlyWeatherDTO()
                .hourOfDay(11)
                .temperature(15)
                .precipitation(60)
                .status("Sunny");

        List<HourlyWeatherDTO> listDTO = List.of(dto1, dto2);

        String requestBody = objectMapper.writeValueAsString(listDTO);

        mockMvc.perform(put(requestURI).contentType(REQUEST_CONTENT_TYPE).content(requestBody))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    public void testUpdateShouldReturn404NotFound() throws Exception {
        String locationCode = "NYC_USA";
        String requestURI = END_POINT_PATH + "/" + locationCode;

        HourlyWeatherDTO dto = new HourlyWeatherDTO()
                .hourOfDay(10)
                .temperature(13)
                .precipitation(70)
                .status("Cloudy");

        List<HourlyWeatherDTO> listDTO = List.of(dto);

        when(hourlyWeatherService.updateByLocationCode(Mockito.eq(locationCode), Mockito.anyList()))
                .thenThrow(LocationNotFoundException.class);

        String requestBody = objectMapper.writeValueAsString(listDTO);

        mockMvc.perform(put(requestURI).contentType(REQUEST_CONTENT_TYPE).content(requestBody))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    public void testUpdateShouldReturn200OK() throws Exception {
        String locationCode = "NYC_USA";
        String requestURI = END_POINT_PATH + "/" + locationCode;

        HourlyWeatherDTO dto1 = new HourlyWeatherDTO()
                .hourOfDay(10)
                .temperature(13)
                .precipitation(70)
                .status("Cloudy");

        HourlyWeatherDTO dto2 = new HourlyWeatherDTO()
                .hourOfDay(11)
                .temperature(15)
                .precipitation(60)
                .status("Sunny");

        Location location = new Location();
        location.setCode("NYC_USA");
        location.setCityName("New York City");
        location.setRegionName("New York");
        location.setCountryCode("US");
        location.setCountryName("United States Of America");
        location.setEnabled(true);

        HourlyWeather forecast1 = new HourlyWeather()
                .id(location, 10)
                .temperature(13)
                .precipitation(70)
                .status("Cloudy");


        HourlyWeather forecast2 = new HourlyWeather()
                .id(location, 11)
                .temperature(15)
                .precipitation(60)
                .status("Sunny");

        List<HourlyWeatherDTO> listDTO = List.of(dto1, dto2);

        List<HourlyWeather> hourlyForecast = List.of(forecast1, forecast2);

        when(hourlyWeatherService.updateByLocationCode(Mockito.eq(locationCode), Mockito.anyList()))
                .thenReturn(hourlyForecast);

        String requestBody = objectMapper.writeValueAsString(listDTO);

        mockMvc.perform(put(requestURI).contentType(REQUEST_CONTENT_TYPE).content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location",is(location.toString())))
                .andExpect(jsonPath("$.hourly_forecast[0].hour_of_day",is(dto1.getHourOfDay())))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/v1/hourly/" + locationCode)))
                .andExpect(jsonPath("$._links.realtime_weather.href", is("http://localhost/v1/realtime/" + locationCode)))
                .andExpect(jsonPath("$._links.daily_forecast.href", is("http://localhost/v1/daily/" + locationCode)))
                .andExpect(jsonPath("$._links.full_forecast.href", is("http://localhost/v1/full/" + locationCode)))
                .andDo(print());
    }
}
