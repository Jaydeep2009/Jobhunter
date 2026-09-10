package com.jaydeep.jobhunter;
import java.util.*;
public class JobMatcher {
 private static final List<String> ROLES=List.of("software engineer","software developer","sde","backend engineer","java developer","developer intern","engineering intern","technology intern");
 private static final List<String> SKILLS=List.of("java","spring boot","spring","backend","rest","jpa","hibernate","postgresql","sql","kafka","aws","docker","react","node.js","kotlin","android","microservices","python","c++");
 private static final List<String> LEVEL=List.of("2027","intern","internship","new grad","new graduate","fresher","entry level","0-1 year","0-2 years");
 public static boolean relevant(Job j){String s=j.searchableText();return any(s,ROLES)&&any(s,SKILLS)&&any(s,LEVEL)&&!any(s,List.of("senior","staff","principal","manager","director"));}
 public static int score(Job j){String s=j.searchableText();int x=0;if(s.matches(".*(\\b2027\\b|class of 2027|graduating 2027|2027 batch).*") )x+=35;if(any(s,List.of("intern","internship","new grad","new graduate","fresher","entry level","0-1 year","0-2 years")))x+=25;if(any(s,List.of("software engineer","sde","backend engineer","java developer")))x+=15;for(String k:SKILLS)if(s.contains(k))x+=2;if(any(s,List.of("india","pune","bangalore","bengaluru","hyderabad","mumbai","remote")))x+=5;if(any(s,List.of("senior","staff","principal","manager","director","5+ years","6+ years","7+ years","8+ years","10+ years")))x-=45;return Math.max(0,Math.min(100,x));}
 private static boolean any(String s,List<String> xs){for(String x:xs)if(s.contains(x))return true;return false;}
}
