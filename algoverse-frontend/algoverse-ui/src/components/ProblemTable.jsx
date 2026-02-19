import ProblemRow from "./ProblemRow";

const ProblemTable = ({ problems }) => {
  return (
    <table className="w-full text-white border border-gray-800">
      <thead className="bg-gray-900">
        <tr>
          <th className="p-2">Problem</th>
          <th className="p-2">Difficulty</th>
          <th className="p-2">Status</th>
          <th className="p-2">LeetCode</th>
          <th className="p-2">YouTube</th>
        </tr>
      </thead>
      <tbody>
        {problems.map((p) => (
          <ProblemRow key={p.id} problem={p} />
        ))}
      </tbody>
    </table>
  );
};

export default ProblemTable;
