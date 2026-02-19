// Topics.jsx
const topics = ["Arrays", "Strings", "Linked List", "Stack", "Queue", "Trees", "Graphs", "DP", "Greedy"];

export default function Topics() {
  return (
    <div className="p-10">
      <h1 className="text-4xl font-bold text-indigo-400">DSA Topics</h1>

      <div className="grid md:grid-cols-4 gap-6 mt-10">
        {topics.map(t => (
          <div key={t} className="glass hover:bg-indigo-600 cursor-pointer p-6 text-center">
            {t}
          </div>
        ))}
      </div>
    </div>
  );
}
