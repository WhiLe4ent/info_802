# Déploiement du projet sur Azure avec GitHub

Le projet est déployé sur Azure avec un déploiement continu via GitHub.
J'ai choisi de faire le côté backend en spring-boot et le frontend en React.

Toutes les fonctionnalités montrées lors de la présentation sont maintenant déployées, y compris l'appel SOAP du frontend au backend pour afficher le temps total de trajet.  
Cependant, une erreur `JAXBException` survient lorsque j'essaie de déployer et de faire un appel SOAP depuis mon backend vers mon backend dans l'API REST `/api/trajet-complet`.  
Je n'ai malheureusement pas réussi à la corriger, car je n'arrive pas à la reproduire en local (sûrement un problème de dépendance dû à la version 17 de Java).

## Endpoints

- Le service SOAP est déployé à `/ws`
- Le service GraphQL est déployé à `/graphql`
- Les services REST sont disponibles aux endpoints suivants :
  - `/itineraire` (POST) : attend une requête avec `nomVilleDepart` et `nomVilleArrivee` en `string`
  - `/bornes` (GET) : attend une requête avec `latitude` (double), `longitude` (double) et `rayon` (int) (le rayon dans lequel rechercher des bornes autour des coordonnées données)
  - `/graphql` : attend une requête GraphQL avec les arguments `page`, `size` et `search`
  - `/ws` : attend une requête SOAP de type XML avec `distance` (double), `autonomie` (double) et `temps de recharge` (double)
  - `/api/trajet-complet` (POST) : attend une requête REST en JSON avec `departVille`, `arriveeVille`, `bestRange` (autonomie) et `worstRange`  
    → Ce service effectue les appels nécessaires aux autres services pour renvoyer l'ensemble du trajet avec les bornes, la distance, le temps et l'itinéraire.  
    **⚠️ Dans la version déployée, l'appel au service SOAP ne fonctionne pas ici (comme mentionné précédemment), mais il fonctionne en local et est donc envoyé depuis le frontend dans une seconde requête.**

## URL projet : https://agreeable-dune-09cad6c10.4.azurestaticapps.net/
## URL backend : https://master1-backend.azurewebsites.net

## Comment tester ?

1. Entrer les villes de départ et d'arrivée, puis cliquer sur **Rechercher** pour obtenir l'itinéraire **sans bornes de recharge**.
2. Sélectionner un véhicule dans le menu, puis cliquer sur **Choisir** pour recalculer l'itinéraire en tenant compte des bornes de recharge nécessaires.
