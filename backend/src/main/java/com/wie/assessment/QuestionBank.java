package com.wie.assessment;
import java.util.*;
import org.springframework.stereotype.Service;
/** Short-answer questions (answerable in a few seconds). Accepted answers never leave the server. */
@Service
public class QuestionBank {
  public record Q(String id, String text, List<String> answers) {}
  private final Map<String,List<Q>> bank = new LinkedHashMap<>();
  private static Q q(String id, String t, String... a){ return new Q(id, t, List.of(a)); }
  public QuestionBank(){
    bank.put("Python", List.of(q("py1","Keyword that defines a function?","def"), q("py2","Immutable type: list or tuple?","tuple"),
      q("py3","What does len('abc') return?","3","three"), q("py4","Keyword that catches errors (try / ...)?","except"), q("py5","Tool used to install packages?","pip")));
    bank.put("Docker", List.of(q("dk1","File that defines how an image is built?","dockerfile"), q("dk2","Command to list running containers?","docker ps","ps"),
      q("dk3","Tool for defining multi-container apps?","compose"), q("dk4","Instruction that sets the base image?","from"), q("dk5","Feature that persists container data?","volume")));
    bank.put("Machine Learning", List.of(q("ml1","Model memorising its training data is called?","overfit"), q("ml2","Metric: correct positives / predicted positives?","precision"),
      q("ml3","Algorithm that follows the loss gradient downhill?","gradient"), q("ml4","Which data split gives the final unbiased score?","test"), q("ml5","Learning from labelled data is called?","supervised")));
    bank.put("Edge AI", List.of(q("ed1","TensorFlow's runtime for mobile and edge devices?","lite","tflite"), q("ed2","Shrinking a model by lowering numeric precision?","quantiz"),
      q("ed3","Running inference on the device, not the cloud, is AI at the ...?","edge"), q("ed4","Removing unneeded weights is called?","prun"), q("ed5","Open format for portable models (O..X)?","onnx")));
  }
  public Set<String> skills(){ return bank.keySet(); }
  public List<Q> questions(String skill){ return bank.getOrDefault(skill, List.of()); }
  public Q byId(String skill, String id){ return questions(skill).stream().filter(x -> x.id().equals(id)).findFirst().orElseThrow(); }
  public boolean matches(Q q, String answer){
    String n = answer == null ? "" : answer.toLowerCase().replaceAll("[^a-z0-9 ]", " ").trim();
    String[] tokens = n.split("\\s+");
    for (String a : q.answers()) {
      if (a.contains(" ")) { if (n.contains(a)) return true; continue; }
      for (String t : tokens) if (a.length() <= 3 ? t.equals(a) : t.startsWith(a)) return true; }
    return false; }
}
