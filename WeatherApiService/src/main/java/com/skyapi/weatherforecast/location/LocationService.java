package com.skyapi.weatherforecast.location;

import com.skyapi.weatherforecast.AbstractLocationService;
import com.skyapi.weatherforecast.common.Location;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class LocationService extends AbstractLocationService {

    public LocationService(LocationRepository locationRepository) {
        super();
        this.locationRepository = locationRepository;
    }

    public Location add(Location location) {
        return locationRepository.save(location);
    }

    public List<Location> list() {
        return locationRepository.findUntrashed();
    }


    public Location update(Location locationInRequest){

        String code = locationInRequest.getCode();
        Location locationInDB = locationRepository.findByCode(code);

        if (locationInDB == null) {
            throw new LocationNotFoundException(code);
        }

        locationInDB.copyFieldsFrom(locationInRequest);

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
