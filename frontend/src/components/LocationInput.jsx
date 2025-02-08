import { useState } from "react";

function LocationInput({ onSearch }) {
  const [depart, setDepart] = useState("");
  const [arrivee, setArrivee] = useState("");

  return (
    <div className="location-input">
      <input type="text" placeholder="Ville de départ" value={depart} onChange={(e) => setDepart(e.target.value)} />
      <input type="text" placeholder="Ville d’arrivée" value={arrivee} onChange={(e) => setArrivee(e.target.value)} />
      <button onClick={() => onSearch(depart, arrivee)}>🔍 Rechercher</button>
    </div>
  );
}

export default LocationInput;
