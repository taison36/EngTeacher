English Teacher Backend
When learning English, I write down unfamiliar words and phrases but rarely review them.
This app solves that by automatically generating contextual exercises and tracking my progress.

How it works?
  Add phrases you want to learn
  AI generates practice exercises for each phrase
  Practice by using phrases in natural sentences
  AI evaluates your answers and tracks your progress
  Exercises adapt based on your performance

Setup
Create env/.env:
envOPENAI_API_KEY=your_key_here
MONGO_ROOT_USERNAME=admin
MONGO_ROOT_PASSWORD=your_password

Run:

bashdocker-compose up -d --build
Application runs on http://localhost:8080
