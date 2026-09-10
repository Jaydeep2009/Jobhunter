package com.jaydeep.jobhunter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class LeverCollector implements JobCollector {
    private static final ObjectMapper M = new ObjectMapper();
    private final List<String> sites;
    private final HttpClient client = HttpClient.newHttpClient();

    public LeverCollector(List<String> sites) { this.sites = sites; }
    public String name() { return "Lever"; }

    public List<Job> collect() {
        List<Job> out = new ArrayList<>();
        for (String site : sites) {
            try {
                HttpRequest request = HttpRequest.newBuilder(
                        URI.create("https://api.lever.co/v0/postings/" + site + "?mode=json"))
                        .header("User-Agent", "JobHunter/1.0")
                        .header("Accept", "application/json")
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() / 100 != 2) {
                    System.err.println("[Lever] " + site + ": HTTP " + response.statusCode());
                    continue;
                }
                JsonNode jobs = M.readTree(response.body());
                if (!jobs.isArray()) {
                    System.err.println("[Lever] " + site + ": unexpected response");
                    continue;
                }
                for (JsonNode j : jobs) {
                    String description = j.path("descriptionPlain").asText(j.path("description").asText())
                            + " " + j.path("additionalPlain").asText();
                    out.add(new Job(site,
                            j.path("text").asText(),
                            j.path("categories").path("location").asText(),
                            description,
                            j.path("hostedUrl").asText(j.path("applyUrl").asText()),
                            name()));
                }
            } catch (Exception e) {
                System.err.println("[Lever] " + site + ": " + e.getMessage());
            }
        }
        return out;
    }
}
