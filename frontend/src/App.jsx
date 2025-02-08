import { useEffect, useState } from "react";
import { getTrajet } from "./api";
import { MapContainer, TileLayer, Marker, Polyline, Popup } from "react-leaflet";
import "leaflet/dist/leaflet.css";
import L from "leaflet";
import carIcon from "../public/car.png";

function App() {
  const [trajet, setTrajet] = useState(null);
  const [selectedVehicle, setSelectedVehicle] = useState(null);
  const selectedVehicleData = trajet?.vehicles?.find(vehicle => vehicle.id === selectedVehicle);
  console.log("📊 Détails du véhicule sélectionné :", selectedVehicleData);

  const depart = [48.8566, 2.3522]; // Paris
  const arrivee = [45.7640, 4.8357]; // Lyon
 
  useEffect(() => {
    async function fetchData() {
      try {
        const data = await getTrajet(depart[0], depart[1], arrivee[0], arrivee[1]);
        console.log("📡 Données reçues :", data);
  
        // Si geometry est une string JSON, on la convertit
        if (typeof data.itineraire?.geometry === "string") {
          data.itineraire.geometry = JSON.parse(data.itineraire.geometry);
        }
  
        setTrajet(data);
      } catch (error) {
        console.error("Erreur API :", error);
      }
    }
    fetchData();
  }, []);
  
  useEffect(() => {
    if (trajet?.itineraire?.geometry?.coordinates) {
      console.log("🛠️ Mise à jour de coordinates :", trajet.itineraire.geometry.coordinates);
    }
  }, [trajet]);
  
  if (!trajet) {
    console.log("⏳ En attente des données...");
    return <div>Chargement des données...</div>;
  }
  console.log("📍 Vérification itinéraire :", trajet?.itineraire);
  console.log("📍 Vérification geometry :", trajet?.itineraire?.geometry);
  console.log("📍 Vérification coordinates :", trajet?.itineraire?.geometry?.coordinates);
  console.log("📍 Type de coordinates :", typeof trajet?.itineraire?.geometry?.coordinates);
  console.log("📍 Première coordonnée :", trajet?.itineraire?.geometry?.coordinates?.[0]);

  const vehicleIcon = new L.Icon({
    iconUrl: carIcon,
    iconSize: [40, 40], // Taille de l'icône
    iconAnchor: [20, 40], // Ancrage
    popupAnchor: [0, -40] // Position du popup
  });

  const distanceTrajet = trajet?.itineraire?.distance_km || 0;
  const autonomieVehicule = selectedVehicleData?.bestRange || 0;
  const rechargeNecessaire = distanceTrajet > autonomieVehicule;

  console.log("📏 Distance du trajet :", distanceTrajet, "km");
  console.log("🔋 Autonomie max du véhicule :", autonomieVehicule, "km");
  console.log(rechargeNecessaire ? "⚠️ Recharge nécessaire !" : "✅ Pas besoin de recharge.");

  const distanceMaxBorne = 5; // km autour de l'itinéraire

  const bornesUtiles = trajet?.bornes_recharge?.filter((borne) => {
    const [lat, lon] = borne.fields.coordonnees || [];
    if (!lat || !lon) return false;

    // On vérifie si la borne est à moins de X km d'un point de l'itinéraire
    return trajet.itineraire.geometry.coordinates.some(([trajLon, trajLat]) => {
      const distance = Math.sqrt((trajLat - lat) ** 2 + (trajLon - lon) ** 2) * 111; // Approximation
      return distance < distanceMaxBorne;
    });
  });

  console.log("🔋 Bornes utiles :", bornesUtiles);

  

  return (
    <div>
      <h2>🚗 Sélectionne un véhicule :</h2>
      <select onChange={(e) => setSelectedVehicle(e.target.value)}>
        <option value="">-- Choisir un véhicule --</option>
        {trajet?.vehicles?.map((vehicle) => (
          <option key={vehicle.id} value={vehicle.id}>
            {vehicle.make} {vehicle.model} - ⚡ {vehicle.batteryCapacity} kWh
          </option>
        ))}
      </select>
      <div style={{ width: "100vw", height: "100vh" }}>
      {rechargeNecessaire && (
        <div style={{ padding: "10px", backgroundColor: "#ffcc00", color: "#000", textAlign: "center" }}>
          ⚠️ L'autonomie du véhicule est insuffisante ! Vous devrez recharger en chemin.
        </div>
      )}

        <MapContainer center={depart} zoom={6} style={{ width: "100%", height: "100%" }}>
          <TileLayer
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          />

          {selectedVehicleData && (
            <Marker position={depart} icon={vehicleIcon}>
              <Popup>
                🚗 {selectedVehicleData.make} {selectedVehicleData.model} <br />
                🔋 Batterie : {selectedVehicleData.batteryCapacity} kWh <br />
                📏 Autonomie : {selectedVehicleData.bestRange} km (max) <br />
              </Popup>
            </Marker>
          )}



          {/* Affichage de l'itinéraire */}
          {trajet?.itineraire?.geometry?.coordinates &&
            Array.isArray(trajet.itineraire.geometry.coordinates) &&
            trajet.itineraire.geometry.coordinates.length > 0 ? (
              <Polyline
                positions={trajet.itineraire.geometry.coordinates
                  .filter(coord => 
                    Array.isArray(coord) && 
                    coord.length === 2 && 
                    !isNaN(coord[0]) && 
                    !isNaN(coord[1])
                  ) // Vérification stricte des valeurs
                  .map(([lon, lat]) => [lat, lon])} // Inversion correcte
                color="blue"
              />
            ) : (
              console.warn("⚠️ Itinéraire non affiché, données invalides :", trajet?.itineraire?.geometry)
            )}


          {/* Affichage des bornes de recharge */}
          {bornesUtiles.map((borne, index) => (
            <Marker key={index} position={[borne.fields.coordonnees[0], borne.fields.coordonnees[1]]}>
              <Popup>
                ⚡ {borne.fields.adresse || "Borne de recharge"} <br />
                🔌 Type : {borne.fields.type_prise || "Inconnu"} <br />
                💰 Accès : {borne.fields.acces_recharge || "Non précisé"} <br />
              </Popup>
            </Marker>
          ))}



        </MapContainer>
      </div>
    </div>
  );
}

export default App;
