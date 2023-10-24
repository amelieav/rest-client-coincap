package com.am.restclient.restclient;

import org.springframework.web.client.RestTemplate;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.time.Instant;
import java.util.List;

@SpringBootApplication
public class RestClientApplication {

    private static final String COINCAP_BASE_URL = "https://api.coincap.io/v2";
    private static final String ASSET_ID = "tether";

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(RestClientApplication.class, args);
        RestClientApplication application = context.getBean(RestClientApplication.class);

        application.displayAssetInfo();

        context.close();
    }

    public void displayAssetInfo() {
        String currentPrice = getCurrentPrice(ASSET_ID);
        String priceTwoWeeksAgo = getPriceTwoWeeksAgo(ASSET_ID);

        List<AssetsResponse.Asset> topNAssets = getTopNAssets(10);
        topNAssets.forEach(asset -> System.out.println(asset.getName() + " (" + asset.getId() + ")"));

        System.out.printf("%nCurrent price: %s%nPrice two weeks ago: %s%n", currentPrice, priceTwoWeeksAgo);
    }

	private List<AssetsResponse.Asset> getTopNAssets(int n) {
		String url = COINCAP_BASE_URL + "/assets/?limit=" + n + "&sort=rank";
		RestTemplate restTemplate = new RestTemplate();
		AssetsResponse response = restTemplate.getForObject(url, AssetsResponse.class);
		return response.getData();
	}

	private String getCurrentPrice(String assetId) {
		String url = COINCAP_BASE_URL + "/assets/" + assetId;
		RestTemplate restTemplate = new RestTemplate();
		CryptoResponse response = restTemplate.getForObject(url, CryptoResponse.class);
		return response.getData().getPriceUsd();
	}

	private String getPriceTwoWeeksAgo(String assetId) {
		long currentTime = Instant.now().toEpochMilli();
		long twoWeeksInMillis = 14L * 24 * 60 * 60 * 1000;
		long startTime = currentTime - twoWeeksInMillis;

		String url = COINCAP_BASE_URL + "/assets/" + assetId + "/history?interval=d1&start=" + startTime + "&end="
				+ currentTime;
		RestTemplate restTemplate = new RestTemplate();
		HistoryResponse response = restTemplate.getForObject(url, HistoryResponse.class);

		// Assuming the first record is the oldest
		return response.getData().get(0).getPriceUsd();
	}

	static class AssetsResponse {
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

    static class CryptoResponse {
        private Asset data;

        public Asset getData() {
            return data;
        }

        public void setData(Asset data) {
            this.data = data;
        }

        static class Asset {
            private String priceUsd;

            public String getPriceUsd() {
                return priceUsd;
            }

            public void setPriceUsd(String priceUsd) {
                this.priceUsd = priceUsd;
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

            public String getPriceUsd() {
                return priceUsd;
            }

            public void setPriceUsd(String priceUsd) {
                this.priceUsd = priceUsd;
            }
        }
    }
}