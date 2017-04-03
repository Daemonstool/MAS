---
layout: default
---

This project is done for the course Multi-Agent Systems given at the University of Groningen. Any questions regarding this project should be directed to r.van.dijk.16@student.rug.nl, h.maathuis@student.rug.nl and j.doornkamp@student.rug.nl. It is also possible to construct a GitHub issue.

  ❤ Ruben, Henry and Joost.

# [](#header-1)Introduction
Welcome to our report and access point for our "Exploding Kittens"-related project. In this project, we want to model the game ["Exploding Kittens"](https://www.explodingkittens.com/) and represent the knowledge of the different players in Kripke models. 

We already succeeded in the first part, as an open source project [Exploding Ketchup](https://github.com/Mikunj/Exploding-Ketchup) provided much of the game in code. However, we decided to add a few cards that enhance the use of knowledge in the game, such as:
* a card that has players show and pass a card on to their neighbours;
* a card that allows players to see one card from a chosen opponent;
* a card that allows players to see three cards from a chosen opponent;

To model the Kripke model of the knowledge in a game we created a Java application relying heavily on the [GraphStream](http://graphstream-project.org/) library. This library provided all the essentials for representing the graphs that Kripke models essentially are and as an added bonus allows for some neat real-time rendering. As our goal is to view the knowledge in a game as it is played, having the model update in parallel to the game being played is very nice.

The Java application is an independent part that remotely connects to the game and receives messages from the game as it runs. These messages are used to update the model.

# [](#header-2)Running the Game
An instance of the game is playable [here](https://mas-ek.herokuapp.com/). You can sign in with any username to your liking, and start a game. Find someone else to sign in, and they can join the game you started. If you can't find anyone else right now, you can also sign in in two different tabs and play against yourself.

The Java application needs to be downloaded separately [here](https://daemonstool.github.io/MAS/Final_Project.zip). Once downloaded, extract the zipped folder somewhere and run the executable JAR. Keep the `stylesheet.css` in the same folder as it defines the makeup of the graph. The JAR can be run as an executable independently, but it is encouraged you start it in a terminal (using `java -jar EK_Model.jar`) as this will give you the debug messages that of right now are our main method of showing results. Naturally, you must have Java installed for this to work. The project was developed in Java 8.

When running the Java application there are two options. You can either connect to the already running server hosted at [https://mas-ek.herokuapp.com/](https://mas-ek.herokuapp.com/) or you can download the repository and host the server yourself after which you can connect to the local server. When choosing the latter option, note that you are connecting to localhost on port 3000. Either way, once you are connected to a server a "connected" message will appear in the terminal to confirm the connection and messages from the Exploding Ketchup game will stream to the Java application which represent the actions of players while they are playing the game.

Due a large number of labels in the graph UI, we added some controls to it. To show the agents that have a certain relation, you need to select both worlds it is between. A selected node is red, and a node can be selected simply by clicking it. Note that you can also drag the nodes around, this is useful if the graph becomes "tangled", or you feel the order is counterintuitive.

# [](#header-3)Restrictions
We very quickly realised this project was much larger than the time for this project allowed. We decided to restrict ourselves to considering only the knowledge of where the exploding kittens cards are. We also possibly look at diffuse cards (which are in the player's hands instead of the stack), but it was quickly apparent that that was also not going to happen.

We also restricted the game to have only two players, while having more is technically possible. There is also a technical restriction that the server hosting the game can only host one game at a time, because otherwise the java application would need to separate the different games into different models. This is possible but not the goal of the course.

Another goal was to do continuous derivation in the model, but as the model is still very simple we restricted it to only use common knowledge rules and safe time that way. This means that there are common knowledge rules that are applied during updating of the model, but if only one agent is technically able to derive a rule (not everyone), that is so far ignored. On that note, while our epistemic logic interface is complete, operators like `K`, `M` and `Iff` are not actually used right now.

# [](#header-4)Theoretical basis
If you look at the code you will find two packages: `logic` and `model`. In the logic package, a rather straight-forward implementation of epistemic logic can be found. All of the classes in there implement the `Formula` interface, and each formula provides a method for representing the formula as a string and one for evaluating itself in a model. In the model package you'll find two classes: `Game` and `Model`. The Game class handles communication between the Kripke model and the game on the server, and we will not look at this class much for this section. The Model class is an implementation of a Kripke model, using the `GraphStream` library to represent all the states and relations between them.

A Kripke model consists of a set of worlds/states, a set of relations between those states and a truth assignment function assigning a value to each atom in each world. All these elements are implemented using some part of `GraphStream`:
* The set of world are simply the nodes in the graph. We defined our model as such that each world is simply named "w1", "w2", etc.
* The set of relations are represented by the edges of the graph. In the pre-existing implementation it was not possible to draw multiple lines between the same worlds, so you'll see in the final graph that for each directional edge the label may correspond of multiple agents rather than having one line per agent.
* The truth assignment function is defined as follows: each node has the attribute _atoms_. In this list are all the atoms that are true in this world. As a consequence, a world with an empty list of atoms has all atoms be false. This is defined this way because in a Kripke model all worlds must have a value for all atoms. The labels of the worlds in the graph UI show all atoms and their value, not just the ones that are true.
  
When a model is created, a world is created for all possible combinations of atoms' values. In our model, which has 3 atoms, this leads to 8 worlds. The three atoms are `ek1`, `ek2` and `ek3`, where `ek1` means that the first (top) card of the stack is an exploding kitten card. We only regard the top three cards because those are the only cards you can have knowledge about, because of the see the future card allowing you to see the top three cards. This world are technically all possible states and they will just float there as there are no agents yet to have knowledge.

When the game is started, the game sends the names of the players to the model and the model will add appropriate relations between worlds for all agents. The model applies CommonKnowledge rules, rules that all agents know, to determine whether some worlds are already not possible, and not make relations to those worlds as no agents will consider them possible. For example, in our implementation there are the common knowledge rules that if one card is the exploding kitten card, the others are not (because there is only one exploding kitten in a game with two players). Worlds with multiple exploding kitten cards are ignored by agents as a consequence.

# [](#header-5)Some examples

As the game is played, the game will send messages to the Game class informing it of actions in the game, and it will appropriately update the model. 

For example, when a see the future card is played, it will update all relations concerning the player that played that card. If no exploding kitten is seen, only the world {¬ek1, ¬ek2, ¬ek3} will hold. In the model this world will have a reflexive relation only for this agent. Drawing a card, causes a relations to be added to any possible world that has `ek3`, e.g. {¬ek1, ¬ek2, ek3}. Drawing more cards will extend relations even more.

Another case for the see the future cards is when an exploding kitten card is seen. Assume a player has seen it on the third card, so `ek3` is true. A world {¬ek1, ¬ek2, ek3} then only holds, so the player has a relation to this world only. Then drawing a card will shift the knowledge to {¬ek1, ek2, ¬ek3}.

Another example is when the shuffle card is played: this "throws" away all knowledge of any `ek` card. Each possible world has a relation again for each agent. The same effect is obtained by defusing an exploding kitten, since the exploding kitten card is randomly put back in the stack. Hence, also after defusing an exploding kitten, all knowledge about the exploding kitten card is thrown away and all possible relations are returned appropriately.

An interesting feature is the case when the stack size becomes small (e.g. 2 cards left), this means that the exploding kitten card has to be at `ek1` or `ek2`, thus, by means of common knowledge derivation, there are only two worlds possible and there are no relations with any other world. For example worlds with {¬ek1, ¬ek2, ¬ek3}, {¬ek1, ¬ek2, ek3} are no longer possible. The first one because there has to be an exploding kitting card at a position, the second one because it cannot be on the third position since there are only 2 cards left.

There are too many other updates possible (or combinations of the above mentioned updates) too discuss now, even with our simplified game. The logic is used periodically during updates to derivate knowledge and exclude possible worlds.

# [](#header-6)Discussion
As discussed extensively already, we had to severly cut down in the size of this project and still it turned out smaller than we had hoped. The logic interface is not used as much as we'd wanted, only common knowledge is really used to show the power of derivation. Thus, we concluded that our project serves quite well as a proof of concept and a baseline for further systems, but it does not reach its goal of modelling the knowledge in a game of exploding kittens.
