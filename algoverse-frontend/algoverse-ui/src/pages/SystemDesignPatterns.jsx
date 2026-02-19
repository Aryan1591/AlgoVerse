import { useState } from "react";

export default function SystemDesignPatterns() {
  const [search, setSearch] = useState("");
  const [difficultyFilter, setDifficultyFilter] = useState("All");
  const [topicFilter, setTopicFilter] = useState("All");
  const [solved, setSolved] = useState([]);

  const problems = [
    {
      id: 1,
      title: "Design URL Shortener",
      difficulty: "Medium",
      topic: "Scalability",
      url: "https://leetcode.com/discuss/interview-question/system-design/124658/design-url-shortener",
    },
    {
      id: 2,
      title: "Design Twitter",
      difficulty: "Hard",
      topic: "Distributed Systems",
      url: "https://leetcode.com/problems/design-twitter/",
    },
    {
      id: 3,
      title: "Design Rate Limiter",
      difficulty: "Hard",
      topic: "Networking",
      url: "https://leetcode.com/discuss/interview-question/system-design/344866/design-rate-limiter",
    },
    {
      id: 4,
      title: "Design Chat System",
      difficulty: "Medium",
      topic: "Messaging",
      url: "https://leetcode.com/discuss/interview-question/system-design/329887/design-chat-system",
    },
  ];

  const toggleSolved = (id) => {
    if (solved.includes(id)) {
      setSolved(solved.filter((item) => item !== id));
    } else {
      setSolved([...solved, id]);
    }
  };

  const filtered = problems.filter((item) => {
    return (
      item.title.toLowerCase().includes(search.toLowerCase()) &&
      (difficultyFilter === "All" || item.difficulty === difficultyFilter) &&
      (topicFilter === "All" || item.topic === topicFilter)
    );
  });

  return (
    <div className="min-h-screen bg-black text-white px-6 md:px-16 py-10">
      <h1 className="text-4xl font-bold text-indigo-400 mb-8">
        🧩 System Design Practice Patterns
      </h1>

      {/* Filters */}
      <div className="flex flex-col md:flex-row gap-4 mb-8">
        <input
          type="text"
          placeholder="Search problems..."
          className="p-3 rounded bg-gray-800 border border-gray-700 w-full md:w-1/3"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />

        <select
          className="p-3 rounded bg-gray-800 border border-gray-700"
          value={difficultyFilter}
          onChange={(e) => setDifficultyFilter(e.target.value)}
        >
          <option value="All">All Difficulties</option>
          <option value="Medium">Medium</option>
          <option value="Hard">Hard</option>
        </select>

        <select
          className="p-3 rounded bg-gray-800 border border-gray-700"
          value={topicFilter}
          onChange={(e) => setTopicFilter(e.target.value)}
        >
          <option value="All">All Topics</option>
          <option value="Scalability">Scalability</option>
          <option value="Distributed Systems">Distributed Systems</option>
          <option value="Networking">Networking</option>
          <option value="Messaging">Messaging</option>
        </select>
      </div>

      {/* Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {filtered.map((item) => (
          <div
            key={item.id}
            className={`p-6 rounded-xl transition-all duration-300 ${
              solved.includes(item.id)
                ? "bg-green-700"
                : "bg-gray-900 hover:scale-105"
            }`}
          >
            <h2 className="text-xl font-semibold mb-2">
              {item.title}
            </h2>

            <p className="text-sm text-gray-400">
              Difficulty: {item.difficulty}
            </p>
            <p className="text-sm text-gray-400 mb-4">
              Topic: {item.topic}
            </p>

            <div className="flex justify-between items-center">
              <a
                href={item.url}
                target="_blank"
                rel="noreferrer"
                className="text-indigo-400 hover:underline"
              >
                View Problem →
              </a>

              <button
                onClick={() => toggleSolved(item.id)}
                className="bg-indigo-600 px-3 py-1 rounded hover:bg-indigo-500 text-sm"
              >
                {solved.includes(item.id)
                  ? "Unmark"
                  : "Mark Solved"}
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
