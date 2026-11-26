# Online Feedback App - DevOps Workflow Demo

A single-page feedback form served by a minimal Java 17 / Maven application. The project demonstrates a basic DevOps workflow with Git/GitHub, Maven, Jenkins, GitHub Actions, and Docker.

## Features

- Feedback form with Name, Email, Feedback Category, Rating, and Comments
- Server-side confirmation page showing the submitted feedback summary
- Input escaping to prevent HTML injection
- JUnit 5 tests for form parsing, escaping, and bundled resources
- Maven build that produces a runnable JAR
- Jenkins pipeline: Checkout -> Build -> Test -> Package
- GitHub Actions workflow: Maven build and test on every push
- Multi-stage Docker image and Docker Compose setup
- `/health` endpoint for container health checks

## Tech stack

| Layer | Technology |
| --- | --- |
| Application | Java 17, `com.sun.net.httpserver` |
| Build | Maven 3.9 |
| Tests | JUnit 5 |
| CI | Jenkins, GitHub Actions |
| Container | Docker multi-stage build, Eclipse Temurin 17 JRE |
| Version control | Git and GitHub |

## Project layout

```text
feedback form/
|-- feedback.html                  # The single-page form
|-- pom.xml                        # Maven project
|-- src/main/java/.../App.java     # HTTP server and form handler
|-- src/test/java/.../AppTest.java # JUnit tests
|-- Jenkinsfile                    # Jenkins pipeline
|-- Dockerfile                     # Multi-stage image
|-- docker-compose.yml             # Local container setup
|-- .github/workflows/maven-build.yml
|-- evidence/                      # Captured checkpoint evidence
`-- README.md
```

## Prerequisites

- Java 17 or newer
- Maven 3.9+
- Git
- Docker Desktop
- Jenkins (optional, for the Jenkins checkpoint)
- GitHub CLI (optional, used to create the pull request)

## Run locally

```powershell
mvn -B clean package
java -jar target/feedback-app-1.0.0.jar 8090
```

Open `http://localhost:8090`.

The server uses port 8080 by default. Pass a port argument or set the `PORT` environment variable to change it. Port 8090 is used above because Jenkins is running on 8080 in this environment.

## Maven build

```powershell
mvn -B clean package
```

The build runs the JUnit test suite (7 tests) and writes `target/feedback-app-1.0.0.jar`. The log is saved in `evidence/maven-build.log`.

## GitHub workflow

The project uses a feature-branch workflow:

```powershell
git switch -c feature/feedback-confirmation
git add .
git commit -m "Add feedback confirmation summary and DevOps workflow files"
git push -u origin feature/feedback-confirmation
gh pr create --base main --head feature/feedback-confirmation --title "Add feedback confirmation and DevOps workflow" --body "Adds the feedback confirmation summary, health endpoint, Jenkins pipeline, GitHub Actions workflow, and Docker setup."
gh pr merge --merge
```

Repository: <https://github.com/Rqwler/online-feedback-devops>

## Jenkins pipeline

1. Create a Pipeline job.
2. Select **Pipeline script from SCM**.
3. Repository URL: `https://github.com/Rqwler/online-feedback-devops.git`
4. Branch specifier: `*/main`
5. Script path: `Jenkinsfile`
6. Run **Build Now**.

The Jenkinsfile is cross-platform: it uses `bat` on Windows and `sh` on Unix agents. The stages are Checkout, Build, Test, and Package. Test results are published from `target/surefire-reports/*.xml`, and the JAR is archived as a build artifact.

## GitHub Actions

`.github/workflows/maven-build.yml` runs on every push, on pull requests to `main`, and on manual dispatch. It:

1. Checks out the repository.
2. Sets up Temurin JDK 17 with Maven caching.
3. Runs `mvn -B clean verify`.
4. Uploads the JAR and Surefire reports as artifacts.

## Docker

Build and run:

```powershell
docker build -t feedback-app:1.0 .
docker run -d --name feedback-app -p 8081:8080 feedback-app:1.0
docker ps
curl http://localhost:8081/health
```

Open `http://localhost:8081`. Port 8081 is used on the host so it does not conflict with Jenkins on 8080.

Or use Docker Compose:

```powershell
docker compose up --build -d
```

Stop and remove the container:

```powershell
docker rm -f feedback-app
```

## Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/` | Feedback form |
| `POST` | `/submit` | Process the form and show a confirmation |
| `GET` | `/health` | Health check (`{"status":"UP"}`) |

## Evidence checklist

| Checkpoint | Evidence |
| --- | --- |
| Feedback form in a browser | `evidence/feedback-form.png` |
| Successful Maven build and JAR | `evidence/maven-build.log`, `evidence/maven-artifact.txt` |
| Feature branch, commit, and PR/merge | `evidence/github-branch.txt`, `evidence/github-pr.png` |
| Jenkins pipeline success | `evidence/jenkins-success.png`, `evidence/jenkins-console.log` |
| GitHub Actions success | `evidence/github-actions-success.png` |
| Docker image and running container | `evidence/docker-build.log`, `evidence/docker-ps.txt`, `evidence/docker-app.png` |

## Notes

- Feedback is handled in memory and is not persisted to a database. This keeps the demo focused on the delivery workflow.
- The application is intentionally small so the build, test, CI, and container steps complete quickly.
- Jenkins and the Docker host both use port 8080; the container is mapped to host port 8081 for the local demonstration.
