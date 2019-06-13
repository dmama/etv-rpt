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
		LOGGER.info("Periode Fiscale = {}, mode = {}  ", PROPERTIES_INSTANCE.getPeriodeFiscale(), "PP".equalsIgnoreCase(PROPERTIES_INSTANCE.getPopulation()) ? PROPERTIES_INSTANCE.getTypeExtraction() : PROPERTIES_INSTANCE.getModeExtraction());

		try {
			//injection des informations d'authentifications.
			AuthenticationHelper.pushPrincipal("RPT-Batch");

			//démarrage du job
			final JobDefinition jobDefinition = demarrerJob();

			// on attend la fin de l'execution du job
			while (jobDefinition.isRunning()) {
				//noop just wait
			}
			AuthenticationHelper.popPrincipal();
			stopWatch.stop();
			LOGGER.info("BATCH D'EXTRACTION RPT UNIREG - FIN DU TRAITEMENT {}. Duree = {} sec.", PROPERTIES_INSTANCE.getJobName(), formatMsToSecondes(stopWatch.getLastTaskTimeMillis()));
			System.exit(0);
		}
		catch (BatchSystemException exp) {
			stopWatch.stop();
			System.err.println("Erreur d'execution .  Raison: " + exp.getMessage());
			LOGGER.error("Erreur d'execution .  Raison: {}", exp.getMessage());
			AuthenticationHelper.popPrincipal();
			System.exit(1);
		}

	}


	private static JobDefinition demarrerJob() throws BatchSystemException {
		// Chargement du context Spring.
		LOGGER.info("Chargement du context Spring");
		final ApplicationContext context = new ClassPathXmlApplicationContext("unireg-all-module.xml");

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
			return batchScheduler.startJob(PROPERTIES_INSTANCE.getJobName(), params);
		}
		catch (Exception exp) {
			throw new BatchSystemException(exp.getMessage(), exp);
		}
	}


	private static String formatMsToSecondes(long duration) {
		float durationSec = (float) duration / 1000.0F;
		return DECIMAL_FORMAT_SEC.format((double) durationSec);
	}

}
