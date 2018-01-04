# HeRos Android Application

![heros_presentation](https://user-images.githubusercontent.com/7831572/34561379-f82c6d30-f149-11e7-9297-d10f649d7ee5.gif)

## How does a HeRos work ? 

A HeRos can move to any direction thanks to his Pololu 3pi base, letting him cross the battlefield easily.

The firing system is based on IR emissions, a HeRos can shoot long range direct shots and even emit 360° short-range shots. We decided to add some new components for the beauty of the game, such as lasers to see where the HeRos is aiming and decorative LEDs to see the team of a HeRos and if he has been shot.

During the game, the different HeRos are split into two teams that fight each other until all the HeRos of a team are destroyed (meaning they don't have any more health points). A deathmatch mode is also available, meaning every HeRos is alone and has to kill the other HeRos to win the game. 

## How to control him ?

A HeRos is connected to the user's smartphone via Wi-fi, and transmits the video stream to the user's smartphone. The smartphone is also connected to a game server that deals with the virtual characteristics of all the HeRos and rules the game.

The Android application offers the possibility to create different games, such as Deathmatch (free-for-all game) or Team Deatchmatch. Other users can then join the game to start playing.

The game screen of the application includes the video stream acquired by the HeRos, buttons to emit direct and global shots and a boost button that will increase the speed of the HeRos for a short time. 

Information concerning the current game, such as life points of the HeRos in the game, shots received and teams is also displayed on the screen.

## Inside the App
 - Local connection to HeRos (Same Wifi)
 - Connection to a remote Parse Server (For robot identity and game information)
 - Control to change game/identity
 - Control for gaming: show camera from the HeRos and commands for controlling the Robot
 
 ![screenshot_menu png](https://user-images.githubusercontent.com/7831572/34564939-20e37534-f159-11e7-9ff7-c530c962bf9f.jpg)
 ![screenshot_robot_control.png](https://user-images.githubusercontent.com/7831572/34564936-1eaef8d8-f159-11e7-9d58-ba3043c892df.jpg)
 
## Notes
- The app needs HeRos for working
- The app needs a Parse Server
- Made by Team HeRos (Alexis Polti, Morikko, Charles Parent, BigFatFlo, and Samuel Tardieu)

More information on [HeRos](https://www.hackster.io/team-heros/heros-4b0c73)
