import { motion } from "framer-motion";
import { useNavigate } from "react-router-dom";

const Career = () => {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-600 to-violet-700 flex items-center justify-center px-6">
      <motion.div
        initial={{ opacity: 0, y: 40 }}
        animate={{ opacity: 1, y: 0 }}
        className="bg-white/80 backdrop-blur-xl rounded-3xl p-10 max-w-xl text-center shadow-2xl"
      >
        <h1 className="text-4xl font-bold mb-4">
          Navigate Your Career with AI Precision 🚀
        </h1>
        <p className="text-gray-600 mb-8">
          Analyze job readiness, uncover skill gaps, and get AI-driven guidance.
        </p>

        <div className="space-y-4">
          <button
            onClick={() => navigate("/analyze")}
            className="w-full py-3 rounded-xl text-white bg-gradient-to-r from-blue-600 to-indigo-600 hover:scale-105 transition"
          >
            Analyze My Resume
          </button>
          <button
            onClick={() => navigate("/ask")}
            className="w-full py-3 rounded-xl text-white bg-gradient-to-r from-violet-600 to-purple-600 hover:scale-105 transition"
          >
            Ask Career AI
          </button>
        </div>
      </motion.div>
    </div>
  );
};

export default Career;
