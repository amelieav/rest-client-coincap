package com.am.restclient.restclient;

import javafx.application.Application;
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

public class RestClientGraphPlotter extends Application {

    private static final String COINCAP_BASE_URL = "https://api.coincap.io/v2";

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Crypto Asset Price History");

        long currentTime = Instant.now().toEpochMilli();
        long twoDaysInMillis = 2L * 24 * 60 * 60 * 1000;
        long startTime = currentTime - twoDaysInMillis;

        final NumberAxis xAxis = new NumberAxis(startTime, currentTime, twoDaysInMillis / 24);
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Time (in milliseconds)");
        yAxis.setLabel("Price (in USD)");

        final LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);

        Label lastRefreshedLabel = new Label();
        VBox layout = new VBox(10); // Use a VBox to stack the label on top of the chart
        layout.getChildren().addAll(lastRefreshedLabel, lineChart);
        Scene scene = new Scene(layout, 800, 600);
        primaryStage.setScene(scene);

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

        List<String> top5Assets = getTop5AssetIds();
        for (String assetId : top5Assets) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(assetId); // Or use the asset name instead
            lineChart.getData().add(series);

            // Fetch data and plot every 20 seconds, similar to what you did for Bitcoin
            executorService.scheduleAtFixedRate(() -> {
                List<HistoryResponse.HistoryData> dataPoints = getAssetPriceHistory(assetId);
                series.getData().clear();

                for (HistoryResponse.HistoryData dataPoint : dataPoints) {
                   System.out.println(dataPoint.getTime() + " " + dataPoint.getPriceUsd());
                    series.getData()
                            .add(new XYChart.Data<>(dataPoint.getTime(), Double.parseDouble(dataPoint.getPriceUsd())));
                }

                // Refresh the xAxis
                xAxis.setLowerBound(dataPoints.get(0).getTime());
                xAxis.setUpperBound(dataPoints.get(dataPoints.size() - 1).getTime());

                // Set last refreshed time
                lastRefreshedLabel.setText(
                        "Last Refreshed: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));

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
