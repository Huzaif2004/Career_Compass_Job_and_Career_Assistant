**Career Compass – Job & Career Assistant**

Career Compass is a full-stack, AI-powered career guidance platform that helps users analyze resumes, extract skills, match job descriptions, and interact with an intelligent chatbot for personalized career advice. The system combines semantic search, machine learning embeddings, and a RAG-based chatbot to provide accurate and explainable career insights.

**1. System Overview**

The platform allows users to upload resumes, compare them with job descriptions, and understand their skill gaps. A Retrieval-Augmented Generation (RAG) chatbot enables users to ask follow-up questions and receive context-aware answers based on their resume and job requirements.

**Core Capabilities**

Resume parsing and text extraction (PDF/DOCX)

Automatic skill extraction and embeddings

Resume ↔ Job Description matching

RAG-based AI chatbot for career guidance

Secure authentication using JWT

Fast semantic retrieval using Elasticsearch

**2. Architecture (High Level)**

**Frontend (React)**:
Handles authentication, resume upload, job analysis UI, and chatbot interaction.

**Backend (Spring Boot)**:
Manages resume processing, skill extraction, embeddings, matching logic, chatbot orchestration, and security.

**Elasticsearch**:
Stores resume chunks and embeddings for fast semantic retrieval.

**AI Services (OpenAI / Hugging Face)**:
Used for embeddings and AI-generated responses.

**MySQL**:
Stores user data, authentication details, and metadata.

**3 Technologies Used**

**Backend**: Java 17, Spring Boot 3.2.3

**Frontend**: React 19

**Search & Retrieval**: Elasticsearch 8.8.1

**Database**: MySQL

**Authentication**: JWT

**AI APIs**: OpenAI, Hugging Face

**4 Prerequisites**

Ensure the following are installed:

- Java 17+

- Node.js 18+ & npm

- Docker & Docker Compose

- MySQL (or Dockerized MySQL)

- Git

**5️ Backend Setup (Spring Boot)**   

- git clone https://github.com/Huzaif2004/Career_Compass_Job_and_Career_Assistant.git
- cd Career_Compass_Job_and_Career_Assistant/prj


**Configure application.properties**   
**Database**   
- spring.datasource.url=jdbc:mysql://localhost:3306/career_assistant_db   
- spring.datasource.username=root   
- spring.datasource.password=your_password   

**Elasticsearch**   
- elasticsearch.host=http://localhost:9200   

**JWT**   
- jwt.secret=your-long-secret-key  
- jwt.expiration=86400000   

**OpenAI**  
- openai.api.key=your-openai-api-key   

**Hugging Face**   
- huggingface.api.key=your-huggingface-api-key   

**File Upload**   
- spring.servlet.multipart.max-file-size=10MB     
- spring.servlet.multipart.max-request-size=10MB   

**6 OpenAI API Key Setup**   

Go to https://platform.openai.com

- Create an API key

- Add it to:

openai.api.key=your-openai-api-key

Used for:

- Embedding generation

- AI chatbot responses

**7 Elasticsearch Setup**
**Using Docker**

**From prj directory**:

docker-compose up -d

**Elasticsearch will run at**:

http://localhost:9200

**Verify**:

curl http://localhost:9200

**Elasticsearch is used for**:

- Storing resume chunks

- Semantic similarity search

- RAG retrieval

**8. Database Setup (MySQL)**   
CREATE DATABASE career_assistant_db;    

Update credentials in application.properties.

**9 Run Backend**   
mvn clean install   
mvn spring-boot:run   

**Backend URL:**

http://localhost:8081

**10 Frontend Setup (React)**     
- cd prj/frontend/my-app
- npm install
- npm start

Frontend URL:
http://localhost:3000

**11 How RAG Chatbot Works**

- User asks a question

- Relevant resume chunks are retrieved from Elasticsearch

- Retrieved context + user query is sent to OpenAI

- AI generates a personalized, grounded response

- This avoids generic answers and improves accuracy.

**12 Usage Flow**

- Sign up / Login

- Upload resume

- Analyze job descriptions

- Ask career questions via chatbot

- View skill gaps and improvement suggestions
