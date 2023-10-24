# CoinCap Price History Graph
A simple JavaFX application that plots the historical price of Bitcoin over the past two days using data fetched from the CoinCap API.

## Features
- Fetches live Bitcoin price data from CoinCap API (RESTful API setup)
- Displays the price trend over the past 2 days using JavaFX line chart.
- Updates the chart data every 20 seconds for real-time visualization.
- Uses a descriptive time scale on the x-axis for easy readability.

## How It Works
The application uses Java's HttpURLConnection to connect to CoinCap's API and fetch the price data. This data is then deserialized using the Gson library. The chart is drawn using JavaFX'.

## Setup and Run
Ensure you have Java and JavaFX set up on your system.
Clone the repository.
Navigate to the project directory.
To run, enter `mvn clean install` & `mvn javafx:run` into console to run the RestClientGraphPlotter class.
Watch the live Bitcoin price trends!


## TODO

Currently, it appears that only bitcoin's values are fetched, all the other assets seemto be stuck at 0? or a certain dollar amount? but i might be wrong. need to debug more.