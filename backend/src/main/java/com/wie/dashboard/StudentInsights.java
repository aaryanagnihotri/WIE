package com.wie.dashboard;
import com.wie.intelligence.IntelligenceService;
import com.wie.model.User;
import java.util.*;
import org.springframework.stereotype.Service;
/** Computes every student metric from the student's own skills. Skills are stored as "Name" or "Name:level". */
@Service
public class StudentInsights {
  public record Skill(String name, int level) {}
  public static final Map<String,Integer> W = new LinkedHashMap<>();   // skill -> industry demand weight
  static { W.put("Python",10); W.put("Machine Learning",9); W.put("Edge AI",9); W.put("Computer Vision",8); W.put("Generative AI",8);
           W.put("MLOps",7); W.put("Cloud",7); W.put("Docker",6); W.put("ROS2",6); W.put("Data Engineering",6); }
  private static final Set<String> EMERGING = Set.of("Edge AI","MLOps","ROS2","Generative AI");
  private static final int TOTAL = W.values().stream().mapToInt(Integer::intValue).sum();

  static String canon(String n){ for (String k : W.keySet()) if (k.equalsIgnoreCase(n)) return k; return n; }

  public List<Skill> skillsOf(User u){
    List<Skill> out = new ArrayList<>();
    Object raw = u.profile() == null ? null : u.profile().get("skills");
    if (raw instanceof List<?> l) for (Object o : l) {
      String s = String.valueOf(o).trim(); if (s.isEmpty()) continue;
      String name = s; int lvl = 60; int i = s.lastIndexOf(':');
      if (i > 0) { name = s.substring(0, i).trim();
        try { lvl = Math.max(0, Math.min(100, Integer.parseInt(s.substring(i + 1).trim()))); } catch (NumberFormatException e) {} }
      out.add(new Skill(canon(name), lvl)); }
    return out; }

  public Map<String,Integer> levels(List<Skill> skills){
    Map<String,Integer> m = new LinkedHashMap<>(); for (Skill s : skills) m.merge(s.name(), s.level(), Math::max); return m; }

  int readiness(Map<String,Integer> lv){
    double sum = 0; for (var e : W.entrySet()) sum += e.getValue() * lv.getOrDefault(e.getKey(), 0) / 100.0;
    return (int) Math.round(100 * sum / TOTAL); }

  public Map<String,Object> dashboard(User u, IntelligenceService intel){
    List<Skill> skills = skillsOf(u); Map<String,Integer> lv = levels(skills);
    int ownedW = 0, emTotal = 0; double emOwned = 0;
    for (var e : W.entrySet()) { int l = lv.getOrDefault(e.getKey(), 0);
      if (l > 0) ownedW += e.getValue();
      if (EMERGING.contains(e.getKey())) { emTotal += e.getValue(); emOwned += e.getValue() * l / 100.0; } }
    double avg = skills.stream().mapToInt(Skill::level).average().orElse(0);
    String top = W.keySet().stream().max(Comparator.comparingInt(k -> W.get(k) * (100 - lv.getOrDefault(k, 0)))).orElse("Edge AI");
    List<String> missing = W.keySet().stream().filter(k -> lv.getOrDefault(k, 0) < 50)
      .sorted(Comparator.comparingInt((String k) -> -W.get(k) * (100 - lv.getOrDefault(k, 0)))).limit(4).toList();
    List<Map<String,Object>> list = skills.stream().sorted(Comparator.comparingInt(Skill::level).reversed())
      .map(s -> Map.<String,Object>of("name", s.name(), "level", s.level())).toList();
    return Map.of("greetingName", u.name(), "futureReadiness", readiness(lv),
      "cards", Map.of("skillStrength", (int) Math.round(avg), "industryAlignment", (int) Math.round(100.0 * ownedW / TOTAL),
        "adaptability", Math.min(100, skills.isEmpty() ? 0 : 30 + skills.size() * 10), "emergingSkillExposure", (int) Math.round(100 * emOwned / emTotal)),
      "skills", list, "missingSkills", missing, "targetSkill", top, "interventions", intel.interventionsFor(top)); }

  public Map<String,Object> whatIf(User u, List<String> add){
    Map<String,Integer> lv = new LinkedHashMap<>(levels(skillsOf(u)));
    List<Map<String,Object>> steps = new ArrayList<>(); steps.add(Map.of("label", "Current", "readiness", readiness(lv)));
    List<String> done = new ArrayList<>();
    for (String raw : add) { String s = canon(raw); if (!W.containsKey(s) || done.contains(s)) continue;
      done.add(s); lv.merge(s, 70, Math::max);
      steps.add(Map.of("label", "After " + String.join(" + ", done), "readiness", readiness(lv))); }
    return Map.of("steps", steps); }
}
