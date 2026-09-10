package com.jaydeep.jobhunter;
import java.util.*;

public class JobMatcher {
 private static final List<String> ROLES=List.of("software engineer","software developer","software development engineer","sde","backend engineer","backend developer","java developer","developer intern","software engineering intern","engineering intern","technology intern");
 private static final List<String> SKILLS=List.of("java","spring boot","spring","backend","rest","jpa","hibernate","postgresql","sql","kafka","aws","docker","react","node.js","kotlin","android","microservices","python","c++");
 private static final List<String> LEVEL=List.of("2027","intern","internship","new grad","new graduate","fresher","entry level","0-1 year","0-2 years");
 private static final List<String> INDIA_LOCATIONS=List.of("india","bangalore","bengaluru","pune","mumbai","hyderabad","chennai","delhi","new delhi","gurgaon","gurugram","noida","kolkata","ahmedabad","jaipur","indore","chandigarh","kochi","coimbatore","thiruvananthapuram","trivandrum","nagpur","surat","vadodara","bhubaneswar","mysore","mysuru");

 public static boolean relevant(Job j){
  String s=j.searchableText();
  return allowedLocation(j) && any(s,ROLES) && any(s,SKILLS) && any(s,LEVEL)
      && !any(s,List.of("senior","staff","principal","manager","director","lead engineer","tech lead"));
 }

 // India is the default target. A non-India role is accepted only when the
 // posting explicitly says it is remote/WFH, not merely hybrid or onsite.
 private static boolean allowedLocation(Job j){
  String location=j.location()==null?"":j.location().toLowerCase().trim();
  if(any(location,INDIA_LOCATIONS)) return true;
  if(location.isBlank() && any(j.searchableText(),INDIA_LOCATIONS)) return true;
  boolean remote=location.contains("remote")||location.contains("work from home")||location.contains("work-from-home")||location.contains("wfh")||location.contains("fully distributed")||location.contains("anywhere");
  boolean hybrid=location.contains("hybrid")||location.contains("on-site")||location.contains("onsite")||location.contains("in office")||location.contains("in-office");
  return remote && !hybrid;
 }

 public static int score(Job j){
  String s=j.searchableText();
  int x=0;
  if(s.contains("2027") || s.contains("class of 2027") || s.contains("graduating 2027") || s.contains("2027 batch")) x+=35;
  if(any(s,List.of("intern","internship","new grad","new graduate","fresher","entry level","0-1 year","0-2 years"))) x+=25;
  if(any(s,List.of("software engineer","sde","backend engineer","java developer"))) x+=15;
  for(String k:SKILLS) if(s.contains(k)) x+=2;
  String loc=j.location()==null?"":j.location().toLowerCase();
  if(any(loc,INDIA_LOCATIONS)) x+=12;
  else if(loc.contains("remote")||loc.contains("work from home")||loc.contains("wfh")||loc.contains("anywhere")) x+=5;
  if(any(s,List.of("senior","staff","principal","manager","director","5+ years","6+ years","7+ years","8+ years","10+ years"))) x-=45;
  return Math.max(0,Math.min(100,x));
 }
 private static boolean any(String s,List<String> xs){for(String x:xs)if(s.contains(x))return true;return false;}
}
