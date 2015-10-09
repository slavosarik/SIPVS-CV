# SIPVS-CV

##Requirements:
- Java JDK 7 http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html
- Apache Maven 3 http://tux.rainside.sk/apache/maven/maven-3/3.3.3/binaries/apache-maven-3.3.3-bin.zip
- Eclipse IDE for Java EE Developers (odporúčam)

Návod na nastavenie enviroment variables pre Apache Maven: https://maven.apache.org/install.html

##Vytvorenie projektu (pre Eclipse):
- `git clone https://github.com/slavosarik/SIPVS-CV.git`
- `cd SIPVS-CV`
- `mvn eclipse:eclipse` - vytvorenie Eclipse štruktúry projektu 
- spustiť Eclipse
- Import -> General -> Import existing project into Workspace
- zvoliť project z adresára
- kliknúť pravým na projekt -> Configure -> Convert to Maven project

##Vytvorenie jar archívu:
- `mvn clean install`
- `cd target`

alebo
-  kliknúť pravým na projekt -> Run as -> Maven install
