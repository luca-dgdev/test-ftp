Demo project for transfer file via FTP using JCraft library

# How to build

mvn clean package assembly:single

# How to run

# TEST FILE TRANSFER THROUGH INTERNAL NETWORK
java -cp test-ftp-0.0.1-SNAPSHOT-jar-with-dependencies.jar com.nexi.gft.Main2 "C:\data\documents\test.txt"
