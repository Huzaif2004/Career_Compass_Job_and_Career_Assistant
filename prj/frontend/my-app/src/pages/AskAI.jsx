import { useState } from 'react';
import api from '../api';

const AskAI = () => {
  const [question, setQuestion] = useState('');
  const [response, setResponse] = useState('');

  const handleAsk = async () => {
    try {
      const res = await api.post('/ask', { question });
      setResponse(res.data.response);
    } catch (error) {
      alert('Failed to get response');
    }
  };

  return (
    <div className="min-h-screen bg-gray-100 p-8">
      <div className="max-w-2xl mx-auto">
        <h1 className="text-3xl font-bold mb-8 text-center">Ask Career AI</h1>
        <div className="bg-white p-6 rounded-lg shadow-md">
          <div className="mb-4">
            <input
              type="text"
              value={question}
              onChange={(e) => setQuestion(e.target.value)}
              className="w-full p-3 border border-gray-300 rounded"
              placeholder="Ask a career-related question..."
            />
          </div>
          <div className="text-center">
            <button
              onClick={handleAsk}
              className="bg-blue-600 text-white px-6 py-2 rounded hover:bg-blue-700 transition duration-200"
            >
              Ask AI
            </button>
          </div>
          {response && (
            <div className="mt-6 p-4 bg-gray-50 rounded">
              <p className="text-gray-800">{response}</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default AskAI;
