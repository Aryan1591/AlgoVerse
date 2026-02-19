import { useState } from "react";

export default function Profile() {

  const [profile] = useState({
    authId: "AUTH12345",
    displayName: "Dharm",
    leetCodeUserName: "dharm_codes",
    active: true,
    memberShip: "PREMIUM",
    syncStatus: "SYNCED",
    lastSyncedAt: "2026-02-14T10:00:00Z",
    createdAt: "2025-01-10T08:00:00Z",
    updatedAt: "2026-02-14T09:30:00Z",
    currentStreak: 7,
    maxStreak: 15,
    stats: {
      totalSolved: 120,
      easySolved: 50,
      mediumSolved: 55,
      hardSolved: 10,
      systemDesignSolved: 5
    }
  });

  return (
    <div className="min-h-screen bg-gradient-to-br from-black via-gray-900 to-indigo-950 text-white px-6 md:px-16 py-8">

  {/* TOP SECTION */}
  <div className="flex items-center gap-6 mb-10">

    {/* Smaller Avatar */}
    <div className="w-20 h-20 rounded-full bg-indigo-600 flex items-center justify-center text-3xl font-semibold shadow-lg">
      {profile.displayName.charAt(0)}
    </div>

    {/* Basic Info */}
    <div>
      <h1 className="text-2xl font-semibold text-indigo-400">
        {profile.displayName}
      </h1>

      <p className="text-sm text-gray-400">
        LeetCode: {profile.leetCodeUserName}
      </p>

      <div className="flex gap-3 mt-2 text-xs">
        <span className="px-3 py-1 bg-indigo-700 rounded-full">
          {profile.memberShip}
        </span>

        <span className={`px-3 py-1 rounded-full ${
          profile.active ? "bg-green-600" : "bg-red-600"
        }`}>
          {profile.active ? "Active" : "Inactive"}
        </span>
      </div>
    </div>
  </div>

  {/* STREAK */}
  <div className="grid grid-cols-2 gap-6 mb-10">

    <div className="bg-gray-800 p-4 rounded-xl text-center">
      <h2 className="text-xs text-gray-400 mb-1">🔥 Current</h2>
      <p className="text-3xl font-bold text-orange-400">
        {profile.currentStreak}
      </p>
    </div>

    <div className="bg-gray-800 p-4 rounded-xl text-center">
      <h2 className="text-xs text-gray-400 mb-1">🏆 Max</h2>
      <p className="text-3xl font-bold text-green-400">
        {profile.maxStreak}
      </p>
    </div>

  </div>

  {/* STATS */}
  <div className="bg-gray-800 p-6 rounded-xl mb-10">
    <h2 className="text-lg font-medium mb-6 text-indigo-400">
      Problem Stats
    </h2>

    <div className="grid grid-cols-5 gap-4 text-center text-sm">

      <div>
        <p className="font-bold">{profile.stats.totalSolved}</p>
        <p className="text-gray-400 text-xs">Total</p>
      </div>

      <div>
        <p className="font-bold text-green-400">{profile.stats.easySolved}</p>
        <p className="text-gray-400 text-xs">Easy</p>
      </div>

      <div>
        <p className="font-bold text-yellow-400">{profile.stats.mediumSolved}</p>
        <p className="text-gray-400 text-xs">Medium</p>
      </div>

      <div>
        <p className="font-bold text-red-400">{profile.stats.hardSolved}</p>
        <p className="text-gray-400 text-xs">Hard</p>
      </div>

      <div>
        <p className="font-bold text-purple-400">
          {profile.stats.systemDesignSolved}
        </p>
        <p className="text-gray-400 text-xs">System</p>
      </div>

    </div>
  </div>

  {/* ACCOUNT INFO */}
  <div className="bg-gray-800 p-4 rounded-xl text-xs text-gray-300 grid grid-cols-2 gap-3">

    <p><strong>Auth:</strong> {profile.authId}</p>
    <p><strong>Status:</strong> {profile.syncStatus}</p>

    <p>
      <strong>Synced:</strong>{" "}
      {new Date(profile.lastSyncedAt).toLocaleDateString()}
    </p>

    <p>
      <strong>Created:</strong>{" "}
      {new Date(profile.createdAt).toLocaleDateString()}
    </p>

    <p>
      <strong>Updated:</strong>{" "}
      {new Date(profile.updatedAt).toLocaleDateString()}
    </p>

  </div>

</div>

  );
}
