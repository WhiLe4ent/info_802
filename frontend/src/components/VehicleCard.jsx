// VehicleCard.js
import React from 'react';

function VehicleCard({ vehicle, onClick }) {
  return (
    <div className="vehicle-card" onClick={() => onClick(vehicle)}>
      {/* Affichage de l'image du véhicule */}
      <img
        src={vehicle.media?.image?.thumbnail_url || 'default-image.jpg'}
        alt={`${vehicle.naming.make} ${vehicle.naming.model}`}
        className="vehicle-image"
      />
      <div className="vehicle-info">
        <h3>{vehicle.naming.make} {vehicle.naming.model}</h3>
        <p>Batterie: {vehicle.battery.usable_kwh} kWh</p>
        <p>Autonomie: {vehicle.range.chargetrip_range.best} km - {vehicle.range.chargetrip_range.worst} km</p>
      </div>
    </div>
  );
}

export default VehicleCard;
