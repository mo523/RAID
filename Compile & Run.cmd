@echo off
set /P compile="(D)ownload, compile & run or just (R)un? "
set /P program="(R)AID Server, (C)lient, (S)lave? "
if "%compile%" == "D" (
rmdir raid /s /q
git clone https://github.com/mo523/RAID.git
cd raid
cd src
javac RAID/*.java
cd ../..
)
cd RAID/src
cls
if "%program%" == "R" (
java RAID/RAID_Server
) else (
   if "%program%" == "C" (
	java RAID/Client
   ) else (
	java RAID/Slave
   )
)
@pause
