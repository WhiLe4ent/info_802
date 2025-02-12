const API_URL = "http://localhost:8080"; 

export async function getTrajet(depart, arrivee, vehicleId) {
  try {
    const response = await fetch(`${API_URL}/itineraire`, {
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

export async function getTrajetComplet(depart, arrivee, vehicleId, bestRange, worstRange) {
  try {
    const response = await fetch(`https://master1-backend.azurewebsites.net/api/trajet-complet`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        departVille: depart,
        arriveeVille: arrivee,
        vehicleId,
        bestRange,
        worstRange
      }),
    });

    const data = await response.json();
    console.log("🚗 Trajet complet reçu :", data);
    // const duree = getTrajetDuration(data.distance_km, worstRange, 30);
    
    return data;
  } catch (error) {
    console.error("❌ Erreur API trajet complet :", error);
    return null;
  }
}



export async function fetchVehicles(page = 0, size = 10, search = '') {
  const query = "query GetVehicles($page: Int!, $size: Int!, $search: String) { carList(page: $page, size: $size, search: $search) { id naming { make model } media { image { thumbnail_url } }battery { usable_kwh } range { chargetrip_range { best worst } } } }";

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
    console.log('🚗 Véhicules récupérés:', data.carList);
    return data.carList || [];
  } catch (error) {
    console.error('Erreur lors de la récupération des véhicules:', error);
    return [];
  }
}

export async function getTrajetDuration(distance, autonomie, tempsRecharge) {
  const soapRequest = `
      <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:tp="http://tp.vehicule.com/ws">
          <soapenv:Header/>
          <soapenv:Body>
              <tp:CalculTrajetRequest>
                  <tp:distance>${distance}</tp:distance>
                  <tp:autonomie>${autonomie}</tp:autonomie>
                  <tp:tempsRecharge>${tempsRecharge}</tp:tempsRecharge>
              </tp:CalculTrajetRequest>
          </soapenv:Body>
      </soapenv:Envelope>`;

  try {
      const response = await fetch("http://localhost:8080/ws", {
          method: "POST",
          headers: {
              "Content-Type": "text/xml",
              "SOAPAction": ""
          },
          body: soapRequest
      });

      if (!response.ok) {
          throw new Error(`HTTP error! Status: ${response.status}`);
      }

      const textResponse = await response.text();
      
      // Parser la réponse XML
      const parser = new DOMParser();
      const xmlDoc = parser.parseFromString(textResponse, "text/xml");

      // Extraire la durée du trajet 
      const tempsTotalElement = xmlDoc.getElementsByTagNameNS("http://tp.vehicule.com/ws", "tempsTotal")[0];
      if (tempsTotalElement) {
          console.log("🚗 Durée du trajet trouvée :", tempsTotalElement.textContent);
          return tempsTotalElement.textContent;
      } else {
          throw new Error("La réponse SOAP ne contient pas l'élément <tempsTotal>");
      }

  } catch (error) {
      console.error("Erreur lors de l'appel SOAP :", error);
      return null;
  }
}
