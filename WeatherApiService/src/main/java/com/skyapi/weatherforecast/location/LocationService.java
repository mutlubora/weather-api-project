package com.skyapi.weatherforecast.location;

import com.skyapi.weatherforecast.common.Location;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class LocationService {
    private final LocationRepository locationRepository;

    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public Location add(Location location) {
        return locationRepository.save(location);
    }

    public List<Location> list() {
        return locationRepository.findUntrashed();
    }

    public Location get(String code){
        Location location = locationRepository.findByCode(code);

        if (location == null) {
            throw new LocationNotFoundException(code);
        }

        return location;
    }

    public Location update(Location locationInRequest){

        String code = locationInRequest.getCode();
        Location locationInDB = locationRepository.findByCode(code);

        if (locationInDB == null) {
            throw new LocationNotFoundException(code);
        }

        locationInDB.setCountryCode(locationInRequest.getCountryCode());
        locationInDB.setCountryName(locationInRequest.getCountryName());
        locationInDB.setRegionName(locationInRequest.getRegionName());
        locationInDB.setCityName(locationInRequest.getCityName());
        locationInDB.setEnabled(locationInRequest.isEnabled());

        return locationRepository.save(locationInDB);
    }

    public void delete(String code){
        Location location = locationRepository.findByCode(code);
        if (location == null) {
            throw new LocationNotFoundException(code);
        }

        locationRepository.trashByCode(code);
    }
}
