/*
 * *********************************************
 * *        CARTOUCHE DES MODIFICATIONS        *
 * *********************************************
 *
 */
package ch.vd.unireg.utils;

import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.vd.registre.base.date.RegDate;
import ch.vd.unireg.exception.BatchSystemException;
import ch.vd.unireg.listes.afc.ExtractionDonneesRptJob;
import ch.vd.unireg.listes.afc.TypeExtractionDonneesRpt;
import ch.vd.unireg.listes.afc.pm.ExtractionDonneesRptPMJob;
import ch.vd.unireg.listes.afc.pm.ModeExtraction;
import ch.vd.unireg.listes.afc.pm.VersionWS;


/**
 * Classe permettant d'acceder aux propriétés du batch de cloture automatique
 */
public final class BatchRptProperties extends AbstractProperties {

	private static final Logger LOG = LoggerFactory.getLogger(BatchRptProperties.class);

	private static final String PROP_NAME_PERIODE_FISCALE = "rpt.periode.fiscale";

	private static final String PROP_NAME_POPULATION = "rpt.population";

	private static final String PROP_NAME_MODE = "rpt.mode";

	private static final String PROP_NAME_WS = "rpt.version.ws";

	private static final String NOMBRE_THREADS = "nombre.threads";

	/**
	 * L'unique instance de ce singleton.
	 */
	private static final BatchRptProperties UNIQUE_INSTANCE = new BatchRptProperties();

	/**
	 * @return l'unique instance de ce singleton.
	 */
	public static BatchRptProperties getInstance() {
		return UNIQUE_INSTANCE;
	}


	/**
	 * Constructeur privé pour empêcher l'instanciation.
	 */
	private BatchRptProperties() {
		super(LOG);
	}

	/**
	 * @return la période fiscale spécifiée dans le fichier de propriétés.
	 */
	public Long getPeriodeFiscale() {
		Long periodeFiscale;

		String propertyValue = getProperty(PROP_NAME_PERIODE_FISCALE);

		if (StringUtils.isBlank(propertyValue)) {
			propertyValue = String.valueOf(RegDate.get().year() - 1);
		}
		try {
			periodeFiscale = Long.valueOf(propertyValue);
		}
		catch (NumberFormatException e) {
			throw new BatchSystemException("Le format de la période fiscale est incorrect. [propriété : " + PROP_NAME_PERIODE_FISCALE + "].", e);
		}

		return periodeFiscale;
	}

	public String getPopulation() {
		String propertyValue = getProperty(PROP_NAME_POPULATION);

		if (StringUtils.isBlank(propertyValue)) {
			throw new BatchSystemException("La propriété [" + PROP_NAME_POPULATION + "] doit obligatoirement être définie.");
		}
		return propertyValue;
	}

	public String getJobName() {
		return "PP".equals(getPopulation()) ? ExtractionDonneesRptJob.NAME : ExtractionDonneesRptPMJob.NAME;
	}

	@NotNull
	public ModeExtraction getModeExtraction() {
		ModeExtraction mode;
		final String propertyValue = getProperty(PROP_NAME_MODE);
		try {
			mode = StringUtils.isNotBlank(propertyValue) ? ModeExtraction.valueOf(propertyValue) : ModeExtraction.BENEFICE;
		}
		catch (IllegalArgumentException ex) {
			throw new BatchSystemException("La propriété [" + PROP_NAME_MODE + "] doit  être définie avec la bonne valeur.");
		}
		return mode;
	}

	@NotNull
	public TypeExtractionDonneesRpt getTypeExtraction() {
		TypeExtractionDonneesRpt mode;
		final String propertyValue = getProperty(PROP_NAME_MODE);
		try {
			mode = StringUtils.isNotBlank(propertyValue) ? TypeExtractionDonneesRpt.valueOf(propertyValue) : TypeExtractionDonneesRpt.REVENU_ORDINAIRE;
		}
		catch (IllegalArgumentException ex) {
			throw new BatchSystemException("La propriété [" + PROP_NAME_MODE + "] doit  être définie avec la bonne valeur.");
		}
		return mode;
	}

	@NotNull
	public VersionWS getparamVersionWS() {
		VersionWS versionWS;
		final String version_ws = getProperty(PROP_NAME_WS);
		try {
			versionWS = StringUtils.isNotBlank(version_ws) ? VersionWS.valueOf(StringUtils.upperCase(version_ws)) : VersionWS.V7;
		}
		catch (IllegalArgumentException ex) {
			throw new BatchSystemException("La propriété [" + PROP_NAME_WS + "] doit  être définie avec la bonne valeur.");
		}
		return versionWS;
	}


	/**
	 * Traitement particulier pour récupérer le fichier de config
	 *
	 * @return
	 */
	@Override
	protected PropertiesFile getLocalProperties() {
		synchronized (this) {
			if (this.properties == null) {
				String nomFichier = System.getProperty("ch.vd.unireg.rpt.batch.arg.propertyFile");

				this.properties = new PropertiesFile();
				try {
					this.properties.load(new FileInputStream(nomFichier));
				}
				catch (IOException var5) {
					this.properties = null;
					throw new BatchSystemException("[" + nomFichier + "] impossible d'accéder au fichier de propriétés", var5);
				}
			}
		}

		return this.properties;
	}

	public String getPropertiesFileName() {
		return "unireg-metier.properties";
	}

	/**
	 * @return le nombre de threads à utiliser
	 */
	public final int getNombreThreads() {
		return Integer.parseInt(getProperty(NOMBRE_THREADS));
	}


}
