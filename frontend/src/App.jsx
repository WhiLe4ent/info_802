import { useEffect, useState } from "react";
import { getTrajet } from "./api";
import { MapContainer, TileLayer, Marker, Polyline, Popup } from "react-leaflet";
import "leaflet/dist/leaflet.css";

function App() {
  const [trajet, setTrajet] = useState(null);
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

    
  return (
    
    <div style={{ width: "100vw", height: "100vh" }}>
      <MapContainer center={depart} zoom={6} style={{ width: "100%", height: "100%" }}>
        <TileLayer
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        />

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
        {console.log(
          "🔍 Vérification des bornes de recharge :",
          trajet?.bornes_recharge.map((borne, index) => ({
            index,
            coordonnees: borne.fields?.coordonnees,
            adresse: borne.fields?.adresse,
            type: Array.isArray(borne.fields?.coordonnees) ? "tableau" : "non tableau",
            longueur: borne.fields?.coordonnees?.length ?? "inconnu"
          }))
        )}
        {trajet?.bornes_recharge
          ?.filter(borne =>
            Array.isArray(borne.fields?.coordonnees) &&
            borne.fields.coordonnees.length === 2 &&
            typeof borne.fields.coordonnees[0] === "number" &&
            typeof borne.fields.coordonnees[1] === "number" &&
            !isNaN(borne.fields.coordonnees[0]) &&
            !isNaN(borne.fields.coordonnees[1])
          ) // Filtrage des données invalides
          .map((borne, index) => (
            <Marker 
              key={index} 
              position={[borne.fields.coordonnees[0], borne.fields.coordonnees[1]]}>
              <Popup>{borne.fields.adresse || "Borne de recharge"}</Popup>
            </Marker>
          ))
        }


      </MapContainer>
    </div>
  );
}

export default App;
