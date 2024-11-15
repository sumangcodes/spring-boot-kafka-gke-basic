# spring-boot-kafka-gke-basic

# GKE Kafka and Spring Boot Integration

## Overview

This repository demonstrates how to set up a Kafka environment on Google Kubernetes Engine (GKE) using Helm, and a Spring Boot application that interacts with Kafka for producing and consuming messages. The setup involves configuring Kafka, Kafka UI, and Zookeeper on GKE, and then using a Spring Boot application for interacting with Kafka topics.

## Table of Contents

1. [Kafka, Kafka UI, and Zookeeper Setup on GKE](#kafka-kafka-ui-and-zookeeper-setup-on-gke)
    - [Prerequisites](#prerequisites)
    - [Setup Instructions](#setup-instructions)
    - [Verification and Cleanup](#verification-and-cleanup)
2. [Spring Boot Kafka Integration](#spring-boot-kafka-integration)
    - [Project Features](#project-features)
    - [Project Structure](#project-structure)
    - [Setup and Configuration](#setup-and-configuration)
    - [Running the Application](#running-the-application)
    - [REST API Endpoints](#rest-api-endpoints)
    - [Kafka Configuration](#kafka-configuration)
    - [Logging Configuration](#logging-configuration)
3. [License](#license)

---

## Kafka, Kafka UI, and Zookeeper Setup on GKE

### Prerequisites

- **GKE Cluster**: Ensure your GKE cluster is up and running.
- **kubectl**: Connected to your GKE cluster.
- **Helm**: Installed and configured.

### Setup Instructions

1. **Create a Namespace**

   Create a namespace called `kafka`:

   ```bash
   kubectl create namespace kafka
Install Zookeeper

Use the Bitnami Helm chart to install Zookeeper:

bash
Copy code
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update
helm install zookeeper bitnami/zookeeper --namespace kafka --set replicaCount=3
Install Kafka

Install Kafka with external access enabled:

bash
Copy code
helm install kafka bitnami/kafka --namespace kafka \
  --set zookeeper.enabled=false \
  --set replicaCount=3 \
  --set externalAccess.enabled=true \
  --set externalAccess.service.type=LoadBalancer \
  --set externalAccess.autoDiscovery.enabled=true \
  --set auth.enabled=false \
  --set zookeeper.hosts=zookeeper
Install Kafka UI

Add the Kafka UI Helm repository and install:

bash
Copy code
helm repo add kafka-ui https://provectus.github.io/kafka-ui
helm repo update
helm install kafka-ui kafka-ui/kafka-ui --namespace kafka \
  --set envs.config.KAFKA_CLUSTERS_0_NAME=my-cluster \
  --set envs.config.KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS=kafka:9092 \
  --set service.type=LoadBalancer
Verification and Cleanup
Verify Zookeeper:

bash
Copy code
kubectl get pods -n kafka -l app.kubernetes.io/name=zookeeper
Verify Kafka:

bash
Copy code
kubectl get pods -n kafka -l app.kubernetes.io/name=kafka
Verify Kafka UI:

bash
Copy code
kubectl get svc -n kafka -l app.kubernetes.io/name=kafka-ui
Cleanup:

bash
Copy code
helm uninstall kafka --namespace kafka
helm uninstall zookeeper --namespace kafka
helm uninstall kafka-ui --namespace kafka
kubectl delete namespace kafka
Spring Boot Kafka Integration
Project Features
Produce and consume Kafka messages using REST endpoints.
Configurable Kafka producer, consumer, and admin.
Logging setup for debugging Kafka interactions.
Project Structure
plaintext
Copy code
spring-kafka-example/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com.example.spring_kafka_example/
│   │   │       ├── config/                # Kafka configuration classes
│   │   │       ├── controller/            # REST Controllers for Kafka Producer and Consumer
│   │   └── resources/
│   │       └── application.properties     # Application and Kafka configurations
├── logs/                                  # Logs folder
├── pom.xml                                # Maven Project File
└── README.md                              # Project Documentation
Setup and Configuration
1. Clone the Repository
bash
Copy code
git clone https://github.com/YOUR_GITHUB_USERNAME/spring-kafka-example.git
cd spring-kafka-example
2. Configure Kafka Details
Set the following environment variables:

bash
Copy code
export KAFKA_BOOTSTRAP_SERVERS="your_kafka_bootstrap_server"
export KAFKA_USERNAME="your_kafka_username"
export KAFKA_PASSWORD="your_kafka_password"
Or configure them in application.properties.

3. Install Dependencies
Install dependencies using Maven:

bash
Copy code
mvn clean install
Running the Application
1. Start the Kafka Server
Ensure your Kafka server is running and accessible.

2. Run the Application
bash
Copy code
mvn spring-boot:run
The application will start at http://localhost:8080.

REST API Endpoints
1. Publish a Message
Endpoint: POST /api/kafka/publish

Parameter	Type	Description
message	String	The message to send
Example:

bash
Copy code
curl -X POST "http://localhost:8080/api/kafka/publish?message=HelloKafka"
2. Consume a Message
Endpoint: GET /api/kafka/consume

Response: Last consumed message from Kafka.

Kafka Configuration
Producer: Configured with KafkaTemplate.
Consumer: Uses a listener with ConcurrentKafkaListenerContainerFactory.
Topic Creation: Managed by Spring Kafka Admin.
Logging Configuration
Configured for better observability with logs stored in logs/application.log.

Example Configuration in application.properties:

properties
Copy code
logging.level.root=DEBUG
logging.file.name=logs/application.log
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n
