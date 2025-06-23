# Zello Java API

A Java API for interacting with Zello Channels, designed with a builder methodology for ease of use and flexibility. This API aims to simplify real-time, push-to-talk (PTT) communication over Zello channels from Java applications.

## Table of Contents
- [Features](#features)
- [Installation](#installation)
- [Usage](#usage)
- [Project Structure](#project-structure)
- [Contributing](#contributing)
- [License](#license)
- [Acknowledgements & Disclaimer](#acknowledgements--disclaimer)

## Features

* **WebSocket Communication:** Manages the full lifecycle of WebSocket connections to Zello channels, including authentication (username/password or JWT token) and keep-alive mechanisms.
* **Push-To-Talk (PTT):** Provides methods to manually start and stop audio streaming to a Zello channel.
* **Voice Activity Detection (VOX):** Optional feature to automatically detect speech and manage audio streaming, conserving bandwidth by only transmitting when voice is present.
* **Custom PTT Framework:** Offers an interface to integrate custom PTT activation logic, allowing you to hook up UI buttons, hardware triggers, or other events to control audio transmission.
* **Opus Codec Integration:** Designed to handle Opus audio encoding and decoding, as required by the Zello API.
* **Real-time Audio Management:** Includes considerations for buffering, low-latency processing, and jitter mitigation for a smooth audio experience.

## Installation

This project is built with Maven. To include it in your project, add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.eakins.zello</groupId>
    <artifactId>zello-java-api</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>