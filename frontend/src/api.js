const API_URL = "http://localhost:8080/api";

export async function getTrajet(departLat, departLon, arriveeLat, arriveeLon) {
    const response = await fetch(
      `${API_URL}/trajet?departLat=${departLat}&departLon=${departLon}&arriveeLat=${arriveeLat}&arriveeLon=${arriveeLon}`
    );
  
    if (!response.ok) {
      throw new Error("Erreur lors de la récupération des données");
    }
  
    const data = await response.json();
    console.log("📡 Données API reçues :", data);
    return data;
  }
  