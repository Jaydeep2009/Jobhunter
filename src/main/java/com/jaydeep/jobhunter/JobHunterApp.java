package com.jaydeep.jobhunter;

import java.util.*;

public class JobHunterApp {
    private static final int DAILY_LIMIT = 40;

    public static void main(String[] args) throws Exception {
        List<JobCollector> cs = List.of(
                new GreenhouseCollector(List.of(
                        "airbnb", "stripe", "datadog", "cloudflare", "hubspot", "plaid", "reddit", "ramp", "coinbase", "duolingo",
                        "figma", "notion", "okta", "asana", "brex", "rippling", "toast", "affirm", "lyft", "doordash",
                        "razorpay", "swiggy", "zomato", "meesho", "phonepe", "groww", "browserstack", "postman", "freshworks", "chargebee")),
                new LeverCollector(List.of(
                        "netflix", "shopify", "scaleai", "anduril", "anthropic", "intercom", "samsara", "mistral", "pinterest", "coursera",
                        "udemy", "benchling", "faire", "gusto", "flexport", "weekdayworks", "drivetrain", "paytm", "entrata", "oneimpression", "resilinc",
                        "razorpay", "phonepe", "meesho", "swiggy", "dream11", "clevertap", "browserstack", "postman", "freshworks", "chargebee")),
                new AshbyCollector(List.of(
                        "Ashby", "OpenAI", "Ramp", "Linear", "Notion", "Vercel", "Figma", "Rippling", "certa", "sarvam", "emergence",
                        "zepto", "meesho", "browserstack", "groww", "cred")),
                new SmartRecruitersCollector(List.of(
                        "HitachiSolutions", "WNSGlobalServices144", "BigBinary", "RepliconSoftware", "schoolapply",
                        "CognitiveCloudsSoftwarePrivateLimited", "linkedin3", "IINTRIS1", "T-SystemsICTIndiaPvtLtd1", "intuit2")),
                new JobsPipeCollector(),
                new AdzunaCollector(),
                new JobvettaCollector(),
                new RemoteOkCollector()
        );

        List<Job> all = new ArrayList<>();
        for (JobCollector c : cs) {
            try {
                List<Job> x = c.collect();
                System.out.println(c.name() + ": " + x.size());
                all.addAll(x);
            } catch (Exception e) {
                System.err.println("[" + c.name() + "] " + e.getMessage());
            }
        }

        Map<String, Job> unique = new LinkedHashMap<>();
        for (Job j : all) {
            if (j.url() != null && !j.url().isBlank() && JobMatcher.relevant(j)) {
                unique.putIfAbsent(norm(j.url()), j);
            }
        }

        SeenStore seen = new SeenStore("seen_urls.txt");
        DailyQuota quota = new DailyQuota("daily_quota.txt");
        int remainingToday = Math.min(DAILY_LIMIT, quota.remaining());

        if (remainingToday == 0) {
            System.out.println("Daily limit of " + DAILY_LIMIT + " already reached. No email sent.");
            return;
        }

        List<Job> fresh = unique.values().stream()
                .filter(j -> !seen.contains(j.url()))
                .sorted(Comparator.comparingInt(JobMatcher::score).reversed())
                .limit(remainingToday)
                .toList();

        System.out.println("Relevant=" + unique.size() + ", fresh=" + fresh.size() + ", remaining today=" + remainingToday);

        if (fresh.isEmpty()) return;

        new EmailSender().send(fresh);
        seen.mark(fresh.stream().map(Job::url).toList());
        quota.add(fresh.size());
        System.out.println("Email sent; " + fresh.size() + " jobs marked seen; daily quota updated.");
    }

    private static String norm(String u) {
        return u.trim().replaceAll("[?#].*$", "");
    }
}
