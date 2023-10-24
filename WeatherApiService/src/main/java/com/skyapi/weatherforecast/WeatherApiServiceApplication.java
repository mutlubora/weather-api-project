package com.skyapi.weatherforecast;

import com.skyapi.weatherforecast.common.HourlyWeather;
import com.skyapi.weatherforecast.hourly.HourlyWeatherDTO;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class WeatherApiServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(WeatherApiServiceApplication.class, args);
	}

	@Bean
	public ModelMapper getModelMapper() {
		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

		TypeMap<HourlyWeather, HourlyWeatherDTO> typeMap1 =
				mapper.typeMap(HourlyWeather.class, HourlyWeatherDTO.class);

		typeMap1.addMapping(src -> src.getId().getHourOfDay(), HourlyWeatherDTO::setHourOfDay);

		TypeMap<HourlyWeatherDTO, HourlyWeather> typeMap2 =
				mapper.typeMap(HourlyWeatherDTO.class, HourlyWeather.class);

		typeMap2.addMapping(HourlyWeatherDTO::getHourOfDay, (dest, value) -> {
			dest.getId().setHourOfDay(value != null ? (Integer) value : 0);
		});

		return mapper;

	}

}
