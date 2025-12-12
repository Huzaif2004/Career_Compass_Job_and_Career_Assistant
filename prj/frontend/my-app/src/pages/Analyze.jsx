import { useState } from "react";
import api from "../api";

const Analyze = () => {
  const [jobDescription, setJobDescription] = useState("");
  const [resumeFile, setResumeFile] = useState(null);
  const [resumeInfo, setResumeInfo] = useState("");
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);

  // Convert any string like "microservices" → "Microservices"
  const toTitle = (str) =>
    typeof str === "string"
      ? str.replace(/\b\w/g, (c) => c.toUpperCase())
      : str;

  // Convert skill list to Title Case
  const formatSkillList = (list) =>
    list && list.length > 0
      ? list.map(toTitle).join(", ")
      : "None";

  // ------------------------ Resume Upload ------------------------
  const handleFileUpload = async (e) => {
    const file = e.target.files[0];
    setResumeFile(file);

    const formData = new FormData();
    formData.append("resume", file);

    try {
      const response = await api.post("/resume-upload", formData, {
        headers: {
          "Content-Type": "multipart/form-data",
          "Authorization": "Bearer " + localStorage.getItem("token"),
        },
      });

      setResumeInfo(
        `Resume uploaded successfully. Extracted skills: ${
          formatSkillList(response.data.skillsFound || [])
        }`
      );
    } catch (err) {
      console.error(err);
      alert("Failed to upload resume!");
    }
  };

  // ------------------------ Analyze Resume + JD ------------------------
  const handleAnalyze = async () => {
    if (!jobDescription.trim()) return alert("Paste a Job Description first!");

    setLoading(true);
    setResult(null);

    try {
      const response = await api.post(
        `/match?jd=${encodeURIComponent(jobDescription)}`,
        {},
        { headers: { Authorization: "Bearer " + localStorage.getItem("token") } }
      );
      setResult(response.data);
    } catch (err) {
      console.error(err);
      alert("Matching failed!");
    }

    setLoading(false);
  };

  // ------------------------ Human-friendly interpretation ------------------------
  const friendlyScore = (score) => {
    if (score >= 0.75) return "Excellent Match";
    if (score >= 0.5) return "Good Match";
    if (score >= 0.3) return "Average Match";
    return "Weak Match";
  };

  const experienceComment = (exp, required) => {
    if (required === 0) return "Experience not specified";
    if (exp >= required) return "Experience meets requirement";
    if (exp === 0) return "No experience found — needs improvement";
    return `Has ${exp} years, needs ${required} years`;
  };

  const projectComment = (count) => {
    if (count >= 3) return "Strong project background";
    if (count === 2) return "Good project experience";
    if (count === 1) return "Some project exposure — needs more";
    return "No significant projects detected";
  };

  return (
    <div className="min-h-screen bg-gray-50 p-8">
      <h1 className="text-4xl font-bold text-center mb-10 text-gray-800">
        AI Career Match Analyzer
      </h1>

      {/* ------------------------ INPUT SECTION ------------------------ */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-8">

        {/* Job Description */}
        <textarea
          className="border p-4 rounded-lg w-full h-64 shadow bg-white focus:ring-2 focus:ring-blue-400 outline-none"
          placeholder="Paste Job Description here..."
          value={jobDescription}
          onChange={(e) => setJobDescription(e.target.value)}
        />

        {/* Resume Upload */}
        <div className="bg-white p-6 shadow rounded-lg">
          <h2 className="font-semibold mb-3 text-lg text-gray-700">
            Upload Resume (PDF / DOC / DOCX)
          </h2>

          <input
            type="file"
            accept=".pdf,.doc,.docx"
            onChange={handleFileUpload}
            className="mb-3"
          />

          {resumeInfo && (
            <p className="mt-3 text-sm text-gray-700 bg-gray-100 p-3 rounded-lg border">
              {resumeInfo}
            </p>
          )}
        </div>
      </div>

      {/* Analyze Button */}
      <div className="text-center">
        <button
          onClick={handleAnalyze}
          className="mt-8 bg-blue-600 hover:bg-blue-700 text-white px-10 py-3 rounded-xl shadow-lg text-lg"
        >
          Analyze Match
        </button>
      </div>

      {/* LOADING SPINNER */}
      {loading && (
        <div className="flex justify-center mt-10">
          <div className="animate-spin rounded-full h-14 w-14 border-4 border-blue-500 border-t-transparent"></div>
        </div>
      )}

      {/* ------------------------ RESULT SECTION ------------------------ */}
      {result && !loading && (
        <div className="mt-12 bg-white p-8 rounded-xl shadow-lg animate-fadeIn">

          <h2 className="text-2xl font-bold mb-6 text-gray-800">Match Summary</h2>

          {/* Summary Block */}
          <div className="bg-blue-50 border border-blue-300 p-5 rounded-lg mb-6">
            <p className="text-lg font-semibold text-blue-800">
              Overall Match: {friendlyScore(result.final_score)}
            </p>
            <p className="text-3xl font-bold text-blue-700 mt-2">
              {result.percentage}% Match
            </p>
          </div>

          {/* JD Skills */}
          <div className="mb-6 bg-gray-50 p-4 rounded-lg border">
            <p>
              <strong>Job Description Skills:</strong>{" "}
              {formatSkillList(result.jdSkills || [])}
            </p>
          </div>

          {/* Human-friendly evaluation cards */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">

            {/* Skill Match */}
            <div className="bg-gray-100 p-5 rounded-lg border shadow-sm">
              <p className="font-semibold">Skill Match</p>
              <p className="mt-1">
                {result.matched_skills} skill(s) match out of{" "}
                {result.jdSkills?.length || 0}.
              </p>
              <p className="mt-2 text-gray-600">
                Missing Skills: {formatSkillList(result.missing_skills || [])}
              </p>
            </div>

            {/* Experience */}
            <div className="bg-gray-100 p-5 rounded-lg border shadow-sm">
              <p className="font-semibold">Experience Match</p>
              <p className="mt-1">
                {experienceComment(
                  result.experience_score * (result.jdSkills?.length || 1),
                  result.jdSkills?.length || 1
                )}
              </p>
            </div>

            {/* Projects */}
            <div className="bg-gray-100 p-5 rounded-lg border shadow-sm">
              <p className="font-semibold">Projects</p>
              <p className="mt-1">
                {projectComment(result.project_score * 3)}
              </p>
            </div>

            {/* Keywords */}
            <div className="bg-gray-100 p-5 rounded-lg border shadow-sm">
              <p className="font-semibold">Role Relevance</p>
              <p className="mt-1">
                Based on keywords found in your resume that match the job role.
              </p>
            </div>
          </div>

          {/* Resume Snippet */}
          <div className="mt-6 bg-gray-50 p-5 border rounded-lg">
            <strong>Resume Snippet:</strong>
            <p className="text-gray-700 mt-2">{result.resume_text}</p>
          </div>

          {/* AI Feedback */}
          <div className="mt-8 bg-blue-50 p-6 rounded-lg border border-blue-300 shadow">
            <h3 className="font-semibold text-xl mb-3 text-blue-800">
              AI Evaluation
            </h3>
            <pre className="whitespace-pre-wrap text-gray-800 text-sm leading-relaxed">
              {result.ai_feedback}
            </pre>
          </div>

        </div>
      )}
    </div>
  );
};

export default Analyze;
