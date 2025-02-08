package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class Vehicle {

    private String id;
    private Naming naming;
    private Media media;
    private Battery battery;
    private Range range;

    // Constructeur personnalisé
    public Vehicle(String id, Naming naming, Media media, Battery battery, Range range) {
        this.id = id;
        this.naming = naming;
        this.media = media;
        this.battery = battery;
        this.range = range;
    }

    @Getter
    @Setter
    public static class Naming {
        @JsonProperty("make")
        private String make;
    
        @JsonProperty("model")
        private String model;
    
        @JsonProperty("chargetrip_version")
        private String chargetrip_version;

        // Constructeur de Naming
        public Naming(String make, String model, String chargetrip_version) {
            this.make = make;
            this.model = model;
            this.chargetrip_version = chargetrip_version;
        }
    }

    @Getter
    @Setter
    public static class Media {
        private Image image;

        @Getter
        @Setter
        public static class Image {
            private String thumbnail_url;
        }
    }

    @Getter
    @Setter
    public static class Battery {
        private double usable_kwh;

        // Constructeur de Battery
        public Battery(double usable_kwh) {
            this.usable_kwh = usable_kwh;
        }
    }

    @Getter
    @Setter
    public static class Range {
        private ChargetripRange chargetrip_range;

        // Constructeur de Range
        public Range(ChargetripRange chargetrip_range) {
            this.chargetrip_range = chargetrip_range;
        }

        @Getter
        @Setter
        public static class ChargetripRange {
            private float best;
            private float worst;

            // Constructeur de ChargetripRange
            public ChargetripRange(float best, float worst) {
                this.best = best;
                this.worst = worst;
            }
        }
    }
}
