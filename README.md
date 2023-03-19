# Therlock

Therlock is a Java library that helps detect when a monitored thread, thread group, or anything that can continuously accept and execute runnables is blocked by a long-running operation.

The library is well-documented with JavaDoc and includes meaningful annotations in appropriate spots, making it very user-friendly and easy to get started with. The consistent use of @NotNull and @Nullable also makes the library fully compatible with Kotlin's Null safety.

Therlock follows the principle of semantic versioning. I plan to actively maintain the library and gradually integrate it into all my apps.

The library is written in pure Java and is fully compatible with Android starting from API level 16 as well as with Kotlin on the JVM.

# Getting started
### The library is not yet available on Maven Central. This will happen in the next few days.

To start using Therlock in your project, add the dependency.

### Maven
```xml
<dependency>
  <groupId>com.conena.therlock</groupId>
  <artifactId>therlock</artifactId>
  <version>1.0.0</version>
</dependency>
```

### Gradle
```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation 'com.conena.therlock:therlock:1.0.0'
}
```

# Usage

### BlockedThreadDetector example
```java
// In the constructor, the ThreadAccessor is passed, through which the check code is executed in the monitored thread.
// The example monitors the JavaFX main thread via javafx.application.Platform.runLater.
BlockedThreadDetector blockedThreadDetector = new BlockedThreadDetectorBuilder(Platform::runLater)
        // Provides the threads for which the stack traces are collected in case the detector hits.
        .setThreadProvider(new FilteredThreadProvider(new DaemonThreadFilter()))
        // Log the exception or print the stack trace.
        .setListener((detector, event) -> event.printStackTrace())
        // Defines exemptions when no BlockedThreadEvent should be raised.
        // E.g. it would be possible to create an exemption for a debugger.
        .setExemption(() -> false)
        // Defines how long the thread must be continuously blocked for a BlockedThreadEvent to be triggered.
        .setThreshold(1000L)
        // Defines how often the thread should be pinged.
        .setInspectionInterval(200L)
        // Build the detector.
        .build()
        // Start the detection.
        // The optional parameter is a delay from when the detection should start.
        .startDetection(1000L);

// If you want to stop detection at some point.
blockedThreadDetector.stopDetection();
// You can start it again at any time.
blockedThreadDetector.startDetection();
```

### ThreadAccessor examples
```java
// Android (android.os.Handler)
Handler uiHandler = new Handler(Looper.getMainLooper());
ThreadAccessor accessor = uiHandler::post;

// JavaFx (javafx.application.Platform)
ThreadAccessor javaFxUiThreadAccessor = Platform::runLater;

// Swing (javax.swing.SwingUtilities)
ThreadAccessor swingUiThreadAccessor = SwingUtilities::invokeLater;

// AWT (java.awt.EventQueue)
ThreadAccessor awtUiThreadAccessor = EventQueue::invokeLater;
```

### ThreadProvider examples
```java
// Provides all active threads
ThreadProvider activeThreads = new ActiveThreadProvider();
        
// Provides all user threads
ThreadProvider activeUserThreads = new FilteredThreadProvider(new DaemonThreadFilter());

// Provides all threads not created by therlock
ThreadProvider activeNonLibraryThreads = new FilteredThreadProvider(new LibraryThreadFilter());
        
// Provides all threads with priority >=5
ThreadProvider activeHghPriorityThreads = new FilteredThreadProvider(new PriorityThreadFilter(5));
        
// Provides all daemon threads without library threads
ThreadProvider activeNonLibraryDaemonThreads = new FilteredThreadProvider(
        new CombinedThreadFilter(Thread::isDaemon, new LibraryThreadFilter())
);
```

### DetectionExemption example
```java
// Android - Debugger exemption (android.os.Debug)
// Don't report blocked threads when connected to a debugger
DetectionExemption exemption = new CombinedDetectionExemption(
        Debug::isDebuggerConnected,
        Debug::waitingForDebugger
);
```

# FAQ
### How does it work?
The ThreadAccessor executes code on the monitored thread at the specified interval. The library checks if this code was executed. If the code is not executed for longer than the specified threshold, a BlockedThreadEvent is passed to the listener. The BlockedThreadEvents contains the stack traces of all threads that should be reported.

### Is there an additional load on the monitored thread?
All checks happen on background threads. On the monitored thread, a callback is submitted to an ExecutorService of the library at the specified interval only. The additional load on the monitored thread is negligible.

### Can I use it on Android?
Yes. In addition, a version adapted for Android will be released soon.

# Contribution

Please feel free to open an issue or submit a pull request if you have any suggestions for improvement. When submitting a pull request, please confirm that you wrote the code yourself, waive any copyright rights, and agree that the code will be placed under the original license of the library.

# License
```
Copyright (C) 2023 Fabian Andera

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```