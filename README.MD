# Unireg RPT Batch

Avant d'exécuter le projet les librairies suivantes sont nécessaires:
* git 
* maven 3 
* java JDK 8, 11

installer toutes les dependencies:
```mvn clean install ```
permet de générer le  unireg-rpt-batch-@version-release.zip 

##Run
* [OBLIGATOIRE]  population=PM,  Type de population : PM, PP
* [FACULTATIF] Période fiscale , valeur par défaut année courante -1
* [FACULTATIF]  mode=BENEFICE,  type d'extraction des données de référence RPT à générer.
 valeur par défaut `REVENU_ORDINAIRE` pour PP, `BENEFICE` pour PM .
 Valeurs possibles pour les PP `(REVENU_ORDINAIRE|REVENU_SOURCE_PURE|FORTUNE)`, les PM `(BENEFICE)`
* [FACULTATIF] version de ws utilisée, valeur par défaut v7.  Valeurs possibles (v6,v7) exemple -Dversion_ws=v7
version_ws=v7

***Lancement du batch à partir des scripts :***
Lancement du batch de clôture
* En local :
  * Modifier les fichier de config : unireg-metier.properties, unireg-rpt.properties, unireg.nexus.properties et unireg-web.properties
  * VM Options : -Dch.vd.unireg.propertyFolder="C:\projects\unireg-rpt-batch\config\devel" -Dlog4j.configurationFile="C:\projects\unireg-rpt-batch\config\rpt-log4j2.xml" -Dch.vd.appDir="C:\projects\unireg-rpt-batch\"
  * Program arguments : "C:\projects\unireg-rpt-batch\config\devel\unireg-metier.properties"
* En intégration :
  * TODO selon monde DevEx

