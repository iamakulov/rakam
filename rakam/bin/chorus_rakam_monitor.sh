#!/bin/bash

while true
do
    if ./launcher status; then
        # The app is running, nothing to do
    else
        ./launcher start
    fi

    sleep 2000
done