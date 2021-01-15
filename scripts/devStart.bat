@echo off
echo --------------------------------------------
echo            Dev Shell
echo --------------------------------------------
echo Working Directory is : %CD%
echo Script Directory is  : %~dp0
cd %~dp0\..
echo --------------------------------------------
echo        OPEN PROCESSES          
echo --------------------------------------------
ps OpenFiles /local on
ps Set-ExecutionPolicy RemoteSigned
echo --------------------------------------------
echo        MOD DIRECTORY FILE CONTENTS          
echo --------------------------------------------
dir
echo --------------------------------------------