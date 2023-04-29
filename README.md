# Therlock

Therlock is a Java library that helps detect when a monitored thread, thread group, or anything that can continuously accept and execute runnables is blocked by a long-running operation.

The library is well-documented with JavaDoc and includes meaningful annotations in appropriate spots, making it very user-friendly and easy to get started with. The consistent use of @NotNull and @Nullable also makes the library fully compatible with Kotlin's Null safety.

Therlock follows the principle of semantic versioning. I plan to actively maintain the library and gradually integrate it into all my apps.

The library is written in pure Java and is fully compatible with Android starting from API level 16 as well as with Kotlin on the JVM.

# Getting started

To start using Therlock in your project, add the dependency.

### Maven
```xml
<dependency>
  <groupId>com.conena.therlock</groupId>
  <artifactId>therlock</artifactId>
  <version>1.1.0</version>
</dependency>
```

### Gradle
```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation 'com.conena.therlock:therlock:1.1.0'
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

### Interpretation of a BlockedThreadEvent
If the monitored thread is found to be blocked for as long as the specified threshold, a BlockedThreadEvent is created and passed to the listener.

BlockedThreadEvent inherits from Throwable, but is never thrown by this library. The main reason for this is that most logging and tracking solutions support logging of throwables and their stacktrace. Furthermore, the event can therefore be printed very easily and well-structured in the log.

The stack trace represents the stack trace of all threads returned by the ThreadProvider at the time of the BlockedThreadEvent. The order is also respected and the stacktrace of the thread with index 0 in the array will be the first in the stacktrace of the BlockedThreadEvent.

Below is an example where the "main" thread is monitored and the ThreadProvider has returned the three threads "main", "worker-1" and "worker-2". We see that the main thread is blocked by a call to Thread.sleep. The stacktraces of the other threads have nothing to do with the cause of the event in this case, but might be the cause in other cases. It is up to you to judge this, as the library cannot do this for you.
```
com.conena.therlock.BlockedThreadEvent: The monitored thread was blocked for at least 1000 milliseconds. The stack trace contains the stack traces of all threads selected for reporting. Please refer to the documentation when interpreting the stack traces.
Caused by: com.conena.therlock.ThreadInfo: Stacktrace of the thread 'main'.
	at java.lang.Thread.sleep(Native Method)
	at java.lang.Thread.sleep(Thread.java:450)
	at java.lang.Thread.sleep(Thread.java:355)
	at com.conena.sample.App.onCreate$lambda$1(App.kt:87)
	at com.conena.sample.App.$r8$lambda$RyyZsiiZRbAhnfOCj96-SMpRwi0(Unknown Source:0)
	at com.conena.sample.App$$ExternalSyntheticLambda1.run(Unknown Source:0)
	at android.os.Handler.handleCallback(Handler.java:942)
	at android.os.Handler.dispatchMessage(Handler.java:99)
	at android.os.Looper.loopOnce(Looper.java:201)
	at android.os.Looper.loop(Looper.java:288)
	at android.app.ActivityThread.main(ActivityThread.java:7898)
	at java.lang.reflect.Method.invoke(Native Method)
	at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:548)
	at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:936)
Caused by: com.conena.therlock.ThreadInfo: Stacktrace of the thread 'worker-1'.
	at android.os.MessageQueue.nativePollOnce(Native Method)
	at android.os.MessageQueue.next(MessageQueue.java:335)
	at android.os.Looper.loopOnce(Looper.java:161)
	at android.os.Looper.loop(Looper.java:288)
	at android.os.HandlerThread.run(HandlerThread.java:67)
Caused by: com.conena.therlock.ThreadInfo: Stacktrace of the thread 'worker-2'.
	at jdk.internal.misc.Unsafe.park(Native Method)
	at java.util.concurrent.locks.LockSupport.park(LockSupport.java:194)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.await(AbstractQueuedSynchronizer.java:2081)
	at java.util.concurrent.LinkedBlockingQueue.take(LinkedBlockingQueue.java:433)
	at java.util.concurrent.ThreadPoolExecutor.getTask(ThreadPoolExecutor.java:1063)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1123)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:637)
	at java.lang.Thread.run(Thread.java:1012)
```

# FAQ
### How does it work?
The ThreadAccessor executes code on the monitored thread at the specified interval. The library checks if this code was executed. If the code is not executed for longer than the specified threshold, a BlockedThreadEvent is passed to the listener. The BlockedThreadEvents contains the stack traces of all threads that should be reported.

### Is there an additional load on the monitored thread?
All checks happen on background threads. On the monitored thread, a callback is submitted to an ExecutorService of the library at the specified interval only. The additional load on the monitored thread is negligible.

### Can I use it on Android?
For Android, it is recommended to use [ANR Detective][anr-detective link], which is based on this library but is lifecycle aware and contains useful default parameters. However, you can also use this library directly on Android.

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
[anr-detective link]:https://github.com/conena/anr-detective