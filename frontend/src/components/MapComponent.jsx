import { MapContainer, TileLayer, Polyline } from "react-leaflet";
import "leaflet/dist/leaflet.css";
import { useState, useEffect } from "react";

function MapComponent({ trajet }) {
  const [coordinates, setCoordinates] = useState([]);
  const [mapCenter, setMapCenter] = useState([48.8566, 2.3522]); // Position initiale

  useEffect(() => {
    if (trajet?.features && trajet.features.length > 0) {
      // Extraction des coordonnées de la première feature
      const feature = trajet.features[0];
      
      if (feature.geometry.type === "LineString") {
        // Extraction des coordonnées de type LineString
        const itineraryCoordinates = feature.geometry.coordinates.map(([lon, lat]) => [lat, lon]);
        setCoordinates(itineraryCoordinates);

        // Calculer le centre de la carte en fonction des coordonnées du trajet
        const latitudes = itineraryCoordinates.map(coord => coord[0]);
        const longitudes = itineraryCoordinates.map(coord => coord[1]);

        const centerLat = (Math.min(...latitudes) + Math.max(...latitudes)) / 2;
        const centerLon = (Math.min(...longitudes) + Math.max(...longitudes)) / 2;
        setMapCenter([centerLat, centerLon]);
      }
    }
  }, [trajet]); // Déclenche l'effet chaque fois que trajet change

  return (
    <MapContainer center={mapCenter} zoom={6} className="leaflet-container">
      <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" attribution='&copy; OpenStreetMap contributors' />

      {/* Affichage de l'itinéraire si des coordonnées sont présentes */}
      {coordinates.length > 0 && (
        <Polyline positions={coordinates} color="blue" />
      )}
    </MapContainer>
  );
}

export default MapComponent;
