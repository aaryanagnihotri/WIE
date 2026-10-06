package com.wie.assessment;
import com.wie.assessment.QuestionBank.Q;
import com.wie.dashboard.StudentInsights;
import com.wie.exception.ApiException;
import com.wie.model.User;
import com.wie.repository.UserRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
@Service
public class AssessmentService {
  static final long LIMIT_MS = 6000, GRACE_MS = 1000;   // grace covers network latency only
  private final QuestionBank bank; private final AssessmentRepository repo; private final StudentInsights ins; private final UserRepository users;
  public AssessmentService(QuestionBank b, AssessmentRepository r, StudentInsights i, UserRepository u){ bank=b; repo=r; ins=i; users=u; }

  public Map<String,Object> start(User u, String skill){
    List<Q> qs = new ArrayList<>(bank.questions(skill));
    if (qs.isEmpty()) throw new ApiException(HttpStatus.BAD_REQUEST, "UNKNOWN_SKILL", "No assessment for this skill");
    Collections.shuffle(qs);
    AssessmentSession s = repo.save(new AssessmentSession(null, u.id(), skill, qs.stream().map(Q::id).toList(), 0, 0, Instant.now(), "ACTIVE"));
    return Map.of("sessionId", s.id(), "done", false, "index", 0, "total", qs.size(), "question", qs.get(0).text(), "timeLimitMs", LIMIT_MS); }

  public Map<String,Object> answer(User u, String id, String ans){
    AssessmentSession s = repo.findById(id).filter(x -> x.userId().equals(u.id()))
      .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Assessment not found"));
    if (!"ACTIVE".equals(s.status())) throw new ApiException(HttpStatus.CONFLICT, "FINISHED", "Assessment already finished");
    Q q = bank.byId(s.skill(), s.questionIds().get(s.index()));
    boolean timedOut = Duration.between(s.issuedAt(), Instant.now()).toMillis() > LIMIT_MS + GRACE_MS;   // server clock decides
    boolean ok = !timedOut && bank.matches(q, ans);
    int correct = s.correct() + (ok ? 1 : 0), idx = s.index() + 1, total = s.questionIds().size();
    if (idx >= total) {
      int level = Math.round(100f * correct / total);
      repo.save(new AssessmentSession(s.id(), s.userId(), s.skill(), s.questionIds(), idx, correct, s.issuedAt(), "DONE"));
      updateSkill(u, s.skill(), level);
      return Map.of("done", true, "lastCorrect", ok, "timedOut", timedOut, "correct", correct, "total", total, "level", level, "skill", s.skill()); }
    repo.save(new AssessmentSession(s.id(), s.userId(), s.skill(), s.questionIds(), idx, correct, Instant.now(), "ACTIVE"));
    return Map.of("done", false, "lastCorrect", ok, "timedOut", timedOut, "index", idx, "total", total,
      "question", bank.byId(s.skill(), s.questionIds().get(idx)).text(), "timeLimitMs", LIMIT_MS); }

  private void updateSkill(User u, String skill, int level){
    List<String> out = new ArrayList<>(); boolean found = false;
    for (StudentInsights.Skill k : ins.skillsOf(u)) {
      if (k.name().equals(skill)) { out.add(skill + ":" + level); found = true; } else out.add(k.name() + ":" + k.level()); }
    if (!found) out.add(skill + ":" + level);
    Map<String,Object> p = new HashMap<>(u.profile() == null ? Map.of() : u.profile()); p.put("skills", out);
    users.save(new User(u.id(), u.name(), u.email(), u.passwordHash(), u.role(), u.verified(), p, u.createdAt())); }
}
