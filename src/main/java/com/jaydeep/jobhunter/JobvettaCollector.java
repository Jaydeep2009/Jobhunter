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

/** India-only collector backed by Jobvetta's public REST API. */
public class JobvettaCollector implements JobCollector {
    private static final ObjectMapper M = new ObjectMapper();
    private static final String BASE = "https://api.jobvetta.com/v1/jobs";
    private final HttpClient client = HttpClient.newHttpClient();

    @Override
    public String name() { return "Jobvetta"; }

    @Override
    public List<Job> collect() {
        List<Job> out = new ArrayList<>();
        String key = System.getenv("JOBVETTA_API_KEY");
        if (key == null || key.isBlank()) {
            System.out.println("Jobvetta: skipped (JOBVETTA_API_KEY not configured)");
            return out;
        }

        // Five targeted India/fresher searches. The API returns up to 10 per call.
        List<String> queries = List.of(
                "software engineer intern",
                "backend engineer intern",
                "java developer fresher",
                "software developer fresher",
                "graduate engineer trainee");

        for (String q : queries) {
            try {
                String url = BASE + "?q=" + URLEncoder.encode(q, StandardCharsets.UTF_8)
                        + "&days=30&limit=10";
                HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                        .header("Authorization", "Bearer " + key)
                        .header("Accept", "application/json")
                        .header("User-Agent", "JobHunter/1.0")
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() / 100 != 2) {
                    System.err.println("[Jobvetta] " + q + ": HTTP " + response.statusCode());
                    continue;
                }

                JsonNode jobs = M.readTree(response.body()).path("jobs");
                if (!jobs.isArray()) continue;
                for (JsonNode j : jobs) {
                    String company = j.path("company").asText();
                    String title = j.path("title").asText();
                    String location = j.path("location").asText();
                    String workModel = j.path("work_model").asText("");
                    String employment = j.path("employment_type").asText("");
                    String urlValue = j.path("url").asText();
                    String description = q + " " + workModel + " " + employment;
                    out.add(new Job(company, title, location, description, urlValue, name()));
                }
            } catch (Exception e) {
                System.err.println("[Jobvetta] " + q + ": " + e.getMessage());
            }
        }
        return out;
    }
}
