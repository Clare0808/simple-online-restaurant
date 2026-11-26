FROM node:20-alpine AS frontend
WORKDIR /build
COPY package.json package-lock.json ./
RUN npm ci --include=dev
COPY babel.config.js vue.config.js .eslintrc.js .browserslistrc jsconfig.json ./
COPY public ./public
COPY src ./src
RUN npm run build

FROM maven:3.9-eclipse-temurin-17 AS backend-build
WORKDIR /build
COPY backend/pom.xml backend/pom.xml
COPY backend/src backend/src
COPY --from=frontend /build/dist backend/src/main/resources/static
RUN mvn -f backend/pom.xml -B -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=backend-build /build/backend/target/restaurant-backend-1.0.0.jar app.jar
ENV PORT=10000
EXPOSE 10000
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
