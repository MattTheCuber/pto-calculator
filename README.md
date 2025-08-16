# Paid Time Off Calculator

[![JUnit Tests with Maven](https://github.com/MattTheCuber/pto-calculator/actions/workflows/maven.yml/badge.svg)](https://github.com/MattTheCuber/pto-calculator/actions/workflows/maven.yml)

## Demo

[Video](https://www.youtube.com/watch?v=NqNEjELOV2M)

![Demonstration](docs/images/demonstration.png)

## Installation

1. Install [JDK](https://www.oracle.com/java/technologies/downloads/) (make sure its in your `PATH`).
2. Install [Maven](https://maven.apache.org/download.cgi) (make sure its in your `PATH`).
3. Run `mvn javafx:run` to start the GUI application.
4. Run `mvn test` to test the program.

## Building Instructions

1. Install [Wix](https://github.com/wixtoolset/wix/releases/).
2. Download the [JavaFX SDK](https://gluonhq.com/products/javafx/).
3. Run `mvn clean package` to create a JAR file and installer for the program in the `./target/` folder.
4. Run `java --module-path "C:\path\to\javafx" --add-modules javafx.controls,javafx.fxml -jar target\pto-calculator-1.0-jar-with-dependencies.jar` to run the app using the JAR file.
5. Install the program by opening the installer.
