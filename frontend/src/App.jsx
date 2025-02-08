import { useState } from "react";
import MapComponent from "./components/MapComponent";
import VehicleSelector from "./components/VehicleSelector";
import LocationInput from "./components/LocationInput";
import { getTrajet } from "./api/api";
import "./styles.css";

function App() {
  const [selectedVehicle, setSelectedVehicle] = useState(null);
  const [trajet, setTrajet] = useState(null);

  const handleSearch = async (depart, arrivee) => {
    console.log("📍 Recherche itinéraire :", depart, "➡️", arrivee);
    const data = await getTrajet(depart, arrivee);
    setTrajet(data);
  };

  return (
    <div className="app-container">
      <MapComponent trajet={trajet} />
      <div className="top-right">
        <LocationInput onSearch={handleSearch} />
      </div>
      <div className="bottom-left">
        <VehicleSelector onSelect={setSelectedVehicle} />
      </div>
    </div>
  );
}

export default App;
