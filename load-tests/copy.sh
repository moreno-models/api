#!/bin/bash

source ./common.sh
start_tunnels

sleep 3
KILL_SCREENS_COMMAND="killall -9 screen"
echo "Making sure locust is stopped..."
ssh -i "$WORKER_KEY" ec2-user@localhost -p "$MAIN_PORT" "$KILL_SCREENS_COMMAND"


rm -rf RAW/current

mkdir -p RAW/current/load-1
scp -i "$WORKER_KEY" -P "$MAIN_PORT" ec2-user@localhost:~/api/load-tests/current_\* RAW/current/load-1/. 
scp -i "$WORKER_KEY" -P "$MAIN_PORT" ec2-user@localhost:~/api/load-tests/locust.log RAW/current/load-1/. 

echo "Copying from workers..."
for ((worker_idx = 0; worker_idx < WORKERS; worker_idx++))
do
    LOCAL_PORT=$((MAIN_PORT + 1 + worker_idx))

    echo "Copying worker: $LOCAL_PORT"
    mkdir -p RAW/current/load-$((worker_idx + 1 + 1))
    scp -i "$WORKER_KEY" -P $LOCAL_PORT ec2-user@localhost:~/api/load-tests/locust.log RAW/current/load-$((worker_idx + 1 + 1))/.
done

