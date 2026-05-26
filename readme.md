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

```bash
mvn verify
```

# Run the application

```bash
mvn spring-boot:run
```

Or via the generated JAR (after `mvn verify`) :

```bash
java -jar target/tourguide-0.0.1-SNAPSHOT.jar
```

The application starts on **http://localhost:8080**
