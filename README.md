# Bing Bing Wahoo
Bringing the physics of Super Mario 64(+ more) to Minecraft, one wahoo at a time.<br>
Made for ModFest 1.17.
# Features
Bing Bing Wahoo has a variety of features designed to fill your wahooing needs. To get started, craft yourself a Mysterious Cap with 5 wool in the helmet formation.<br>
The Mysterious Cap can also be worn in the hat slot when using Trinkets.
- Double / Triple Jump
    - After jumping, jump again shortly after you land to jump higher!
    - The 3rd jump in a row (Triple Jump) will make you do a flip!
    - Higher jumps will not activate when holding the jump button, it 
      must be tapped each time.
    
- Long Jump
    - Activated by quickly holding crouch and jumping while moving. 
    - Backwards long jump included!
    
- Dive
    - Triggered by clicking the attack button while sprinting. Works midair and on 
      the ground, however ground activation may be annoying, so it is configurable 
      through Mod Menu.
    - Once you land, you begin sliding on your stomach.

- Sliding
  - By diving or sneaking on a slope, you will begin to slide.
  - Both stairs and slopes from [Automobility](https://github.com/FoundationGames/Automobility) count as slopes.
  - Once you're sliding on flat ground, you can control your movement by looking around.
  - If the floor you're sliding on is in the `#bingbingwahoo:slides` tag, sliding mechanics are changed slightly, such as being able to go a longer distance.
    
- Bonking
    - Hitting a wall too hard will knock you over for a bit!
    - Disabled in creative mode.
    - Toggleable in config.
    
- Wall Jump
    - Once you jump into a wall, you have a short time to jump again, and 
      you will be sent in the opposite direction.
    - Does not activate for regular jumps by default, too annoying. However, 
      can be enabled through Mod Menu.
    
- Ground Pound
    - Crouching midair will make you do a flip and send you flying downwards!
    - Soft blocks will crumble beneath you as you plummet!
    - Gain enough velocity by falling for long enough, and the impact will 
      cause an explosion!
    - Fall damage from a Ground Pound will never be lethal.
    
- Ledge Grab
    - If you jump up to the edge of a flat block, you'll grab onto it!
    - Disables looking left and right.
    - Pressing forwards or jump will boost you up!
    - Pressing back or sneak will drop you down.
    - You can move slowly left and right across ledges.
    
- Backflip
    - If you jump while sneaking and standing still, you will do a backflip 
      and be sent backwards!
    - Could be annoying, so it can be disabled through Mod Menu.

- Cap throwing
  - When held, the cap can be thrown by clicking.
  - When worn, it can be thrown via the G key by default.
  - The thrown cap will damage any entity it hits.
  - It will also retrieve any items or experience it finds!

- Slider music disc
  - A music disc which plays a version of Slider. This version has been modified to be in the Minecraft soundfont.

# Moderation
Some of Bing Bing Wahoo's features are controllable from the server side by Gamerules. These include:
- Destructive ground pounds - `destructiveGroundPounds`
- Backwards long jumps - `backwardsLongJumps`
- Rapid Fire long jumps - `rapidFireLongJumps`
- Maximum long jump speed - `longJumpMaxSpeed`
- Long jump speed multiplier - `longJumpSpeedMultiplier`
- Mysterious Cap needed for abilities - `mysteriousCapRequired`

On top of this, destructiveGroundPounds can be overridden on a per-player basis using the command `/bingbingwahoo:setDestructionPerms`. However, this is not persistent and will be lost on world reload.

# Photosensitivity warning
The flips caused by some moves may be too much for some people. If 
this is the case for you, you can lower the Flip Speed Multiplier in the 
mod's config menu, accessed through Mod Menu. Setting it to 0 will 
disable flips completely.