package com.jaydeep.jobhunter;
import java.util.List;
public interface JobCollector { String name(); List<Job> collect() throws Exception; }
