package com.skyapi.weatherforecast.realtime;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyapi.weatherforecast.GeolocationException;
import com.skyapi.weatherforecast.GeolocationService;
import com.skyapi.weatherforecast.common.Location;
import com.skyapi.weatherforecast.common.RealtimeWeather;
import com.skyapi.weatherforecast.location.LocationNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RealtimeWeatherController.class)
public class RealtimeWeatherControllerTest {

    private static final String END_POINT_PATH = "/v1/realtime";
    private static final String RESPONSE_CONTENT_TYPE = "application/hal+json";
    private static final String REQUEST_CONTENT_TYPE = "application/json";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private RealtimeWeatherService realtimeWeatherService;

    @MockBean
    private GeolocationService geolocationService;

    @Test
    public void testGetShouldReturnStatus400BadRequest() throws Exception {
        Mockito.when(geolocationService.getLocation(Mockito.anyString()))
                .thenThrow(GeolocationException.class);

        mockMvc.perform(get(END_POINT_PATH))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    public void testGetShouldReturnStatus404NotFound() throws Exception {
        Location location = new Location();
        location.setCountryCode("US");
        location.setCityName("Tampa");
        LocationNotFoundException ex = new LocationNotFoundException(location.getCountryCode(), location.getCityName());

        Mockito.when(geolocationService.getLocation(Mockito.anyString()))
                .thenReturn(location);
        Mockito.when(realtimeWeatherService.getByLocation(location))
                .thenThrow(ex);

        mockMvc.perform(get(END_POINT_PATH))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors.error", is(ex.getMessage())))
                .andDo(print());
    }

    @Test
    public void testGetShouldReturnStatus200OK() throws Exception {
        Location location = new Location();
        location.setCode("SFCA_USA");
        location.setRegionName("California");
        location.setCountryName("United States Of America");
        location.setCountryCode("US");
        location.setCityName("San Francisco");

        RealtimeWeather realtimeWeather = new RealtimeWeather();
        realtimeWeather.setTemperature(12);
        realtimeWeather.setHumidity(32);
        realtimeWeather.setLastUpdated(new Date());
        realtimeWeather.setPrecipitation(88);
        realtimeWeather.setStatus("Cloudy");
        realtimeWeather.setWindSpeed(5);

        realtimeWeather.setLocation(location);
        location.setRealtimeWeather(realtimeWeather);

        Mockito.when(geolocationService.getLocation(Mockito.anyString())).thenReturn(location);
        Mockito.when(realtimeWeatherService.getByLocation(location)).thenReturn(realtimeWeather);

        String expectedLocation = location.getCityName() + ", "
                + location.getRegionName() + ", " + location.getCountryName();

        mockMvc.perform(get(END_POINT_PATH))
                .andExpect(status().isOk())
                .andExpect(content().contentType(RESPONSE_CONTENT_TYPE))
                .andExpect(jsonPath("$.location", is(expectedLocation)))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/v1/realtime")))
                .andExpect(jsonPath("$._links.hourly_forecast.href", is("http://localhost/v1/hourly")))
                .andExpect(jsonPath("$._links.daily_forecast.href", is("http://localhost/v1/daily")))
                .andExpect(jsonPath("$._links.full_forecast.href", is("http://localhost/v1/full")))
                .andDo(print());
    }

    @Test
    public void testGetByLocationCodeShouldReturnStatus404NotFound() throws Exception {
        String locationCode = "ABCD";
        String requestURI = END_POINT_PATH + "/" +locationCode;

        LocationNotFoundException ex = new LocationNotFoundException(locationCode);

        Mockito.when(realtimeWeatherService.getByLocationCode(locationCode))
                .thenThrow(ex);

        mockMvc.perform(get(requestURI))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors.error", is(ex.getMessage())))
                .andDo(print());
    }

    @Test
    public void testGetByLocationCodeShouldReturnStatus200OK() throws Exception {
        String locationCode = "LACA_USA";
        String requestURI = END_POINT_PATH + "/" + locationCode;

        Location location = new Location();
        location.setCode(locationCode);
        location.setCityName("Los Angeles");
        location.setCountryCode("US");
        location.setCountryName("United States Of America");
        location.setRegionName("California");

        RealtimeWeather realtimeWeather = new RealtimeWeather();
        realtimeWeather.setStatus("Cloudy");
        realtimeWeather.setPrecipitation(88);
        realtimeWeather.setLastUpdated(new Date());
        realtimeWeather.setHumidity(32);
        realtimeWeather.setTemperature(12);
        realtimeWeather.setWindSpeed(5);

        realtimeWeather.setLocation(location);
        location.setRealtimeWeather(realtimeWeather);

        Mockito.when(realtimeWeatherService.getByLocationCode(locationCode)).thenReturn(realtimeWeather);

        String expectedLocation = location.getCityName() + ", "
                + location.getRegionName() + ", " + location.getCountryName();

        mockMvc.perform(get(requestURI))
                .andExpect(status().isOk())
                .andExpect(content().contentType(RESPONSE_CONTENT_TYPE))
                .andExpect(jsonPath("$.location", is(expectedLocation)))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/v1/realtime/" + locationCode)))
                .andExpect(jsonPath("$._links.hourly_forecast.href", is("http://localhost/v1/hourly/" + locationCode)))
                .andExpect(jsonPath("$._links.daily_forecast.href", is("http://localhost/v1/daily/" + locationCode)))
                .andExpect(jsonPath("$._links.full_forecast.href", is("http://localhost/v1/full/" + locationCode)))
                .andDo(print());
    }

    @Test
    public void testUpdateShouldReturn400BadRequest() throws Exception {
        String locationCode = "ABC_DEF";
        String requestURI = END_POINT_PATH + "/" + locationCode;

        RealtimeWeatherDTO dto = new RealtimeWeatherDTO();
        dto.setStatus("Cl");
        dto.setPrecipitation(188);
        dto.setHumidity(132);
        dto.setTemperature(120);
        dto.setWindSpeed(500);

        String bodyContent = mapper.writeValueAsString(dto);

        mockMvc.perform(put(requestURI).contentType(REQUEST_CONTENT_TYPE).content(bodyContent))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    public void testUpdateShouldReturn404NotFound() throws Exception {
        String locationCode = "ABC_DEF";
        String requestURI = END_POINT_PATH + "/" + locationCode;

        RealtimeWeatherDTO dto = new RealtimeWeatherDTO();
        dto.setStatus("Cloudy");
        dto.setPrecipitation(88);
        dto.setHumidity(32);
        dto.setTemperature(12);
        dto.setWindSpeed(5);

        LocationNotFoundException ex = new LocationNotFoundException(locationCode);

        Mockito.when(realtimeWeatherService.update(Mockito.eq(locationCode), Mockito.any()))
                .thenThrow(ex);

        String bodyContent = mapper.writeValueAsString(dto);

        mockMvc.perform(put(requestURI).contentType(REQUEST_CONTENT_TYPE).content(bodyContent))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors.error", is(ex.getMessage())))
                .andDo(print());
    }

    @Test
    public void testUpdateShouldReturn200OK() throws Exception {
        String locationCode = "LACA_USA";
        String requestURI = END_POINT_PATH + "/" + locationCode;

        RealtimeWeather realtimeWeather = new RealtimeWeather();
        realtimeWeather.setStatus("Cloudy");
        realtimeWeather.setPrecipitation(88);
        realtimeWeather.setHumidity(32);
        realtimeWeather.setTemperature(12);
        realtimeWeather.setWindSpeed(5);
        realtimeWeather.setLastUpdated(new Date());

        RealtimeWeatherDTO dto = new RealtimeWeatherDTO();
        dto.setStatus("Cloudy");
        dto.setPrecipitation(88);
        dto.setHumidity(32);
        dto.setTemperature(12);
        dto.setWindSpeed(5);

        Location location = new Location();
        location.setCode(locationCode);
        location.setCityName("Los Angeles");
        location.setCountryCode("US");
        location.setCountryName("United States Of America");
        location.setRegionName("California");

        realtimeWeather.setLocation(location);
        location.setRealtimeWeather(realtimeWeather);

        Mockito.when(realtimeWeatherService.update(locationCode, realtimeWeather))
                .thenReturn(realtimeWeather);

        String bodyContent = mapper.writeValueAsString(dto);

        String expectedLocation = location.getCityName() + ", "
                + location.getRegionName() + ", " + location.getCountryName();

        mockMvc.perform(put(requestURI).contentType(REQUEST_CONTENT_TYPE).content(bodyContent))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location", is(expectedLocation)))
                .andExpect(content().contentType(RESPONSE_CONTENT_TYPE))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/v1/realtime/" + locationCode)))
                .andExpect(jsonPath("$._links.hourly_forecast.href", is("http://localhost/v1/hourly/" + locationCode)))
                .andExpect(jsonPath("$._links.daily_forecast.href", is("http://localhost/v1/daily/" + locationCode)))
                .andExpect(jsonPath("$._links.full_forecast.href", is("http://localhost/v1/full/" + locationCode)))
                .andDo(print());
    }
}
