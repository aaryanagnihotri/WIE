package com.wie.dashboard;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
import org.springframework.web.bind.annotation.*;
/** Simulation, early-warning and outcome endpoints. Replace the demo data with real services later. */
@RestController @RequestMapping("/api")
public class IntelligenceController {
  public record SimReq(@Min(0) @Max(1000000) int demand, @Min(0) @Max(1000000) int supply,
                       @Min(0) @Max(1000000) int capacity, @Min(0) @Max(100) int completion) {}

  @PostMapping("/intel/simulate")
  public Map<String,Object> simulate(@Valid @RequestBody SimReq r){
    int gap0 = Math.max(0, r.demand() - r.supply());
    double supply = r.supply() + r.capacity() * r.completion() / 100.0;
    double gap1 = Math.max(0, r.demand() - supply);
    double red = gap0 == 0 ? 0 : Math.round((gap0 - gap1) / gap0 * 1000) / 10.0;
    return Map.of("gapBefore", gap0, "projectedSupply", Math.round(supply), "gapAfter", Math.round(gap1), "reductionPct", red); }

  @GetMapping("/intel/alerts")
  public List<Map<String,Object>> alerts(){ return List.of(
    Map.of("severity","CRITICAL","title","Embedded AI shortage predicted in North India within 8 months","demandGrowthPct",63,"currentTalent",184,"projectedDemand",670,
      "actions", List.of("Students: upskill","Colleges: modify curriculum","Recruiters: start pipeline","Agencies: launch training")),
    Map.of("severity","WARNING","title","ROS2 talent gap widening in West India","demandGrowthPct",47,"currentTalent",210,"projectedDemand",640,
      "actions", List.of("Agencies: launch ROS2 bootcamp","Recruiters: partner with robotics labs")),
    Map.of("severity","WATCH","title","MLOps demand outpacing supply nationwide","demandGrowthPct",38,"currentTalent",480,"projectedDemand",900,
      "actions", List.of("Students: learn Docker + CI/CD","Agencies: add MLOps track"))); }

  @GetMapping("/intel/outcomes")
  public Map<String,Object> outcomes(){ return Map.of("funnel", List.of(
    Map.of("stage","Trained","count",842), Map.of("stage","Passed","count",671), Map.of("stage","Industry-ready","count",350),
    Map.of("stage","Interviews","count",280), Map.of("stage","Hired","count",190)), "skillGapReducedPct", 61); }
}
