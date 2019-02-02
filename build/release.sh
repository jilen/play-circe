#!/usr/bin/env bash
sbt -Dplay.version="2.6.21" release cross with-defaults
sbt -Dplay.version="2.7.0" release cross with-defaults
