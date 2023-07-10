# HajLib

HajLib is NotNet's Minecraft mod library. It contains some boilerplate that isn't too useful as a library, but is too helpful to have to paste into each project we make.

It is written in Kotlin and uses Fabric (but also targets Quilt compatibility). Most features are designed with the assumption that you are using Kotlin.

## Features

- Wrapper around creating and managing Kotlin coroutines
- A no-bullshit event bus

Classes and functions are commented with KDoc - check them out for more information.

## Usage

HajLib is available on multiple Maven repositories, under `pm.n2:hajlib`:

- blockbuild (`https://notnite.github.io/blockbuild/mvn/`)
  - This is the recommended repository to use
  - See [blockbuild's README](https://github.com/NotNite/blockbuild) for more information
- JitPack (`https://jitpack.io`)
  - Group `com.github.n2pm` instead of `pm.n2`
- GitHub Packages (`https://maven.pkg.github.com/n2pm/hajlib`)
  - Requires authentication

It is suggested to jar-in-jar HajLib (`modApi()` and `include()`).
