package ch.vd.unireg.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

import org.slf4j.Logger;

import ch.vd.unireg.exception.BatchSystemException;

/**
 * Classe permettant d'accéder à un fichier de propriétés.
 */
public abstract class AbstractProperties {

	/**
	 * Les proprietes
	 */
	protected PropertiesFile properties;

	/**
	 * Le logger fournit par l'implementation parente
	 */
	private Logger log;

	/**
	 * Constructeur
	 *
	 * @param unLog un logger log4j
	 */
	protected AbstractProperties(Logger unLog) {
		log = unLog;
	}

	/**
	 * Les implémentation de cette classe doivent retourner le chemin du fichier de propriété.
	 *
	 * @return le chemin du fichier de propriété
	 */
	public abstract String getPropertiesFileName();

	/**
	 * Retourne une map triée par ordre alphabétique contenant les propriétés disponibles, les propriétés encryptées ne sont pas inclues dans le résultat. La map est reconstruite à chaque appel.
	 *
	 * @return une map triée par ordre alphabétique contenant les propriétés disponibles
	 */
	public final SortedMap getProperties() {
		return new TreeMap(getLocalProperties());
	}


	/**
	 * @param key la clé de la propriété
	 * @return une propriété en fonction de sa clé
	 */
	protected final String getProperty(String key) {
		return getLocalProperties().getProperty(key);
	}


	/**
	 * @return les propriétés locales
	 */
	protected PropertiesFile getLocalProperties() {
		synchronized (this) {
			if (properties == null) {
				String nomFichier = BootConstantes.getPropertyFolder() + getPropertiesFileName();
				properties = new PropertiesFile();

				try {
					properties.load(new FileInputStream(nomFichier));
				}
				catch (IOException e) {
					properties = null;
					throw new BatchSystemException("[" + nomFichier + "] impossible d'accéder au fichier de propriétés", e);
				}
			}
		}

		return properties;
	}
}
