import { useState, useEffect } from "react";
import { MapContainer, TileLayer, Polyline, Marker, Popup } from "react-leaflet";
import "leaflet/dist/leaflet.css";
import L from "leaflet";


const chargingStationIcon = new L.DivIcon({
  className: "custom-charging-icon", // Classe CSS pour personnaliser l'affichage
  html: "📍", // Utilisation d'un emoji ou d'un texte
  iconSize: [30, 30], 
  iconAnchor: [15, 30],
  popupAnchor: [0, -30]
});

function MapComponent({ trajet }) {
  const [coordinates, setCoordinates] = useState([]);
  const [bornes, setBornes] = useState([]);
  const [mapCenter, setMapCenter] = useState([48.8566, 2.3522]); // Position initiale
  const [isLoading, setIsLoading] = useState(true); // Ajout du chargement

  useEffect(() => {
    console.log("📡 Trajet reçu dans MapComponent :", trajet);

    if (trajet) {
      setIsLoading(false); // Les données sont chargées

      // Récupérer l'itinéraire
      if (trajet?.geometry?.coordinates) {
        const itineraryCoordinates = trajet.geometry.coordinates.map(([lon, lat]) => [lat, lon]);
        setCoordinates(itineraryCoordinates);

        // Déterminer le centre de la carte
        const latitudes = itineraryCoordinates.map(coord => coord[0]);
        const longitudes = itineraryCoordinates.map(coord => coord[1]);

        const centerLat = (Math.min(...latitudes) + Math.max(...latitudes)) / 2;
        const centerLon = (Math.min(...longitudes) + Math.max(...longitudes)) / 2;
        setMapCenter([centerLat, centerLon]);
      }

      // Vérifier les bornes de recharge
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

      {/* Affichage de l'itinéraire */}
      {coordinates.length > 0 && <Polyline positions={coordinates} color="blue" />}

      {/* Affichage des bornes de recharge */}
      {bornes.length > 0 ? (
        bornes.map((borne, index) => {
          const coords = borne.geometry?.coordinates; // ✅ Correction ici
          if (!coords || coords.length !== 2) return null; // Vérifier si les coordonnées sont valides

          const [lon, lat] = coords; // Inverser pour Leaflet (lat, lon)
          console.log(`📍 Affichage borne ${index} à ${lat},${lon}`);

          return (
            <Marker key={index} position={[lat, lon]} icon={chargingStationIcon}>
              <Popup>
                ⚡ <strong>Borne de recharge</strong> <br />
                📍 {borne.fields?.n_station || "Adresse inconnue"} <br />
                🔌 Type : {borne.fields?.type_prise || "Non précisé"} <br />
                💰 Accès : {borne.fields?.acces_recharge || "Non précisé"}
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
