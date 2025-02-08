const API_URL = "http://localhost:8080"; 

export async function getTrajet(depart, arrivee, vehicleId) {
  try {
    const response = await fetch(`${API_URL}/api/trajet`, {
      method: "POST", 
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ departVille: depart, arriveeVille: arrivee, vehicleId }),
    });
    const data = await response.json();
    console.log("🚗 Trajet trouvé :", data);
    return data;
  } catch (error) {
    console.error("❌ Erreur API trajet :", error);
    return null;
  }
}


export async function fetchVehicles(page = 0, size = 10, search = '') {
  const query = "query GetVehicles($page: Int!, $size: Int!, $search: String) { carList(page: $page, size: $size, search: $search) { id naming { make model } battery { usable_kwh } range { chargetrip_range { best worst } } } }";

  const variables = {
    page,
    size,
    search
  };

  try {
    const response = await fetch('http://localhost:8080/graphql', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        query,
        variables
      })
    });

    const { data } = await response.json();
    return data.carList || [];
  } catch (error) {
    console.error('Erreur lors de la récupération des véhicules:', error);
    return [];
  }
}
