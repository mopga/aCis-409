![TvEdrvu.jpg](https://i.imgur.com/TvEdrvu.jpg)

# Changeset Rev 409 (3301) 

---

## MariaDB 10.9 and Java 21

SCH, Castles, IU, Npc movement, Drop rate rework, Bugfixes, Organization

SCH - Ty Bandnentans for the good work

All SCHs are normally fixed and working.\
CH decorations are reworked (they got their own XML, related Configs are dropped).\
Aden CHs got the Wyvern option, as stated in Patch Notes.\
Ty Denzel for report.\
CH features levels are corrected.\
Ty Denzel for report.\
Fix NPC clan crest issue.\
Castles

Keep Castle next tax percent instead of resetting it to 0.\
Implementation of missing variables over Castle vault management.\
Ty KejbL for report.\
Remove doublons over Residence npcIds.\
Add npcId 35552 HTMs.\
Ty Dev for report.\
Fix the tax income calculation.\
IU

Add PAPERDOLL as potential location for IU (fix gm enchant, arrows consumption).\
Ty Roko91 for report.\
Fix weight calculation over login.\
Ty KejbL for report.\
Fix inventory update upon teleport (BSOE consumption).\
Ty Dev for report, La Roja for fix.\
Fix inventory weight upon NPC buy.\
Npc movement

MOVE_TO desire is cleansed over onEvtBlocked, avoiding to build infinite desire.\
Don't add MOVE_TO desire if given Location isn't reachable.\
FLEE doesn't call event upon onEvtBlocked (that event means something wrong happened, it's then impossible to trigger regular FLEE checks).\
Use Location#equals in few scripts.\
NpcAI#thinkFollow cleanup (-8 arrays, -24 List#get, -8 distance2D).\
Drop rate rework

Main idea is to iterate each category X times, as if X monsters were killed.\
No % re-calculation or whatever, it's plain and simple.\
x50 means you got the calculated drops of 50 monsters.\
Avoid to generate IntIntHolder, manage the dropspoil using a MapInteger, Integer instead, which also allow to merge similar itemIds.\
Categories % are tested no matter if drop or spoil, which allow levelMultiplier to be properly applied.\
Monster#dropItem is moved to Npc#dropItem, which allow any Npc to drop an item and avoid cast.\
killer is now part of all Npc#dropItem, meaning the item is properly item protected.\
Bugfixes

Fix a ClassCastException over Quest#onClanAttacked.\
Fix a NegativeSizeArrayException upon client logging.\
Drop few logging errors related to invalid client attempts.\
Fix NPE over Q635.\
Fix PDAM calculation prior to rev 399 physical attackskill split.\
Ty Yoska for report.\
Fix TradeList automatic title cleanup.\
Ty Denzel for reportfix.\
Add back missing Config.\\PARTY_XP_CUTOFF_METHOD none option.\
Ty CUCU23 for reportfix.\
Fix upper roof NPCs.\
Ty Bandnentans for fix.\
Fix Seven Signs individual stone contribution method.\
Ty Dev for report.\
Fix Q372 reward table and drop rate.\
Ty Dev for report.\
Q348 now distributes drops as party-random, despite the client info.\
Ty Denzel for report.\
Fix Benom teleports out.\
Hardcode other inout Locations.\
Ty Dev for report.\
Don't show Crystallize icon on inventory for Bounty Hunters (was an addition of CT1 GP1).\
Ty Bandnentans for report.\
Replace weightPenalty for weightLimit over skills XMLs.\
Ty Dev for reportfix.\
When Heroes participate in a raid against Antharas, Valakas, and Baium, the boss monster has a chance to shout out the Hero characters’ names.\
Ty deekay for fix.\
Fix isRaidBoss implementation (a raidboss minion without master was considered raidboss).\
Fix Nurse Ants not healing the Queen Ant larva.\
Fix a SQLException over Olympiad server startup.\
Fix a SQLException over Clan member removal (since clan privs rework).\
Fix default 30169 npcId HTM.\
Ty Bandnentans for report.\
Fix Festival Guide missing rift option.\
Ty Denzel for reportsemi-fix.\
Few dwarvengeneral manufacture fixes
Add the missing max recipe integrity check.\
Upon shop fail, call back the manage window.\
Upon shop fail, don't cleanup the manufacture list.\
Upon shop success, cleanup the reverse manufacture list (successful general shop resets dwarven, successful dwarven shop resets general).\
Organization

Rework HtmCache and CrestCache to use NIO.\
Move CrestType to enums.\
Implementation of WorldObject#forEachKnownType WorldRegion#forEachType & forEachRegion - Avoid List overhead in numerous popular locations (notably broadcastPacket or region checks - which are done on every knownlist check).\\

Rework SkillList packet, it is now handled as other packets.\
Delete Player#sendSkillList method.\
Delete DeadlockDetector class and related configs.\
Add GameServer#isServerCrash, based on LaRoja implementation - without uses, for now.\
Move IPv4Filter class to commons.\\network, delete net.\\sf.\\l2j.\\util package.\
Add DefaultSeeRange config, use the retail value 450 instead of 400.\
Add more records, ty LaRoja for the merge request.\
Delete unused dimensionalRift.\\xml.\
Few ItemContainer optimizations.\
Rename all ocurrences of adenas to Adena.\
SonarLint UCDetector fixes
Drop MathUtil#limit, use Math#clamp instead (introduced in JDK21).\
Drop following unused Configs FS_TIME_ENTRY, FS_TIME_END, RAID_MINION_RESPAWN_TIMER.\
Few public protected private edits.\
A lot more to come.\
switch cases are merged (introduced in JDK12).\
Boolean object is compared to Boolean.\\FALSETRUE, not directly tested as a boolean.\
String#replaceAll is replaced with String#replace when a regex pattern isn't involved.\
Generate few records Sequence, TutorialEvent.\
Few class-based variables are now local.\
Use HashMap.\\newHashMap instead of new HashMap when the capacity is known (static final maps).\
HashMap.\\newHashMap avoids to set 0.\\75 capacity when it's not needed.\
LogRecord record is renamed LogRecord logRecord, due to record being now a keyword.\
Use proper Singleton pattern for instance type (notably listeners).\
Generate private constructors calling IllegalStateException for utility classes.\
httpsgitlab.\\comTryskellacis_public

Tryskell words

Hellllllo everyone ! Good news for some, bad news for others aCis was, once again, leaked.\\

It wouldn't be that problematic if it was old content, or even last revision 408 ; but this time, the whole content of under developmentmaster branches was leaked out.\
One pack project is actually reselling NEXT revision content - before even being announced on aCis forums.\\

The moleleaker is still part of customers, and is still capable to leak data, at the date I speak about.\\

Since I'm on a joyful mood, following events will occur

PIRATE PLANK MINIGAME

Since we got restricted amount of donators (we're actually 13 on sources counting developers, over all), it's not extremely hard to actually delete the mole ; few ppl are actually matching the description, and a list can be easily generated based on time leak, potential country, contributions,.\\.\\.\\

Which basically end with that representation

leak-plank.\\jpg

How will it work Everytime a new leak will occur, the following donator on the board will jump out of ship (and Talking Island waters are kinda cold).\
Since I'm not a monster and got principles, I will send back spent money for the non-granted months to the kicked dude.\
You won't be added back to the sources, anytime.\
The game ends when the mole is dropped out, or when I'm alone with my most loyal peeps around.\\

CONTRIBUTION

Leftover donators will have to contribute to the pack, being reports or code edits.\
Silent people won't be renewed anymore solely based on money.\\

In the same order of idea, I will now request a minimum of 100 cookies contribution before accepting any new ppl on the gitlab - which anyway won't be hard to do if you're a minimum invested into the pack.\\

If you understood the concept, free ppl can access gitlab sharing for 200 cookies contribution (100+100), and donators can access with 100 cookies + 200€.\
Regarding monthly contribution, there are no special numbers to achieve, stay active and you will stay.\\

PUBLIC REVISION WILL STEP UP

Next rev 409 will be exceptionally released as public revision.\
This revision got unique reworks, notably AI (L2OFF GF 11) and pathfind systems (up to 100 times faster, see #for-your-eyes-only over aCis discord for screens proofs).\\

This revision got a lot of new content, and is far ahead of any other L2J pack in terms of AI fidelity with L2OFF - even the costier.\\

ENDING WORD

Thanks to all loyal people who have, will or currently support this pack - one of the very few to offer unique reworks.\\

L2J community, as a whole, unfortunately never stepped up or shined by its cleverness or integrity - and is more preocuppied to add poorly written customs over quality leaked sources.\\

The olympic medal goes to the poop-eater project owner applying straight leak, not even knowing what exactly is the changeset content (because yes, he doesn't know).\\.\\.\
It's actually sad real people follow and pay for your work, but well, good job surfing on my own merits, I guess.\
Maybe one day you will go out of my shadows, and make your own path.\
That's the best I can wish you.\\

So, my thanks to the few beacons of light in this mere pool of shadows.\
That's essentially for you (and for my own pleasure, ofc) I continue to work on this hobbyist project - started almost 14 years ago.\\

Editado Setembro 9 por mikado
