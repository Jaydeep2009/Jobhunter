package com.jaydeep.jobhunter;

import java.util.*;
import java.util.regex.Pattern;

public class JobMatcher {
    private static final List<String> ROLES = List.of(
            "software engineer", "software developer", "software development engineer", "sde",
            "backend engineer", "backend developer", "backend intern", "java developer", "java intern",
            "developer intern", "software engineering intern", "software engineer intern", "sde intern",
            "software developer intern", "engineering intern", "technology intern", "software trainee",
            "graduate engineer trainee", "trainee software engineer", "associate software engineer",
            "junior software engineer", "junior developer", "application developer", "mobile developer",
            "android developer", "android intern");

    private static final List<String> SKILLS = List.of(
            "java", "spring boot", "spring", "backend", "rest", "rest api", "jpa", "hibernate", "postgresql", "sql",
            "kafka", "aws", "docker", "react", "node.js", "nodejs", "kotlin", "android", "microservices",
            "python", "c++");

    private static final List<String> LEVEL = List.of(
            "2027", "intern", "internship", "new grad", "new graduate", "fresher", "freshers", "entry level",
            "graduate trainee", "graduate engineer trainee", "no experience", "no prior experience", "0 years",
            "0-1 year", "0-2 years", "0 to 1 year", "0 to 2 years", "less than 1 year", "1 year experience",
            "1+ year experience");

    private static final List<String> INDIA_LOCATIONS = List.of(
            "india", "bangalore", "bengaluru", "pune", "mumbai", "hyderabad", "chennai", "delhi", "new delhi",
            "gurgaon", "gurugram", "noida", "kolkata", "ahmedabad", "jaipur", "indore", "chandigarh", "kochi",
            "coimbatore", "thiruvananthapuram", "trivandrum", "nagpur", "surat", "vadodara", "bhubaneswar",
            "mysore", "mysuru", "mohali", "faridabad", "ghaziabad", "visakhapatnam", "vizag", "goa");

    private static final List<String> SENIOR = List.of(
            "senior", "staff", "principal", "manager", "director", "lead engineer", "tech lead",
            "5+ years", "6+ years", "7+ years", "8+ years", "10+ years", "10 years", "12+ years");

    public static boolean relevant(Job j) {
        String s = j.searchableText();
        boolean role = anyPhrase(s, ROLES);
        boolean skill = anyPhrase(s, SKILLS);
        boolean earlyCareer = anyPhrase(s, LEVEL) || containsAny(lower(j.title()),
                "intern", "trainee", "new grad", "graduate", "fresher", "junior");
        return allowedLocation(j) && role && skill && earlyCareer && !anyPhrase(s, SENIOR);
    }

    // India is the default target. Foreign roles are accepted only when the
    // actual location explicitly says remote/WFH/anywhere. Hybrid/onsite is rejected.
    private static boolean allowedLocation(Job j) {
        String location = lower(j.location());
        if (anyPhrase(location, INDIA_LOCATIONS)) return true;

        if (location.isBlank()) {
            String titleAndDescription = lower(j.title() + " " + j.description());
            if (anyPhrase(titleAndDescription, INDIA_LOCATIONS)) return true;
        }

        boolean remote = containsAny(location,
                "remote", "work from home", "work-from-home", "wfh", "fully distributed", "anywhere");
        boolean hybridOrOnsite = containsAny(location,
                "hybrid", "on-site", "onsite", "in office", "in-office", "office based", "office-based");
        return remote && !hybridOrOnsite;
    }

    public static int score(Job j) {
        String s = j.searchableText();
        int x = 0;
        if (containsAny(s, "class of 2027", "graduating 2027", "2027 batch", "2027 graduates", "2027")) x += 35;
        if (anyPhrase(s, LEVEL) || containsAny(lower(j.title()), "intern", "trainee", "new grad", "graduate", "fresher", "junior")) x += 25;
        if (anyPhrase(s, List.of("software engineer intern", "sde intern", "backend intern", "java intern",
                "android intern", "software engineer", "software development engineer", "backend engineer", "java developer"))) x += 15;
        for (String k : SKILLS) if (hasPhrase(s, k)) x += 2;
        String loc = lower(j.location());
        if (anyPhrase(loc, INDIA_LOCATIONS)) x += 20;
        else if (containsAny(loc, "remote", "work from home", "work-from-home", "wfh", "anywhere")) x += 5;
        if (anyPhrase(s, SENIOR)) x -= 60;
        return Math.max(0, Math.min(100, x));
    }

    private static boolean anyPhrase(String text, List<String> phrases) {
        for (String p : phrases) if (hasPhrase(text, p)) return true;
        return false;
    }

    private static boolean hasPhrase(String text, String phrase) {
        String p = phrase.toLowerCase(Locale.ROOT).trim();
        if (p.isBlank()) return false;
        if (p.matches(".*[^a-z0-9].*")) return text.contains(p);
        return Pattern.compile("(?<![a-z0-9])" + Pattern.quote(p) + "(?![a-z0-9])")
                .matcher(text).find();
    }

    private static boolean containsAny(String text, String... values) {
        for (String v : values) if (text.contains(v)) return true;
        return false;
    }

    private static String lower(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT).trim();
    }
}
