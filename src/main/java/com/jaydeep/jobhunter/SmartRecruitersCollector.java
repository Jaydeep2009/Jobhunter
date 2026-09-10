package com.jaydeep.jobhunter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

/** Collects public SmartRecruiters postings for India-heavy employers. */
public class SmartRecruitersCollector implements JobCollector {
    private static final ObjectMapper M = new ObjectMapper();
    private final List<String> companies;
    private final HttpClient client = HttpClient.newHttpClient();

    public SmartRecruitersCollector(List<String> companies) {
        this.companies = companies;
    }

    @Override
    public String name() {
        return "SmartRecruiters";
    }

    @Override
    public List<Job> collect() {
        List<Job> out = new ArrayList<>();
        for (String company : companies) {
            try {
                String url = "https://api.smartrecruiters.com/v1/companies/" + company
                        + "/postings?limit=100&offset=0&country=IN";
                HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                        .header("User-Agent", "JobHunter/1.0")
                        .header("Accept", "application/json")
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() / 100 != 2) {
                    System.err.println("[SmartRecruiters] " + company + ": HTTP " + response.statusCode());
                    continue;
                }

                JsonNode root = M.readTree(response.body());
                JsonNode postings = root.path("content");
                if (!postings.isArray()) postings = root.path("postings");
                if (!postings.isArray()) continue;

                for (JsonNode j : postings) {
                    String title = j.path("name").asText();
                    JsonNode loc = j.path("location");
                    String city = loc.path("city").asText();
                    String region = loc.path("region").asText();
                    String country = loc.path("country").asText();
                    String location = String.join(", ", List.of(city, region, country)).replaceAll("^, |, $", "");
                    String description = j.path("jobAd").path("sections").toString();
                    if (description.equals("null") || description.isBlank()) description = j.toString();
                    String applyUrl = j.path("refNumber").asText();
                    String jobUrl = j.path("refNumber").asText();
                    if (j.path("refNumber").isTextual()) {
                        jobUrl = "https://jobs.smartrecruiters.com/" + company + "/" + j.path("id").asText();
                    }
                    if (j.path("jobAd").path("applyUrl").isTextual()) {
                        applyUrl = j.path("jobAd").path("applyUrl").asText();
                    }
                    if (!applyUrl.startsWith("http")) applyUrl = jobUrl;
                    out.add(new Job(company, title, location, description, applyUrl, name()));
                }
            } catch (Exception e) {
                System.err.println("[SmartRecruiters] " + company + ": " + e.getMessage());
            }
        }
        return out;
    }
}
