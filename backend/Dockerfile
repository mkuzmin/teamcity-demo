FROM openjdk:8u131-jre-alpine
COPY ./todo.jar /app/todo.jar
CMD ["java", "-jar", "/app/todo.jar"]
EXPOSE 8080
# HEALTHCHECK --timeout=3s --interval=5s --retries=10 CMD curl -f http://localhost:8080/rest/health
