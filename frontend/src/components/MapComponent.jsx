import { useState, useEffect } from "react";
import { MapContainer, TileLayer, Polyline, Marker, Popup } from "react-leaflet";
import "leaflet/dist/leaflet.css";
import markerIconPng from "leaflet/dist/images/marker-icon.png";
import markerShadowPng from "leaflet/dist/images/marker-shadow.png";
import L from "leaflet";

function MapComponent({ trajet }) {
  const [coordinates, setCoordinates] = useState([]);
  const [bornes, setBornes] = useState([]);
  const [mapCenter, setMapCenter] = useState([48.8566, 2.3522]);
  const [isLoading, setIsLoading] = useState(true);
  const [departCoords, setDepartCoords] = useState(null);
  const [arriveeCoords, setArriveeCoords] = useState(null);

  useEffect(() => {
    console.log("📡 Trajet reçu dans MapComponent :", trajet);

    if (trajet) {
      setIsLoading(false); // Fin du chargement

      // 📍 Récupérer l'itinéraire
      if (trajet?.geometry?.coordinates) {
        const itineraryCoordinates = trajet.geometry.coordinates.map(([lon, lat]) => [lat, lon]);
        setCoordinates(itineraryCoordinates);

        // 📍 Mettre à jour les coordonnées du départ et de l'arrivée
        if (itineraryCoordinates.length > 1) {
          setDepartCoords(itineraryCoordinates[0]); // Premier point = départ
          setArriveeCoords(itineraryCoordinates[itineraryCoordinates.length - 1]); // Dernier point = arrivée
        }

        // 🗺 Déterminer le centre de la carte
        const latitudes = itineraryCoordinates.map(coord => coord[0]);
        const longitudes = itineraryCoordinates.map(coord => coord[1]);

        const centerLat = (Math.min(...latitudes) + Math.max(...latitudes)) / 2;
        const centerLon = (Math.min(...longitudes) + Math.max(...longitudes)) / 2;
        setMapCenter([centerLat, centerLon]);
      }

      // 🔋 Vérifier les bornes de recharge
      if (Array.isArray(trajet?.bornes_recharge)) {
        console.log("🔌 Bornes reçues :", trajet.bornes_recharge);
        setBornes(trajet.bornes_recharge);
      } else {
        console.warn("⚠️ Aucune borne reçue ou mauvais format :", trajet?.bornes_recharge);
      }
    }
  }, [trajet]);

  return (
    <MapContainer center={mapCenter} zoom={6} className="leaflet-container">
      <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" attribution='&copy; OpenStreetMap contributors' />

      {/* Affichage du message de chargement */}
      {isLoading && (
        <div className="loading-overlay">
          <p>⏳ Chargement de l'itinéraire et des bornes...</p>
        </div>
      )}

      {/* 📍 Affichage du trajet */}
      {coordinates.length > 0 && <Polyline positions={coordinates} color="blue" />}

      {/* 🟢 Marqueur de départ */}
      {departCoords && (
        <Marker
          key="depart"
          position={departCoords}
          icon={L.icon({
            iconUrl: markerIconPng,
            shadowUrl: markerShadowPng,
            iconSize: [25, 41],
            iconAnchor: [12, 41],
            popupAnchor: [1, -34], 
            shadowSize: [41, 41], 
          })}
          className="custom-marker"
        >
          <Popup>
            <div className="popup-content">
              <h3>🚀 Départ</h3>
              <p>📍 Point de départ de l'itinéraire</p>
            </div>
          </Popup>
        </Marker>
      )}

      {/* 🔴 Marqueur d'arrivée */}
      {arriveeCoords && (
        <Marker
          key="arrivee"
          position={arriveeCoords}
          icon={L.icon({
            iconUrl: markerIconPng,
            shadowUrl: markerShadowPng,
            iconSize: [25, 41], 
            iconAnchor: [12, 41], 
            popupAnchor: [1, -34], 
            shadowSize: [41, 41],
          })}
          className="custom-marker"
        >
          <Popup>
            <div className="popup-content">
              <h3>🏁 Arrivée</h3>
              <p>📍 Point d'arrivée de l'itinéraire</p>
            </div>
          </Popup>
        </Marker>
      )}

      {/* ⚡ Affichage des bornes de recharge */}
      {bornes.length > 0 ? (
        bornes.map((borne, index) => {
          const coords = borne.geometry?.coordinates;
          if (!coords || coords.length !== 2) return null;
          const [lon, lat] = coords;

          return (
            <Marker
              key={index}
              position={[lat, lon]}
              icon={L.icon({
                iconUrl: markerIconPng,
                shadowUrl: markerShadowPng,
                iconSize: [25, 41], 
                iconAnchor: [12, 41],
                popupAnchor: [1, -34],
                shadowSize: [41, 41], 
              })}
              className="custom-marker"
            >
              <Popup>
                <div className="popup-content">
                  <h3>⚡ Borne de recharge</h3>
                  <p><strong>📍 Adresse :</strong> {borne.fields?.n_station || "Non précisé"}</p>
                  <p><strong>🔌 Type :</strong> {borne.fields?.type_prise || "Non précisé"}</p>
                  <p><strong>💰 Accès :</strong> {borne.fields?.acces_recharge || "Non précisé"}</p>
                </div>
              </Popup>
            </Marker>
          );
        })
      ) : (
        !isLoading && <p>⚠️ Aucune borne trouvée sur l'itinéraire.</p>
      )}
    </MapContainer>
  );
}

export default MapComponent;
