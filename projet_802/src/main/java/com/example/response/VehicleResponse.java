package com.example.response;

import com.example.model.Vehicle;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class VehicleResponse {

    private Data data;

    public Data getData() {
        return data != null ? data : new Data();
    }

    public static class Data {
        private List<Vehicle> vehicleList;

        public List<Vehicle> getVehicleList() {
            return vehicleList != null ? vehicleList : new ArrayList<>(); 
        }
    }
    
}
