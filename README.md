Career Compass – Job & Career Assistant

A full-stack AI-powered career assistant platform that helps users analyze resumes, extract skills, match job descriptions, and interact with an intelligent chatbot for personalized career guidance.

1️⃣ Project Overview

Career Compass is a modern career assistant application designed to help students and professionals evaluate their job readiness and improve their applications using AI.

✨ Key Features

📄 Resume upload (PDF/DOCX) and text extraction

🧠 Automatic skill extraction and embedding generation

🔍 Job description vs resume matching

💬 AI-powered chatbot using RAG (Retrieval-Augmented Generation)

🔐 Secure user authentication using JWT

⚡ Fast semantic search using Elasticsearch

🐳 Fully containerized with Docker

🛠 Technologies Used

Backend: Java 17, Spring Boot 3.2.3

Frontend: React 19

Search & Embeddings: Elasticsearch 8.8.1

Database: MySQL

Authentication: JWT

AI APIs: OpenAI, Hugging Face

Containerization: Docker, Docker Compose

2️⃣ Prerequisites

Make sure the following are installed:

Java 17+

Node.js 18+ and npm

Docker & Docker Compose

MySQL (or Dockerized MySQL)

Git

3️⃣ Installation and Setup
📦 Backend Setup (Spring Boot)
git clone https://github.com/Huzaif2004/Career_Compass_Job_and_Career_Assistant.git
cd Career_Compass_Job_and_Career_Assistant/prj

Configure application.properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/career_assistant_db
spring.datasource.username=root
spring.datasource.password=your_password

# Elasticsearch
elasticsearch.host=http://localhost:9200

# JWT
jwt.secret=your-long-secret-key-here
jwt.expiration=86400000

# AI APIs
openai.api.key=your-openai-key
huggingface.api.key=your-huggingface-key

# File upload
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB


⚠️ Do not commit application.properties (keep it in .gitignore).

Build and Run Backend
mvn clean install
mvn spring-boot:run


📍 Backend runs on: http://localhost:8081

🎨 Frontend Setup (React)
cd prj/frontend/my-app
npm install
npm start


📍 Frontend runs on: http://localhost:3000

🗄 Database Setup (MySQL)
CREATE DATABASE career_assistant_db;


Update credentials in application.properties.

🔍 Elasticsearch Setup

From prj directory:

docker-compose up -d


📍 Elasticsearch runs on: http://localhost:9200

🐳 Full Docker Setup (Recommended)
docker-compose up --build

Service	URL
Backend	http://localhost:8081

Frontend	http://localhost:3000

Elasticsearch	http://localhost:9200

MySQL	localhost:3306
4️⃣ Configuration

Secrets: Use environment variables for production

File Upload Limit: 10MB

Logging: Configure via application.properties

Ports: Backend (8081), Frontend (3000)

5️⃣ API Endpoints
🔐 Authentication
POST /signup

Register a new user.

{
  "username": "john",
  "email": "john@email.com",
  "password": "password123"
}


Response

{
  "message": "User registered successfully"
}

POST /login

Authenticate and get JWT.

{
  "username": "john",
  "password": "password123"
}


Response

{
  "token": "jwt-token-here"
}

📄 Resume APIs
POST /api/resume/resume-upload

Upload and process resume.

Headers

Authorization: Bearer <token>


Request

Multipart file (PDF/DOCX)

Response

{
  "message": "success",
  "userId": 1,
  "skills": ["Java", "Spring Boot"],
  "chunks": 5
}

💬 Chat API
POST /api/chat

AI career chatbot (RAG-based).

Headers

Authorization: Bearer <token>


Request

{
  "message": "What skills should I improve for backend roles?"
}


Response

{
  "reply": "You should focus on Docker, Microservices, and AWS."
}

🔎 Retrieval API
POST /api/retrieval/topk

Retrieve top-k relevant resume chunks.

{
  "query": "Spring Boot experience",
  "userId": 1,
  "k": 5
}


Response

[
  {
    "text": "Worked on Spring Boot microservices...",
    "score": 0.87
  }
]

📊 Analysis APIs
GET /test-embed

Test embedding generation.

Response

[0.012, -0.034, 0.98, ...]

POST /match

Match job description with resume.

{
  "jd": "Looking for Java backend developer with Spring Boot experience"
}


Response

{
  "match_percentage": 78,
  "matched_skills": ["Java", "Spring Boot"],
  "missing_skills": ["Docker", "AWS"]
}

POST /test-chunk

Test resume chunking.

Response

{
  "total_chunks": 5,
  "chunks": ["chunk1", "chunk2"]
}

6️⃣ Usage

Sign up / Login

Upload resume

Ask AI career questions

Analyze job descriptions

View skill gaps and suggestions

Frontend Pages

Login

Signup

Analyze

Ask AI

Career Dashboard

7️⃣ Testing
Backend
mvn test

Frontend
npm test

API Testing

Postman

curl

8️⃣ Deployment
Production Build
npm run build
mvn clean package

Docker Deployment
docker-compose up --build -d

9️⃣ Contributing

Contributions are welcome!

Fork the repo

Create a new branch

Commit changes

Open a Pull Request
