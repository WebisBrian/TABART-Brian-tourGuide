# Technologies

> Java 17  
> Spring Boot 3.X  
> JUnit 5  

# Setup — Install local dependencies

Run the following commands **once** from the **project root** (works on Windows, Linux and macOS) :

```bash
mvn install:install-file -Dfile=libs/gpsUtil.jar -DgroupId=gpsUtil -DartifactId=gpsUtil -Dversion=1.0.0 -Dpackaging=jar
mvn install:install-file -Dfile=libs/RewardCentral.jar -DgroupId=rewardCentral -DartifactId=rewardCentral -Dversion=1.0.0 -Dpackaging=jar
mvn install:install-file -Dfile=libs/TripPricer.jar -DgroupId=tripPricer -DartifactId=tripPricer -Dversion=1.0.0 -Dpackaging=jar
```

# Build & tests

Run all tests (excluding performance tests) :

```bash
mvn test -DexcludedGroups=performance
```

Build and run all tests including performance tests (100,000 users — expect several minutes) :

```bash
mvn verify
```

Run performance tests only :

> ⚠ These tests run against 100,000 users and may take several minutes to complete.
> - `highVolumeTrackLocation` : must finish within 15 minutes -> Actually 398 seconds (6.6 minutes)
> - `highVolumeGetRewards` : must finish within 20 minutes -> Actually 259 seconds (4.3 minutes)

```bash
mvn test -Dtest=TestPerformance
```

# Continuous Integration

The project uses **GitHub Actions** for CI, triggered on every push and pull request to `main` and `develop`.

Pipeline steps (`.github/workflows/ci.yml`) :

| Step | Command | Description |
|---|---|---|
| Compile | `mvn compile` | Verifies the code compiles without errors |
| Test | `mvn test` | Runs all tests except performance tests |
| Build | `mvn package` | Produces the executable JAR artifact |

> Performance tests are excluded from the CI pipeline as they run against 100,000 users and would significantly slow down the pipeline. Run them locally with `mvn test -Dtest=TestPerformance`.

# Run the application

```bash
mvn spring-boot:run
```

Or via the generated JAR (after `mvn verify`) :

```bash
java -jar target/tourguide-0.0.1-SNAPSHOT.jar
```

The application starts on **http://localhost:8080**
