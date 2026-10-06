package com.wie.intelligence;
import java.util.List;
import org.springframework.stereotype.Service;
@Service
public class DemoIntelligenceService implements IntelligenceService {
  private static SkillForecast f(String s,int sup,int dem,int mom){
    int gap=Math.max(0,dem-sup); String risk = gap>600?"HIGH":gap>250?"MEDIUM":"LOW";
    return new SkillForecast(s,sup,dem,gap,risk,mom); }
  public List<SkillForecast> forecasts(){ return List.of(
    f("Edge AI",320,1100,43), f("ROS2",210,640,47), f("MLOps",480,900,38),
    f("Computer Vision",900,1250,31), f("Generative AI",700,1300,35), f("jQuery",1500,900,-19)); }
  public List<Intervention> interventionsFor(String skill){ return List.of(
    new Intervention("STUDENT","Learn " + skill + ": take a course and build one small project"),
    new Intervention("COLLEGE","Add an " + skill + " module to the curriculum"),
    new Intervention("RECRUITER","Start a campus pipeline for " + skill),
    new Intervention("COURSE_AGENCY","Launch a " + skill + " training program")); }
}
