import React, { useState, useEffect } from 'react';
import { fetchVehicles } from '../api/api';  // Assurez-vous d'importer la fonction fetchVehicles

function VehicleSelector({ onSelect }) {
  const [vehicles, setVehicles] = useState([]);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(0); // Page par défaut
  const [size, setSize] = useState(10); // Taille de page par défaut
  const [search, setSearch] = useState(''); // Recherche par défaut

  useEffect(() => {
    async function loadVehicles() {
      setLoading(true);
      const newVehicles = await fetchVehicles(page, size, search);
      setVehicles(newVehicles);
      setLoading(false);
    }

    loadVehicles();
  }, [page, size, search]); // Recharger les véhicules à chaque modification des paramètres

  const handleSearchChange = (event) => {
    setSearch(event.target.value);
    setPage(0); // Réinitialiser la page à 0 chaque fois que la recherche change
  };

  const handlePageChange = (newPage) => {
    setPage(newPage);
  };

  const handleSizeChange = (newSize) => {
    setSize(newSize);
  };

  return (
    <div>
      <div>
        <label>Recherche :</label>
        <input
          type="text"
          value={search}
          onChange={handleSearchChange}
          placeholder="Recherche de véhicule"
        />
      </div>
      <div>
        <label>Taille de page :</label>
        <select value={size} onChange={(e) => handleSizeChange(Number(e.target.value))}>
          <option value="5">5</option>
          <option value="10">10</option>
          <option value="20">20</option>
        </select>
      </div>
      <div>
        <label>Page :</label>
        <button onClick={() => handlePageChange(page - 1)} disabled={page <= 0}>Précédent</button>
        <span>{page + 1}</span>
        <button onClick={() => handlePageChange(page + 1)}>Suivant</button>
      </div>

      {loading ? (
        <p>Chargement...</p>
      ) : (
        <div>
          <label>Choisissez un véhicule :</label>
          <select onChange={(e) => onSelect(e.target.value)}>
            <option value="">-- Sélectionnez --</option>
            {vehicles.map((vehicle) => (
              <option key={vehicle.id} value={vehicle.id}>
                {vehicle.naming.make} {vehicle.naming.model} - ⚡ {vehicle.battery.usable_kwh} kWh
              </option>
            ))}
          </select>
        </div>
      )}
    </div>
  );
}

export default VehicleSelector;
