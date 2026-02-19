import { useNavigate } from "react-router-dom";
import { useState } from "react";

export default function Dashboard() {
  const navigate = useNavigate();
  const [activeCard, setActiveCard] = useState(null);

  const handleClick = (card, path) => {
    setActiveCard(card);
    setTimeout(() => navigate(path), 600);
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-black via-gray-900 to-indigo-950 text-white px-6 md:px-16 py-12">

      {/* HEADER SECTION */}
      <div className="flex justify-between items-start mb-12">

        {/* LEFT SIDE */}
        <div>
          <h1 className="text-4xl md:text-6xl font-bold text-indigo-400 mb-2">
            Your Learning Dashboard 🚀
          </h1>
          <p className="text-gray-400 text-lg">
            Choose your learning path
          </p>
        </div>

        {/* RIGHT SIDE - STREAK + PROFILE */}
        <div className="flex items-center gap-6">

          {/* Streak */}
          <div className="text-orange-400 font-semibold flex items-center gap-2 text-lg">
            🔥 7
          </div>

          {/* Profile Avatar */}
          <div
            onClick={() => navigate("/profile")}
            className="w-12 h-12 rounded-full bg-indigo-600 flex items-center justify-center cursor-pointer hover:scale-110 transition duration-300 shadow-lg"
          >
            <span className="text-xl font-bold">👤</span>
          </div>

        </div>
      </div>

      {/* MAIN FEATURE CARDS */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-8">

        {/* PRACTICE */}
        <div
          onClick={() => handleClick("practice", "/problems")}
          className="glass-card cursor-pointer hover:scale-105 transition-transform duration-300"
        >
          <h2 className="text-2xl font-semibold">
            <span className={activeCard === "practice" ? "rotate-emoji inline-block" : ""}>🧠</span>
            {" "}Practice Problems
          </h2>
          <p className="text-gray-400 mt-2">
            Topic wise DSA questions
          </p>
        </div>

        {/* TUTORIALS */}
        <div
          onClick={() => handleClick("tutorials", "/tutorials")}
          className="glass-card cursor-pointer hover:scale-105 transition-transform duration-300"
        >
          <h2 className="text-2xl font-semibold">
            <span className={activeCard === "tutorials" ? "rotate-emoji inline-block" : ""}>🎥</span>
            {" "}Watch Tutorials
          </h2>
          <p className="text-gray-400 mt-2">
            Curated YouTube explanations
          </p>
        </div>

        {/* LEADERBOARD */}
        <div
          onClick={() => handleClick("leaderboard", "/leaderboard")}
          className="glass-card cursor-pointer hover:scale-105 transition-transform duration-300"
        >
          <h2 className="text-2xl font-semibold">
            <span className={activeCard === "leaderboard" ? "rotate-emoji inline-block" : ""}>🏆</span>
            {" "}Leaderboard
          </h2>
          <p className="text-gray-400 mt-2">
            See top AlgoVerse learners
          </p>
        </div>

      </div>

      {/* SYSTEM DESIGN SECTION */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-8 mt-12">

        {/* SYSTEM DESIGN TUTORIALS */}
        <div
          onClick={() => handleClick("sd-tutorials", "/system-design-tutorials")}
          className="glass-card cursor-pointer hover:scale-105 transition-transform duration-300"
        >
          <h2 className="text-2xl font-semibold">
            <span className={activeCard === "sd-tutorials" ? "rotate-emoji inline-block" : ""}>📘</span>
            {" "}System Design Tutorials
          </h2>
          <p className="text-gray-400 mt-2">
            Learn scalable system concepts
          </p>
        </div>

        {/* SYSTEM DESIGN PRACTICE */}
        <div
          onClick={() => handleClick("sd-patterns", "/system-design-patterns")}
          className="glass-card cursor-pointer hover:scale-105 transition-transform duration-300"
        >
          <h2 className="text-2xl font-semibold">
            <span className={activeCard === "sd-patterns" ? "rotate-emoji inline-block" : ""}>🧩</span>
            {" "}System Design Practice Patterns
          </h2>
          <p className="text-gray-400 mt-2">
            Real-world interview problems
          </p>
        </div>

      </div>

    </div>
  );
}
