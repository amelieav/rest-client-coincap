package com.am.restclient.restclient;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import com.google.gson.Gson;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javafx.util.StringConverter;
import java.text.SimpleDateFormat;
import java.util.Date;


public class RestClientGraphPlotter extends Application {

    private static final String COINCAP_BASE_URL = "https://api.coincap.io/v2";

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Crypto Asset Price History");

        long currentTime = Instant.now().toEpochMilli();
        long twoDaysInMilliseconds = 2L * 24 * 60 * 60 * 1000;
        long startTime = currentTime - twoDaysInMilliseconds;

        final NumberAxis xAxis = new NumberAxis(startTime, currentTime, twoDaysInMilliseconds / 24);
        xAxis.setLabel("Date & Time");
        xAxis.setTickLabelFormatter(new StringConverter<Number>() {
            private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm:ss");
            
            @Override
            public String toString(Number object) {
                return sdf.format(new Date(object.longValue()));
            }
        
            @Override
            public Number fromString(String string) {
                // code later
                return 0;
            }
        });
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Time (in milliseconds)");
        yAxis.setLabel("Price (in USD)");

        final LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);

        Label lastRefreshedLabel = new Label();
        Label titleLabel = new Label();
        titleLabel.setText("Crypto Asset Price History - Top 5 as ranked by CoinCap.io");
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setMaxWidth(Double.MAX_VALUE);

        VBox layout = new VBox(10);
        layout.getChildren().addAll(titleLabel, lastRefreshedLabel, lineChart);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 800, 800);
        primaryStage.setScene(scene);

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

        List<String> top5Assets = getTop5AssetIds();
        for (String assetId : top5Assets) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(assetId); // alternatively you can use the asset name here, but TODO need to write function to get asset name
            lineChart.getData().add(series);
            lineChart.setPrefHeight(700); // set this to 500 as smaller values tend to squish non-bitcoin currencies to the bottom of the graph

            // uses multithreading to fetch data and plot every 20 seconds
            executorService.scheduleAtFixedRate(() -> {
                List<HistoryResponse.HistoryData> dataPoints = getAssetPriceHistory(assetId);

                Platform.runLater(() -> { // use platform runlater to fix multithreading errors in console
                    series.getData().clear();

                    if (!dataPoints.isEmpty()) {
                        HistoryResponse.HistoryData mostRecentDataPoint = dataPoints.get(dataPoints.size() - 1);
                        System.out.println("Asset: " + assetId + ", Time: " + mostRecentDataPoint.getTime()
                                + ", Price: " + mostRecentDataPoint.getPriceUsd());

                        long twoDaysInMillis = 2L * 24 * 60 * 60 * 1000;
                        long currentUpperBound = mostRecentDataPoint.getTime();
                        long currentLowerBound = currentUpperBound - twoDaysInMillis;

                        xAxis.setLowerBound(currentLowerBound);
                        xAxis.setUpperBound(currentUpperBound);
                        xAxis.setTickUnit(twoDaysInMillis / 24);
                    }

                    for (HistoryResponse.HistoryData dataPoint : dataPoints) {
                        series.getData().add(
                                new XYChart.Data<>(dataPoint.getTime(), Double.parseDouble(dataPoint.getPriceUsd())));
                    }

                    lastRefreshedLabel.setText(
                            "Last Refreshed: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                });

            }, 0, 20, TimeUnit.SECONDS);
        }

        primaryStage.setOnCloseRequest(event -> executorService.shutdown());
        primaryStage.show();
    }

    private List<HistoryResponse.HistoryData> getAssetPriceHistory(String assetId) {
        long currentTime = Instant.now().toEpochMilli();
        long twoDaysInMillis = 2L * 24 * 60 * 60 * 1000;
        long startTime = currentTime - twoDaysInMillis;

        String url = COINCAP_BASE_URL + "/assets/" + assetId + "/history?interval=h2&start=" + startTime + "&end="
                + currentTime;

        String response = getResponseFromUrl(url);

        Gson gson = new Gson();
        HistoryResponse historyResponse = gson.fromJson(response, HistoryResponse.class);

        return historyResponse.getData();
    }

    private String getResponseFromUrl(String url) {
        StringBuilder response = new StringBuilder();

        // error handling for url connection, useful for debugging
        try {
            URL website = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) website.openConnection();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response.toString();
    }

    private List<String> getTop5AssetIds() {
        String url = COINCAP_BASE_URL + "/assets?limit=5";
        String response = getResponseFromUrl(url);

        Gson gson = new Gson();
        AssetListResponse assetListResponse = gson.fromJson(response, AssetListResponse.class);

        return assetListResponse.getData().stream()
                .map(AssetListResponse.Asset::getId)
                .collect(Collectors.toList());
    }

    public static void main(String[] args) {
        launch(args);
    }

    // to keep data handling ✨ neat ✨
    static class AssetListResponse {
        private List<Asset> data;

        public List<Asset> getData() {
            return data;
        }

        public void setData(List<Asset> data) {
            this.data = data;
        }

        static class Asset {
            private String id;
            private String name;

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }
        }
    }

    static class HistoryResponse {
        private List<HistoryData> data;

        public List<HistoryData> getData() {
            return data;
        }

        public void setData(List<HistoryData> data) {
            this.data = data;
        }

        static class HistoryData {
            private String priceUsd;
            private long time;

            public long getTime() {
                return time;
            }

            public void setTime(long time) {
                this.time = time;
            }

            public String getPriceUsd() {
                return priceUsd;
            }

            public void setPriceUsd(String priceUsd) {
                this.priceUsd = priceUsd;
            }
        }
    }
}
