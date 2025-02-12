# Étape 1 : Utiliser une image de base avec JDK 21
FROM eclipse-temurin:21-jdk AS build

# Définir le répertoire de travail
WORKDIR /app

# Copier le fichier JAR généré
COPY target/*.jar app.jar

# Exposer le port utilisé par Spring Boot
EXPOSE 8080

# Commande pour exécuter l'application
CMD ["java", "-jar", "app.jar"]
