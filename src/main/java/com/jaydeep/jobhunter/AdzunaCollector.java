package com.jaydeep.jobhunter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Optional Adzuna India collector using Adzuna's official REST API. */
public class AdzunaCollector implements JobCollector {
    private static final ObjectMapper M = new ObjectMapper();
    private static final String BASE = "https://api.adzuna.com/v1/api/jobs/in/search/1";
    private final HttpClient client = HttpClient.newHttpClient();

    @Override
    public String name() { return "Adzuna"; }

    @Override
    public List<Job> collect() {
        List<Job> out = new ArrayList<>();
        String appId = System.getenv("ADZUNA_APP_ID");
        String appKey = System.getenv("ADZUNA_APP_KEY");
        if (appId == null || appId.isBlank() || appKey == null || appKey.isBlank()) {
            System.out.println("Adzuna: skipped (ADZUNA_APP_ID/ADZUNA_APP_KEY not configured)");
            return out;
        }

        List<String> queries = List.of(
                "software engineer intern",
                "backend java developer",
                "software developer fresher");

        for (String q : queries) {
            try {
                String url = BASE
                        + "?app_id=" + enc(appId)
                        + "&app_key=" + enc(appKey)
                        + "&results_per_page=25"
                        + "&what=" + enc(q)
                        + "&max_days_old=7"
                        + "&content-type=application/json";
                HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                        .header("Accept", "application/json")
                        .header("User-Agent", "JobHunter/1.0")
                        .GET().build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() / 100 != 2) {
                    System.err.println("[Adzuna] " + q + ": HTTP " + response.statusCode());
                    continue;
                }

                JsonNode results = M.readTree(response.body()).path("results");
                if (!results.isArray()) continue;
                for (JsonNode j : results) {
                    String company = j.path("company").path("display_name").asText();
                    String title = j.path("title").asText();
                    String location = j.path("location").path("display_name").asText();
                    String description = j.path("description").asText("");
                    String urlValue = j.path("redirect_url").asText();
                    out.add(new Job(company, title, location, description, urlValue, name()));
                }
            } catch (Exception e) {
                System.err.println("[Adzuna] " + q + ": " + e.getMessage());
            }
        }
        return out;
    }

    private static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
