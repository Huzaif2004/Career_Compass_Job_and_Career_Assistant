import { useState, useEffect } from "react";
import { motion, AnimatePresence } from "framer-motion";
import api from "../api";

const Analyze = () => {
  const [jobDescription, setJobDescription] = useState("");
  const [resumeInfo, setResumeInfo] = useState("");
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);

  // animated project %
  const [projectPercent, setProjectPercent] = useState(0);

  
  const toTitle = (str) =>
    typeof str === "string"
      ? str.replace(/\b\w/g, (c) => c.toUpperCase())
      : str;

  const formatSkillList = (list) =>
    list && list.length ? list.map(toTitle).join(", ") : "None";

 
  const handleFileUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    const formData = new FormData();
    formData.append("resume", file);

    try {
      const res = await api.post("/api/resume/resume-upload", formData, {
        headers: { "Content-Type": "multipart/form-data" },
      });

      setResumeInfo(
        `Resume uploaded successfully. Extracted skills: ${formatSkillList(
          res.data.skills || []
        )}`
      );
    } catch {
      alert("Resume upload failed");
    }
  };

  
  const handleAnalyze = async () => {
    if (!jobDescription.trim()) {
      alert("Paste a Job Description first!");
      return;
    }

    setLoading(true);
    setResult(null);

    try {
      const res = await api.post(
        `/match?jd=${encodeURIComponent(jobDescription)}`,
        {},
        {
          headers: {
            Authorization: "Bearer " + localStorage.getItem("token"),
          },
        }
      );
      setResult(res.data);
    } catch {
      alert("Matching failed");
    }

    setLoading(false);
  };

  
  useEffect(() => {
    if (result?.project_score != null) {
      const target = Math.round(result.project_score * 100);
      let current = 0;

      const timer = setInterval(() => {
        current += 2;
        if (current >= target) {
          current = target;
          clearInterval(timer);
        }
        setProjectPercent(current);
      }, 15);

      return () => clearInterval(timer);
    }
  }, [result]);

  
  const friendlyScore = (s) =>
    s >= 0.75
      ? "Excellent Match"
      : s >= 0.5
      ? "Good Match"
      : s >= 0.3
      ? "Average Match"
      : "Weak Match";

  
  const jdSkills = result?.jdSkills || [];
  console.log(result);
  const matchedSkills = result?.matched_skills || [];
  const missingSkills = result?.missing_skills || [];

  const experienceText =
    result?.experience_score >= 0.75
      ? "Experience strongly matches job requirements"
      : result?.experience_score >= 0.4
      ? "Experience partially matches requirements"
      : "Experience gap detected — consider internships or relevant projects";

 
  return (
    <div className="min-h-screen px-6 py-12 bg-gradient-to-br from-slate-100 to-slate-200">
      <motion.h1
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
        className="text-4xl font-extrabold text-center mb-12 text-gray-800"
      >
        AI Career Match Analyzer
      </motion.h1>

      
      <div className="grid md:grid-cols-2 gap-8 max-w-6xl mx-auto">
        <textarea
          className="h-64 p-6 rounded-2xl bg-white/80 backdrop-blur-xl shadow-xl border focus:ring-4 focus:ring-blue-400 outline-none"
          placeholder="Paste Job Description here..."
          value={jobDescription}
          onChange={(e) => setJobDescription(e.target.value)}
        />

        <div className="bg-white/80 backdrop-blur-xl rounded-2xl p-6 shadow-xl border">
          <h2 className="font-semibold text-lg mb-4">Upload Resume</h2>
          <input
            type="file"
            accept=".pdf,.doc,.docx"
            onChange={handleFileUpload}
          />
          {resumeInfo && (
            <p className="mt-4 text-sm bg-blue-50 p-3 rounded-xl border">
              {resumeInfo}
            </p>
          )}
        </div>
      </div>

      
      <div className="text-center mt-10">
        <button
          onClick={handleAnalyze}
          className="px-12 py-4 rounded-2xl text-white font-semibold text-lg
          bg-gradient-to-r from-blue-600 to-violet-600
          hover:scale-105 transition shadow-xl"
        >
          Analyze Match
        </button>
      </div>

      
      <AnimatePresence>
        {loading && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            className="mt-12 max-w-xl mx-auto bg-white/80 backdrop-blur-xl p-6 rounded-2xl shadow-xl"
          >
            <p className="font-semibold mb-4">
              AI is analyzing your profile…
            </p>
            <ul className="space-y-2 text-gray-600">
              <li>🔍 Parsing Resume</li>
              <li>🧠 Understanding Job Description</li>
              <li>⚙️ Matching Skills</li>
              <li>✨ Generating Insights</li>
            </ul>
          </motion.div>
        )}
      </AnimatePresence>

     
      {result && !loading && (
        <motion.div
          initial={{ opacity: 0, y: 30 }}
          animate={{ opacity: 1, y: 0 }}
          className="mt-16 max-w-6xl mx-auto bg-white/80 backdrop-blur-xl p-10 rounded-3xl shadow-2xl"
        >
          
          <div className="flex items-center gap-8 mb-10">
            <div className="h-32 w-32 rounded-full border-8 border-blue-500 flex items-center justify-center text-3xl font-bold">
              {result.percentage}%
            </div>
            <div>
              <p className="text-2xl font-bold">
                {friendlyScore(result.final_score)}
              </p>
              <p className="text-gray-600">Overall Job Compatibility</p>
            </div>
          </div>

         
          <div className="grid md:grid-cols-2 gap-6 mb-10">
            
            <div className="bg-slate-100 p-5 rounded-xl">
              <p className="font-semibold mb-2">Matched Skills</p>
              {matchedSkills.length ? (
                <div className="flex flex-wrap gap-2">
                  {matchedSkills.map((s) => (
                    <span
                      key={s}
                      className="px-3 py-1 bg-green-100 text-green-700 rounded-full text-sm"
                    >
                      {toTitle(s)}
                    </span>
                  ))}
                </div>
              ) : (
                <p className="text-gray-500">No skills matched</p>
              )}
            </div>

           
            <div className="bg-slate-100 p-5 rounded-xl">
              <p className="font-semibold mb-2">Missing Skills</p>
              {missingSkills.length ? (
                <div className="flex flex-wrap gap-2">
                  {missingSkills.map((s) => (
                    <span
                      key={s}
                      className="px-3 py-1 bg-red-100 text-red-600 rounded-full text-sm"
                    >
                      {toTitle(s)}
                    </span>
                  ))}
                </div>
              ) : (
                <p className="text-green-600">No missing skills 🎉</p>
              )}
            </div>
          </div>

         
          <div className="bg-slate-100 p-5 rounded-xl mb-8">
            <p className="font-semibold mb-2">Experience</p>
            <p>{experienceText}</p>
          </div>

         
          <div className="bg-slate-100 p-5 rounded-xl mb-10">
            <p className="font-semibold mb-3">Projects Strength</p>
            <div className="w-full bg-gray-300 rounded-full h-4 overflow-hidden">
              <motion.div
                className="h-4 bg-gradient-to-r from-blue-500 to-violet-500"
                initial={{ width: 0 }}
                animate={{ width: `${projectPercent}%` }}
              />
            </div>
            <p className="mt-2 font-semibold">{projectPercent}%</p>
          </div>

          
          <div className="bg-blue-50 border border-blue-200 p-6 rounded-2xl">
            <h3 className="text-xl font-semibold mb-3">AI Evaluation</h3>
            <pre className="whitespace-pre-wrap text-sm text-gray-700">
              {result.ai_feedback}
            </pre>
          </div>
        </motion.div>
      )}
    </div>
  );
};

export default Analyze;
