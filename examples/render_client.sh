#!/bin/bash
# Usage: ./render_client.sh [script_file] [output_file]

SCRIPT=${1:-examples/render_chart.js}
OUTPUT=${2:-chart.png}

echo "Sending script $SCRIPT to Rendering Server..."
curl -X POST --data-binary "@$SCRIPT" http://localhost:8080/render --output "$OUTPUT"
echo "Saved result to $OUTPUT"
