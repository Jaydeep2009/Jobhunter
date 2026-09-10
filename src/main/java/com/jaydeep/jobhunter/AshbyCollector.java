package com.jaydeep.jobhunter;
import com.fasterxml.jackson.databind.JsonNode; import com.fasterxml.jackson.databind.ObjectMapper; import java.net.URI; import java.net.http.*; import java.util.*;
public class AshbyCollector implements JobCollector {
 private static final ObjectMapper M=new ObjectMapper(); private final List<String> boards; private final HttpClient c=HttpClient.newHttpClient();
 public AshbyCollector(List<String> boards){this.boards=boards;} public String name(){return "Ashby";}
 public List<Job> collect(){List<Job> out=new ArrayList<>(); for(String b:boards)try{var r=HttpRequest.newBuilder(URI.create("https://api.ashbyhq.com/posting-api/job-board/"+b+"?includeCompensation=true")).header("User-Agent","JobHunter/1.0").build(); JsonNode a=M.readTree(c.send(r,HttpResponse.BodyHandlers.ofString()).body()).path("jobs"); for(JsonNode j:a)if(j.path("isListed").asBoolean(true)){StringBuilder loc=new StringBuilder(j.path("location").asText());for(JsonNode x:j.path("secondaryLocations"))loc.append(" ").append(x.path("location").asText());out.add(new Job(b,j.path("title").asText(),loc.toString(),j.toString(),j.path("applyUrl").asText(j.path("jobUrl").asText()),name()));}}catch(Exception e){System.err.println("[Ashby] "+b+": "+e.getMessage());} return out; }
}
