package com.example.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class VehicleResponse {
    @JsonProperty("data") // Assure le bon mapping
    private Data data;

    public Data getData() { return data; }
    public void setData(Data data) { this.data = data; }

    public static class Data {
        @JsonProperty("carList")
        private List<Car> carList;

        public List<Car> getCarList() { return carList; }
        public void setCarList(List<Car> carList) { this.carList = carList; }
    }

    public static class Car {
        @JsonProperty("id")
        private String id;

        @JsonProperty("naming")
        private Naming naming;

        @JsonProperty("battery")
        private Battery battery;

        @JsonProperty("range")
        private Range range;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public Naming getNaming() { return naming; }
        public void setNaming(Naming naming) { this.naming = naming; }

        public Battery getBattery() { return battery; }
        public void setBattery(Battery battery) { this.battery = battery; }

        public Range getRange() { return range; }
        public void setRange(Range range) { this.range = range; }
    }

    public static class Naming {
        @JsonProperty("make")
        private String make;

        @JsonProperty("model")
        private String model;

        public String getMake() { return make; }
        public void setMake(String make) { this.make = make; }

        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
    }

    public static class Battery {
        @JsonProperty("usable_kwh")
        private double usableKwh;

        public double getUsableKwh() { return usableKwh; }
        public void setUsableKwh(double usableKwh) { this.usableKwh = usableKwh; }
    }

    public static class Range {
        @JsonProperty("chargetrip_range")
        private ChargetripRange chargetripRange;

        public ChargetripRange getChargetripRange() { return chargetripRange; }
        public void setChargetripRange(ChargetripRange chargetripRange) { this.chargetripRange = chargetripRange; }
    }

    public static class ChargetripRange {
        @JsonProperty("best")
        private int best;

        @JsonProperty("worst")
        private int worst;

        public int getBest() { return best; }
        public void setBest(int best) { this.best = best; }

        public int getWorst() { return worst; }
        public void setWorst(int worst) { this.worst = worst; }
    }
}
