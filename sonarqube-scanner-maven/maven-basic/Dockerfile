From jenkins/jnlp-agent-maven:latest as build

RUN mvn sonar:sonar -X -Dsonar.projectKey=sho.ikea.test:cn-maven-test

From  jenkins/jnlp-agent-maven:latest
COPY --from=build /root/.sonar  /root/.sonar
