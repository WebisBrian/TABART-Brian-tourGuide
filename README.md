<p align="center">
  <img src=".readme/logo.png" alt="TourGuide" width="640"/>
</p>

<p align="center">
  Application Spring Boot de recommandation touristique :<br/>
  suivi de la position GPS des utilisateurs, attractions proches et offres de voyage.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white" alt="Java 17"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-3.1.1-6DB33F?logo=springboot&logoColor=white" alt="Spring Boot 3.1.1"/>
  <img src="https://img.shields.io/badge/Maven-build-C71A36?logo=apachemaven&logoColor=white" alt="Maven"/>
  <img src="https://img.shields.io/badge/Tests-JUnit%205-25A162?logo=junit5&logoColor=white" alt="JUnit 5"/>
  <img src="https://img.shields.io/badge/CI-GitHub%20Actions-2088FF?logo=githubactions&logoColor=white" alt="GitHub Actions"/>
</p>

<p align="center">
  <img src=".readme/louvre_banner.jpg" alt="TourGuide" width="100%"/>
</p>

---

## Sommaire

- [Présentation](#présentation)
- [Stack technique](#stack-technique)
- [Prérequis](#prérequis)
- [Installation des dépendances locales](#installation-des-dépendances-locales)
- [Build & tests](#build--tests)
- [Tests de performance](#tests-de-performance)
- [Intégration continue](#intégration-continue)
- [Lancer l'application](#lancer-lapplication)
- [Endpoints](#endpoints)

---

## Présentation

TourGuide est une API REST qui suit la position GPS d'utilisateurs et leur propose
les attractions les plus proches ainsi que des offres de voyage personnalisées.

Le projet s'appuie sur trois librairies externes simulant des services réels à la
latence variable (`gpsUtil`, `RewardCentral`, `tripPricer`).

### Contexte & enjeux

L'application a connu une forte croissance (de quelques centaines à plusieurs dizaines
de milliers d'utilisateurs), avec une cible de **100 000 utilisateurs par jour**. À cette
échelle, deux services externes deviennent des goulots d'étranglement : `gpsUtil`
(collecte de position) et `RewardCentral` (calcul des récompenses via un réseau de
partenaires au temps de réponse non maîtrisable). Le traitement séquentiel ne tenait
plus la charge.

Cette mission couvre quatre axes :

- **Correction de bugs** ➡️ notamment des tests unitaires échouant par intermittence,
  causés par des accès concurrents non protégés.
- **Nouvelle fonctionnalité** ➡️️ `getNearbyAttractions` doit retourner les 5 attractions
  les plus proches, **quelle que soit la distance**.
- **Montée en charge** ➡️ paralléliser les traitements pour tenir 100 000 utilisateurs
  sous des seuils de temps stricts (voir [Tests de performance](#tests-de-performance)).
- **Qualité** ➡️ mettre en place une chaîne d'intégration continue produisant un
  artefact validé à chaque évolution.

> La suite de tests de performance (`TestPerformance`), simulant 100 000 utilisateurs,
> a été fournie par l'équipe Assurance Qualité et sert de référence pour valider les
> objectifs de montée en charge. C'est elle qui a révélé les problèmes de performance
> initiaux.


---

## Stack technique

| Composant | Version |
|---|---|
| Java | 17 |
| Spring Boot | 3.1.1 |
| Maven | build & gestion des dépendances |
| JUnit 5 | tests unitaires, d'intégration et de performance |
| GitHub Actions | intégration continue |

---

## Prérequis

- **JDK 17**
- **Maven 3.8+**

---

## Installation des dépendances locales

`gpsUtil`, `RewardCentral` et `tripPricer` ne sont pas disponibles sur Maven Central.
Ils sont fournis dans le dossier `libs/` et doivent être installés **une seule fois**
dans le dépôt Maven local, depuis la **racine du projet** (compatible Windows, Linux et macOS).

Exécuter les trois commandes, une par une :

**1. gpsUtil**

```bash
mvn install:install-file -Dfile=libs/gpsUtil.jar -DgroupId=gpsUtil -DartifactId=gpsUtil -Dversion=1.0.0 -Dpackaging=jar
```

**2. RewardCentral**

```bash
mvn install:install-file -Dfile=libs/RewardCentral.jar -DgroupId=rewardCentral -DartifactId=rewardCentral -Dversion=1.0.0 -Dpackaging=jar
```

**3. TripPricer**

```bash
mvn install:install-file -Dfile=libs/TripPricer.jar -DgroupId=tripPricer -DartifactId=tripPricer -Dversion=1.0.0 -Dpackaging=jar
```

---

## Build & tests

Exécuter l'ensemble des tests **hors tests de performance** :

```bash
mvn test -DexcludedGroups=performance
```

Build complet, **tous les tests inclus** (100 000 utilisateurs — plusieurs minutes attendues) :

```bash
mvn verify
```

---

## Tests de performance

> ⚠️ Ces tests s'exécutent sur **100 000 utilisateurs** et peuvent durer plusieurs minutes.

La parallélisation des traitements (CompletableFuture + pool de threads) a permis de
passer de plusieurs heures à quelques minutes :

| Mesure (100 000 users) | Baseline | Après optimisation | Seuil | |
|---|---|---|---|:---:|
| `trackLocation` | ~7 579 s (~2 h 06) | ~392 s (~6,5 min) | < 15 min | ✅ |
| `getRewards` | ~64 020 s (~17 h 47) | ~264 s (~4,4 min) | < 20 min | ✅ |

Lancer uniquement les tests de performance :

```bash
mvn test -Dtest=TestPerformance
```

---

## Intégration continue

Le projet utilise **GitHub Actions**, déclenché à chaque push et pull request sur
les branches `main` et `develop`.

Étapes du pipeline (`.github/workflows/ci.yml`) :

| Étape | Commande | Description |
|---|---|---|
| Compile | `mvn compile` | Vérifie que le code compile sans erreur |
| Test | `mvn test -DexcludedGroups=performance` | Exécute tous les tests hors performance |
| Build | `mvn package -DskipTests` | Produit l'artefact JAR exécutable |

> Les tests de performance sont exclus de la CI : ils s'exécutent sur 100 000
> utilisateurs et ralentiraient considérablement le pipeline. Ils se lancent
> localement avec `mvn test -Dtest=TestPerformance`.

---

## Lancer l'application

```bash
mvn spring-boot:run
```

Ou via le JAR généré (après `mvn verify`) :

```bash
java -jar target/tourguide-0.0.1-SNAPSHOT.jar
```

L'application démarre sur **http://localhost:8080**

---

## Endpoints

| Méthode | Endpoint | Description |
|---|---|---|
| `GET` | `/getLocation?userName=` | Dernière position GPS de l'utilisateur |
| `GET` | `/getNearbyAttractions?userName=` | Les 5 attractions les plus proches (enrichies) |
| `GET` | `/getRewards?userName=` | Récompenses accumulées |
| `GET` | `/getTripDeals?userName=` | Offres de voyage |

Exemple :

```
GET http://localhost:8080/getNearbyAttractions?userName=internalUser0
```