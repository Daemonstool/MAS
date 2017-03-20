---
layout: default
---

This project is done for the course Multi-Agent Systems given at the University of Groningen. Any questions regarding this project should be directed to r.van.dijk.16@student.rug.nl, h.maathuis@student.rug.nl and j.doornkamp@student.rug.nl. It is also possible to construct a GitHub issue.

# [](#header-1)Introduction
Welcome to the work-in-progress report and access point for our "Exploding Kittens"-related project. There will not be a lot of report as of now, as the project is not finished, but we will provide enough information for you to be able to view our progress.

In this project, we want to model the game ["Exploding Kittens"](https://www.explodingkittens.com/) and represent the knowledge of the different players in Kripke models. We already succeeded in the first part, as an open source project [Exploding Ketchup](https://github.com/Mikunj/Exploding-Ketchup) provided much of the game in code. However, we decided to add a few cards that enhance the use of knowledge in the game, such as:
* a card that has players pass a card on to their neighbours;
* a card that allows players to change the order of the central stack;
* a card that allows players to see one card from a chosen opponent;
* a card that allows players to see three cards from a chosen opponent;
Currently only the last card is implemented.

To model the Kripke model of the knowledge in a game we created a Java application relying heavily on the [GraphStream](http://graphstream-project.org/) library. This library provided all the essentials for representing the graphs that Kripke models essentially are and as an added bonus allows for some neat real-time rendering. As our goal is to view the knowledge in a game as it is played, having the model update in parallel to the game being played is very nice.

The Java application is an independent part that remotely connects to the game and receives messages from the game as it runs. These messages are used to update the model.

# [](#header-2)Running the Game
A preview of the game is playable [here](https://mas-ek.herokuapp.com/). You can sign in with any username to your liking, and start a game. Find someone else to sign in, and they can join the game you started. If you can't find anyone else right now, you can also sign in in two different tabs and play against yourself.

The Java application needs to be downloaded separately [here]. Once downloaded, extract the zipped folder somewhere and run the executable JAR. Keep the `stylesheet.css` in the same folder as it defines the makeup of the graph. The JAR can be run as an executable independently, but it is encouraged you start it in a terminal (using `java -jar CoolGuys.jar`) as this will give you the debug messages that of right now are our main method of showing results. Naturally, you must have Java installed for this to work. The project was developed in Java 8.

When running the Java application there are two options. You can either connect to the already running server hosted at [https://mas-ek.herokuapp.com/] (https://mas-ek.herokuapp.com/) or you can download the repository and host the server yourself after which you can connect to the local server. When choosing the latter option, note that you are connecting to localhost on port 3000. Either way, once you are connected to a server messages from the Exploding Ketchup game will stream to the Java application which represent the actions of players while they are playing the game.

# [](#header-3)Results so far
As you will have seen if you have done the above, the terminal will print some code representing each action by players. There is even some prediction going on as players use the "nope" card, which cancels earlier played cards. You can also see a simple model with three states and some relations in the window opened by the Java application. Sadly, this is really but an example model and the actions being sent to the Java application are not connected yet. This is because this is by far the largest part of the work: making sure each action updates the model in the correct way. We have set up everything up to the point that we could solely focus on this, and that is where we stand right now.

We look forward to continuing this project. See you in a few weeks!
