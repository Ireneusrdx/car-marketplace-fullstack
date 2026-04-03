package com.automarket.marketplace.car;

import com.automarket.marketplace.car.dto.CarMakeDto;
import com.automarket.marketplace.car.dto.CarModelDto;
import com.automarket.marketplace.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CarsService {

    private final CarMakeRepository carMakeRepository;
    private final CarModelRepository carModelRepository;

    @Transactional(readOnly = true)
    public List<CarMakeDto> getMakes() {
        return carMakeRepository.findByActiveTrueOrderByNameAsc().stream()
            .map(make -> new CarMakeDto(make.getId(), make.getName(), make.getLogoUrl(), make.getCountry()))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<CarModelDto> getModelsByMake(UUID makeId) {
        if (!carMakeRepository.existsById(makeId)) {
            throw new ResourceNotFoundException("Make not found");
        }

        return carModelRepository.findByMakeIdOrderByNameAsc(makeId).stream()
            .map(model -> new CarModelDto(model.getId(), model.getName()))
            .toList();
    }
}

