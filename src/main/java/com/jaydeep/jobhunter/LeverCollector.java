package com.jaydeep.jobhunter;
import com.fasterxml.jackson.databind.JsonNode; import com.fasterxml.jackson.databind.ObjectMapper; import java.net.URI; import java.net.http.*; import java.util.*;
public class LeverCollector implements JobCollector {
 private static final ObjectMapper M=new ObjectMapper(); private final List<String> sites; private final HttpClient c=HttpClient.newHttpClient();
 public LeverCollector(List<String> sites){this.sites=sites;} public String name(){return "Lever";}
 public List<Job> collect(){List<Job> out=new ArrayList<>(); for(String s:sites)try{var r=HttpRequest.newBuilder(URI.create("https://api.lever.co/v0/postings/"+s+"?mode=json")).header("User-Agent","JobHunter/1.0").build(); JsonNode a=M.readTree(c.send(r,HttpResponse.BodyHandlers.ofString()).body()); if(!a.isArray())continue; for(JsonNode j:a){String d=j.path("descriptionPlain").asText(j.path("description").asText())+" "+j.path("additionalPlain").asText(); out.add(new Job(s,j.path("text").asText(),j.path("categories").path("location").asText(),d,j.path("hostedUrl").asText(j.path("applyUrl").asText()),name()));}}catch(Exception e){System.err.println("[Lever] "+s+": "+e.getMessage());} return out; }
}
