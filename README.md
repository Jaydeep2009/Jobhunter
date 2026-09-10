# JobHunter

Personal job discovery and email alert system for 2027 graduate opportunities.

## Goal
Periodically collect public job listings, identify internships/fresher/new-grad roles relevant to Jaydeep's profile, deduplicate them, and send a concise email containing direct application URLs.

## Profile
- Expected graduation: June 2027
- Target: SDE Intern, Software Engineer Intern, New Grad SWE, Fresher SWE, Backend Engineer, Java Developer
- Strong stack: Java, Spring Boot, REST APIs, JPA/Hibernate, PostgreSQL, SQL, Kafka, AWS, Docker, React, Node.js, Kotlin/Android
- Preferred geography: India, especially Pune/Bangalore/Hyderabad/Mumbai; remote when eligible

## Architecture
Collectors -> normalization -> eligibility/skill scoring -> URL deduplication -> email digest.

Collectors are isolated so a failing source cannot stop the rest of the pipeline.

## Security
Never commit Gmail credentials. Configure `GMAIL_USER`, `GMAIL_APP_PASSWORD`, and `MAIL_TO` as GitHub Actions secrets.

## Current status
Initial repository scaffold. Source integrations and workflow are being added incrementally.
