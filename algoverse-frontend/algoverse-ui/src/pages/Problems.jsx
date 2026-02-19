import { useState } from "react";

export default function Problems() {
  const [search, setSearch] = useState("");
  const [difficultyFilter, setDifficultyFilter] = useState("All");
  const [topicFilter, setTopicFilter] = useState("All");
  const [solvedProblems, setSolvedProblems] = useState([]);

  const problems = [
    {
      id: 1,
      title: "Two Sum",
      difficulty: "Easy",
      topic: "Arrays",
      url: "https://leetcode.com/problems/two-sum/",
    },
    {
      id: 2,
      title: "Maximum Subarray",
      difficulty: "Medium",
      topic: "Dynamic Programming",
      url: "https://leetcode.com/problems/maximum-subarray/",
    },
    {
      id: 3,
      title: "Longest Substring Without Repeating Characters",
      difficulty: "Medium",
      topic: "Strings",
      url: "https://leetcode.com/problems/longest-substring-without-repeating-characters/",
    },
    {
      id: 4,
      title: "Merge Intervals",
      difficulty: "Medium",
      topic: "Intervals",
      url: "https://leetcode.com/problems/merge-intervals/",
    },
    {
      id: 5,
      title: "LRU Cache",
      difficulty: "Hard",
      topic: "Design",
      url: "https://leetcode.com/problems/lru-cache/",
    },
  ];

  const toggleSolved = (id) => {
    if (solvedProblems.includes(id)) {
      setSolvedProblems(solvedProblems.filter((pid) => pid !== id));
    } else {
      setSolvedProblems([...solvedProblems, id]);
    }
  };

  const filteredProblems = problems.filter((problem) => {
    return (
      problem.title.toLowerCase().includes(search.toLowerCase()) &&
      (difficultyFilter === "All" || problem.difficulty === difficultyFilter) &&
      (topicFilter === "All" || problem.topic === topicFilter)
    );
  });

  return (
    <div className="min-h-screen bg-black text-white px-6 md:px-16 py-10">
      <h1 className="text-4xl font-bold text-indigo-400 mb-8">
        Practice Problems 🧠
      </h1>

      {/* SEARCH + FILTERS */}
      <div className="flex flex-col md:flex-row gap-4 mb-8">

        {/* Search */}
        <input
          type="text"
          placeholder="Search problems..."
          className="p-3 rounded bg-gray-800 border border-gray-700 w-full md:w-1/3"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />

        {/* Difficulty Filter */}
        <select
          className="p-3 rounded bg-gray-800 border border-gray-700"
          value={difficultyFilter}
          onChange={(e) => setDifficultyFilter(e.target.value)}
        >
          <option value="All">All Difficulties</option>
          <option value="Easy">Easy</option>
          <option value="Medium">Medium</option>
          <option value="Hard">Hard</option>
        </select>

        {/* Topic Filter */}
        <select
          className="p-3 rounded bg-gray-800 border border-gray-700"
          value={topicFilter}
          onChange={(e) => setTopicFilter(e.target.value)}
        >
          <option value="All">All Topics</option>
          <option value="Arrays">Arrays</option>
          <option value="Strings">Strings</option>
          <option value="Dynamic Programming">Dynamic Programming</option>
          <option value="Intervals">Intervals</option>
          <option value="Design">Design</option>
        </select>

      </div>

      {/* PROBLEM CARDS */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {filteredProblems.map((problem) => (
          <div
            key={problem.id}
            className={`p-6 rounded-xl transition-all duration-300 ${
              solvedProblems.includes(problem.id)
                ? "bg-green-700"
                : "bg-gray-900 hover:scale-105"
            }`}
          >
            <h2 className="text-xl font-semibold mb-2">
              {problem.title}
            </h2>

            <p className="text-sm text-gray-400 mb-1">
              Difficulty: {problem.difficulty}
            </p>
            <p className="text-sm text-gray-400 mb-4">
              Topic: {problem.topic}
            </p>

            <div className="flex justify-between items-center">
              <a
                href={problem.url}
                target="_blank"
                rel="noreferrer"
                className="text-indigo-400 hover:underline"
              >
                Solve on LeetCode →
              </a>

              <button
                onClick={() => toggleSolved(problem.id)}
                className="bg-indigo-600 px-3 py-1 rounded hover:bg-indigo-500 text-sm"
              >
                {solvedProblems.includes(problem.id)
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
