import problems from "../data/problems.json";

export const getProblems = () => {
  return new Promise((resolve) => {
    setTimeout(() => resolve(problems), 500); // simulate backend delay
  });
};
