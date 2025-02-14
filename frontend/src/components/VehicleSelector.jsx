import React, { useState, useEffect } from 'react';
import { fetchVehicles } from '../api/api';
import VehicleCard from './VehicleCard';

function VehicleSelector({ onSelect, trajet, distance, tempsTrajet }) {
  const [vehicles, setVehicles] = useState([]);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [search, setSearch] = useState('');
  const [selectedVehicle, setSelectedVehicle] = useState(null);
  const [autonomie, setAutonomie] = useState(""); 
  const [tempsRecharge, setTempsRecharge] = useState("30"); 
  const [tempsTotal, setTempsTotal] = useState(trajet?.temps_total || ""); 


  useEffect(() => {
    async function loadVehicles() {
      setLoading(true);
      const newVehicles = await fetchVehicles(page, size, search);
      setVehicles(newVehicles);
      setLoading(false);
    }
    loadVehicles();
  }, [page, size, search]);

  useEffect(() => {
    if (typeof trajet.temps_total === "number" && !isNaN(trajet.temps_total)) {
      const hours = Math.floor(trajet.temps_total);
      const minutes = Math.round((trajet.temps_total - hours) * 60);

      const formattedTime = `${hours}h${minutes < 10 ? '0' : ''}${minutes}`;
      
      setTempsTotal(formattedTime);
    } else {
      setTempsTotal("--:--"); // Valeur par défaut en cas d'erreur
    }
  }, [tempsTrajet]);


  
  const handleSearchChange = (event) => {
    setSearch(event.target.value);
    setPage(0);
  };

  const handleVehicleSelect = (vehicle) => {
    console.log("✅ Véhicule sélectionné :", vehicle);
    setSelectedVehicle(vehicle);
    setAutonomie(vehicle.range.chargetrip_range.worst); 
  };

  const handleBackToList = () => {
    setSelectedVehicle(null);
  };

  const handleChoisirVehicule = () => {
    const selectedVehicleData = {
      ...selectedVehicle,
      autonomie: autonomie,
      tempsRecharge: tempsRecharge
    };
    onSelect(selectedVehicleData);
  };

  return (
    <div className="vehicle-selector">
      {selectedVehicle ? (
        <div className="vehicle-detail">
          <button className="back-button" onClick={handleBackToList}>← Retour</button>
          <h2>{selectedVehicle.naming.make} {selectedVehicle.naming.model}</h2>
          <div className="selected-vehicle-card">
            <img src={selectedVehicle.media?.image?.thumbnail_url || 'default-image.jpg'} alt="Véhicule" />
            <div className="vehicle-info">
              <p><strong>Batterie :</strong> {selectedVehicle.battery.usable_kwh} kWh</p>
              <p><strong>Autonomie :</strong></p>
              <input 
                type="number" 
                value={autonomie} 
                onChange={(e) => setAutonomie(e.target.value)}
              />
              <p><strong>Temps de recharge :</strong></p>
              <input 
                type="number" 
                value={tempsRecharge} 
                onChange={(e) => setTempsRecharge(e.target.value)}
              /> min
              <p><strong>Distance du trajet :</strong> {distance || "0"} km</p>
              <p><strong>Temps total :</strong> {trajet.temps_total || "--:--"} h</p>
              <button className="choose-button" onClick={handleChoisirVehicule}>✅ Choisir ce véhicule</button>
            </div>
          </div>
        </div>
      ) : (
        <>
          <div className="search-bar">
            <input
              type="text"
              value={search}
              onChange={handleSearchChange}
              placeholder="Rechercher un véhicule..."
            />
          </div>
          <div className="vehicle-list">
            {loading ? (
              <p>Chargement...</p>
            ) : (
              vehicles.map((vehicle) => (
                <VehicleCard key={vehicle.id} vehicle={vehicle} onClick={handleVehicleSelect} />
              ))
            )}
          </div>
          <div className="pagination">
            <button onClick={() => setPage(page - 1)} disabled={page <= 0}>Précédent</button>
            <span>{page + 1}</span>
            <button onClick={() => setPage(page + 1)}>Suivant</button>
          </div>
        </>
      )}
    </div>
  );
}

export default VehicleSelector;
