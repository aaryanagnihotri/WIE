# Workforce Intelligence Ecosystem: backend core

## Run
1. Start MongoDB locally (or set MONGODB_URI).
2. Copy .env.example values into your environment (replace the secrets first).
3. `cd backend && mvn spring-boot:run`

## Try it
```
curl -i -X POST localhost:8080/api/auth/login -H 'Content-Type: application/json' \
  -d '{"email":"recruiter@demo.com","password":"Demo@12345"}'
curl localhost:8080/api/recruiter/dashboard -H "Authorization: Bearer <accessToken>"   # 200
curl localhost:8080/api/student/dashboard   -H "Authorization: Bearer <recruiterToken>" # 403
```
Demo users (SEED_DEMO=true): aryan@demo.com, recruiter@demo.com, agency@demo.com, all `Demo@12345`.

## Security model
- Access JWT (15 min) holds only the user id; role is read from MongoDB on every request.
- Refresh token: random, HMAC-hashed at rest, HttpOnly + SameSite=Strict cookie scoped to /api/auth, rotated on each use, TTL-indexed.
- BCrypt (cost 12). Role URL rules in SecurityConfig. Consistent JSON errors (SESSION_EXPIRED, FORBIDDEN, VALIDATION_ERROR).
- Set COOKIE_SECURE=true behind HTTPS.


## Frontend
`cd frontend && cp .env.example .env && npm install && npm run dev` (http://localhost:5173)

## Not built yet
Separate profile/skills/jobs/courses collections, skill adjacency graph, collapsible sidebar, dark/light toggle, jobs and course CRUD.

## Skill assessment
Students: /assessment. 5 short-answer questions, 6s each (server clock decides, 1s latency grace). Verified score overwrites the skill level and feeds readiness. Dashboards auto-refresh every 15s.
# WIE
