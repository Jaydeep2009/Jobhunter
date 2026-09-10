package com.jaydeep.jobhunter;
import java.util.*;

public class JobMatcher {
 private static final List<String> ROLES=List.of("software engineer","software developer","sde","backend engineer","java developer","developer intern","engineering intern","technology intern");
 private static final List<String> SKILLS=List.of("java","spring boot","spring","backend","rest","jpa","hibernate","postgresql","sql","kafka","aws","docker","react","node.js","kotlin","android","microservices","python","c++");
 private static final List<String> LEVEL=List.of("2027","intern","internship","new grad","new graduate","fresher","entry level","0-1 year","0-2 years");

 public static boolean relevant(Job j){
  String s=j.searchableText();
  return allowedLocation(j.location())
      && any(s,ROLES)
      && any(s,SKILLS)
      && any(s,LEVEL)
      && !any(s,List.of("senior","staff","principal","manager","director"));
 }

 // Accept jobs physically located in India, or jobs explicitly marked remote.
 // This allows an overseas remote/WFH role while excluding overseas onsite/hybrid roles.
 private static boolean allowedLocation(String location){
  if(location==null)return false;
  String l=location.toLowerCase().trim();
  if(l.contains("india")||l.matches(".*\\bind\\b.*"))return true;
  return l.contains("remote")||l.contains("work from home")||l.contains("worldwide")||l.contains("anywhere")||l.contains("global");
 }

 public static int score(Job j){String s=j.searchableText();int x=0;if(s.matches(".*(\\b2027\\b|class of 2027|graduating 2027|2027 batch).*") )x+=35;if(any(s,List.of("intern","internship","new grad","new graduate","fresher","entry level","0-1 year","0-2 years")))x+=25;if(any(s,List.of("software engineer","sde","backend engineer","java developer")))x+=15;for(String k:SKILLS)if(s.contains(k))x+=2;if(j.location()!=null&&j.location().toLowerCase().matches(".*(india|remote|work from home|worldwide|anywhere|global).*))x+=5;if(any(s,List.of("senior","staff","principal","manager","director","5+ years","6+ years","7+ years","8+ years","10+ years")))x-=45;return Math.max(0,Math.min(100,x));}
 private static boolean any(String s,List<String> xs){for(String x:xs)if(s.contains(x))return true;return false;}
}
