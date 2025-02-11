import { useState, useEffect } from "react";
import MapComponent from "./components/MapComponent";
import VehicleSelector from "./components/VehicleSelector";
import LocationInput from "./components/LocationInput";
import { getTrajet, getTrajetComplet } from "./api/api";
import "./styles.css";

function App() {
  const [selectedVehicle, setSelectedVehicle] = useState(null);
  const [trajet, setTrajet] = useState(null);
  const [depart, setDepart] = useState("");
  const [arrivee, setArrivee] = useState("");
  const [distance, setDistance] = useState(null);  // Ajout de l'état distance
  const [tempsTrajet, setTempsTrajet] = useState(null); // Ajout de l'état temps du trajet

  // 🏁 Met à jour `depart` et `arrivee` à chaque modification de `LocationInput`
  const handleDepartArriveeChange = (depart, arrivee) => {
    setDepart(depart);
    setArrivee(arrivee);
    console.log("📍 Nouvelle recherche :", depart, "➡️", arrivee);
  };

  useEffect(() => {
    if (depart && arrivee) {
      if (!selectedVehicle) {
        console.log("📍 Recherche itinéraire simple :", depart, "➡️", arrivee);
        getTrajet(depart, arrivee).then((data) => {
          setTrajet(data);
          setDistance(data.distance);  // Mise à jour de la distance
          setTempsTrajet(data.time);   // Mise à jour du temps du trajet
        });
      } else {
        console.log("🚗 Recherche itinéraire avec véhicule :", selectedVehicle.naming.make, selectedVehicle.naming.model);
        
        const { id, range } = selectedVehicle;
        const bestRange = range.chargetrip_range.best;
        const worstRange = range.chargetrip_range.worst;
  
        getTrajetComplet(depart, arrivee, id, bestRange, worstRange)
          .then((data) => {
            console.log("✅ Trajet complet reçu :", data);
            setTrajet(data);
            setDistance(data.distance);  // Mise à jour de la distance
            setTempsTrajet(data.time);   // Mise à jour du temps du trajet
          })
          .catch((error) => console.error("❌ Erreur API trajet complet :", error));
      }
    }
  }, [depart, arrivee, selectedVehicle]); // 🔥 Se déclenche à chaque changement

  return (
    <div className="app-container">
      <MapComponent trajet={trajet} />
      
      <div className="top-right">
        <LocationInput onDepartArriveeChange={handleDepartArriveeChange} />
      </div>

      <div className="bottom-left">
        <VehicleSelector
          onSelect={setSelectedVehicle}
          trajet={trajet}
          distance={distance}
          tempsTrajet={tempsTrajet}  // Passer les props
        />
      </div>
    </div>
  );
}

export default App;
