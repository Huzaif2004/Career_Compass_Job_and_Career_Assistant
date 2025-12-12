import { Link, useNavigate } from 'react-router-dom';

const Navbar = () => {
  const token = localStorage.getItem('token');
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.removeItem('token');
    navigate('/login');
  };

  return (
    <nav className="bg-blue-600 p-4">
      <div className="container mx-auto flex justify-between items-center">
        <Link to="/" className="text-white text-xl font-bold">Career Compass</Link>
        <div className="space-x-4">
          {!token ? (
            <>
              <Link to="/login" className="text-white hover:text-gray-200">Login</Link>
              <Link to="/signup" className="text-white hover:text-gray-200">Signup</Link>
            </>
          ) : (
            <>
              <Link to="/career" className="text-white hover:text-gray-200">Career</Link>
              <Link to="/analyze" className="text-white hover:text-gray-200">Analyze</Link>
              <Link to="/ask" className="text-white hover:text-gray-200">Ask AI</Link>
              <button onClick={handleLogout} className="text-white hover:text-gray-200">Logout</button>
            </>
          )}
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
