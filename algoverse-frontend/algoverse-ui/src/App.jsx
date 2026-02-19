import { BrowserRouter, Routes, Route } from "react-router-dom";
import Landing from "./pages/Landing";
import Login from "./pages/Login";
import Register from "./pages/Register";
import Dashboard from "./pages/Dashboard";
import Tutorials from "./pages/Tutorials";
import Leaderboard from "./pages/Leaderboard";
import Profile from "./pages/Profile";
import About from "./pages/About";
import Topics from "./pages/Topics";
import Problems from "./pages/Problems";
import SystemDesignTutorials from "./pages/SystemDesignTutorials";
import SystemDesignPatterns from "./pages/SystemDesignPatterns";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Landing />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/dashboard" element={<Dashboard />} />
         <Route path="/tutorials" element={<Tutorials />} />
         <Route path="/leaderboard" element={<Leaderboard />} />
         <Route path="/profile" element={<Profile />} />
         <Route path="/about" element={<About />} />
         <Route path="/topics" element={<Topics />} />
         <Route path="/problems" element={<Problems />} />
         <Route path="/system-design-tutorials" element={<SystemDesignTutorials />} />
         <Route path="/system-design-patterns" element={<SystemDesignPatterns />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
