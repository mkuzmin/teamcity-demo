FROM openjdk:8u131-jre-alpine
COPY ./todo.jar /app/todo.jar
CMD ["java", "-jar", "/app/todo.jar"]
EXPOSE 8080
HEALTHCHECK --timeout=40s --interval=1m --retries=3 CMD wget http://localhost:8080/health -O /dev/null
