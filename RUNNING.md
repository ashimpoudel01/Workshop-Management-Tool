# How to Run Motorcycle Workshop Manually

This project is a Maven-based Java desktop application. It builds into a shaded JAR and launches a Swing UI.

## Requirements

- Java 25
- Maven 3.9+ recommended
- A graphical desktop session

If you run it from this workspace, use the installed Java 25 binary directly because the shell default is Java 21.

## Build

From the project root:

```bash
mvn clean package
```

This creates the runnable JAR at:

```bash
target/MotorcycleWorkshop-1.0.0.jar
```

## Run

Use the Java 25 runtime to start the app:

```bash
/home/ashim-poudel/.jdk/jdk-25.0.2/bin/java -jar target/MotorcycleWorkshop-1.0.0.jar
```

If you prefer to use your own Java 25 installation, replace the path with your local `java` binary.

## What Happens at Startup

- The app applies the FlatLaf look and feel.
- It initializes the SQLite database in `~/.motorworkshop/workshop.db`.
- It opens the main workshop management window.

## If Startup Fails

- If you see a Java version error, make sure you are using Java 25 instead of Java 21 or older.
- If you see an SQLite schema error from an older database, delete or rename `~/.motorworkshop/workshop.db` and start again. The application now also repairs several older schema layouts during startup.

## Quick Launch Summary

```bash
mvn clean package
/home/ashim-poudel/.jdk/jdk-25.0.2/bin/java -jar target/MotorcycleWorkshop-1.0.0.jar
```