package com.am.restclient.restclient;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import com.google.gson.Gson;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RestClientGraphPlotter extends Application {

    private static final String COINCAP_BASE_URL = "https://api.coincap.io/v2";

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Bitcoin Price History");

        // Set up the JavaFX chart
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Time (in milliseconds)");
        yAxis.setLabel("Price (in USD)");

        final LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Bitcoin Price");

        Scene scene = new Scene(lineChart, 800, 600);
        primaryStage.setScene(scene);

        // Schedule data updates every 20 seconds
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> {
            List<HistoryResponse.HistoryData> dataPoints = getBitcoinPriceHistory();
            series.getData().clear();

            for (HistoryResponse.HistoryData dataPoint : dataPoints) {
                series.getData().add(new XYChart.Data<>(dataPoint.getTime(), Double.parseDouble(dataPoint.getPriceUsd())));
            }
        }, 0, 20, TimeUnit.SECONDS);

        primaryStage.setOnCloseRequest(event -> executorService.shutdown());

        lineChart.getData().add(series);
        primaryStage.show();
    }

    private List<HistoryResponse.HistoryData> getBitcoinPriceHistory() {
        long currentTime = Instant.now().toEpochMilli();
        long twoDaysInMillis = 2L * 24 * 60 * 60 * 1000;
        long startTime = currentTime - twoDaysInMillis;
    
        String url = COINCAP_BASE_URL + "/assets/bitcoin/history?interval=h2&start=" + startTime + "&end=" + currentTime;
    
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


    public static void main(String[] args) {
        launch(args);
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
