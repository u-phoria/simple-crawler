# Simple Single Domain Crawler

Given a URL for the root of a domain (e.g. landing page for a website),
crawls that domain and builds up a sitemap. 

## Prereqs
- Java 8
- Maven

## To run
- Build with ```mvn clean package``` - this creaters an uberjar in target
- Run with ```java -jar <built jar> <root url>```

