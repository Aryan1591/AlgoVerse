const ProblemRow = ({ problem }) => {
  return (
    <tr className="border-b border-gray-800 hover:bg-gray-800">
      <td className="p-2">{problem.title}</td>
      <td className="p-2">{problem.difficulty}</td>
      <td className="p-2">
        {problem.solved ? "✅ Solved" : "❌ Not Solved"}
      </td>
      <td className="p-2">
        <a href={problem.leetcodeUrl} target="_blank" className="text-blue-400 underline">
          Open
        </a>
      </td>
      <td className="p-2">
        <a href={problem.youtubeUrl} target="_blank" className="text-red-400 underline">
          Watch
        </a>
      </td>
    </tr>
  );
};

export default ProblemRow;
