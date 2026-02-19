import Navbar from "../components/Navbar";
import ParticlesBg from "../components/ParticlesBg";


export default function Landing() {
  return (
    <div className="min-h-screen relative overflow-hidden">

      {/* PARTICLE BACKGROUND */}
      <ParticlesBg />

      {/* NAVBAR */}
      <Navbar />

      {/* FLOATING DEV ICONS */}
      <div className="absolute inset-0 pointer-events-none z-10">
        <div className="text-indigo-500 text-7xl floating absolute top-24 left-20">{`</>`}</div>
      </div>

      {/* HERO SECTION */}
      <div className="relative z-20 flex flex-col items-center text-center pt-44 px-6">
        <h1 className="text-7xl font-extrabold glow">
          Welcome to <span className="text-indigo-400">AlgoVerse</span>
        </h1>

        <p className="text-gray-400 mt-6 max-w-3xl text-xl">
          Learn Data Structures & Algorithms in a structured way, built by industry professionals for beginners.
        </p>

        <p className="mt-4 text-indigo-400 font-mono text-lg animate-pulse">
          Arrays → Strings → Trees → Graphs → Design → Crack Interviews
        </p>
      </div>

      {/* ABOUT US SECTION */}
      <div className="relative z-20 mt-24 px-10 md:px-24 grid md:grid-cols-2 gap-12 items-center">

        {/* Left Text */}
        <div>
          <h2 className="text-4xl font-bold text-indigo-400 mb-4">About AlgoVerse</h2>
          <p className="font-rubik text-gray-300 text-lg leading-relaxed">
            We are seasoned IT professionals who understand the challenges of mastering Data Structures and Algorithms.
            AlgoVerse was created to empower beginners with a structured, practical, and results-driven learning path.
            Our platform provides a clear, guided, and practical Knowledge to mastering DSA and SystemDesign.
          </p>
        </div>

        {/* Right Glass Card */}
        <div className="glass floating">
          <h3 className="text-2xl font-semibold mb-4">✨ What You Get</h3>
          <ul className="space-y-3 text-gray-300">
            <li>📌 Topic-wise DSA problem roadmap</li>
            <li>🎥 Curated YouTube tutorials</li>
            <li>🏆 Leaderboard to stay motivated</li>
            <li>🧠 System Design Roadmap </li>
            <li>📊 Track your coding progress</li>
          </ul>
        </div>

      </div>
    </div>
  );
}
