package com.skyapi.weatherforecast.location;

import com.skyapi.weatherforecast.common.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, String> {

    @Query("SELECT l FROM Location l WHERE l.trashed = false")
    List<Location> findUntrashed();

    @Query("SELECT l FROM Location l WHERE l.code = ?1 AND l.trashed = false")
    Location findByCode(String code);

    @Modifying
    @Query("UPDATE Location l SET l.trashed = true WHERE l.code = ?1 ")
    void trashByCode(String code);

    @Query("SELECT l FROM Location l WHERE l.countryCode = ?1 AND l.cityName =?2 AND l.trashed = false")
    Location findByCountryCodeAndCityName(String countryCode, String cityName);
}
