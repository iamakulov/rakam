#!/bin/bash

while true
do
    if $(dirname $0)/launcher status > /dev/null; then
        # The app is running, nothing to do
        echo "[chorus_rakam_monitor] Rakam is running, doing nothing..."
    else
        echo "[chorus_rakam_monitor] Launching Rakam..."
        $(dirname $0)/launcher start --skip-monitor-launch
    fi

    sleep 2
done