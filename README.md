# SaltyRTC Relayed Data Task for Java

[![Build status](https://circleci.com/gh/saltyrtc/saltyrtc-task-relayed-data-java.svg?style=shield&circle-token=:circle-token)](https://circleci.com/gh/saltyrtc/saltyrtc-task-relayed-data-java)
[![Java Version](https://img.shields.io/badge/java-8%2B-orange.svg)](https://github.com/saltyrtc/saltyrtc-task-relayed-data-java)
[![License](https://img.shields.io/badge/license-MIT%20%2F%20Apache%202.0-blue.svg)](https://github.com/saltyrtc/saltyrtc-task-relayed-data-java)
[![Chat on Gitter](https://badges.gitter.im/saltyrtc/Lobby.svg)](https://gitter.im/saltyrtc/Lobby)

This is a [SaltyRTC](https://github.com/saltyrtc/saltyrtc-meta) v1
implementation for Java 8+.

## Installing

TODO

The package is available [on Bintray](https://bintray.com/saltyrtc/maven/saltyrtc-task-relayed-data/).

Gradle:

```groovy
TODO
compile 'org.saltyrtc.client:saltyrtc-client:0.12.0'
```

Maven:

```xml
TODO
<dependency>
  <groupId>org.saltyrtc.client</groupId>
  <artifactId>saltyrtc-client</artifactId>
  <version>0.12.0</version>
  <type>pom</type>
</dependency>
```

## Manual Testing

To try a development version of the library, you can build a local version to
the maven repository at `/tmp/maven`:

    ./gradlew uploadArchives

Include it in your project like this:

    repositories {
        ...
        maven { url "/tmp/maven" }
    }

## Coding Guidelines

Unfortunately we cannot use all Java 8 features, in order to be compatible with
Android API <24. Please avoid using the following APIs:

- `java.lang.annotation.Repeatable`
- `AnnotatedElement.getAnnotationsByType(Class)`
- `java.util.stream`
- `java.lang.FunctionalInterface`
- `java.lang.reflect.Method.isDefault()`
- `java.util.function`

The CI tests contains a script to ensure that these APIs aren't being called. You can also run it manually:

    bash .circleci/check_android_support.sh


## Security

### Responsible Disclosure / Reporting Security Issues

Please report security issues directly to one or both of the following contacts:

- Danilo Bargen
    - Email: mail@dbrgn.ch
    - Threema: EBEP4UCA
    - GPG: [EA456E8BAF0109429583EED83578F667F2F3A5FA][keybase-dbrgn]
- Lennart Grahl
    - Email: lennart.grahl@gmail.com
    - Threema: MSFVEW6C
    - GPG: [3FDB14868A2B36D638F3C495F98FBED10482ABA6][keybase-lgrahl]

[keybase-dbrgn]: https://keybase.io/dbrgn
[keybase-lgrahl]: https://keybase.io/lgrahl


## License

    Copyright (c) 2018 Threema GmbH

    Licensed under the Apache License, Version 2.0, <see LICENSE-APACHE file>
    or the MIT license <see LICENSE-MIT file>, at your option. This file may not be
    copied, modified, or distributed except according to those terms.
