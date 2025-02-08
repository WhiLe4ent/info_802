package com.example.resolver;

import com.example.model.Vehicle;
import com.example.service.VehicleService;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.graphql.data.method.annotation.Argument;
import java.util.List;

@Controller
public class VehicleResolver {

    private final VehicleService vehicleService;

    public VehicleResolver(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @QueryMapping
    public List<Vehicle> carList(
            @Argument int page, 
            @Argument int size, 
            @Argument String search) {
        return vehicleService.getAllVehicles(page, size, search);
    }
}
