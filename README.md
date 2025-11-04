# TeamScraps.md 

Snake Game Project
A classic Snake game built in Java, developed as a collaborative group project.

Project Description
This is a graphical implementation of the classic Snake game. The player controls a snake, guiding it around the playfield to eat food. Each time the snake eats a piece of food, it grows longer. The game ends if the snake collides with the walls or with its own body.


TEAM ROLES:

Leader: Chloe Vegiga (screen/UI)

Sub-leader: Ashley Lee (sound and animation)

Developer
1. Melih Emir (input & event)
2. Ahmad Najmi (core and github manager)
3. Emmitt Aguirre (snake development)
4. Damisola Talabi (score & file manager)
5. Muhammad Haziq (environment & collision)

Member/	Role	                        	Key Responsibilities &                                                          Folder Focus     Files
1. Project Lead / Core Developer	:  Oversees full project structure, Git management, and core game flow.	              engine/       	Core.java, GameState.java
2. Input & Event Developer: Implements user input detection and control mapping.	                                      engine/	        InputManager.java
3. Snake Logic Developer:	Builds and maintains the snake's behavior, movement, and rendering.	                          entity/	        Snake.java, Segment.java
4. Environment & Collision Developer:	Manages game world elements and collision detection.	                            entity/	        Food.java, CollisionHandler.java
5. Screen/UI Developer:	Designs and implements all visual screens and transitions.	                                    screen/	        MenuScreen.java, GameScreen.java, GameOverScreen.java
6. Sound & Animation Developer:	Adds audio feedback and visual animations.	                                            animations/, engine/	  SoundManager.java, Animation Classes
7. Score & File: Manager Developer	Handles scoring, high scores, and persistent data storage.	                        engine/	        Score.java, FileManager.java


## Team Requirements: 

The overall project requirements that we're responsible for include redesigning the game to include two ships and make it more challenging for the players to work together. We'd handle the two input settings to work at the same time without conflicting with one another, and manage the collision between the two players when onscreen at the same time.


## Detailed Requirements: 
- Handle input and coordinate controls so that both players may use the same keyboard at the same time
- Manage the way the two ships will interact when onscreen and whether they'll conflict with each other (friendly fire?)
- Increase the speed of game mode to have the players work as a team against mobs
- The creation of two spaceships and have them separately co-exist on the same screen and be able to distinguish from each other    
- Decide between two health bars/one big health bar - co-op vs versus?

## Dependencies on Other Teams:
- Engine -> Najmi, Melih, Ashley
- Entity -> Emmitt, Haziq
- Screen -> Chloe, Dami





TEAM MEMBERS:
[Chloe Vegiga](https://github.com/k0eeee/k0eeee), [이서연/Ashley Lee](https://github.com/ashlsylee), [Damisola Talabi](https://github.com/damisolatalabi), [Melih Emir](https://github.com/Emir-M10), [Ahmad Najmi](https://github.com/Jeminana), [Emmitt Aguirre](https://github.com/EmmittAguirre), [Muhammad Haziq](https://github.com/cyckerz)                                                                              |

