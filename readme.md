![TvEdrvu.jpg](https://i.imgur.com/TvEdrvu.jpg)

# Installing and running

---

## Reqirements

 - Mysql - tested on 14.14
 - Java 21 - tested on openjdk version "21.0.2"

## Compile

Make distrib with Ant

```
ant clean
ant compile
ant dist
```

## Starting

1. Compile
2. Go to dist folder
3. Run ..login/startLoginServer
4. Run ..gameserver/startGameServer
5. Run ../login RegisterGameServer

## Client side

 - Use ini instruments for Interlude to change IP and port for connecting
 - Use clean Interlude client
 - for adding GM/Adm account use startSQLAccountManager from loginserver folder
 - Admin accesslevel is 7 (by default)

 All config files for server is in the *config* folder and names as %name%.properties

