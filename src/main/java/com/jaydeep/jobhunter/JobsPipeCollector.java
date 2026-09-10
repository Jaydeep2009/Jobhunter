package com.jaydeep.jobhunter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

/** Optional JobsPipe collector. Uses the normalized API instead of scraping job boards. */
public class JobsPipeCollector implements JobCollector {
    private static final ObjectMapper M = new ObjectMapper();
    private static final String ENDPOINT = "https://api.jobspipe.dev/v1/jobs/search";
    private final HttpClient client = HttpClient.newHttpClient();

    @Override
    public String name() { return "JobsPipe"; }

    @Override
    public List<Job> collect() {
        List<Job> out = new ArrayList<>();
        String key = System.getenv("JOBSPIPE_API_KEY");
        if (key == null || key.isBlank()) {
            System.out.println("JobsPipe: skipped (JOBSPIPE_API_KEY not configured)");
            return out;
        }

        try {
            String body = M.createObjectNode()
                    .set("job_title_or", M.valueToTree(List.of(
                            "software engineer", "software developer", "SDE",
                            "backend engineer", "backend developer", "Java developer",
                            "Java engineer", "Android developer", "graduate engineer trainee")))
                    .toString();
            JsonNode root = M.readTree(body);
            ((com.fasterxml.jackson.databind.node.ObjectNode) root).putArray("job_country_code_or").add("IN");
            ((com.fasterxml.jackson.databind.node.ObjectNode) root).put("posted_at_max_age_days", 7);
            ((com.fasterxml.jackson.databind.node.ObjectNode) root).put("limit", 25);

            HttpRequest request = HttpRequest.newBuilder(URI.create(ENDPOINT))
                    .header("Authorization", "Bearer " + key)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("User-Agent", "JobHunter/1.0")
                    .POST(HttpRequest.BodyPublishers.ofString(root.toString()))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                System.err.println("[JobsPipe] HTTP " + response.statusCode() + ": " + response.body());
                return out;
            }

            JsonNode jobs = M.readTree(response.body()).path("data");
            if (!jobs.isArray()) return out;
            for (JsonNode j : jobs) {
                String company = j.path("company").asText();
                String title = j.path("job_title").asText();
                String location = j.path("location").asText();
                String description = j.path("description").asText("");
                String url = firstNonBlank(j.path("final_url").asText(""),
                        j.path("url").asText(""), j.path("source_url").asText(""));
                String arrangement = j.path("work_arrangement").asText("");
                String seniority = j.path("seniority").asText("");
                out.add(new Job(company, title, location,
                        description + " " + arrangement + " " + seniority,
                        url, name()));
            }
        } catch (Exception e) {
            System.err.println("[JobsPipe] " + e.getMessage());
        }
        return out;
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) if (v != null && !v.isBlank()) return v;
        return "";
    }
}
