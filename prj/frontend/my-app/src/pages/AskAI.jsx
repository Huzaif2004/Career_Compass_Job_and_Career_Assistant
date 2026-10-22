import { useState, useRef, useEffect } from "react";
import { Send, Copy, Bot } from "lucide-react";
import api from "../api";

const AskAIChat = () => {
  const [messages, setMessages] = useState([
    {
      role: "ai",
      content:
        "Hi 👋 I’m your Career AI. Ask me anything about your resume or career.",
    },
  ]);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);

  const chatEndRef = useRef(null);

  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, loading]);

  const sendMessage = async () => {
  if (!input.trim() || loading) return;

  const userMessage = { role: "user", content: input };
  setMessages((prev) => [...prev, userMessage]);
  setInput("");
  setLoading(true);

  try {
    const res = await api.post("/api/chat", {
      message: userMessage.content   
    });

    const aiMessage = {
      role: "ai",
      content: res.data              
    };

    setMessages((prev) => [...prev, aiMessage]);
  } catch (err) {
    console.error(err);
    setMessages((prev) => [
      ...prev,
      { role: "ai", content: "⚠️ Failed to get response from AI." }
    ]);
  } finally {
    setLoading(false);
  }
};


  const copyText = (text) => {
    navigator.clipboard.writeText(text);
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-[#0f0f1a] via-[#111827] to-[#0b1020] flex items-center justify-center px-4">
      
      <div className="w-full max-w-4xl h-[85vh] bg-white/5 backdrop-blur-xl border border-white/10 rounded-2xl shadow-2xl flex flex-col overflow-hidden">
        
        <div className="flex items-center gap-3 px-6 py-4 border-b border-white/10">
          <div className="relative">
            <Bot className="text-purple-400" />
            <span className="absolute -bottom-1 -right-1 w-3 h-3 bg-green-400 rounded-full animate-pulse" />
          </div>
          <div>
            <h2 className="text-white font-semibold">Career AI</h2>
            <p className="text-xs text-gray-400">Online • Ready to help</p>
          </div>
        </div>

        
        <div className="flex-1 overflow-y-auto px-6 py-4 space-y-6 scrollbar-thin scrollbar-thumb-white/10">
          {messages.map((msg, index) => (
            <div
              key={index}
              className={`flex ${
                msg.role === "user" ? "justify-end" : "justify-start"
              }`}
            >
              <div
                className={`max-w-[75%] px-5 py-4 rounded-2xl text-sm leading-relaxed
                ${
                  msg.role === "user"
                    ? "bg-gradient-to-br from-purple-600 to-blue-600 text-white rounded-br-none"
                    : "bg-white/10 text-gray-100 backdrop-blur-md border border-white/10 rounded-bl-none"
                }
                animate-fadeInUp`}
              >
                <p className="whitespace-pre-wrap">{msg.content}</p>

                {msg.role === "ai" && (
                  <div className="flex gap-2 mt-3 text-xs text-gray-400">
                    <button
                      onClick={() => copyText(msg.content)}
                      className="flex items-center gap-1 hover:text-white transition"
                    >
                      <Copy size={14} /> Copy
                    </button>
                  </div>
                )}
              </div>
            </div>
          ))}

          
          {loading && (
            <div className="flex justify-start">
              <div className="bg-white/10 px-4 py-3 rounded-xl text-gray-300 animate-pulse">
                AI is typing<span className="animate-bounce">...</span>
              </div>
            </div>
          )}

          <div ref={chatEndRef} />
        </div>

     
        <div className="p-4 border-t border-white/10 bg-white/5 backdrop-blur-lg">
          <div className="flex items-center gap-3">
            <input
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={(e) => e.key === "Enter" && sendMessage()}
              placeholder="Ask me anything..."
              className="flex-1 bg-white/10 text-white placeholder-gray-400 px-5 py-3 rounded-full outline-none focus:ring-2 focus:ring-purple-500 transition"
            />
            <button
              onClick={sendMessage}
              className="bg-gradient-to-br from-purple-600 to-blue-600 p-3 rounded-full text-white shadow-lg hover:scale-105 hover:shadow-purple-500/50 transition-all"
            >
              <Send size={18} />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AskAIChat;
