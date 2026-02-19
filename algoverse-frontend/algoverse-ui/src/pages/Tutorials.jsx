import { useState } from "react";

export default function Tutorials() {
  const [search, setSearch] = useState("");
  const [difficultyFilter, setDifficultyFilter] = useState("All");
  const [topicFilter, setTopicFilter] = useState("All");
  const [watchedVideos, setWatchedVideos] = useState([]);

  const videos = [
    {
      id: 1,
      title: "Two Sum Explained",
      difficulty: "Easy",
      topic: "Arrays",
      url: "https://www.youtube.com/watch?v=KLlXCFG5TnA",
    },
    {
      id: 2,
      title: "Kadane’s Algorithm (Maximum Subarray)",
      difficulty: "Medium",
      topic: "Dynamic Programming",
      url: "https://www.youtube.com/watch?v=5WZl3MMT0Eg",
    },
    {
      id: 3,
      title: "Longest Substring Without Repeating Characters",
      difficulty: "Medium",
      topic: "Strings",
      url: "https://www.youtube.com/watch?v=wiGpQwVHdE0",
    },
    {
      id: 4,
      title: "Merge Intervals Explained",
      difficulty: "Medium",
      topic: "Intervals",
      url: "https://www.youtube.com/watch?v=44H3cEC2fFM",
    },
    {
      id: 5,
      title: "LRU Cache Design",
      difficulty: "Hard",
      topic: "Design",
      url: "https://www.youtube.com/watch?v=R5ON3iwx78M",
    },
  ];

  const toggleWatched = (id) => {
    if (watchedVideos.includes(id)) {
      setWatchedVideos(watchedVideos.filter((vid) => vid !== id));
    } else {
      setWatchedVideos([...watchedVideos, id]);
    }
  };

  const filteredVideos = videos.filter((video) => {
    return (
      video.title.toLowerCase().includes(search.toLowerCase()) &&
      (difficultyFilter === "All" || video.difficulty === difficultyFilter) &&
      (topicFilter === "All" || video.topic === topicFilter)
    );
  });

  return (
    <div className="min-h-screen bg-black text-white px-6 md:px-16 py-10">
      <h1 className="text-4xl font-bold text-indigo-400 mb-8">
        Watch Tutorials 🎥
      </h1>

      {/* SEARCH + FILTERS */}
      <div className="flex flex-col md:flex-row gap-4 mb-8">

        {/* Search */}
        <input
          type="text"
          placeholder="Search tutorials..."
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

      {/* VIDEO CARDS */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {filteredVideos.map((video) => (
          <div
            key={video.id}
            className={`p-6 rounded-xl transition-all duration-300 ${
              watchedVideos.includes(video.id)
                ? "bg-green-700"
                : "bg-gray-900 hover:scale-105"
            }`}
          >
            <h2 className="text-xl font-semibold mb-2">
              {video.title}
            </h2>

            <p className="text-sm text-gray-400 mb-1">
              Difficulty: {video.difficulty}
            </p>
            <p className="text-sm text-gray-400 mb-4">
              Topic: {video.topic}
            </p>

            <div className="flex justify-between items-center">
              <a
                href={video.url}
                target="_blank"
                rel="noreferrer"
                className="text-indigo-400 hover:underline"
              >
                Watch on YouTube →
              </a>

              <button
                onClick={() => toggleWatched(video.id)}
                className="bg-indigo-600 px-3 py-1 rounded hover:bg-indigo-500 text-sm"
              >
                {watchedVideos.includes(video.id)
                  ? "Unmark"
                  : "Mark Watched"}
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
