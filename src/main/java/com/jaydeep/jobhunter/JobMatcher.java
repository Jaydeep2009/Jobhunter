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
            "android developer", "android intern", "full stack engineer", "fullstack engineer",
            "full stack developer", "fullstack developer", "platform engineer", "systems engineer",
            "application engineer", "sdet", "qa automation engineer");

    private static final List<String> SKILLS = List.of(
            "java", "spring boot", "spring", "backend", "rest", "rest api", "jpa", "hibernate", "postgresql", "sql",
            "kafka", "aws", "docker", "react", "node.js", "nodejs", "kotlin", "android", "microservices",
            "python", "c++", "javascript", "typescript", "git", "linux", "mongodb", "mysql", "firebase");

    private static final List<String> LEVEL = List.of(
            "2027", "2028", "intern", "internship", "new grad", "new graduate", "fresher", "freshers",
            "entry level", "entry-level", "graduate trainee", "graduate engineer trainee", "no experience",
            "no prior experience", "0 years", "0-1 year", "0-2 years", "0 to 1 year", "0 to 2 years",
            "less than 1 year", "1 year experience", "1+ year experience", "up to 2 years", "2 years experience",
            "0-3 years", "0 to 3 years", "up to 3 years");

    private static final List<String> INDIA_LOCATIONS = List.of(
            "india", "bangalore", "bengaluru", "pune", "mumbai", "hyderabad", "chennai", "delhi", "new delhi",
            "gurgaon", "gurugram", "noida", "kolkata", "ahmedabad", "jaipur", "indore", "chandigarh", "kochi",
            "coimbatore", "thiruvananthapuram", "trivandrum", "nagpur", "surat", "vadodara", "bhubaneswar",
            "mysore", "mysuru", "mohali", "faridabad", "ghaziabad", "visakhapatnam", "vizag", "goa");

    // Only reject seniority when it is a title signal or an explicit experience requirement.
    // We intentionally do not scan the entire description for words like "manager" because
    // normal postings often say the intern reports to a manager or collaborates with managers.
    private static final List<String> SENIOR_TITLE = List.of(
            "senior", "sr.", "sr ", "staff", "principal", "manager", "director", "lead engineer",
            "tech lead", "engineering lead", "head of");

    private static final List<String> EXCESS_EXPERIENCE = List.of(
            "3+ years", "4+ years", "5+ years", "6+ years", "7+ years", "8+ years", "9+ years",
            "10+ years", "11+ years", "12+ years", "3 years of experience", "4 years of experience",
            "5 years of experience", "6 years of experience", "7 years of experience", "8 years of experience");

    public static boolean relevant(Job j) {
        String all = j.searchableText();
        String title = lower(j.title());
        boolean role = anyPhrase(title, ROLES) || anyPhrase(all, ROLES);
        boolean skill = anyPhrase(all, SKILLS);
        boolean earlyCareer = anyPhrase(all, LEVEL) || containsAny(title,
                "intern", "trainee", "new grad", "graduate", "fresher", "junior", "associate");

        // A specific early-career title is enough even if the posting's body does not
        // repeat the level/technology keywords. For generic roles, require an explicit
        // early-career signal so we don't flood the inbox with mid/senior openings.
        boolean specificEarlyTitle = containsAny(title,
                "intern", "trainee", "new grad", "graduate", "fresher", "junior", "associate");

        boolean senior = anyPhrase(title, SENIOR_TITLE) || anyPhrase(all, EXCESS_EXPERIENCE);
        boolean roleSkillMatch = skill || specificEarlyTitle;
        return allowedLocation(j) && role && roleSkillMatch && earlyCareer && !senior;
    }

    // India is the default target. Foreign roles are accepted only when the actual
    // location explicitly says remote/WFH/anywhere. Hybrid/onsite is rejected.
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
        String all = j.searchableText();
        String title = lower(j.title());
        int x = 0;
        if (containsAny(all, "class of 2027", "graduating 2027", "2027 batch", "2027 graduates", "2027")) x += 35;
        if (anyPhrase(all, LEVEL) || containsAny(title, "intern", "trainee", "new grad", "graduate", "fresher", "junior", "associate")) x += 25;
        if (anyPhrase(title, List.of("software engineer intern", "sde intern", "backend intern", "java intern",
                "android intern", "software engineer", "software development engineer", "backend engineer", "java developer"))) x += 15;
        for (String k : SKILLS) if (hasPhrase(all, k)) x += 1;
        String loc = lower(j.location());
        if (anyPhrase(loc, INDIA_LOCATIONS)) x += 20;
        else if (containsAny(loc, "remote", "work from home", "work-from-home", "wfh", "anywhere")) x += 5;
        if (anyPhrase(title, SENIOR_TITLE) || anyPhrase(all, EXCESS_EXPERIENCE)) x -= 60;
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
