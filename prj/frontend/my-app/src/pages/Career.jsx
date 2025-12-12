import { useNavigate } from 'react-router-dom';

const Career = () => {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-gradient-to-r from-blue-500 to-purple-600 flex items-center justify-center">
      <div className="bg-white p-8 rounded-lg shadow-lg w-full max-w-md text-center">
        <h1 className="text-3xl font-bold mb-6 text-gray-800">Welcome to Career Compass</h1>
        <p className="text-gray-600 mb-8">Navigate your career path with AI-powered tools.</p>
        <div className="space-y-4">
          <button
            onClick={() => navigate('/analyze')}
            className="w-full bg-blue-600 text-white p-3 rounded hover:bg-blue-700 transition duration-200"
          >
            Job Analyzer
          </button>
          <button
            onClick={() => navigate('/ask')}
            className="w-full bg-purple-600 text-white p-3 rounded hover:bg-purple-700 transition duration-200"
          >
            Ask Career AI
          </button>
        </div>
      </div>
    </div>
  );
};

export default Career;
