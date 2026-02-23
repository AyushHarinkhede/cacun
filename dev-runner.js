const { spawn } = require('child_process');

function runNpm(args) {
  const npmCli = process.env.npm_execpath;
  if (!npmCli) {
    throw new Error(
      'npm_execpath is not set. Please run this script via `npm run dev` (not `node dev-runner.js`).'
    );
  }

  return spawn(process.execPath, [npmCli, ...args], {
    stdio: 'inherit',
    windowsHide: true,
  });
}

const server = runNpm(['--prefix', 'server', 'run', 'dev']);
const client = runNpm(['--prefix', 'client', 'run', 'dev']);

function shutdown(code) {
  if (server && !server.killed) server.kill();
  if (client && !client.killed) client.kill();
  process.exit(code);
}

server.on('exit', (code) => {
  if (typeof code === 'number' && code !== 0) shutdown(code);
});

client.on('exit', (code) => {
  if (typeof code === 'number' && code !== 0) shutdown(code);
});

process.on('SIGINT', () => shutdown(0));
process.on('SIGTERM', () => shutdown(0));
