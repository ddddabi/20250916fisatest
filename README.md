# Jenkins 와 Docker를 활용한 Gradle 프로젝트 빌드 및 실행

## 📌 목표
- Spring Boot Gradle 프로젝트를 **GitHub → Jenkins 빌드 → Docker Ubuntu 컨테이너 실행**까지 실행
- **두 가지 마운트 방식(Bind Mount, Volume Mount)** 으로 JAR 실행 실습

---

## ⚙️ 환경
- **호스트 OS**: Ubuntu Server 20.04  
- **Docker 컨테이너**
  - Jenkins (LTS, JDK17)
  - MySQL 8.0
  - Ubuntu 20.04 (앱 실행용)
- **빌드 도구**: Gradle Wrapper (`./gradlew`)
- **소스 코드 저장소**: GitHub

---

## 🚀 전체 흐름
```
GitHub ──▶ Jenkins 컨테이너 ──▶ 빌드(JAR 생성)
                           │
                           ├─▶ Bind Mount (호스트 디렉토리 공유)
                           │        └─▶ ubt-bind 컨테이너 실행
                           │
                           └─▶ Volume Mount (Docker Volume 공유)
                                    └─▶ ubt-vol 컨테이너 실행
```
---
## 1️⃣ 프로젝트 빌드 (Jenkins)

1. GitHub Push

2. Jenkins Job 생성 (step03_teamArt)
    - Build Steps → Execute Shell
      ```
      chmod +x ./gradlew
      ./gradlew clean bootJar -x test
      ```
  
    - 빌드 성공 후 JAR 생성 위치
      ```
      /var/jenkins_home/workspace/step03_teamArt/build/libs/step04_gradleBuild-0.0.1-SNAPSHOT.jar
      ```

## 2️⃣ Bind Mount 방식

1. 호스트 홈 디렉토리에 jar 복사
    ```
    docker cp myjenkins:/var/jenkins_home/workspace/step03_teamArt/build/libs/step04_gradleBuild-0.0.1-SNAPSHOT.jar /home/ubuntu/
    ```
2. 컨테이너 실행 (호스트 /home/ubuntu ↔ 컨테이너 /app):
    ```
      docker run -it --name ubt-bind \
        -p 8080:8080 \
        -v /home/ubuntu:/app \
        ubuntu:20.04 /bin/bash
    ```

3. 컨테이너 내부:
    ```
    apt-get update
    apt-get install -y openjdk-17-jdk
    cd /app
    java -jar step04_gradleBuild-0.0.1-SNAPSHOT.jar --server.address=0.0.0.0
    ```
    
## 3️⃣ Volume Mount 방식

1. 볼륨 생성:
    ```
    docker volume create gradle_vol
    ```
2. jar를 볼륨 경로에 복사:
    ```
    sudo cp /home/ubuntu/step04_gradleBuild-0.0.1-SNAPSHOT.jar /var/lib/docker/volumes/gradle_vol/_data/
    ```
3.컨테이너 실행 (gradle_vol ↔ /app):
    ```
    docker run -it --name ubt-vol \
      -p 9090:9090 \
      -v gradle_vol:/app \
      ubuntu:20.04 /bin/bash
    ```
4. 컨테이너 내부:
    ```
    apt-get update
    apt-get install -y openjdk-17-jdk
    cd /app
    java -jar step04_gradleBuild-0.0.1-SNAPSHOT.jar --server.address=0.0.0.0 --server.port=9090
    ```
    
## ✅ 결과

- Jenkins 빌드 성공 → jar 생성 확인
- ubt-bind (Bind Mount) 컨테이너에서 jar 실행 성공
- ubt-vol (Volume Mount) 컨테이너에서 jar 실행 성공

- 브라우저 확인:
  - http://localhost:8080/hello (Bind Mount)
  - http://localhost:9090/hello (Volume Mount)
