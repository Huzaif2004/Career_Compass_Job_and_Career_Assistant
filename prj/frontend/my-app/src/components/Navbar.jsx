import { Link, useNavigate } from "react-router-dom";
import { useState } from "react";
import { motion, AnimatePresence } from "framer-motion";

const Navbar = () => {
  const token = localStorage.getItem("token");
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);

  const username = localStorage.getItem("user") || "User";

  const logout = () => {
    localStorage.clear();
    navigate("/login");
  };

  return (
    <nav className="sticky top-0 z-50 backdrop-blur-xl bg-gradient-to-r from-blue-600/80 to-violet-600/80 shadow-lg">
      <div className="max-w-7xl mx-auto px-6 py-3 flex justify-between items-center">
        <Link className="text-white text-2xl font-bold tracking-wide" to="/">
          Career Compass 
        </Link>

        {!token ? (
          <div className="space-x-6 text-white">
            <Link to="/login">Login</Link>
            <Link to="/signup">Signup</Link>
          </div>
        ) : (
          <div className="relative">
            <button
              onClick={() => setOpen(!open)}
              className="flex items-center gap-3 text-white"
            >
              <div className="h-9 w-9 rounded-full bg-white/20 flex items-center justify-center font-bold">
                {username[0]}
              </div>
              <span className="hidden md:block">{username}</span>
            </button>

            <AnimatePresence>
              {open && (
                <motion.div
                  initial={{ opacity: 0, y: -10 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0 }}
                  className="absolute right-0 mt-4 w-48 bg-white rounded-xl shadow-xl overflow-hidden"
                >
                  <Link className="block px-4 py-3 hover:bg-gray-100" to="/career">
                    Dashboard
                  </Link>
                  <Link className="block px-4 py-3 hover:bg-gray-100" to="/analyze">
                    Analyze Resume
                  </Link>
                  <Link className="block px-4 py-3 hover:bg-gray-100" to="/ask">
                    Ask Career AI
                  </Link>
                  <button
                    onClick={logout}
                    className="w-full text-left px-4 py-3 text-red-600 hover:bg-red-50"
                  >
                    Logout
                  </button>
                </motion.div>
              )}
            </AnimatePresence>
          </div>
        )}
      </div>
    </nav>
  );
};

export default Navbar;
