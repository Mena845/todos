#!/bin/bash

set -e

echo "Formatting project..."
./gradlew spotlessApply

echo "Done!"