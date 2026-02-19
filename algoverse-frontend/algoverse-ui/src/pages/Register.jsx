const Register = () => {
  return (
    <div className="flex items-center justify-center min-h-screen bg-black text-white">
      <form className="bg-gray-900 p-8 rounded-xl shadow-lg w-96">
        <h2 className="text-2xl font-bold mb-4">Create AlgoVerse Account</h2>

        <input
          type="text"
          placeholder="Name"
          className="w-full p-2 mb-3 bg-gray-800 border border-gray-700 rounded"
        />

        <input
          type="email"
          placeholder="Email"
          className="w-full p-2 mb-3 bg-gray-800 border border-gray-700 rounded"
        />

        <input
          type="password"
          placeholder="Password"
          className="w-full p-2 mb-3 bg-gray-800 border border-gray-700 rounded"
        />

        <button className="w-full bg-green-600 p-2 rounded hover:bg-green-700">
          Register
        </button>

        <p className="mt-3 text-sm">
          Already have an account?
          <a href="/login" className="text-blue-400 underline ml-1">Login</a>
        </p>
      </form>
    </div>
  );
};

export default Register;
