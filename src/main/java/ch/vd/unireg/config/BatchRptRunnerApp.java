package ch.vd.unireg.config;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.StopWatch;

import ch.vd.registre.base.date.RegDate;
import ch.vd.unireg.common.AuthenticationHelper;
import ch.vd.unireg.listes.afc.ExtractionDonneesRptJob;
import ch.vd.unireg.listes.afc.TypeExtractionDonneesRpt;
import ch.vd.unireg.listes.afc.pm.ExtractionDonneesRptPMJob;
import ch.vd.unireg.listes.afc.pm.ModeExtraction;
import ch.vd.unireg.listes.afc.pm.VersionWS;
import ch.vd.unireg.scheduler.BatchScheduler;
import ch.vd.unireg.scheduler.BatchSchedulerImpl;
import ch.vd.unireg.scheduler.JobDefinition;

public class BatchRptRunnerApp {
	private static final Logger LOGGER = LoggerFactory.getLogger(BatchRptRunnerApp.class);
	private static final DecimalFormat DECIMAL_FORMAT_SEC;

	static {
		Locale chLocale = (new Locale.Builder()).setRegion("CH").setLanguage("fr").build();
		DECIMAL_FORMAT_SEC = new DecimalFormat("#.###", DecimalFormatSymbols.getInstance(chLocale));
		DECIMAL_FORMAT_SEC.setRoundingMode(RoundingMode.HALF_UP);
	}

	private class BatchRunnertException extends Exception {

		BatchRunnertException(Throwable cause) {
			super(cause);
		}

	}

	public static void main(String[] args) {
		final StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		final Properties properties = parseCommandLine(args);
		if (properties == null) {
			LOGGER.error("Erreur lors du parsing des parametres");
			System.exit(1);
		}
		LOGGER.info("Debut du traitement du batch  {} RPT ...", BatchRptRunnerApp.class.getName());
		LOGGER.info("Parametres  du batch {}: ", "PP".equalsIgnoreCase(properties.getProperty("population")) ? ExtractionDonneesRptJob.NAME : ExtractionDonneesRptPMJob.NAME);
		LOGGER.info("Periode Fiscale = {}, mode = {}  ", properties.get("periodeFiscale"), properties.get("mode"));

		try {
			new BatchRptRunnerApp(properties);
			stopWatch.stop();
			LOGGER.info("Fin du traitement {}. Duree = {} sec.", BatchRptRunnerApp.class.getName(), formatMsToSecondes(stopWatch.getLastTaskTimeMillis()));

			System.exit(0);
		}
		catch (BatchRunnertException exp) {
			stopWatch.stop();
			System.err.println("Erreur d'execution .  Raison: " + exp.getMessage());
			LOGGER.error("Erreur d'execution .  Raison: {}", exp.getMessage());
			System.exit(1);
		}

	}

	private BatchRptRunnerApp(Properties properties) throws BatchRunnertException {

		LOGGER.info("Contruction du context spring");
		final ApplicationContext context = new ClassPathXmlApplicationContext("unireg-all-module.xml");
		final String population = properties.getProperty("population");
		final BatchScheduler batchScheduler = context.getBean("batchScheduler", BatchSchedulerImpl.class);
		AuthenticationHelper.pushPrincipal("RPT-Batch");

		try {
			String jobName;
			final Map<String, Object> params = new HashMap<>();
			params.putIfAbsent(ExtractionDonneesRptJob.PERIODE_FISCALE, NumberUtils.toInt(properties.getProperty("periodeFiscale"), RegDate.get().year() - 1));
			params.putIfAbsent(ExtractionDonneesRptJob.NB_THREADS, 4);

			if ("PP".equalsIgnoreCase(population)) {
				jobName = ExtractionDonneesRptJob.NAME;
				params.putIfAbsent(ExtractionDonneesRptJob.MODE, getParamTypeExtraction(properties));
			}
			else {
				jobName = ExtractionDonneesRptPMJob.NAME;
				params.putIfAbsent(ExtractionDonneesRptPMJob.VERSION_WS, getparamVersionWS(properties));
				params.putIfAbsent(ExtractionDonneesRptPMJob.MODE, getParamModeExtraction(properties));
			}

			final JobDefinition jobDefinition = batchScheduler.startJob(jobName, params);
			Integer oldpercentProgression = jobDefinition.getPercentProgression();

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
			throw new BatchRunnertException(exp);
		}
		finally {
			AuthenticationHelper.popPrincipal();

		}
	}

	@NotNull
	private ModeExtraction getParamModeExtraction(Properties properties) {

		final String modeStr = properties.getProperty("mode");
		ModeExtraction mode;
		try {
			mode = StringUtils.isNotBlank(modeStr) ? ModeExtraction.valueOf(modeStr) : ModeExtraction.BENEFICE;
		}
		catch (IllegalArgumentException ex) {
			mode = ModeExtraction.BENEFICE;
		}
		return mode;
	}

	@NotNull
	private TypeExtractionDonneesRpt getParamTypeExtraction(Properties properties) {

		final String modeStr = properties.getProperty("mode");
		TypeExtractionDonneesRpt mode;
		try {
			mode = StringUtils.isNotBlank(modeStr) ? TypeExtractionDonneesRpt.valueOf(modeStr) : TypeExtractionDonneesRpt.REVENU_ORDINAIRE;
		}
		catch (IllegalArgumentException ex) {
			mode = TypeExtractionDonneesRpt.REVENU_ORDINAIRE;
		}
		return mode;
	}

	@NotNull
	private VersionWS getparamVersionWS(Properties properties) {
		final String version_ws = properties.getProperty("version_ws");
		VersionWS versionWS;
		try {
			versionWS = StringUtils.isNotBlank(version_ws) ? VersionWS.valueOf(version_ws) : VersionWS.V7;
		}
		catch (IllegalArgumentException ex) {
			versionWS = VersionWS.V7;
		}
		return versionWS;
	}


	@NotNull
	private static Options getOptions() {
		final Options options = new Options();
		options.addOption("help", "affiche ce message");
		options.addOption("population", "[OBLIGATOIRE] Population, valeur possible (PP|PM) exemple -Dpopulation=PP");
		options.addOption("periodeFiscale", "[FACULTATIF] Période fiscale, année courant -1,   exemple -DperiodeFiscale=2019");
		options.addOption("mode",
		                  " [FACULTATIF]Type d'extraction des données de référence RPT à génére. valeur par défaut REVENU_ORDINAIRE pour PP, BENEFICE pour PM . Valeurs possibles pour les PP (REVENU_ORDINAIRE|REVENU_SOURCE_PURE|FORTUNE), les PM (BENEFICE)");
		options.addOption("version_ws", " [FACULTATIF] version de ws utilisée, valeur par défaut v7.  Valeurs possibles (v6,v7) exemple -Dversion_ws=v7");
		return options;
	}


	private static Properties parseCommandLine(String[] args) {

		CommandLine commandLine = null;
		try {
			final Option propertyOption = Option.builder()
					.longOpt("D")
					.argName("property=value")
					.hasArgs()
					.valueSeparator()
					.desc("use value for given properties")
					.build();

			final Options options = new Options();
			options.addOption(propertyOption);


			// parse the command line arguments
			final CommandLineParser parser = new DefaultParser();
			commandLine = parser.parse(options, args);
			if (commandLine.hasOption("help") || (!commandLine.hasOption("D") && commandLine.getArgs().length != 1)) {
				final HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("BatchRunner [rpt] [options]", "Options:", getOptions(), null);
				return null;
			}
		}
		catch (ParseException exp) {
			LOGGER.error("Erreur de parsing.  Raison: {}", exp.getMessage());
			System.err.println("Erreur de parsing.  Raison: " + exp.getMessage());
			System.exit(1);
		}

		final Properties properties = commandLine.getOptionProperties("D");

		validateParameter(properties);

		return properties;
	}

	private static void validateParameter(Properties properties) {

		final String population = properties.getProperty("population");
		if (StringUtils.isBlank(population)) {
			final HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("BatchRunner [rpt] [options]", "Options:", getOptions(), null);
		}
		LOGGER.info("le parsing des paramètres est correcte.");
		System.out.println("le parsing des paramètres est correcte.");
	}

	private static String formatMsToSecondes(long duration) {
		float durationSec = (float) duration / 1000.0F;
		return DECIMAL_FORMAT_SEC.format((double) durationSec);
	}

}
