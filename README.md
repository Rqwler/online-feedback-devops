# Online Feedback App – DevOps Workflow Demo

Single-page feedback form (`feedback.html`) served by a minimal Java 17 / Maven app.

## Layout
- `feedback.html` – form: Name, Email, Feedback Category, Rating, Comments, Submit
- `pom.xml`, `src/` – Maven project (App server + JUnit tests)
- `Jenkinsfile` – Checkout → Build → Test → Package pipeline
- `.github/workflows/maven-build.yml` – GitHub Actions build on push
- `Dockerfile` – multi-stage build, serves on port 8080

## Commands and evidence to capture

### 1. Form in browser
    mvn package && java -jar target/feedback-app-1.0.0.jar
Open http://localhost:8080 and screenshot the form.

### 2. Maven build
    mvn package
Screenshot "BUILD SUCCESS" and `ls -l target/feedback-app-1.0.0.jar`.

### 3. GitHub branch, commit, PR
    git remote add origin https://github.com/<user>/<repo>.git
    git push -u origin main
    git push -u origin feature/feedback-form
Open a Pull Request feature/feedback-form → main on GitHub, merge it, and screenshot the PR and commit list.

### 4. Jenkins
New Item → Pipeline → "Pipeline script from SCM" → your repo URL, script path `Jenkinsfile`.
Ensure tools named `Maven3` and `JDK17` exist (Manage Jenkins → Tools), or delete the `tools {}` block.
Build Now → screenshot the green stage view / console "Finished: SUCCESS".

### 5. GitHub Actions
Pushing the branch triggers the workflow. Repo → Actions tab → screenshot the green "Maven Build" run.

### 6. Docker
    docker build -t feedback-app:1.0 .
    docker run -d --name feedback -p 8080:8080 feedback-app:1.0
    docker images feedback-app
    docker ps
    curl -I http://localhost:8080
Screenshot the terminal output and http://localhost:8080 in the browser.
Cleanup: `docker rm -f feedback`.
