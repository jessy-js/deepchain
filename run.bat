taskkill /f /im java.exe
call gradlew.bat clean deployNodes
cd build/nodes
call runnodes.bat
cd ../..