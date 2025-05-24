
# 🎮 JFXMaze

**JFXMaze** is a JavaFX-based reimagination of my original terminal game C-Maze, featuring a polished GUI, user authentication, customizable settings, and rich visuals.
> [!NOTE]
> This is V3 of C-Maze now fully graphical with JavaFX, FXML layouts, and Maven support!

## 🎮 Gameplay Demo

Check out a short gameplay demo of **JFXMaze** on YouTube:  
[![JFXMaze Gameplay](https://img.youtube.com/vi/l4Rtd7CSkpI/0.jpg)](https://youtube.com/shorts/l4Rtd7CSkpI)

Watch the maze in action and see how the game plays!


---

## 🛠 Project Structure

```

JFXMaze/
├── src/
│   ├── main/
│   │   ├── java/       ← Java source files and controllers
│   │   └── resources/  ← FXML layouts, CSS, images
├── images/             ← screenshots of UI flows (.png)
│   ├── login.png
│   ├── welcome.png
│   ├── settings.png
│   ├── credits.png
│   ├── gameprompt.png
│   ├── gamescreen.png
│   ├── endgame.png
│   └── scores.png
├── pom.xml             ← Maven build & dependencies
└── README.md

````

---

## 🚀 Features & Flow

1. **Startup & Auth**  
   - `Main.java` boots the JavaFX app.  
   - `UserAuth` presents **Login / Register**.

2. **Welcome & Menu**  
   - `WelcomeController` shows your username and options: **Play**, **Settings**, **Credits**, **Logout**.

3. **Custom Settings**  
   - `SettingsController` lets you adjust: maze size, game mode, seed, audio, and export scores.

4. **Gameplay**  
   - `MazeModel` generates the maze.  
   - `MazeRenderer` draws walls, paths, bonus dots, exit, and players.  
   - `GameScreenController` handles movement (WASD / arrows), timer, move-counter, and bonus collection.

5. **Scoring & Endgame**  
   - `EndGameController` displays final stats and “Save Score” prompt.  
   - `ScoreManager` writes to `jfxmaze_scores.txt` and reloads score history.

6. **Credits**  
   - `CreditsController` lists acknowledgements and asset sources.

---

## 🖥️ Installation & Run
> [!IMPORTANT]
> This project requires Java JDK and Maven to download required libraries, build, compile and run the game.

1. **Clone & Enter**  
   ```bash
   git clone https://github.com/Mnour3593/JFXMaze.git
   cd JFXMaze


2. **Build with Maven**

   ```bash
   mvn clean package
   ```

3. **Launch**

   ```bash
   mvn javafx:run
   ```

> [!NOTE]
> Or, alternatively, open the project in your favorite Java IDE and run the Main.java class directly.
   
---

## 📸 Screenshots


   <img src="images/login.png"    width="300" alt="Login Screen" />
   <img src="images/welcome.png"  width="300" alt="Welcome Menu" />
   <img src="images/gameprompt.png" width="300" alt="Game Prompt" />
   <img src="images/settings.png"  width="300" alt="Settings" />
   <img src="images/gamescreen.png" width="300" alt="Game Screen" />
   <img src="images/endgame.png"    width="300" alt="End Game Screen" />
   <img src="images/scores.png"      width="300" alt="Scores" />
   <img src="images/credits.png"   width="300" alt="Credits" />


---

## 📈 High-Level Architecture

* **Main** → **GameController**
* **GameController** ↔ { `UserAuth`, `UIScreens`, `MazeModel`, `MazeRenderer`, `ScoreManager` }
* **UIScreens** drives FXML controllers:

  * `WelcomeController`
  * `GameScreenController`
  * `EndGameController`
  * `CreditsController`
  * `SettingsController`

---

## 🎨 Assets & Credits


**Developed** by [Mnour3593](https://linktr.ee/mnour3593)

**Music and Sound Effects**
All audio sourced from Pixabay:

* [Bonus Sound – u\_vdwj1c20kz](https://pixabay.com/sound-effects/bonus-level-up-206316/)
* [Victory Sound – freesound\_community](https://pixabay.com/sound-effects/piglevelwin2mp3-14800/)
* [Click Sound – freesound\_community](https://pixabay.com/sound-effects/mouse-click-153941/)
* [Background Music – Nicholas Panek](https://pixabay.com/music/arcade-chiptune-24291/)

**Visual Assets**
Free and AI-generated resources:

* [Textures – OpenGameArt.org](https://opengameart.org/)
* [Player Sprite – cuddle-bug on Itch.io](https://cuddle-bug.itch.io/apocalypse)
* Background – AI-generated imagery

**Frameworks and Libraries**

* [JavaFX](https://openjfx.io/)

**Special Thanks**

* Prof. Dr. Ahmet Gürhanlı, for insightful lectures and guidance
* Claude Sonnet and Google Gemini AI, for helping structure ideas
* Friends and peers for valuable feedback and support

© 2025 Mnour3593 – Open Source ([MIT License](LICENSE).)

---
