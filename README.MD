# Unireg RPT Batch

Avant d'exécuter le projet les librairies suivantes sont nécessaires:
* git 
* maven 3 
* java JDK 8, 11

installer toutes les dependencies:
```mvn clean install ```
permet de générer le  unireg-rpt-batch-@version-release.zip 

##Run
Le batch peut être exécuté soit à partir de son IDE favori

***Lancement du batch à partir de son IDE:***

```
main class: ch.vd.unireg.config.BatchRptRunnerApp
VM options: -Dch.vd.appDir= rpt
            -Dunireg-rpt-web.properties.path=rpt\config\devel\unireg-web.properties
            -Dunireg-rpt-nexus.properties.path=rpt\config\devel\unireg-nexus.properties
            -Dunireg-rpt.properties.path=rpt\config\devel\unireg-rpt.properties
            -Dunireg-rpt.credentials.path=rpt\config\devel\credentials.properties
            -Dlog4j.configurationFile=rpt\config\rpt-log4j2.xml
            
program arguments: -Dpopulation=PM  -DperiodeFiscale=2017 -Dmode=BENEFICE
```
* [OBLIGATOIRE]  population=PM,  Type de population : PM, PP
* [FACULTATIF] Période fiscale , valeur par défaut année courante -1
* [FACULTATIF]  mode=BENEFICE,  type d'extraction des données de référence RPT à générer.
 valeur par défaut `REVENU_ORDINAIRE` pour PP, `BENEFICE` pour PM .
 Valeurs possibles pour les PP `(REVENU_ORDINAIRE|REVENU_SOURCE_PURE|FORTUNE)`, les PM `(BENEFICE)`
* [FACULTATIF] version de ws utilisée, valeur par défaut v7.  Valeurs possibles (v6,v7) exemple -Dversion_ws=v7
version_ws=v7

***Lancement du batch à partir des scripts :***
dézipper l'archive `unireg-rpt-batch-@version-release.zip` dans le répertoire de son choix.
Le répertoire bin: contient tous les exécutables.
* envs.sh : Défini tous les chemins pour les VM options
* metier.sh: Défini les paramètres du batch.
* envsBatch.sh: déclare les VM options ainsi que les program arguments.
* rpt_batch.sh: script de lancement du batch.

```
./rpt_batch.sh
```

##Configuration du batch
Le répertoire **config**  contient les fichiers de configuration
credentials.properties: contient les informations de sécurité ainsi que les données de connexion à la BDD.
unireg-web.properties: contient les informations issues du project unireg-web.
unireg-nexus.properties :contient les informations issues du project unireg-nexus.