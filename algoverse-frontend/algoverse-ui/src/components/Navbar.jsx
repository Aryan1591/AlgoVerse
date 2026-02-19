import { Link } from "react-router-dom";

export default function Navbar() {
  return (
    <nav className="flex justify-between items-center px-12 py-4 fixed w-full z-50 bg-black/40 backdrop-blur-md border-b border-gray-800">

      {/* Logo */}
      <h1 className="text-xl font-bold text-indigo-400">
        Algo<span className="text-white">Verse</span>
      </h1>

      {/* Right Buttons */}
      <div className="flex gap-4">
        <Link to="/login" className="px-4 py-2 text-sm text-gray-300 hover:text-white transition">
          Login
        </Link>

        <Link to="/register" className="px-5 py-2 bg-indigo-600 hover:bg-indigo-700 rounded-lg text-sm font-semibold transition shadow-lg">
          Register
        </Link>
      </div>

    </nav>
  );
}
