package ch.vd.unireg.config;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.StopWatch;

import ch.vd.dfin.tao.batch.common.ind.exception.BatchSystemException;
import ch.vd.unireg.common.AuthenticationHelper;
import ch.vd.unireg.listes.afc.ExtractionDonneesRptJob;
import ch.vd.unireg.listes.afc.pm.ExtractionDonneesRptPMJob;
import ch.vd.unireg.scheduler.BatchScheduler;
import ch.vd.unireg.scheduler.BatchSchedulerImpl;
import ch.vd.unireg.scheduler.JobDefinition;
import ch.vd.unireg.utils.BatchRptProperties;

/**
 * Batch d'extraction RPT d'UNIREG
 */
public final class BatchRptRunnerApp {

	private static final Logger LOGGER = LoggerFactory.getLogger(BatchRptRunnerApp.class);
	private static final DecimalFormat DECIMAL_FORMAT_SEC;
	private static final BatchRptProperties PROPERTIES_INSTANCE = BatchRptProperties.getInstance();

	static {
		Locale chLocale = (new Locale.Builder()).setRegion("CH").setLanguage("fr").build();
		DECIMAL_FORMAT_SEC = new DecimalFormat("#.###", DecimalFormatSymbols.getInstance(chLocale));
		DECIMAL_FORMAT_SEC.setRoundingMode(RoundingMode.HALF_UP);

	}



	/**
	 * Méthode de démarrage du batch.
	 *
	 * @param args les arguments passé en paramètre à l'application, aucun attendu.
	 */
	public static void main(String[] args) {
		final StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		if (args == null || args.length == 0) {
			throw new BatchSystemException("Manque l'argument du path et nom du fichier de configuration");
		}
		System.setProperty("ch.vd.unireg.rpt.batch.arg.propertyFile", args[0]);

		LOGGER.info("---- BATCH D'EXTRACTION RPT UNIREG - DEBUT DU TRAITEMENT ------------------------------------");
		LOGGER.info("Parametres  du batch {}: ", PROPERTIES_INSTANCE.getJobName());
		LOGGER.info("Periode Fiscale = {}, mode = {}  ", PROPERTIES_INSTANCE.getPeriodeFiscale(), PROPERTIES_INSTANCE.getModeExtraction());

		try {
			demarrerBatch();
			stopWatch.stop();
			LOGGER.info("BATCH D'EXTRACTION RPT UNIREG - FIN DU TRAITEMENT {}. Duree = {} sec.", PROPERTIES_INSTANCE.getJobName(), formatMsToSecondes(stopWatch.getLastTaskTimeMillis()));
			System.exit(0);
		}
		catch (BatchSystemException exp) {
			stopWatch.stop();
			System.err.println("Erreur d'execution .  Raison: " + exp.getMessage());
			LOGGER.error("Erreur d'execution .  Raison: {}", exp.getMessage());
			System.exit(1);
		}

	}


	private static void demarrerBatch() throws BatchSystemException {
		// Chargement du context Spring.
		LOGGER.info("Chargement du context Spring");
		final ApplicationContext context = new ClassPathXmlApplicationContext("unireg-all-module.xml");

		//injection des informations d'authentifications.
		AuthenticationHelper.pushPrincipal("RPT-Batch");

		try {
			final Map<String, Object> params = new HashMap<>();
			params.putIfAbsent(ExtractionDonneesRptJob.PERIODE_FISCALE, PROPERTIES_INSTANCE.getPeriodeFiscale());
			params.putIfAbsent(ExtractionDonneesRptJob.NB_THREADS, PROPERTIES_INSTANCE.getNombreThreads());

			if ("PP".equalsIgnoreCase(PROPERTIES_INSTANCE.getPopulation())) {
				params.putIfAbsent(ExtractionDonneesRptJob.MODE, PROPERTIES_INSTANCE.getTypeExtraction());
			}
			else {
				params.putIfAbsent(ExtractionDonneesRptPMJob.VERSION_WS, PROPERTIES_INSTANCE.getparamVersionWS());
				params.putIfAbsent(ExtractionDonneesRptPMJob.MODE, PROPERTIES_INSTANCE.getModeExtraction());
			}

			// Chargement du batchScheduler depuis le context.
			final BatchScheduler batchScheduler = context.getBean("batchScheduler", BatchSchedulerImpl.class);
			final JobDefinition jobDefinition = batchScheduler.startJob(PROPERTIES_INSTANCE.getJobName(), params);

			Integer oldpercentProgression = jobDefinition.getPercentProgression();

			// On attend que toutes le job soit terminé
			while (jobDefinition.isRunning()) {
				if (oldpercentProgression == null) {
					oldpercentProgression = jobDefinition.getPercentProgression();
				}
				if (jobDefinition.getPercentProgression() != null && oldpercentProgression != null && jobDefinition.getPercentProgression() >= oldpercentProgression + 3) {
					System.out.println("###### Batchs atifs #########");
					System.out.println(jobDefinition.getDescription());
					System.out.println(" Progression  " + jobDefinition.getPercentProgression() + "%  " + jobDefinition.getRunningMessage());
					System.out.println(" Statut  " + jobDefinition.getStatut().name());
					if (oldpercentProgression < 100) {
						oldpercentProgression = jobDefinition.getPercentProgression();
					}
				}
			}

		}
		catch (Exception exp) {
			throw new BatchSystemException(exp.getMessage(), exp);
		}
		finally {
			AuthenticationHelper.popPrincipal();

		}
	}


	private static String formatMsToSecondes(long duration) {
		float durationSec = (float) duration / 1000.0F;
		return DECIMAL_FORMAT_SEC.format((double) durationSec);
	}

}
