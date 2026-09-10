package com.jaydeep.jobhunter;
public record Job(String company,String title,String location,String description,String url,String source){public String searchableText(){return String.join(" ",company,title,location,description).toLowerCase();}}
