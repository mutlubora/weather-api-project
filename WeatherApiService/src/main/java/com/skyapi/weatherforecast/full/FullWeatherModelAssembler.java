package com.skyapi.weatherforecast.full;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class FullWeatherModelAssembler
        implements RepresentationModelAssembler<FullWeatherDTO, EntityModel<FullWeatherDTO>> {
    @Override
    public EntityModel<FullWeatherDTO> toModel(FullWeatherDTO dto) {
        EntityModel<FullWeatherDTO> entityModel = EntityModel.of(dto);

        entityModel.add(linkTo(
                methodOn(FullWeatherController.class).getFullWeatherByIPAddress(null))
                .withSelfRel());
        return entityModel;
    }


}
