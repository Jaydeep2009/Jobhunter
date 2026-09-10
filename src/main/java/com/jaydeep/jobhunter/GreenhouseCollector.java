package com.jaydeep.jobhunter;
import com.fasterxml.jackson.databind.JsonNode; import com.fasterxml.jackson.databind.ObjectMapper; import java.net.URI; import java.net.http.*; import java.util.*;
public class GreenhouseCollector implements JobCollector {
 private static final ObjectMapper M=new ObjectMapper(); private final List<String> boards; private final HttpClient c=HttpClient.newHttpClient();
 public GreenhouseCollector(List<String> boards){this.boards=boards;} public String name(){return "Greenhouse";}
 public List<Job> collect(){List<Job> out=new ArrayList<>(); for(String b:boards)try{var r=HttpRequest.newBuilder(URI.create("https://boards-api.greenhouse.io/v1/boards/"+b+"/jobs?content=true")).header("User-Agent","JobHunter/1.0").build(); JsonNode a=M.readTree(c.send(r,HttpResponse.BodyHandlers.ofString()).body()).path("jobs"); for(JsonNode j:a)out.add(new Job(b,j.path("title").asText(),j.path("location").path("name").asText(),j.path("content").asText(),j.path("absolute_url").asText(),name()));}catch(Exception e){System.err.println("[Greenhouse] "+b+": "+e.getMessage());} return out; }
}
