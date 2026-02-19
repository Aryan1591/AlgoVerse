import { Particles } from "react-tsparticles";

export default function ParticlesBg() {
  return (
    <Particles
      options={{
        background: { color: "transparent" },
        particles: {
          number: { value: 60 },
          color: { value: "#6366f1" },
          size: { value: 2 },
          move: { enable: true, speed: 0.6 },
          links: { enable: true, color: "#6366f1" }
        }
      }}
      className="absolute inset-0 -z-10"
    />
  );
}
