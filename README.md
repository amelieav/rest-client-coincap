# CoinCap Price History Graph
A simple JavaFX application that plots the historical price of Bitcoin over the past n days using data fetched from the CoinCap API.

## Features
- Fetches live Bitcoin price data from CoinCap API (RESTful API setup)
- Displays the price trend over the past n days using JavaFX line chart.
- Updates the chart data every n seconds for real-time visualization (uses multithreading)
- Uses a descriptive time scale on the x-axis for easy readability.

## How It Works
The application uses Java's HttpURLConnection to connect to CoinCap's API and fetch the price data. This data is then deserialized using the Gson library. The chart is drawn using JavaFX'.

## Setup and Run
Ensure you have Java and JavaFX set up on your system.
Clone the repository.
Navigate to the project directory.
To run, enter `mvn clean install` & `mvn javafx:run` into console to run the RestClientGraphPlotter class.
Watch the live Bitcoin price trends!


