# Project Context: MPro-Town-Planning

## Overview
"God's Town Planning Simulation Game" (神の街づくりシミュレーションゲーム).
This is a Java-based desktop simulation game where the player acts as a god managing a town. The core gameplay involves managing resources ("Souls"), constructing buildings, and handling resident life cycles and events.

## Technical Stack
*   **Language:** Java 25 (Toolchain configured)
*   **Build System:** Gradle 9.2 (Wrapper included)
*   **GUI Framework:** Java Swing (inferred from `GameWindow`)
*   **Testing:** JUnit 5 (Jupiter)
*   **Key Libraries:** Guava

## Project Structure
The project follows a standard Gradle application structure with a Model-View-Controller (MVC) and Strategy pattern hybrid architecture.

### Key Directories (`app/src/main/java/io/github/sasori_256/town_planning/`)
*   **`model/`**: Contains the core game data and logic.
    *   `GameModel.java`: The central hub holding the game state (Map, Entities, Souls, Time). Acts as the `GameContext`.
    *   `GameMap.java`: Manages the grid-based world.
    *   `GameObject.java`: Represents entities in the game. Uses strategies for behavior.
    *   `strategy/`: Implementations of game logic (e.g., `PopulationGrowthStrategy`, `MeteorDisasterStrategy`).
*   **`view/`**: Handles rendering and UI.
    *   `GameWindow.java`: The main application window.
*   **`controller/`**: Manages user input.
    *   `GameController.java`: Bridges Input and Model.
*   **`core/`**: Core engine components.
    *   `GameLoop.java`: Fixed time-step game loop (60 FPS target).
    *   `EventBus.java` (`event/`): Decouples components via a publish-subscribe system.
    *   `GameContext.java`: Interface exposing game state to entities.

## Architecture Highlights
*   **Game Loop:** A custom `GameLoop` class manages the update (tick) and render cycles independently.
*   **Entity Logic:** Uses a Strategy pattern. `GameObject`s delegate behavior to `UpdateStrategy` and rendering to `RenderStrategy`, allowing flexible entity definitions without deep inheritance hierarchies.
*   **Event System:** An `EventBus` is used for communication between the Model, View, and other components (e.g., `EventType.SOUL_HARVESTED`, `EventType.DAY_PASSED`).
*   **Concurrency:** The `GameModel` uses `CopyOnWriteArrayList` for entities to handle concurrent access from the update loop and the rendering thread.

## Development Commands

### Prerequisites
*   JDK 25 (Gradle toolchain should handle provisioning via Foojay resolver)

### Build & Run
*   **Run Application:**
    ```bash
    ./gradlew run
    ```
*   **Run Tests:**
    ```bash
    ./gradlew test
    ```
*   **Clean Build:**
    ```bash
    ./gradlew clean build
    ```

## Development Conventions
*   **Naming:** standard Java conventions (CamelCase).
*   **Comments:** Japanese comments are present in the source code, explaining key logic (e.g., `AtomicBoolean` usage).
*   **Magic Numbers:** Some constants are defined in `Constants.java`, but `GameLoop` and `GameModel` currently contain some magic numbers (TODO: refactor).
