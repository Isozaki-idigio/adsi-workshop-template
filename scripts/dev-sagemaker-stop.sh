#!/bin/bash

echo "Stopping SageMaker dev services..."

for pidfile in /tmp/sagemaker-backend.pid /tmp/sagemaker-next.pid /tmp/sagemaker-proxy.pid; do
  if [ -f "$pidfile" ]; then
    pid=$(cat "$pidfile")
    kill "$pid" 2>/dev/null && echo "Stopped PID $pid"
    rm -f "$pidfile"
  fi
done

pkill -f "bootRun" 2>/dev/null || true
pkill -f "next start" 2>/dev/null || true
pkill -f "next-server" 2>/dev/null || true
pkill -f "sagemaker-proxy" 2>/dev/null || true

echo "Done."
