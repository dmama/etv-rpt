package ch.vd.unireg.utils;

import java.io.File;

/**
 * Cette classe donne accès aux constantes définies au démarrage.
 */
public final class BootConstantes {
	/**
	 * Contient le chemin du repertoire dans lequel sont stockés les fichiers de propriétés.
	 */
	private static final String PROPERTY_FOLDER;


	static {
		String folder = System.getProperty("ch.vd.unireg.rpt.batch.arg.propertyFile");
		if (folder == null) {
			throw new IllegalStateException("la propriété système 'ch.vd.dfin.tao.propertyFolder' n'est pas définie");
		}
		if (!folder.endsWith(File.separator)) {
			folder = folder + File.separator;
		}
		PROPERTY_FOLDER = folder;
	}

	/**
	 * @return le chemin du répertoire contenant les fichiers de propriétés.
	 */
	public static String getPropertyFolder() {
		return PROPERTY_FOLDER;
	}


	/**
	 * Constructeur privé pour empêcher l'instanciation
	 */
	private BootConstantes() {
	}
}
