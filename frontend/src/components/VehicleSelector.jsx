// VehicleSelector.js
import React, { useState, useEffect } from 'react';
import { fetchVehicles } from '../api/api';
import VehicleCard from './VehicleCard';

function VehicleSelector({ onSelect }) {
  const [vehicles, setVehicles] = useState([]);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [search, setSearch] = useState('');
  const [selectedVehicle, setSelectedVehicle] = useState(null); // Pour gérer la vue détaillée

  useEffect(() => {
    async function loadVehicles() {
      setLoading(true);
      const newVehicles = await fetchVehicles(page, size, search);
      setVehicles(newVehicles);
      setLoading(false);
    }

    loadVehicles();
  }, [page, size, search]);

  const handleSearchChange = (event) => {
    setSearch(event.target.value);
    setPage(0); // Réinitialiser la page à 0
  };

  const handlePageChange = (newPage) => {
    setPage(newPage);
  };

  const handleSizeChange = (newSize) => {
    setSize(newSize);
  };

  const handleVehicleSelect = (vehicle) => {
    setSelectedVehicle(vehicle); // Afficher le véhicule sélectionné
  };

  const handleBackToList = () => {
    setSelectedVehicle(null); // Revenir à la liste
  };

  return (
    <div className="vehicle-selector">
      <div className="vehicle-list-container">
        {/* Si un véhicule est sélectionné, on affiche le détail, sinon on affiche la liste */}
        {selectedVehicle ? (
          <div className="vehicle-detail">
            <button onClick={handleBackToList}>Retour à la liste</button>
            <h2>{selectedVehicle.naming.make} {selectedVehicle.naming.model}</h2>
            <img src={selectedVehicle.media?.image?.thumbnail_url || 'default-image.jpg'} alt="Vehicle" />
            <p>Batterie: {selectedVehicle.battery.usable_kwh} kWh</p>
            <p>Autonomie: {selectedVehicle.range.chargetrip_range.best} km - {selectedVehicle.range.chargetrip_range.worst} km</p>
          </div>
        ) : (
          <>
            <div className="search-bar">
              <input
                type="text"
                value={search}
                onChange={handleSearchChange}
                placeholder="Recherche de véhicule"
              />
              <select value={size} onChange={(e) => handleSizeChange(Number(e.target.value))}>
                <option value="5">5</option>
                <option value="10">10</option>
                <option value="20">20</option>
              </select>
            </div>

            {loading ? (
              <p>Chargement...</p>
            ) : (
              <div className="vehicle-list">
                {vehicles.map((vehicle) => (
                  <VehicleCard
                    key={vehicle.id}
                    vehicle={vehicle}
                    onClick={handleVehicleSelect}
                  />
                ))}
              </div>
            )}

            <div className="pagination">
              <button onClick={() => handlePageChange(page - 1)} disabled={page <= 0}>Précédent</button>
              <span>{page + 1}</span>
              <button onClick={() => handlePageChange(page + 1)}>Suivant</button>
            </div>
          </>
        )}
      </div>
    </div>
  );
}

export default VehicleSelector;
