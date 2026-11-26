// Local launcher for the Java backend. It does not implement backend APIs in Node.
const { spawn } = require("node:child_process");
const { existsSync, readFileSync, readdirSync } = require("node:fs");
const os = require("node:os");
const path = require("node:path");

const backendDirectory = __dirname;
const env = { ...process.env };
const envFile = path.join(backendDirectory, ".env");

if (existsSync(envFile)) {
  for (const line of readFileSync(envFile, "utf8").split(/\r?\n/)) {
    const match = line.match(/^\s*([A-Za-z_][A-Za-z_0-9]*)\s*=\s*(.*)\s*$/);
    if (!match || Object.prototype.hasOwnProperty.call(env, match[1])) continue;

    let value = match[2].trim();
    if (
      (value.startsWith('"') && value.endsWith('"')) ||
      (value.startsWith("'") && value.endsWith("'"))
    ) {
      value = value.slice(1, -1);
    }
    env[match[1]] = value;
  }
}

env.MONGO_URL ||= "mongodb://127.0.0.1:27017/simple_online_restaurant";
const dockerMongoHost = env.MONGO_URL.match(
  /^(mongodb(?:\+srv)?:\/\/(?:[^@/]+@)?)mongo(?=[:/?]|$)/i
);
if (dockerMongoHost) {
  env.MONGO_URL = env.MONGO_URL.replace(dockerMongoHost[0], `${dockerMongoHost[1]}127.0.0.1`);
}

const bundledTools = path.join(os.tmpdir(), "westfood-java-tools");
if (!env.JAVA_HOME && existsSync(bundledTools)) {
  const jdkDirectory = readdirSync(bundledTools).find(
    (entry) =>
      entry.startsWith("jdk-") &&
      existsSync(path.join(bundledTools, entry, "bin", "java.exe"))
  );
  if (jdkDirectory) env.JAVA_HOME = path.join(bundledTools, jdkDirectory);
}

if (env.JAVA_HOME) {
  const javaBin = path.join(env.JAVA_HOME, "bin");
  env.PATH = `${javaBin}${path.delimiter}${env.PATH || env.Path || ""}`;
  env.Path = env.PATH;
}

const bundledMaven = path.join(
  bundledTools,
  "apache-maven-3.9.11",
  "bin",
  "mvn.cmd"
);
const mavenCommand = existsSync(bundledMaven)
  ? bundledMaven
  : process.platform === "win32"
    ? "mvn.cmd"
    : "mvn";
const child = spawn(mavenCommand, ["spring-boot:run"], {
  cwd: backendDirectory,
  env,
  stdio: "inherit",
  shell: process.platform === "win32",
});

child.on("error", (error) => {
  console.error(
    "Unable to start the Java backend. Install Java 17 and Maven, or make them available in PATH.",
    error.message
  );
  process.exitCode = 1;
});
child.on("exit", (code) => {
  process.exitCode = code ?? 1;
});
