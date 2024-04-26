package com.skyapi.weatherforecast.location;


import com.skyapi.weatherforecast.common.Location;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/locations")
public class LocationController {
    private final LocationService locationService;
    private final ModelMapper modelMapper;

    public LocationController(LocationService locationService, ModelMapper modelMapper) {
        this.locationService = locationService;
        this.modelMapper = modelMapper;
    }

    @PostMapping
    public ResponseEntity<LocationDTO> addLocation(@RequestBody @Valid LocationDTO dto) {
        Location addedLocation = locationService.add(dto2Entity(dto));
        URI uri = URI.create("/v1/locations/" + addedLocation.getCode());

        return ResponseEntity.created(uri).body(entity2DTO(addedLocation));
    }

    @GetMapping
    public ResponseEntity<?> listLocations() {
        List<Location> locations = locationService.list();

        if (locations.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(listEntity2ListDTO(locations));
    }

    @GetMapping("/{code}")
    public ResponseEntity<?> getLocation(@PathVariable("code") String code) {
        Location location = locationService.get(code);

        return ResponseEntity.ok(entity2DTO(location));
    }

    @PutMapping
    public ResponseEntity<?> updateLocation(@RequestBody @Valid LocationDTO  dto) {
            Location updatedLocation = locationService.update(dto2Entity(dto));
            return ResponseEntity.ok(entity2DTO(updatedLocation));
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<?> deleteLocation(@PathVariable("code") String code) {
            locationService.delete(code);
            return ResponseEntity.noContent().build();
    }

    private List<LocationDTO> listEntity2ListDTO(List<Location> listEntity) {

        return listEntity.stream().map(this::entity2DTO)
                .collect(Collectors.toList());

    }

    private LocationDTO entity2DTO(Location entity)  {
        return modelMapper.map(entity, LocationDTO.class);
    }

    private Location dto2Entity(LocationDTO dto) {
        return modelMapper.map(dto, Location.class);
    }
}
