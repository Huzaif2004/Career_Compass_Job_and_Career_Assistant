import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import api from "../api";

const Signup = () => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (password !== confirmPassword) {
      alert("Passwords do not match");
      return;
    }

    setLoading(true);
    try {
      await api.post("/signup", { email, password });
      navigate("/login");
    } catch {
      alert("Signup failed");
    }
    setLoading(false);
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-violet-600 to-purple-700 px-4">
      <motion.div
        initial={{ opacity: 0, y: 40 }}
        animate={{ opacity: 1, y: 0 }}
        className="w-full max-w-md bg-white/80 backdrop-blur-xl rounded-3xl p-8 shadow-2xl"
      >
        <h2 className="text-3xl font-bold text-center mb-2">
          Create Account 🚀
        </h2>
        <p className="text-center text-gray-600 mb-8">
          Start your AI-powered career journey
        </p>

        <form onSubmit={handleSubmit} className="space-y-6">
          
          <div className="relative">
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              className="peer w-full px-4 pt-5 pb-2 border rounded-xl focus:ring-4 focus:ring-violet-400 outline-none bg-transparent"
            />
            <label className="absolute left-4 top-2 text-sm text-gray-500 peer-placeholder-shown:top-4 peer-placeholder-shown:text-base transition-all">
              Email
            </label>
          </div>

        
          <div className="relative">
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              className="peer w-full px-4 pt-5 pb-2 border rounded-xl focus:ring-4 focus:ring-violet-400 outline-none bg-transparent"
            />
            <label className="absolute left-4 top-2 text-sm text-gray-500 peer-placeholder-shown:top-4 peer-placeholder-shown:text-base transition-all">
              Password
            </label>
          </div>

        
          <div className="relative">
            <input
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              required
              className="peer w-full px-4 pt-5 pb-2 border rounded-xl focus:ring-4 focus:ring-violet-400 outline-none bg-transparent"
            />
            <label className="absolute left-4 top-2 text-sm text-gray-500 peer-placeholder-shown:top-4 peer-placeholder-shown:text-base transition-all">
              Confirm Password
            </label>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full py-3 rounded-2xl text-white font-semibold text-lg
            bg-gradient-to-r from-violet-600 to-purple-600
            hover:scale-[1.03] transition shadow-xl flex items-center justify-center gap-2"
          >
            {loading ? (
              <>
                <span className="animate-spin h-5 w-5 border-2 border-white border-t-transparent rounded-full"></span>
                Creating account...
              </>
            ) : (
              "Signup"
            )}
          </button>
        </form>
      </motion.div>
    </div>
  );
};

export default Signup;
