# Spare Maze (Minecraft 1.21.11 Fabric)

**Developer:** Ahmed  
**Minecraft Version:** 1.21.11  
**Mod Loader:** Fabric  
**Java Version:** Java 21  

Spare Maze is a lightweight, vanilla-styled client-side combat utility mod for Minecraft Java Edition 1.21.11.

## Features

1. **Pearl Catching System**
   - Strictly tracks only the local player's own thrown `EnderPearlEntity` via UUID ownership verification.
   - Real-time projectile trajectory simulation accounting for gravity (`0.03`) and drag (`0.99`).
   - Automated Wind Charge hotbar selection, intercept calculation, temporary camera rotation lock during active catch, and automatic hotbar slot restoration.
2. **Mace Switching Systems**
   - Independent keybinds and toggles for `Sword -> Mace`, `Axe -> Mace`, and `Spear -> Mace`.
   - Zero artificial delay state-driven hotbar swaps that restore the original slot immediately after attacking.
3. **Axe Instant Slam**
   - Validates if the target is actively blocking with a shield.
   - Executes `Axe -> Shield Break Verification -> Mace -> Hit -> Restore Slot`.
4. **Spear Lunge System (1.21.11)**
   - Validates 1.21.11 Spear Lunge requirements (Lunge enchantment, hunger > 6, not in water, not gliding).
   - Executes `Current Slot -> Spear Slot -> Jab/Lunge Input -> Restore Slot` on a single keybind press.
5. **Native Vanilla GUI & Mod Menu Integration**
   - Full integration with Mod Menu and Minecraft's native Controls screen.
   - Scrollable vanilla-styled configuration screens that adapt to all resolutions and GUI scales.

## Building from Source

```bash
./gradlew build
```

The compiled mod JAR is generated at: build/libs/spare-maze-1.0.0.jar
