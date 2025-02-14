import { useState, useEffect } from "react";
import { MapContainer, TileLayer, Polyline, Marker, Popup } from "react-leaflet";
import "leaflet/dist/leaflet.css";
import markerIconPng from "leaflet/dist/images/marker-icon.png";
import markerShadowPng from "leaflet/dist/images/marker-shadow.png";
import L from "leaflet";

function MapComponent({ trajet }) {
  const [segments, setSegments] = useState([]);
  const [bornes, setBornes] = useState([]);
  const [mapCenter, setMapCenter] = useState([48.8566, 2.3522]);
  const [isLoading, setIsLoading] = useState(true);
  const [depart, setDepart] = useState(null);

  useEffect(() => {
    console.log("📡 Trajet reçu dans MapComponent :", trajet);

    if (trajet) {
      setIsLoading(false);
      if (Array.isArray(trajet.segments)) {
        const parsedSegments = trajet.segments.map(segment => {
          if (segment.itineraire?.geometry?.coordinates) {
            return segment.itineraire.geometry.coordinates.map(([lon, lat]) => [lat, lon]);
          }
          return [];
        }).filter(segment => segment.length > 0);

        setSegments(parsedSegments);

        // Déterminer le centre de la carte
        const allCoords = parsedSegments.flat();
        if (allCoords.length > 0) {
          const latitudes = allCoords.map(coord => coord[0]);
          const longitudes = allCoords.map(coord => coord[1]);
          setMapCenter([
            (Math.min(...latitudes) + Math.max(...latitudes)) / 2,
            (Math.min(...longitudes) + Math.max(...longitudes)) / 2
          ]);
        }

        // Définir la ville de départ
        if (parsedSegments.length > 0 && parsedSegments[0].length > 0) {
          setDepart(parsedSegments[0][0]);
        }
      }

      if (Array.isArray(trajet.bornes)) {
        setBornes(trajet.bornes);
        console.log("🔋 Bornes de recharge :", trajet.bornes);
      }
    }
  }, [trajet]);
  

  return (
    <MapContainer center={mapCenter} zoom={6} className="leaflet-container">
      <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" attribution='&copy; OpenStreetMap contributors' />

      {isLoading && <div className="loading-overlay"><p>⏳ Chargement...</p></div>}

      {depart && (
        <Marker
          position={depart}
          icon={L.icon({
            iconUrl: markerIconPng,
            shadowUrl: markerShadowPng,
            iconSize: [30, 50],
            iconAnchor: [15, 50],
            popupAnchor: [0, -50],
            shadowSize: [50, 50]
          })}
        >
          <Popup>
            <div className="popup-content">
              <h3>🚀 Départ</h3>
              <p>📍 Ville de départ</p>
            </div>
          </Popup>
        </Marker>
      )}

      {segments.map((segment, index) => (
        <>
          <Polyline key={index} positions={segment} color="blue" />
          
          {/* Marqueur à la fin du segment */}
          {segment.length > 0 && (
            <Marker
              key={`segment-${index}`}
              position={segment[segment.length - 1]}
              icon={L.icon({
                iconUrl: markerIconPng,
                shadowUrl: markerShadowPng,
                iconSize: [25, 41],
                iconAnchor: [12, 41],
                popupAnchor: [1, -34],
                shadowSize: [41, 41]
              })}
            >
              <Popup>
                <div className="popup-content">
                  <h3>🔵 Fin du segment {index + 1}</h3>
                  <p>📍 Point intermédiaire du trajet</p>
                </div>
              </Popup>
            </Marker>
          )}
        </>
      ))}

    {bornes.map((borne, index) => {
      return (
        <Marker
        key={`borne-${borne.lat}-${borne.lon}`} 
        position={[borne.lat, borne.lon]}
          icon={L.icon({
            iconUrl: markerIconPng,
            shadowUrl: markerShadowPng,
            iconSize: [25, 41],
            iconAnchor: [12, 41],
            popupAnchor: [1, -34],
            shadowSize: [41, 41]
          })}
        >
          <Popup>
            <div className="popup-content">
              <h3>⚡ Borne de recharge</h3>
              <p>📍 Localisation : [{borne.lat}, {borne.lon}]</p>
            </div>
          </Popup>
        </Marker>
      );
  })}
    </MapContainer>
  );
}

export default MapComponent;
