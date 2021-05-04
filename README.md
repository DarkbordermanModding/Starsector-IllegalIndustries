# IllegalIndustries

Add overpowered industries to enchance gameplay experience in Starsector.

## Mod archived

### The mod has been integrated into [Starsector-IndustryOverhaul](https://github.com/DarkbordermanModding/Starsector-IndustryOverhaul)

## Prerequisite

| Name | Version |
| --- | --- |
| gradle | 4.4.1 |
| jq | 1.6 |

### Build jar

1. Run
```
gradle build
```
Will build jar files under `jars/`

### Run game to test

1. Put project folder under `<game folder>/mods/`

2. Run
```
gradle run
```
To test gameplay

### Release mod

1. Run
```
gradle release
```
Will create `<project name>-<version>.zip` under `bin/`

### Repository cleanup

1. Run
```
gradle clean
```
Will cleanup `bin/` and `jars/`
