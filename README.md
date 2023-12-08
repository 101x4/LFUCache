# LFU Cache Implementation in Java

## Overview
This project provides a thread-safe implementation of an LFU (Least Frequently Used) cache in Java. It includes a generic `Cache` interface and an `LFUCache` class that implements this interface with a focus on performance and concurrency.

## Features
- Generic `Cache` interface with `put` and `get` methods.
- `LFUCache` implementation that maintains cache entries and their access frequencies.
- Thread-safe operations ensuring consistency under concurrent access.
- Time-based entry expiration.
- O(1) time complexity.
