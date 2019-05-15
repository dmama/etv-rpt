#!/usr/bin/env bash
### Paramètres du batch

### [FACULTATIF] indique que le paramètre est facultatif (laisser blanc pour ne pas l'utiliser)


# [OBLIGATOIRE]  Type de population : PM, PP
population=PM

# [FACULTATIF] Période fiscale , valeur par défaut annee courante -1
periodeFiscale=2017

#  [FACULTATIF] Type d'extraction des données de référence RPT à génére.
#  valeur par défaut REVENU_ORDINAIRE pour PP, BENEFICE pour PM .
#  Valeurs possibles pour les PP (REVENU_ORDINAIRE|REVENU_SOURCE_PURE|FORTUNE), les PM (BENEFICE)
mode=BENEFICE

# [FACULTATIF] version de ws utilisée, valeur par défaut v7.  Valeurs possibles (v6,v7) exemple -Dversion_ws=v7
version_ws=v7