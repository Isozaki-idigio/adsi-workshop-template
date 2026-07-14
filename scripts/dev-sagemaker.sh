#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "=== Stopping existing processes ==="
bash "$SCRIPT_DIR/dev-sagemaker-stop.sh" 2>/dev/null || true
sleep 2

export SAGEMAKER=1
export NEXT_PUBLIC_BASE_PATH=/codeeditor/default/absports/3000

echo "=== Building frontend (SageMaker mode) ==="
cd "$PROJECT_DIR/frontend"
npx next build

echo "=== Starting backend (H2, port 8080) ==="
cd "$PROJECT_DIR/backend"
./gradlew bootRun --args='--spring.profiles.active=local' > /tmp/backend.log 2>&1 &
echo $! > /tmp/sagemaker-backend.pid

echo "=== Starting Next.js (port 3001) ==="
cd "$PROJECT_DIR/frontend"
SAGEMAKER=1 npx next start -H 127.0.0.1 -p 3001 > /tmp/next.log 2>&1 &
echo $! > /tmp/sagemaker-next.pid
sleep 3

echo "=== Starting SageMaker proxy (port 3000 -> 3001) ==="
node "$PROJECT_DIR/frontend/scripts/sagemaker-proxy.mjs" > /tmp/proxy.log 2>&1 &
echo $! > /tmp/sagemaker-proxy.pid
sleep 2

echo ""
echo "=== All services started ==="
echo "Backend:  http://localhost:8080"
echo "Next.js:  http://127.0.0.1:3001"
echo "Proxy:    http://localhost:3000"
echo ""
echo "Open in browser: PORTS tab -> port 3000 globe button -> replace 'ports' with 'absports'"
echo "Login: MGR001 / password123"
