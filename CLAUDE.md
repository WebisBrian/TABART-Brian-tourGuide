# CLAUDE.md — TourGuide

## Projet

Application Spring Boot de planification de voyages.
Mission : correction de bugs de concurrence, optimisation des performances
(parallélisation via CompletableFuture), implémentation de getNearbyAttractions,
pipeline CI GitHub Actions.

Stack : Java 17, Spring Boot 3.1.1, Maven, IntelliJ IDEA.

## Structure

src/main/java/com/openclassrooms/tourguide/
├── TourGuideController.java  # exposition HTTP (racine du package)
├── dto/          # objets de transfert (dont NearbyAttractionDTO)
├── helper/       # utilitaires (InternalTestHelper…)
├── service/      # TourGuideService, RewardsService
├── tracker/      # Tracker (thread de suivi de position)
└── user/         # User, UserReward, UserPreferences…
libs/             # JARs locaux : gpsUtil, RewardCentral, TripPricer
.github/workflows/ # pipeline CI

## Dépendances locales

gpsUtil, RewardCentral, TripPricer ne sont pas sur Maven Central.
Ils sont dans libs/ et doivent être installés manuellement avant
tout build :

```bash
mvn install:install-file -Dfile=libs/gpsUtil.jar -DgroupId=gpsUtil -DartifactId=gpsUtil -Dversion=1.0.0 -Dpackaging=jar
mvn install:install-file -Dfile=libs/RewardCentral.jar -DgroupId=rewardCentral -DartifactId=rewardCentral -Dversion=1.0.0 -Dpackaging=jar
mvn install:install-file -Dfile=libs/TripPricer.jar -DgroupId=tripPricer -DartifactId=tripPricer -Dversion=1.0.0 -Dpackaging=jar
```

Le pipeline CI les installe via la même commande (voir workflow).

## Conventions de code

Langue : code et Javadoc en anglais, commentaires explicatifs en anglais.
Commits : format conventionnel (`fix:`, `feat:`, `perf:`, `docs:`, `ci:`).
Branches : depuis `develop`, merge vers `main` via PR.

Javadoc : couvrir les classes publiques et méthodes publiques non triviales.
Ne pas javadocer les getters/setters et méthodes dont le nom est auto-explicatif.

## Pipeline CI (GitHub Actions)

Déclenchement : push et PR sur `main` et `develop`.
Étapes : install JARs locaux → compile → test → package → upload artifact.

Tests de performance exclus du pipeline (`-DexcludedGroups=performance`).
Pour les exécuter localement : `mvn test -Dtest=TestPerformance`

## Tâches restantes

- [ ] Documenter l'API avec Swagger (springdoc-openapi)
- [ ] Javadoc sur les classes et méthodes publiques non triviales

## Invariants de concurrence (NE PAS CASSER)

Le Tracker (thread de fond) et les méthodes batch s'exécutent en parallèle
sur les mêmes User. Trois protections en place, à préserver :

- RewardsService.calculateRewards() : snapshot des deux listes en entrée
  (`new ArrayList<>(...)` sur visitedLocations ET userRewards).
  Ne JAMAIS itérer directement les listes internes du User.
- User.addUserReward() : `synchronized` (check-then-add atomique par user).
  Ne pas retirer le mot-clé ni remplacer par un check non atomique.
- Pools : ExecutorService fixe 500 threads dans TourGuideService et RewardsService.

## Ce que Claude Code ne doit pas faire

- Modifier le code des classes de test pour faire passer un test
- Introduire du multi-threading dans les classes de test
- Modifier les JARs dans libs/
- Committer directement sur main