import { useState } from "react";

function LocationInput({ onDepartArriveeChange }) {
  const [depart, setDepart] = useState("");
  const [arrivee, setArrivee] = useState("");

  const handleSearchClick = () => {
    onDepartArriveeChange(depart, arrivee);
  };

  return (
    <div className="location-input">
      <input
        type="text"
        placeholder="Ville de départ"
        value={depart}
        onChange={(e) => setDepart(e.target.value)}
      />
      <input
        type="text"
        placeholder="Ville d’arrivée"
        value={arrivee}
        onChange={(e) => setArrivee(e.target.value)}
      />
      <button onClick={handleSearchClick}>🔍 Rechercher</button>
    </div>
  );
}

export default LocationInput;
