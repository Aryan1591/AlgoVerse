import { useState } from "react";
import { useNavigate } from "react-router-dom";

const Login = () => {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const handleLogin = (e) => {
    e.preventDefault();

    // TODO: Call backend API
    console.log(email, password);

    // Fake login success
    navigate("/dashboard");
  };

  return (
    <div className="flex items-center justify-center min-h-screen bg-black text-white">
      <form onSubmit={handleLogin} className="bg-gray-900 p-8 rounded-xl shadow-lg w-96">
        <h2 className="text-2xl font-bold mb-4">Login to AlgoVerse</h2>

        <input
          type="email"
          placeholder="Email"
          className="w-full p-2 mb-3 bg-gray-800 border border-gray-700 rounded"
          onChange={(e) => setEmail(e.target.value)}
        />

        <input
          type="password"
          placeholder="Password"
          className="w-full p-2 mb-3 bg-gray-800 border border-gray-700 rounded"
          onChange={(e) => setPassword(e.target.value)}
        />

        <button className="w-full bg-blue-600 p-2 rounded hover:bg-blue-700">
          Login
        </button>

        <p className="mt-3 text-sm">
          Don't have an account?
          <a href="/register" className="text-blue-400 underline ml-1">Register</a>
        </p>
      </form>
    </div>
  );
};

export default Login;
