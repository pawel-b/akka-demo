# akka-demo

Tools needed:
- JDK
- Scala 2.11.x from http://www.scala-lang.org/
- SBT from: http://www.scala-sbt.org/

Usage:
- clone the repo
- run sbt in main folder
- invoke "run" command
- choose the demo
- explore, change the code and play around

Demos available: 
- SimpleActors - just Hello World.
- FlickrTagsApp - Flickr tag count example, how to create actors to run tasks concurrently.
- PizzaDemoBasics - Akka Router demo, how to use pools of actors.
- MinionBecomeApp - become example, how to switch between happy/afraid state of a minion.
- MinionFsmDemo - FSM - when you need actors with more states.
- BasicTransformation - basic Akka(reactive) streams example.
- CheckMaxThreads - a simple test to check how many threads can we create.
- CheckMaxActors - can we create 3M of actors?
