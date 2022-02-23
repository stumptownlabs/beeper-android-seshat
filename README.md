# Seshat-Android

Kotlin bindings for the Matrix message database/indexer Seshat.

## Instalation
- Install Rust on the machine
    
    [https://www.rust-lang.org/tools/install](https://www.rust-lang.org/tools/install)
    
    $ curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh
    
    - Check installed rust version
        
        $ rustc —version
        

→ Tested using rustc 1.56.1 (59eed8a2a 2021-11-01)

- Now rust is installed, we need to add the supported Android architectures as targets.

$ rustup target
        add aarch64-linux-android armv7-linux-androideabi i686-linux-android x86_64-linux-android

- Setup NDK on Android Studio

Open Android Studio. From the toolbar, go
to `Android Studio > Preferences > Appearance & Behaviour > Android SDK > SDK Tools`.
Check NDK and CMAKE  for installation and click `OK`.
Comment: downgrade from NDK 23 to 22 because of https://github.com/bbqsrc/cargo-ndk/issues/22

- Manually publish the library to Package Registry
Setup the GITLAB_PRIVATE_TOKEN environment variable with your temp access token
Use Gradlew to publish -> ./gradlew publish -p android-seshat


