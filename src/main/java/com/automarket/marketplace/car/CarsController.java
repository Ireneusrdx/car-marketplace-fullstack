package com.automarket.marketplace.car;

import com.automarket.marketplace.car.dto.CarMakeDto;
import com.automarket.marketplace.car.dto.CarModelDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cars")
@RequiredArgsConstructor
public class CarsController {

    private final CarsService carsService;

    @GetMapping("/makes")
    public ResponseEntity<List<CarMakeDto>> getMakes() {
        return ResponseEntity.ok(carsService.getMakes());
    }

    @GetMapping("/makes/{makeId}/models")
    public ResponseEntity<List<CarModelDto>> getModels(@PathVariable UUID makeId) {
        return ResponseEntity.ok(carsService.getModelsByMake(makeId));
    }
}

