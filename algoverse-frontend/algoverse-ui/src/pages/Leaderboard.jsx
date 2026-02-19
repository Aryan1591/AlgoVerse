import { useState } from "react";

export default function Leaderboard() {

  const leaderboardData = [
    {
      rank: 1,
      totalScore: 2450,
      displayName: "Arjun Dev",
      leetCodeUserName: "arjun_codes",
      stats: { easy: 120, medium: 85, hard: 25 },
    },
    {
      rank: 2,
      totalScore: 2100,
      displayName: "Sneha Rao",
      leetCodeUserName: "sneha_dsa",
      stats: { easy: 110, medium: 70, hard: 18 },
    },
    {
      rank: 3,
      totalScore: 1980,
      displayName: "Rahul Sharma",
      leetCodeUserName: "rahul_algo",
      stats: { easy: 100, medium: 65, hard: 15 },
    },
    {
      rank: 4,
      totalScore: 1650,
      displayName: "Priya Nair",
      leetCodeUserName: "priya_codes",
      stats: { easy: 95, medium: 50, hard: 10 },
    },
  ];

  return (
    <div className="min-h-screen bg-black text-white px-6 md:px-16 py-10">
      <h1 className="text-4xl font-bold text-indigo-400 mb-10">
        🏆 AlgoVerse Leaderboard
      </h1>

      <div className="space-y-6">

        {leaderboardData.map((user) => (
          <div
            key={user.rank}
            className={`p-6 rounded-xl transition-all duration-300 hover:scale-[1.02]
              ${user.rank === 1 ? "bg-yellow-600/20 border border-yellow-400"
                : user.rank === 2 ? "bg-gray-400/10 border border-gray-400"
                : user.rank === 3 ? "bg-orange-600/20 border border-orange-400"
                : "bg-gray-900 border border-gray-800"}
            `}
          >
            <div className="flex flex-col md:flex-row md:items-center md:justify-between">

              {/* LEFT SIDE */}
              <div className="flex items-center gap-6">

                {/* Rank */}
                <div className="text-3xl font-bold w-12">
                  #{user.rank}
                </div>

                {/* Name + Username */}
                <div>
                  <h2 className="text-xl font-semibold">
                    {user.displayName}
                  </h2>
                  <a
                    href={`https://leetcode.com/${user.leetCodeUserName}`}
                    target="_blank"
                    rel="noreferrer"
                    className="text-indigo-400 text-sm hover:underline"
                  >
                    @{user.leetCodeUserName}
                  </a>
                </div>
              </div>

              {/* RIGHT SIDE */}
              <div className="mt-4 md:mt-0 text-right">

                {/* Total Score */}
                <div className="text-lg font-bold text-green-400">
                  ⭐ {user.totalScore} pts
                </div>

                {/* Stats */}
                <div className="text-sm text-gray-400 mt-2 space-x-4">
                  <span className="text-green-400">
                    Easy: {user.stats.easy}
                  </span>
                  <span className="text-yellow-400">
                    Medium: {user.stats.medium}
                  </span>
                  <span className="text-red-400">
                    Hard: {user.stats.hard}
                  </span>
                </div>

              </div>
            </div>
          </div>
        ))}

      </div>
    </div>
  );
}
