/*
 * Copyright (c) 2011-2014 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

/**
 * = Vert.x Core Manual
 * :toc: left
 *
 * At the heart of Vert.x is a set of Java APIs that we call *Vert.x Core*
 *
 * https://github.com/eclipse/vert.x[Repository].
 *
 * Vert.x core provides functionality for things like:
 *
 * * Writing TCP clients and servers
 * * Writing HTTP clients and servers including support for WebSockets
 * * The Event bus
 * * Shared data - local maps and clustered distributed maps
 * * Periodic and delayed actions
 * * Deploying and undeploying Verticles
 * * Datagram Sockets
 * * DNS client
 * * File system access
 * * High availability
 * * Clustering
 *
 * The functionality in core is fairly low level - you won't find stuff like database access, authorisation or high level
 * web functionality here - that kind of stuff you'll find in *Vert.x ext* (extensions).
 *
 * Vert.x core is small and lightweight. You just use the parts you want. It's also entirely embeddable in your
 * existing applications - we don't force you to structure your applications in a special way just so you can use
 * Vert.x.
 *
 * You can use core from any of the other languages that Vert.x supports. But here'a a cool bit - we don't force
 * you to use the Java API directly from, say, JavaScript or Ruby - after all, different languages have different conventions
 * and idioms, and it would be odd to force Java idioms on Ruby developers (for example).
 * Instead, we automatically generate an *idiomatic* equivalent of the core Java APIs for each language.
 *
 * From now on we'll just use the word *core* to refer to Vert.x core.
 *
 * include::override/dependencies.adoc[]
 *
 * Let's discuss the different concepts and features in core.
 *
 * == In the beginning there was Vert.x
 *
 * NOTE: Much of this is Java specific - need someway of swapping in language specific parts
 *
 * You can't do much in Vert.x-land unless you can commune with a {@link io.vertx.core.Vertx} object!
 *
 * It's the control centre of Vert.x and is how you do pretty much everything, including creating clients and servers,
 * getting a reference to the event bus, setting timers, as well as many other things.
 *
 * So how do you get an instance?
 *
 * If you're embedding Vert.x then you simply create an instance as follows:
 *
 * [source,$lang]
 * ----
 * {@link examples.CoreExamples#example1}
 * ----
 *
 * If you're using Verticles
 *
 * NOTE: Most applications will only need a single Vert.x instance, but it's possible to create multiple Vert.x instances if you
 * require, for example, isolation between the event bus or different groups of servers and clients.
 *
 * === Specifying options when creating a Vertx object
 *
 * When creating a Vertx object you can also specify options if the defaults aren't right for you:
 *
 * [source,$lang]
 * ----
 * {@link examples.CoreExamples#example2}
 * ----
 *
 * The {@link io.vertx.core.VertxOptions} object has many settings and allows you to configure things like clustering,
 * high availability, pool sizes and various other settings. The Javadoc describes all the settings in detail.
 *
 * === Creating a clustered Vert.x object
 *
 * If you're creating a *clustered Vert.x* (See the section on the <<event_bus, event bus>> for more information
 * on clustering the event bus), then you will normally use the asynchronous variant to create the Vertx object.
 *
 * This is because it usually takes some time (maybe a few seconds) for the different Vert.x instances in a cluster to
 * group together. During that time, we don't want to block the calling thread, so we give the result to you asynchronously.
 *
 * == Are you fluent?
 *
 * You may have noticed that in the previous examples a *fluent* API was used.
 *
 * A fluent API is where multiple methods calls can be chained together. For example:
 *
 * [source,$lang]
 * ----
 * {@link examples.CoreExamples#example3}
 * ----
 *
 * This is a common pattern throughout Vert.x APIs, so get used to it.
 *
 * Chaining calls like this allows you to write code that's a little bit less verbose. Of course, if you don't
 * like the fluent approach *we don't force you* to do it that way, you can happily ignore it if you prefer and write
 * your code like this:
 *
 * [source,$lang]
 * ----
 * {@link examples.CoreExamples#example4}
 * ----
 *
 * == Don't call us, we'll call you.
 *
 * The Vert.x APIs are largely _event driven_. This means that when things happen in Vert.x that you are interested in,
 * Vert.x will call you by sending you events.
 *
 * Some example events are:
 *
 * * a timer has fired
 * * some data has arrived on a socket,
 * * some data has been read from disk
 * * an exception has occurred
 * * an HTTP server has received a request
 *
 * You handle events by providing _handlers_ to the Vert.x APIs. For example to receive a timer event every second you
 * would do:
 *
 * [source,$lang]
 * ----
 * {@link examples.CoreExamples#example5}
 * ----
 *
 * Or to receive an HTTP request:
 *
 * [source,$lang]
 * ----
 * {@link examples.CoreExamples#example6}
 * ----
 *
 * Some time later when Vert.x has an event to pass to your handler Vert.x will call it *asynchronously*.
 *
 * This leads us to some important concepts in Vert.x:
 *
 * == Don't block me!
 *
 * With very few exceptions (i.e. some file system operations ending in 'Sync'), none of the APIs in Vert.x block the calling thread.
 *
 * If a result can be provided immediately, it will be returned immediately, otherwise you will usually provide a handler
 * to receive events some time later.
 *
 * Because none of the Vert.x APIs block threads that means you can use Vert.x to handle a lot of concurrency using
 * just a small number of threads.
 *
 * With a conventional blocking API the calling thread might block when:
 *
 * * Reading data from a socket
 * * Writing data to disk
 * * Sending a message to a recipient and waiting for a reply.
 * * ... Many other situations
 *
 * In all the above cases, when your thread is waiting for a result it can't do anything else - it's effectively useless.
 *
 * This means that if you want a lot of concurrency using blocking APIs then you need a lot of threads to prevent your
 * application grinding to a halt.
 *
 * Threads have overhead in terms of the memory they require (e.g. for their stack) and in context switching.
 *
 * For the levels of concurrency required in many modern applications, a blocking approach just doesn't scale.
 *
 * == Reactor and Multi-Reactor
 *
 * We mentioned before that Vert.x APIs are event driven - Vert.x passes events to handlers when they are available.
 *
 * In most cases Vert.x calls your handlers using a thread called an *event loop*.
 *
 * As nothing in Vert.x or your application blocks, the event loop can merrily run around delivering events to different handlers in succession
 * as they arrive.
 *
 * Because nothing blocks, an event loop can potentially deliver huge amounts of events in a short amount of time.
 * For example a single event loop can handle many thousands of HTTP requests very quickly.
 *
 * We call this the http://en.wikipedia.org/wiki/Reactor_pattern[Reactor Pattern].
 *
 * You may have heard of this before - for example Node.js implements this pattern.
 *
 * In a standard reactor implementation there is a *single event loop* thread which runs around in a loop delivering all
 * events to all handlers as they arrive.
 *
 * The trouble with a single thread is it can only run on a single core at any one time, so if you want your single threaded
 * reactor application (e.g. your Node.js application) to scale over your multi-core server you have to start up and
 * manage many different processes.
 *
 * Vert.x works differently here. Instead of a single event loop, each Vertx instance maintains *several event loops*.
 * By default we choose the number based on the number of available cores on the machine, but this can be overridden.
 *
 * This means a single Vertx process can scale across your server, unlike Node.js.
 *
 * We call this pattern the *Multi-Reactor Pattern* to distinguish it from the single threaded reactor pattern.
 *
 * NOTE: Even though a Vertx instance maintains multiple event loops, any particular handler will never be executed
 * concurrently, and in most cases (with the exception of <<worker_verticles, worker verticles>>) will always be called
 * using the *exact same event loop*.
 *
 * [[golden_rule]]
 * == The Golden Rule - Don't Block the Event Loop
 *
 * We already know that the Vert.x APIs are non blocking and won't block the event loop, but that's not much help if
 * you block the event loop *yourself* in a handler.
 *
 * If you do that, then that event loop will not be able to do anything else while it's blocked. If you block all of the
 * event loops in Vertx instance then your application will grind to a complete halt!
 *
 * So don't do it! *You have been warned*.
 *
 * Examples of blocking include:
 *
 * * +Thread.sleep()+
 * * Waiting on a lock
 * * Waiting on a mutex or monitor (e.g. synchronized section)
 * * Doing a long lived database operation and waiting for a result
 * * Doing a complex calculation that takes some significant time.
 * * Spinning in a loop
 *
 * If any of the above stop the event loop from doing anything else for a *significant amount of time* then you should
 * go immediately to the naughty step, and await further instructions.
 *
 * So... what is a *significant amount of time*?
 *
 * How long is a piece of string? It really depends on your application and the amount of concurrency you require.
 *
 * If you have a single event loop, and you want to handle 10000 http requests per second, then it's clear that each request
 * can't take more than 0.1 ms to process, so you can't block for any more time than that.
 *
 * *The maths is not hard and shall be left as an exercise for the reader.*
 *
 * If your application is not responsive it might be a sign that you are blocking an event loop somewhere. To help
 * you diagnose such issues, Vert.x will automatically log warnings if it detects an event loop hasn't returned for
 * some time. If you see warnings like these in your logs, then you should investigate.
 *
 * ----
 * Thread vertx-eventloop-thread-3 has been blocked for 20458 ms
 * ----
 *
 * Vert.x will also provide stack traces to pinpoint exactly where the blocking is occurring.
 *
 * If you want to turn off these warnings or change the settings, you can do that in the
 * {@link io.vertx.core.VertxOptions} object before creating the Vertx object.
 *
 * [[blocking_code]]
 * == Running blocking code
 *
 * In a perfect world, there will be no war or hunger, all APIs will be written asynchronously and bunny rabbits will
 * skip hand-in-hand with baby lambs across sunny green meadows.
 *
 * *But... the real world is not like that. (Have you watched the news lately?)*
 *
 * Fact is, many, if not most libraries, especially in the JVM ecosystem have synchronous APIs and many of the methods are
 * likely to block. A good example is the JDBC API - it's inherently synchronous, and no matter how hard it tries, Vert.x
 * cannot sprinkle magic pixie dust on it to make it asynchronous.
 *
 * We're not going to rewrite everything to be asynchronous overnight so we need to provide you a way to use "traditional"
 * blocking APIs safely within a Vert.x application.
 *
 * As discussed before, you can't call blocking operations directly from an event loop, as that would prevent it
 * from doing any other useful work. So how can you do this?
 *
 * It's done by calling {@link io.vertx.core.Vertx#executeBlocking} specifying both the blocking code to execute and a
 * result handler to be called back asynchronous when the blocking code has been executed.
 *
 * [source,$lang]
 * ----
 * {@link examples.CoreExamples#example7}
 * ----
 *
 * By default, if executeBlocking is called several times from the same context (e.g. the same verticle instance) then
 * the different executeBlocking are executed _serially_ (i.e. one after another).
 *
 * If you don't care about ordering you can call {@link io.vertx.core.Vertx#executeBlocking(io.vertx.core.Handler, boolean, io.vertx.core.Handler)}
 * specifying `false` as the argument to `ordered`. In this case any executeBlocking may be executed in parallel
 * on the worker pool.
 *
 * An alternative way to run blocking code is to use a <<worker_verticles, worker verticle>>
 *
 * A worker verticle is always executed with a thread from the worker pool.
 *
 * By default blocking code is executed on the Vert.x blocking code pool, configured with {@link io.vertx.core.VertxOptions#setWorkerPoolSize(int)}.
 *
 * Additional pools can be created for different purposes:
 *
 * [source,$lang]
 * ----
 * {@link examples.CoreExamples#workerExecutor1}
 * ----
 *
 * The worker executor must be closed when it's not necessary anymore:
 *
 * [source,$lang]
 * ----
 * {@link examples.CoreExamples#workerExecutor2}
 * ----
 *
 * When several workers are created with the same name, they will share the same pool. The worker pool is destroyed
 * when all the worker executors using it are closed.
 *
 * When an executor is created in a Verticle, Vert.x will close it automatically for you when the Verticle
 * is undeployed.
 *
 * Worker executors can be configured when created:
 *
 * [source,$lang]
 * ----
 * {@link examples.CoreExamples#workerExecutor3}
 * ----
 *
 * NOTE: the configuration is set when the worker pool is created
 *
 * == Async coordination
 *
 * Coordination of multiple asynchronous results can be achieved with Vert.x {@link io.vertx.core.Future futures}.
 *
 * {@link io.vertx.core.CompositeFuture#all} takes several futures arguments (up to 6) and returns a future that is
 * _succeeded_ when all the futures are and _failed_ otherwise:
 *
 * [source,$lang]
 * ----
 * {@link examples.CoreExamples#exampleFuture1}
 * ----
 *
 * The handler returned by {@link io.vertx.core.Future#completer()} completes the future.
 *
 * {@link io.vertx.core.CompositeFuture#any} takes several futures arguments (up to 6) and returns a future that is
 * _succeeded_ if one of the futures is, and _failed_ otherwise:
 *
 * [source,$lang]
 * ----
 * {@link examples.CoreExamples#exampleFuture2}
 * ----
 *
 * {@link io.vertx.core.Future#compose} can be used for chaining futures:
 *
 * [source,$lang]
 * ----
 * {@link examples.CoreExamples#exampleFuture3}
 * ----
 *
 * == Verticles
 *
 * Vert.x comes with a simple, scalable, _actor-like_ deployment and concurrency model out of the box that
 * you can use to save you writing your own.
 *
 * *This model is entirely optional and Vert.x does not force you to create your applications in this way if you don't
 * want to.*.
 *
 * The model does not claim to be a strict actor-model implementation, but it does share similarities especially
 * with respect to concurrency, scaling and deployment.
 *
 * To use this model, you write your code as set of *verticles*.
 *
 * Verticles are chunks of code that get deployed and run by Vert.x. A Vert.x instance maintains N event loop threads
 * (where N by default is core*2) by default. Verticles can be written in any of the languages that Vert.x supports
 * and a single application can include verticles written in multiple languages.
 *
 * You can think of a verticle as a bit like an actor in the http://en.wikipedia.org/wiki/Actor_model[Actor Model].
 *
 * An application would typically be composed of many verticle instances running in the same Vert.x instance at the same
 * time. The different verticle instances communicate with each other by sending messages on the <<event_bus, event bus>>.
 *
 * include::override/verticles.adoc[]
 *
 * === Verticle Types
 *
 * There are three different types of verticles:
 *
 * Standard Verticles:: These are the most common and useful type - they are always executed using an event loop thread.
 * We'll discuss this more in the next section.
 * Worker Verticles:: These run using a thread from the worker pool. An instance is never executed concurrently by more
 * than one thread.
 * Multi-threaded worker verticles:: These run using a thread from the worker pool. An instance can be executed concurrently by more
 * than one thread.
 *
 * === Standard verticles
 *
 * Standard verticles are assigned an event loop thread when they are created and the +start+ method is called with that
 * event loop. When you call any other methods that takes a handler on a core API from an event loop then Vert.x
 * will guarantee that those handlers, when called, will be executed on the same event loop.
 *
 * This means we can guarantee that all the code in your verticle instance is always executed on the same event loop (as
 * long as you don't create your own threads and call it!).
 *
 * This means you can write all the code in your application as single threaded and let Vert.x worrying about the threading
 * and scaling. No more worrying about +synchronized+ and +volatile+ any more, and you also avoid many other cases of race conditions
 * and deadlock so prevalent when doing hand-rolled 'traditional' multi-threaded application development.
 *
 * [[worker_verticles]]
 * === Worker verticles
 *
 * A worker verticle is just like a standard verticle but it's executed not using an event loop, but using a thread from
 * the Vert.x worker thread pool.
 *
 * Worker verticles are designed for calling blocking code, as they won't block any event loops.
 *
 * If you don't want to use a worker verticle to run blocking code, you can also run <<blocking_code, inline blocking code>>
 * directly while on an event loop.
 *
 * If you want to deploy a verticle as a worker verticle you do that with {@link io.vertx.core.DeploymentOptions#setWorker}.
 *
 * [source,$lang]
 * ----
 * {@link examples.CoreExamples#example7_1}
 * ----
 *
 * Worker verticle instances are never executed concurrently by Vert.x by more than one thread, but can executed by
 * different threads at different times.
 *
 * ==== Multi-threaded worker verticles
 *
 * A multi-threaded worker verticle is just like a normal worker verticle but it *can* be executed concurrently by
 * different threads.
 *
 * WARNING: Multi-threaded worker verticles are an advanced feature and most applications will have no need for them.
 * Because of the concurrency in these verticles you have to be very careful to keep the verticle in a consistent state
 * using standard Java techniques for multi-threaded programming.
 *
 * === Deploying verticles programmatically
 *
 * You can deploy a verticle using one of the {@link io.vertx.core.Vertx#deployVerticle} method, specifying a verticle
 * name or you can pass in a verticle instance you have already created yourself.
 *
 * NOTE: Deploying Verticle *instances* is Java only.
 *
 * [source,java]
 * ----
 * {@link examples.CoreExamples#example8}
 * ----
 *
 * You can also deploy verticles by specifying the verticle *name*.
 *
 * The verticle name is used to look up the specific {@link io.vertx.core.spi.VerticleFactory} that will be used to
 * instantiate the actual verticle instance(s).
 *
 * Different verticle factories are available for instantiating verticles in different languages and for various other
 * reasons such as loading services and getting verticles from Maven at run-time.
 *
 * This allows you to deploy verticles written in any language from any other language that Vert.x supports.
 *
 * Here's an example of deploying some different types of verticles:
 *
 * [source,$lang]
 * ----
 * {@link examples.CoreExamples#example9}
 * ----
 *
 * === Rules for mapping a verticle name to a verticle factory
 *
 * When deploying verticle(s) using a name, the name is used to select the actual verticle factory that will instantiate
 * the verticle(s).
 *
 * Verticle names can have a prefix - which is a string followed by a colon, which if present will be used to look-up the factory, e.g.
 *
 *  js:foo.js // Use the JavaScript verticle factory
 *  groovy:com.mycompany.SomeGroovyCompiledVerticle // Use the Groovy verticle factory
 *  service:com.mycompany:myorderservice // Uses the service verticle factory
 *
 * If no prefix is present, Vert.x will look for a suffix and use that to lookup the factory, e.g.
 *
 *  foo.js // Will also use the JavaScript verticle factory
 *  SomeScript.groovy // Will use the Groovy verticle factory
 *
 * If no prefix or suffix is present, Vert.x will assume it's a Java fully qualified class name (FQCN) and try
 * and instantiate that.
 *
 * === How are Verticle Factories located?
 *
 * Most Verticle factories are loaded from the classpath and registered at Vert.x startup.
 *
 * You can also programmatically register and unregister verticle factories using {@link io.vertx.core.Vertx#registerVerticleFactory}
 * and {@link io.vertx.core.Vertx#unregisterVerticleFactory} if you wish.
 *
 * === Waiting for deployment to complete
 *
 * Verticle deployment is asynchronous and may complete some time after the call to deploy has returned.
 *
 * If you want to be notified when deployment is complete you can deploy specifying a completion handler:
 *
 * [source,$lang]
 * ----
 * {@link examples.CoreExamples#example10}
 * ----
 *
 * The completion handler will be passed a result containing the deployment ID string, if deployment succeeded.
 *
 * This deployment ID can be used later if you want to undeploy the deployment.
 *
 * === Undeploying verticle deployments
 *
 * Deployments can be undeployed with {@link io.vertx.core.Vertx#undeploy}.
 *
 * Un-deployment is itself asynchronous so if you want to be notified when un-deployment is complete you can deploy specifying a completion handler:
 *
 * [source,$lang]
 * ----
 * {@link examples.CoreExamples#example11}
 * ----
 *
 * === Specifying number of verticle instances
 *
 * When deploying a verticle using a verticle name, you can specify the number of verticle instances that you
 * want to deploy:
 *
 * [source,$lang]
 * ----
 * {@link examples.CoreExamples#example12}
 * ----
 *
 * This is useful for scaling easily across multiple cores. For example you might have a web-server verticle to deploy
 * and multiple cores on your machine, so you want to deploy multiple instances to take utilise all the cores.
 *
 * include::override/verticle-configuration.adoc[]
 *
 * === Verticle Isolation Groups
 *
 * By default, Vert.x has a _flat classpath_. I.e, when Vert.x deploys verticles it does so with the current classloader -
 * it doesn't create a new one. In the majority of cases this is the simplest, clearest and sanest thing to do.
 *
 * However, in some cases you may want to deploy a verticle so the classes of that verticle are isolated from others in
 * your application.
 *
 * This might be the case, for example, if you want to deploy two different versions of a verticle with the same class name
 * in the same Vert.x instance, or if you have two different verticles which use different versions of the same jar library.
 *
 * When using an isolation group you provide a list of the class names that you want isolated using
 * {@link io.vertx.core.DeploymentOptions#setIsolatedClasses(java.util.List)}-  an entry can be a fully qualified
 * classname such as `com.mycompany.myproject.engine.MyClass` or it can be a wildcard which will match any classes in a package and any
 * sub-packages, e.g. `com.mycompany.myproject.*` would match any classes in the package `com.mycompany.myproject` or
 * any sub-packages.
 *
 * Please note that _only_ the classes that match will be isolated - any other classes will be loaded by the current
 * class loader.
 *
 * Extra classpath entries can also be provided with {@link io.vertx.core.DeploymentOptions#setExtraClasspath} so if
 * you want to load classes or resources that aren't already present on the main classpath you can add this.
 *
 * WARNING: Use this feature with caution. Class-loaders can be a can of worms, and can make debugging difficult, amongst
 * other things.
 *
 * Here's an example of using an isolation group to isolate a verticle deployment.
 *
 * [source,$lang]
 * ----
 * {@link examples.CoreExamples#example14}
 * ----
 *
 * === High Availability
 *
 * Verticles can be deployed with High Availability (HA) enabled. In that context, when a verticle is deployed on
 * a vert.x instance that dies abruptly, the verticle is redeployed on another vert.x instance from the cluster.
 *
 * To run an verticle with the high availability enabled, just append the `-ha` switch:
 *
 * [source]
 * ----
 * vertx run my-verticle.js -ha
 * ----
 *
 * When enabling high availability, no need to add `-cluster`.
 *
 * More details about the high availability feature and configuration in the <<High Availability and Fail-Over>>
 *   section.
 *
 *
 * === Running Verticles from the command line
 *
 * You can use Vert.x directly in your Maven or Gradle projects in the normal way by adding a dependency to the Vert.x
 * core library and hacking from there.
 *
 * However you can also run Vert.x verticles directly from the command line if you wish.
 *
 * To do this you need to download and install a Vert.x distribution, and add the `bin` directory of the installation
 * to your `PATH` environment variable. Also make sure you have a Java 8 JDK on your `PATH`.
 *
 * NOTE: The JDK is required to support on the fly compilation of Java code.
 *
 * You can now run verticles by using the `vertx run` command. Here are some examples:
 *
 * ----
 * # Run a JavaScript verticle
 * vertx run my_verticle.js
 *
 * # Run a Ruby verticle
 * vertx run a_n_other_verticle.rb
 *
 * # Run a Groovy script verticle, clustered
 * vertx run FooVerticle.groovy -cluster
 * ----
 *
 * You can even run Java source verticles without compiling them first!
 *
 * ----
 * vertx run SomeJavaSourceFile.java
 * ----
 *
 * Vert.x will compile the Java source file on the fly before running it. This is really useful for quickly
 * prototyping verticles and great for demos. No need to set-up a Maven or Gradle build first to get going!
 *
 * For full information on the various options available when executing `vertx` on the command line,
 * type `vertx` at the command line.
 *
 * === Causing Vert.x to exit
 *
 * Threads maintained by Vert.x instances are not daemon threads so they will prevent the JVM from exiting.
 *
 * If you are embedding Vert.x and you have finished with it, you can call {@link io.vertx.core.Vertx#close} to close it
 * down.
 *
 * This will shut-down all internal thread pools and close other resources, and will allow the JVM to exit.
 *
 * === The Context object
 *
 * When Vert.x provides an event to a handler or calls the start or stop methods of a
 * {@link io.vertx.core.Verticle}, the execution is associated with a `Context`. Usually a context is an
 * *event-loop context* and is tied to a specific event loop thread. So executions for that context always occur
 * on that exact same event loop thread. In the case of worker verticles and running inline blocking code a
 * worker context will be associated with the execution which will use a thread from the worker thread pool.
 *
 * To retrieve the context, use the {@link io.vertx.core.Vertx#getOrCreateContext()} method:
 *
 * [source, $lang]
 * ----
 * {@link examples.CoreExamples#retrieveContext(io.vertx.core.Vertx)}
 * ----
 *
 * If the current thread has a context associated with it, it reuses the context object. If not a new instance of
 * context is created. You can test the _type_ of context you have retrieved:
 *
 * [source, $lang]
 * ----
 * {@link examples.CoreExamples#retrieveContextType(io.vertx.core.Vertx)}
 * ----
 *
 * When you have retrieved the context object, you can run code in this context asynchronously. In other words,
 * you submit a task that will be eventually run in the same context, but later:
 *
 * [source, $lang]
 * ----
 * {@link examples.CoreExamples#runInContext(io.vertx.core.Vertx)}
 * ----
 *
 * When several handlers run in the same context, they may want to share data. The context object offers methods to
 * store and retrieve data shared in the context. For instance, it lets you pass data to some action run with
 * {@link io.vertx.core.Context#runOnContext(io.vertx.core.Handler)}:
 *
 * [source, $lang]
 * ----
 * {@link examples.CoreExamples#runInContextWithData(io.vertx.core.Vertx)}
 * ----
 *
 * The context object also let you access verticle configuration using the {@link io.vertx.core.Context#config()}
 * method. Check the <<Passing configuration to a verticle>> section for more details about this configuration.
 *
 * === Executing periodic and delayed actions
 *
 * It's very common in Vert.x to want to perform an action after a delay, or periodically.
 *
 * In standard verticles you can't just make the thread sleep to introduce a delay, as that will block the event loop thread.
 *
 * Instead you use Vert.x timers. Timers can be *one-shot* or *periodic*. We'll discuss both
 *
 * ==== One-shot Timers
 *
 * A one shot timer calls an event handler after a certain delay, expressed in milliseconds.
 *
 * To set a timer to fire once you use {@link io.vertx.core.Vertx#setTimer} method passing in the delay and a handler
 *
 * [source,$lang]
 * ----
 * {@link examples.CoreExamples#example15}
 * ----
 *
 * The return value is a unique timer id which can later be used to cancel the timer. The handler is also passed the timer id.
 *
 * ==== Periodic Timers
 *
 * You can also set a timer to fire periodically by using {@link io.vertx.core.Vertx#setPeriodic}.
 *
 * There will be an initial delay equal to the period.
 *
 * The return value of `setPeriodic` is a unique timer id (long). This can be later used if the timer needs to be cancelled.
 *
 * The argument passed into the timer event handler is also the unique timer id:
 *
 * Keep in mind that the timer will fire on a periodic basis. If your periodic treatment takes a long amount of time to proceed,
 * your timer events could run continuously or even worse : stack up.
 * 
 * In this case, you should consider using {@link io.vertx.core.Vertx#setTimer} instead. Once your treatment has
 * finished, you can set the next timer.
 * 
 * [source,$lang]
 * ----
 * {@link examples.CoreExamples#example16}
 * ----
 *
 * ==== Cancelling timers
 *
 * To cancel a periodic timer, call {@link io.vertx.core.Vertx#cancelTimer} specifying the timer id. For example:
 *
 * [source,$lang]
 * ----
 * {@link examples.CoreExamples#example17}
 * ----
 *
 * ==== Automatic clean-up in verticles
 *
 * If you're creating timers from inside verticles, those timers will be automatically closed
 * when the verticle is undeployed.
 *
 * === Verticle worker pool
 *
 * Verticle use the Vert.x worker pool for executing blocking actions, i.e {@link io.vertx.core.Context#executeBlocking} or
 * worker verticle.
 *
 * A different worker pool can be specified in deployment options:
 *
 * [source,$lang]
 * ----
 * {@link examples.CoreExamples#deployVerticleWithDifferentWorkerPool}
 * ----
 *
 * [[event_bus]]
 * include::eventbus.adoc[]
 *
 * include::override/json.adoc[]
 *
 * include::buffers.adoc[]
 *
 * include::net.adoc[]
 *
 * include::http.adoc[]
 *
 * include::shareddata.adoc[]
 *
 * include::filesystem.adoc[]
 *
 * include::datagrams.adoc[]
 *
 * include::dns.adoc[]
 *
 * [[streams]]
 * include::streams.adoc[]
 *
 * include::parsetools.adoc[]
 *
 * == Thread safety
 *
 * Most Vert.x objects are safe to access from different threads. _However_ performance is optimised when they are
 * accessed from the same context they were created from.
 *
 * For example if you have deployed a verticle which creates a {@link io.vertx.core.net.NetServer} which provides
 * {@link io.vertx.core.net.NetSocket} instances in it's handler, then it's best to always access that socket instance
 * from the event loop of the verticle.
 *
 * If you stick to the standard Vert.x verticle deployment model and avoid sharing objects between verticles then this
 * should be the case without you having to think about it.
 *
 * == Metrics SPI
 *
 * By default Vert.x does not record any metrics. Instead it provides an SPI for others to implement which can be added
 * to the classpath. The metrics SPI is an advanced feature which allows implementers to capture events from Vert.x in
 * order to gather metrics. For more information on this, please consult the
 * {@link io.vertx.core.spi.metrics.VertxMetrics API Documentation}.
 *
 * == OSGi
 *
 * Vert.x Core is packaged as an OSGi bundle, so can be used in any OSGi R4.2+ environment such as Apache Felix
 * or Eclipse Equinox. The bundle exports `io.vertx.core*`.
 *
 * However, the bundle has some dependencies on Jackson and Netty. To get the vert.x core bundle resolved deploy:
 *
 * * Jackson Annotation [2.6.0,3)
 * * Jackson Core [2.6.2,3)
 * * Jackson Databind [2.6.2,3)
 * * Netty Buffer [4.0.31,5)
 * * Netty Codec [4.0.31,5)
 * * Netty Codec/Socks [4.0.31,5)
 * * Netty Codec/Common [4.0.31,5)
 * * Netty Codec/Handler [4.0.31,5)
 * * Netty Codec/Transport [4.0.31,5)
 *
 * Here is a working deployment on Apache Felix 5.2.0:
 *
 *[source]
 *----
 * 14|Active     |    1|Jackson-annotations (2.6.0)
 * 15|Active     |    1|Jackson-core (2.6.2)
 * 16|Active     |    1|jackson-databind (2.6.2)
 * 18|Active     |    1|Netty/Buffer (4.0.31.Final)
 * 19|Active     |    1|Netty/Codec (4.0.31.Final)
 * 20|Active     |    1|Netty/Codec/HTTP (4.0.31.Final)
 * 21|Active     |    1|Netty/Codec/Socks (4.0.31.Final)
 * 22|Active     |    1|Netty/Common (4.0.31.Final)
 * 23|Active     |    1|Netty/Handler (4.0.31.Final)
 * 24|Active     |    1|Netty/Transport (4.0.31.Final)
 * 25|Active     |    1|Netty/Transport/SCTP (4.0.31.Final)
 * 26|Active     |    1|Vert.x Core (3.1.0)
 *----
 *
 * On Equinox, you may want to disable the `ContextFinder` with the following framework property:
 * `eclipse.bundle.setTCCL=false`
 *
 *
 * == The 'vertx' command line
 *
 * The `vertx` command is used to interact with Vert.x from the command line. It's main use is to run Vert.x verticles.
 * To do this you need to download and install a Vert.x distribution, and add the `bin` directory of the installation
 * to your `PATH` environment variable. Also make sure you have a Java 8 JDK on your `PATH`.
 *
 * NOTE: The JDK is required to support on the fly compilation of Java code.
 *
 * === Run verticles
 *
 * You can run raw Vert.x verticles directly from the command line using `vertx run`. Here is a couple of examples of
 * the `run` _command_:
 *
 * [source]
 * ----
 * vertx run my-verticle.js                                 (1)
 * vertx run my-verticle.groovy                             (2)
 * vertx run my-verticle.rb                                 (3)
 *
 * vertx run io.vertx.example.MyVerticle                    (4)
 * vertx run io.vertx.example.MVerticle -cp my-verticle.jar (5)
 *
 * vertx run MyVerticle.java                                (6)
 * ----
 * 1. Deploys a JavaScript verticle
 * 2. Deploys a Groovy verticle
 * 3. Deploys a Ruby verticle
 * 4. Deploys an already compiled Java verticle. Classpath root is the current directory
 * 5. Deploys a verticle packaged in a Jar, the jar need to be in the classpath
 * 6. Compiles the Java source and deploys it
 *
 * As you can see in the case of Java, the name can either be the fully qualified class name of the verticle, or
 * you can specify the Java Source file directly and Vert.x compiles it for you.
 *
 * You can also prefix the verticle with the name of the language implementation to use. For example if the verticle is
 * a compiled Groovy class, you prefix it with `groovy:` so that Vert.x knows it's a Groovy class not a Java class.
 *
 * [source]
 * ----
 * vertx run groovy:io.vertx.example.MyGroovyVerticle
 * ----
 *
 * The `vertx run` command can take a few optional parameters, they are:
 *
 *  * `-conf <config_file>` - Provides some configuration to the verticle. `config_file` is the name of a text file
 *  containing a JSON object that represents the configuration for the verticle. This is optional.
 *  * `-cp <path>` - The path on which to search for the verticle and any other resources used by the verticle. This
 *  defaults to `.` (current directory). If your verticle references other scripts, classes or other resources
 *  (e.g. jar files) then make sure these are on this path. The path can contain multiple path entries separated by
 *  `:` (colon) or `;` (semi-colon) depending on the operating system. Each path entry can be an absolute or relative
 *  path to a directory containing scripts, or absolute or relative filenames for jar or zip files. An example path
 *  might be `-cp classes:lib/otherscripts:jars/myjar.jar:jars/otherjar.jar`. Always use the path to reference any
 *  resources that your verticle requires. Do **not** put them on the system classpath as this can cause isolation
 *  issues between deployed verticles.
 * * `-instances <instances>`  - The number of instances of the verticle to instantiate. Each verticle instance is
 * strictly single threaded so to scale your application across available cores you might want to deploy more than
 * one instance. If omitted a single instance will be deployed.
 *  * `-worker` - This option determines whether the verticle is a worker verticle or not.
 *  * `-cluster` -  This option determines whether the Vert.x instance will attempt to form a cluster with other Vert.x
 *  instances on the network. Clustering Vert.x instances allows Vert.x to form a distributed event bus with
 *  other nodes. Default is `false` (not clustered).
 *  * `-cluster-port` - If the cluster option has also been specified then this determines which port will be used for
 *  cluster communication with other Vert.x instances. Default is `0` - which means '_choose a free random port_'. You
 *  don't usually need to specify this parameter unless you really need to bind to a specific port.
 *  * `-cluster-host` - If the cluster option has also been specified then this determines which host address will be
 *  used for cluster communication with other Vert.x instances. By default it will try and pick one from the available
 *  interfaces. If you have more than one interface and you want to use a specific one, specify it here.
 *  * `-ha` - if specified the verticle will be deployed as  high availability (HA) deployment. See related section
 *  for more details
 *  * `-quorum` - used in conjunction with `-ha`. It specifies the minimum number of nodes in the cluster for any _HA
 *  deploymentIDs_ to be active. Defaults to 0.
 *  * `-hagroup` - used in conjunction with `-ha`. It specifies the HA group this node will join. There can be
 *  multiple HA groups in a cluster. Nodes will only failover to other nodes in the same group. The default value is `
 *  +++__DEFAULT__+++`
 *
 * You can also set system properties using: `-Dkey=value`.
 *
 * Here are some more examples:
 *
 * Run a JavaScript verticle server.js with default settings
 * [source]
 * ----
 * vertx run server.js
 * ----
 *
 * Run 10 instances of a pre-compiled Java verticle specifying classpath
 * [source]
 * ----
 * vertx run com.acme.MyVerticle -cp "classes:lib/myjar.jar" -instances 10
 * ----
 *
 * Run 10 instances of a Java verticle by source _file_
 * [source]
 * ----
 * vertx run MyVerticle.java -instances 10
 * ----
 *
 * Run 20 instances of a ruby worker verticle
 * [source]
 * ----
 * vertx run order_worker.rb -instances 20 -worker
 * ----
 *
 * Run two JavaScript verticles on the same machine and let them cluster together with each other and any other servers
 * on the network
 * [source]
 * ----
 * vertx run handler.js -cluster
 * vertx run sender.js -cluster
 * ----
 *
 * Run a Ruby verticle passing it some config
 * [source]
 * ----
 * vertx run my_verticle.rb -conf my_verticle.conf
 * ----
 * Where `my_verticle.conf` might contain something like:
 *
 * [source, json]
 * ----
 * {
 *  "name": "foo",
 *  "num_widgets": 46
 * }
 * ----
 *
 * The config will be available inside the verticle via the core API.
 *
 * When using the high-availability feature of vert.x you may want to create a _bare_ instance of vert.x. This
 * instance does not deploy any verticles when launched, but will receive a verticle if another node of the cluster
 * dies. To create a _bare_ instance, launch:
 *
 * [source]
 * ----
 * vertx -ha
 * ----
 *
 * Depending on your cluster configuration, you may have to append the `cluster-host` and `cluster-port` parameters.
 *
 * === Executing a Vert.x application packaged as a fat jar
 *
 * A _fat jar_ is an executable jar embedding its dependencies. This means you don't have to have Vert.x pre-installed
 * on the machine on which you execute the jar. Like any executable Java jar it can be executed with.
 *
 * [source]
 * ----
 * java -jar my-application-fat.jar
 * ----
 *
 * There is nothing really Vert.x specific about this, you could do this with any Java application
 *
 * You can either create your own main class and specify that in the manifest, but it's recommended that you write your
 * code as verticles and use the Vert.x {@link io.vertx.core.Launcher} class (`io.vertx.core.Launcher`) as your main
 * class. This is the same main class used when running Vert.x at the command line and therefore allows you to
 * specify command line arguments, such as `-instances` in order to scale your application more easily.
 *
 * To deploy your verticle in a _fatjar_ like this you must have a _manifest_ with:
 *
 * * `Main-Class` set to `io.vertx.core.Launcher`
 * * `Main-Verticle` specifying the main verticle (fully qualified class name or script file name)
 *
 * You can also provide the usual command line arguments that you would pass to `vertx run`:
 * [source]
 * ----
 * java -jar my-verticle-fat.jar -cluster -conf myconf.json
 * java -jar my-verticle-fat.jar -cluster -conf myconf.json -cp path/to/dir/conf/cluster_xml
 * ----
 *
 * NOTE: Please consult the Maven/Gradle simplest and Maven/Gradle verticle examples in the examples repository for
 * examples of building applications as fatjars.
 *
 * A fat jar executes the `run` command, by default.
 *
 * === Displaying version of Vert.x
 * To display the vert.x version, just launch:
 *
 * [source]
 * ----
 * vertx version
 * ----
 *
 * === Other commands
 *
 * The `vertx` command line and the `Launcher` also provide other _commands_ in addition to `run` and `version`:
 *
 * You can create a `bare` instance using:
 *
 * [source]
 * ----
 * vertx bare
 * # or
 * java -jar my-verticle-fat.jar bare
 * ----
 *
 * You can also start an application in background using:
 *
 * [source]
 * ----
 * java -jar my-verticle-fat.jar start -Dvertx-id=my-app-name
 * ----
 *
 * If `my-app-name` is not set, a random id will be generated, and printed on the command prompt. You can pass `run`
 * options to the `start` command:
 *
 * [source]
 * ----
 * java -jar my-verticle-fat.jar start -Dvertx-id=my-app-name -cluster
 * ----
 *
 * Once launched in background, you can stop it with the `stop` command:
 *
 * [source]
 * ----
 * java -jar my-verticle-fat.jar stop my-app-name
 * ----
 *
 * You can also list the vert.x application launched in background using:
 *
 * [source]
 * ----
 * java -jar my-verticle-fat.jar list
 * ----
 *
 * The `start`, `stop` and `list` command are also available from the `vertx` tool. The start` command supports a couple of options:
 *
 *  * `vertx-id` : the application id, uses a random UUID if not set
 *  * `java-opts` : the Java Virtual Machine options, uses the `JAVA_OPTS` environment variable if not set.
 *  * `redirect-output` : redirect the spawned process output and error streams to the parent process streams.
 *
 *  If option values contain spaces, don't forget to wrap the value between {@code ""} (double-quotes).
 *
 *  As the `start` command spawns a new process, the java options passed to the JVM are not propagated, so you **must**
 *  use `java-opts` to configure the JVM (`-X`, `-D`...). If you use the `CLASSPATH` environment variable, be sure it
 *  contains all the required jars (vertx-core, your jars and all the dependencies).
 *
 * The set of commands is extensible, refer to the <<Extending the vert.x Launcher>> section.
 *
 * === Live Redeploy
 *
 * When developing it may be convenient to automatically redeploy your application upon file changes. The `vertx`
 * command line tool and more generally the {@link io.vertx.core.Launcher} class offers this feature. Here are some
 * examples:
 *
 * [source]
 * ----
 * vertx run MyVerticle.groovy --redeploy="**&#47;*.groovy" --launcher-class=io.vertx.core.Launcher
 * vertx run MyVerticle.groovy --redeploy="**&#47;*.groovy,**&#47;*.rb"  --launcher-class=io.vertx.core.Launcher
 * java io.vertx.core.Launcher run org.acme.MyVerticle --redeploy="**&#47;*.class"  --launcher-class=io.vertx.core
 * .Launcher -cp ...
 * ----
 *
 * The redeployment process is implemented as follows. First your application is launched as a background application
 * (with the `start` command). On matching file changes, the process is stopped and the application is restarted.
 * This way avoids leaks.
 *
 * To enable the live redeploy, pass the `--redeploy` option to the `run` command. The `--redeploy` indicates the
 * set of file to _watch_. This set can use Ant-style patterns (with `\**`, `*` and `?`). You can specify
 * several sets by separating them using a comma (`,`). Patterns are relative to the current working directory.
 *
 * Parameters passed to the `run` command are passed to the application. Java Virtual Machine options can be
 * configured using `--java-opts`.
 *
 * The `--launcher-class` option determine with with _main_ class the application is launcher. It's generally
 * {@link io.vertx.core.Launcher}, but you have use you own _main_.
 *
 * The redeploy feature can be used in your IDE:
 *
 * * Eclipse - create a _Run_ configuration, using the `io.vertx.core.Launcher` class a _main class_. In the _Program
 * arguments_ area (in the _Arguments_ tab), write `run your-verticle-fully-qualified-name --redeploy=\**&#47;*.java
 * --launcher-class=io.vertx.core.Launcher`. You can also add other parameters. The redeployment works smoothly as
 * Eclipse incrementally compiles your files on save.
 * * IntelliJ - create a _Run_ configuration (_Application_), set the _Main class_ to `io.vertx.core.Launcher`. In
 * the Program arguments write: `run your-verticle-fully-qualified-name --redeploy=\**&#47;*.class
 * --launcher-class=io.vertx.core.Launcher`. To trigger the redeployment, you need to _make_ the project or
 * the module explicitly (_Build_ menu -> _Make project_).
 *
 * To debug your application, create your run configuration as a remote application and configure the debugger
 * using `--java-opts`. However, don't forget to re-plug the debugger after every redeployment as a new process is
 * created every time.
 *
 * You can also hook your build process in the redeploy cycle:
 *
 * [source]
 * ----
 * java -jar target/my-fat-jar.jar --redeploy="**&#47;*.java" --on-redeploy="mvn package"
 * java -jar build/libs/my-fat-jar.jar --redeploy="src&#47;**&#47;*.java" --on-redeploy='./gradlew shadowJar'
 * ----
 *
 * The "on-redeploy" option specifies a command invoked after the shutdown of the application and before the
 * restart. So you can hook your build tool if it updates some runtime artifacts. For instance, you can launch `gulp`
 * or `grunt` to update your resources.
 *
 * The redeploy feature also supports the following settings:
 *
 * * `redeploy-scan-period` : the file system check period (in milliseconds), 250ms by default
 * * `redeploy-grace-period` : the amount of time (in milliseconds) to wait between 2 re-deployments, 1000ms by default
 * * `redeploy-termination-period` : the amount of time to wait after having stopped the application (before
 * launching user command). This is useful on Windows, where the process is not killed immediately. The time is given
 * in milliseconds. 0 ms by default.
 *
 * == Cluster Managers
 *
 * In Vert.x a cluster manager is used for various functions including:
 *
 * * Discovery and group membership of Vert.x nodes in a cluster
 * * Maintaining cluster wide topic subscriber lists (so we know which nodes are interested in which event bus addresses)
 * * Distributed Map support
 * * Distributed Locks
 * * Distributed Counters
 *
 * Cluster managers _do not_ handle the event bus inter-node transport, this is done directly by Vert.x with TCP connections.
 *
 * The default cluster manager used in the Vert.x distributions is one that uses http://hazelcast.com[Hazelcast] but this
 * can be easily replaced by a different implementation as Vert.x cluster managers are pluggable.
 *
 * A cluster manager must implement the interface {@link io.vertx.core.spi.cluster.ClusterManager}. Vert.x locates
 * cluster managers at run-time by using the Java
 * https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html[Service Loader] functionality to locate
 * instances of {@link io.vertx.core.spi.cluster.ClusterManager} on the classpath.
 *
 * If you are using Vert.x at the command line and you want to use clustering you should make sure the `lib` directory
 * of the Vert.x installation contains your cluster manager jar.
 *
 * If you are using Vert.x from a Maven or Gradle project just add the cluster manager jar as a dependency of your project.
 *
 * You can also specify cluster managers programmatically if embedding Vert.x using
 * {@link io.vertx.core.VertxOptions#setClusterManager(io.vertx.core.spi.cluster.ClusterManager)}.
 *
 * == Logging
 *
 * Vert.x logs using it's in-built logging API. The default implementation uses the JDK (JUL) logging so no extra
 * logging dependencies are needed.
 *
 * === Configuring JUL logging
 *
 * A JUL logging configuration file can be specified in the normal JUL way by providing a system property called:
 * `java.util.logging.config.file` with the value being your configuration file. For more information on this and
 * the structure of a JUL config file please consult the JUL logging documentation.
 *
 * Vert.x also provides a slightly more convenient way to specify a configuration file without having to set a system
 * property. Just provide a JUL config file with the name `vertx-default-jul-logging.properties` on your classpath (e.g.
 * inside your fatjar) and Vert.x will use that to configure JUL.
 *
 * === Using another logging framework
 *
 * If you don't want Vert.x to use JUL for it's own logging you can configure it to use another logging framework, e.g.
 * Log4J or SLF4J.
 *
 * To do this you should set a system property called `vertx.logger-delegate-factory-class-name` with the name of a Java
 * class which implements the interface {@link io.vertx.core.spi.logging.LogDelegateFactory}. We provide pre-built
 * implementations for Log4J (version 1), Log4J 2 and SLF4J with the class names
 * `io.vertx.core.logging.Log4jLogDelegateFactory`, `io.vertx.core.logging.Log4j2LogDelegateFactory` and
 * `io.vertx.core.logging.SLF4JLogDelegateFactory` respectively. If you want to use these implementations you should
 * also make sure the relevant Log4J or SLF4J jars are on your classpath.
 *
 * Notice that, the provided delegate for Log4J 1 does not support parameterized message. The delegate for Log4J 2
 * uses the `{}` syntax like the SLF4J delegate. JUL delegate uses the `{x}` syntax.
 *
 * === Logging from your application
 *
 * Vert.x itself is just a library and you can use whatever logging library you prefer to log from your own application,
 * using that logging library's API.
 *
 * However, if you prefer you can use the Vert.x logging facility as described above to provide logging for your
 * application too.
 *
 * To do that you use {@link io.vertx.core.logging.LoggerFactory} to get an instance of {@link io.vertx.core.logging.Logger}
 * which you then use for logging, e.g.
 *
 * [source,$lang]
 * ----
 * {@link examples.CoreExamples#example18}
 * ----
 *
 * == Hostname resolution
 *
 * Vert.x uses an an hostname resolver for resolving hostname into IP addresses instead of
 * the JVM built-in blocking resolver.
 *
 * An hostname are resolve to an IP address using:
 *
 * - the _hosts_ file of the operating system
 * - otherwise DNS queries against a list of servers
 *
 * By default it will use the list of the system DNS server addresses from the environment, if that list cannot be
 * retrieved it will use Google's public DNS servers `"8.8.8.8"` and `"8.8.4.4"`.
 *
 * DNS servers can be also configured when creating a {@link io.vertx.core.Vertx} instance:
 *
 * [source,$lang]
 * ----
 * {@link examples.CoreExamples#configureDNSServers}
 * ----
 *
 * The default port of a DNS server is `53`, when a server uses a different port, this port can be set
 * using a colon delimiter: `192.168.0.2:40000`.
 *
 * The resolver can be configured to use an alternative _hosts_ file:
 *
 * [source,$lang]
 * ----
 * {@link examples.CoreExamples#configureHosts}
 * ----
 *
 * == High Availability and Fail-Over
 *
 * Vert.x allows you to run your verticles with high availability (HA) support. In that case, when a vert.x
 * instance running a verticle dies abruptly, the verticle is migrated to another vertx instance. The vert.x
 * instances must be in the same cluster.
 *
 * === Automatic failover
 *
 * When vert.x runs with _HA_ enabled, if a vert.x instance where a verticle runs fails or dies, the verticle is
 * redeployed automatically on another vert.x instance of the cluster. We call this _verticle fail-over_.
 *
 * To run vert.x with the _HA_ enabled, just add the `-ha` flag to the command line:
 *
 * [source]
 * ----
 * vertx run my-verticle.js -ha
 * ----
 *
 * Now for HA to work, you need more than one Vert.x instances in the cluster, so let's say you have another
 * Vert.x instance that you have already started, for example:
 *
 * [source]
 * ----
 * vertx run my-other-verticle.js -ha
 * ----
 *
 * If the Vert.x instance that is running `my-verticle.js` now dies (you can test this by killing the process
 * with `kill -9`), the Vert.x instance that is running `my-other-verticle.js` will automatic deploy `my-verticle
 * .js` so now that Vert.x instance is running both verticles.
 *
 * NOTE: the migration is only possible if the second vert.x instance has access to the verticle file (here
 * `my-verticle.js`).
 *
 * IMPORTANT: Please note that cleanly closing a Vert.x instance will not cause failover to occur, e.g. `CTRL-C`
 * or `kill -SIGINT`
 *
 * You can also start _bare_ Vert.x instances - i.e. instances that are not initially running any verticles, they
 * will also failover for nodes in the cluster. To start a bare instance you simply do:
 *
 * [source]
 * ----
 * vertx run -ha
 * ----
 *
 * When using the `-ha` switch you do not need to provide the `-cluster` switch, as a cluster is assumed if you
 * want HA.
 *
 * NOTE: depending on your cluster configuration, you may need to customize the cluster manager configuration
 * (Hazelcast by default), and/or add the `cluster-host` and `cluster-port` parameters.
 *
 * === HA groups
 *
 * When running a Vert.x instance with HA you can also optional specify a _HA group_. A HA group denotes a
 * logical group of nodes in the cluster. Only nodes with the same HA group will failover onto one another. If
 * you don't specify a HA group the default group `+++__DEFAULT__+++` is used.
 *
 * To specify an HA group you use the `-hagroup` switch when running the verticle, e.g.
 *
 * [source]
 * ----
 * vertx run my-verticle.js -ha -ha-group my-group
 * ----
 *
 * Let's look at an example:
 *
 * In a first terminal:
 *
 * [source]
 * ----
 * vertx run my-verticle.js -ha -hagroup g1
 * ----
 *
 * In a second terminal, let's run another verticle using the same group:
 *
 * [source]
 * ----
 * vertx run my-other-verticle.js -ha -hagroup g1
 * ----
 *
 * Finally, in a third terminal, launch another verticle using a different group:
 *
 * [source]
 * ----
 * vertx run yet-another-verticle.js -ha -hagroup g2
 * ----
 *
 * If we kill the instance in terminal 1, it will fail over to the instance in terminal 2, not the instance in
 * terminal 3 as that has a different group.
 *
 * If we kill the instance in terminal 3, it won't get failed over as there is no other vert.x instance in that
 * group.
 *
 * === Dealing with network partitions - Quora
 *
 * The HA implementation also supports quora. A quorum is the minimum number of votes that a distributed
 * transaction has to obtain in order to be allowed to perform an operation in a distributed system.
 *
 * When starting a Vert.x instance you can instruct it that it requires a `quorum` before any HA deployments will
 * be deployed. In this context, a quorum is a minimum number of nodes for a particular group in the cluster.
 * Typically you chose your quorum size to `Q = 1 + N/2` where `N` is the number of nodes in the group. If there
 * are less than `Q` nodes in the cluster the HA deployments will undeploy. They will redeploy again if/when a
 * quorum is re-attained. By doing this you can prevent against network partitions, a.k.a. _split brain_.
 *
 * There is more information on quora http://en.wikipedia.org/wiki/Quorum_(distributed_computing)[here].
 *
 * To run vert.x instances with a quorum you specify `-quorum` on the command line, e.g.
 *
 * In a first terminal:
 * [source]
 * ----
 * vertx run my-verticle.js -ha -quorum 3
 * ----
 *
 * At this point the Vert.x instance will start but not deploy the module (yet) because there is only one node
 * in the cluster, not 3.
 *
 * In a second terminal:
 * [source]
 * ----
 * vertx run my-other-verticle.js -ha -quorum 3
 * ----
 *
 * At this point the Vert.x instance will start but not deploy the module (yet) because there are only two nodes
 * in the cluster, not 3.
 *
 * In a third console, you can start another instance of vert.x:
 *
 * [source]
 * ----
 * vertx run yet-another-verticle.js -ha -quorum 3
 * ----
 *
 * Yay! - we have three nodes, that's a quorum. At this point the modules will automatically deploy on all
 * instances.
 *
 * If we now close or kill one of the nodes the modules will automatically undeploy on the other nodes, as there
 * is no longer a quorum.
 *
 * Quora can also be used in conjunction with ha groups. In that case, quora are resolved for each particular
 * group.
 *
 * == Security notes
 *
 * Vert.x is a toolkit, not an opinionated framework where we force you to do things in a certain way. This gives you
 * great power as a developer but with that comes great responsibility.
 *
 * As with any toolkit, it's possible to write insecure applications, so you should always be careful when developing
 * your application especially if it's exposed to the public (e.g. over the internet).
 *
 * === Web applications
 *
 * If writing a web application it's highly recommended that you use Vert.x-Web instead of Vert.x core directly for
 * serving resources and handling file uploads.
 *
 * Vert.x-Web normalises the path in requests to prevent malicious clients from crafting URLs to access resources
 * outside of the web root.
 *
 * Similarly for file uploads Vert.x-Web provides functionality for uploading to a known place on disk and does not rely
 * on the filename provided by the client in the upload which could be crafted to upload to a different place on disk.
 *
 * Vert.x core itself does not provide such checks so it would be up to you as a developer to implement them yourself.
 *
 * === Clustered event bus traffic
 *
 * When clustering the event bus between different Vert.x nodes on a network, the traffic is sent un-encrypted across the
 * wire, so do not use this if you have confidential data to send and your Vert.x nodes are not on a trusted network.
 *
 * === Standard security best practices
 *
 * Any service can have potentially vulnerabilities whether it's written using Vert.x or any other toolkit so always
 * follow security best practice, especially if your service is public facing.
 *
 * For example you should always run them in a DMZ and with an user account that has limited rights in order to limit
 * the extent of damage in case the service was compromised.
 *
 * == Vert.x Command Line Interface API
 *
 * include::cli.adoc[]
 *
 * == The vert.x Launcher
 *
 * The vert.x {@link io.vertx.core.Launcher} is used in _fat jar_ as main class, and by the `vertx` command line
 * utility. It executes a set of _commands_ such as _run_, _bare_, _start_...
 *
 * === Extending the vert.x Launcher
 *
 * You can extend the set of command by implementing your own {@link io.vertx.core.spi.launcher.Command} (in Java only):
 *
 * [source, java]
 * ----
 * &#64;Name("my-command")
 * &#64;Summary("A simple hello command.")
 * public class MyCommand extends DefaultCommand {
 *
 *   private String name;
 *
 *   &#64;Option(longName = "name", required = true)
 *   public void setName(String n) {
 *     this.name = n;
 *   }
 *
 *   &#64;Override
 *   public void run() throws CLIException {
 *     System.out.println("Hello " + name);
 *   }
 * }
 * ----
 *
 * You also need an implementation of {@link io.vertx.core.spi.launcher.CommandFactory}:
 *
 * [source, java]
 * ----
 * public class HelloCommandFactory extends DefaultCommandFactory<HelloCommand> {
 *   public HelloCommandFactory() {
 *    super(HelloCommand.class);
 *   }
 * }
 * ----
 *
 * Then, create the `src/main/resources/META-INF/services/io.vertx.core.spi.launcher.CommandFactory` and add a line
 * indicating the fully qualified name of the factory:
 *
 * ----
 * io.vertx.core.launcher.example.HelloCommandFactory
 * ----
 *
 * Builds the jar containing the command. Be sure to includes the SPI file
 * (`META-INF/services/io.vertx.core.spi.launcher.CommandFactory`).
 *
 * Then, place the jar containing the command into the classpath of your fat-jar (or include it inside) or in the `lib`
 * directory of your vert.x distribution, and you would be able to execute:
 *
 * [source]
 * ----
 * vertx hello vert.x
 * java -jar my-fat-jar.jar hello vert.x
 * ----
 *
 * === Using the Launcher in fat jars
 *
 * To use the {@link io.vertx.core.Launcher} class in a _fat-jar_ just set the `Main-Class` of the _MANIFEST_ to
 * `io.vertx.core.Launcher`. In addition, set the `Main-Verticle` _MANIFEST_ entry to the name of your main verticle.
 *
 * By default, it executed the `run` command. However, you can configure the default command by setting the
 * `Main-Command` _MANIFEST_ entry. The default command is used if the _fat jar_ is launched without a command.
 *
 * === Sub-classing the Launcher
 *
 * You can also create a sub-class of {@link io.vertx.core.Launcher} to start your application. The class has been
 * designed to be easily extensible.
 *
 * A {@link io.vertx.core.Launcher} sub-class can:
 *
 * * customize the vert.x configuration in {@link io.vertx.core.Launcher#beforeStartingVertx(io.vertx.core.VertxOptions)}
 * * retrieve the vert.x instance created by the "run" or "bare" command by
 * overriding {@link io.vertx.core.Launcher#afterStartingVertx(io.vertx.core.Vertx)}
 * * configure the default verticle and command with
 * {@link io.vertx.core.impl.launcher.VertxCommandLauncher#getMainVerticle()} and
 * {@link io.vertx.core.impl.launcher.VertxCommandLauncher#getDefaultCommand()}
 * * add / remove commands using {@link io.vertx.core.impl.launcher.VertxCommandLauncher#register(java.lang.Class)}
 * and {@link io.vertx.core.impl.launcher.VertxCommandLauncher#unregister(java.lang.String)}
 *
 * === Launcher and exit code
 *
 * When you use the {@link io.vertx.core.Launcher} class as main class, it uses the following exit code:
 *
 * * {@code 0} if the process ends smoothly, or if an uncaught error is thrown
 * * {@code 1} for general purpose error
 * * {@code 11} if Vert.x cannot be initialized
 * * {@code 12} if a spawn process cannot be started, found or stopped. This error code is used by the `start` and
 * `stop` command
 * * {@code 14} if the system configuration is not meeting the system requirement (shc as java not found)
 * * {@code 15} if the main verticle cannot be deployed
 *
 *
 */
@Document(fileName = "index.adoc")
@io.vertx.codegen.annotations.ModuleGen(name = "vertx", groupPackage = "io.vertx")
package io.vertx.core;

import io.vertx.docgen.Document;