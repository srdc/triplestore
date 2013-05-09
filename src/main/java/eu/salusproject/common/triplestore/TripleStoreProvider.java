package eu.salusproject.common.triplestore;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.shared.JenaException;

import eu.salusproject.common.triplestore.tdb.TDBStore;
import eu.salusproject.common.triplestore.virtuoso.VirtuosoStore;

public class TripleStoreProvider {

	private static final String TDB_STORES_DIR = "stores/tdb/";
	private static final String VIRTUOSO_STORES_DIR = "stores/virtuoso/";

	public static final String DefaultTDBStoreName = "default";

	public static final String DafaultVirtuosoStoreName = "default";
	public static final String DefaultVirtuosoServerURL = "jdbc:virtuoso://localhost:1111";
	public static final String DefaultVirtuosoDBUsername = "dba";
	public static final String DefaultVirtuosoDBPasswd = "dba";

	private static final Logger logger = LoggerFactory
			.getLogger(TripleStoreProvider.class);

	private static TripleStoreProvider instance;

	private Map<String, TDBStore> tdbStores;
	private Map<String, VirtuosoStore> virtuosoStores;

	private TripleStoreProvider() throws JenaStoreException {
		initDirectory(TDB_STORES_DIR);
		initDirectory(VIRTUOSO_STORES_DIR);
		initializeTDBStores();
		initializeVirtuosoStores();
	}

	private void initDirectory(String path) {
		File f = new File(path);
		try {
			FileUtils.forceMkdir(f);
		} catch (IOException e) {
			logger.error(
					"Error during the creation of the stores directory: {}",
					f.getAbsolutePath(), e);
			throw new IllegalStateException(
					"Error during the creation of the stores directory", e);
		}
		logger.info("X_STORES_DIR created to persist JenaStores at {}",
				f.getAbsolutePath());

	}

	private void initializeTDBStores() throws JenaStoreException {
		tdbStores = new HashMap<String, TDBStore>();
		File storesDir = new File(TDB_STORES_DIR);
		for (String fname : storesDir.list()) {
			createTDBStore(fname);
		}
	}

	private void initializeVirtuosoStores() throws JenaStoreException {
		virtuosoStores = new HashMap<String, VirtuosoStore>();
		File storesDir = new File(VIRTUOSO_STORES_DIR);
		for (String fname : storesDir.list()) {
			try {
				String virtData = FileUtils.readFileToString(new File(
						storesDir, fname));
				String[] parts = virtData.split(VirtuosoStore.SEPARATOR);
				createVirtuosoStore(parts[0], parts[1], parts[2], parts[3]);
			} catch (IOException e) {
				String msg = String
						.format("Cannot read virtData from the VIRTUOSO_STORES_DIR: %s",
								fname);
				logger.error(msg);
				throw new JenaException(msg, e);
			}
		}
	}

	/**
	 * This provider has only one instance at current environment. This method
	 * is used to retrieve that singleton instance
	 * 
	 * @return singleton {@link TripleStoreProvider} instance
	 * @throws JenaStoreException
	 */
	public static TripleStoreProvider getInstance() throws JenaStoreException {
		if (instance == null) {
			instance = new TripleStoreProvider();
		}
		return instance;
	}

	/**
	 * Given the type and name, it creates a new {@link JenaStore} and returns
	 * it. If the {@link JenaStoreException} already exists, then this method
	 * simply returns that {@link JenaStore} with not modification.
	 * 
	 * @param type
	 * @param storeName
	 * @return
	 * @throws JenaStoreException
	 */
	public JenaStore createStore(TripleStoreType type, String storeName)
			throws JenaStoreException {
		switch (type) {
		default:
		case JenaTDB:
			return createTDBStore(storeName);
		case Virtuoso:
			return createVirtuosoStore(storeName, null, null, null);
		}
	}

	/**
	 * Given the type, it creates a new {@link JenaStore} with the default name.
	 * If the default {@link JenaStore} already exists, then it returns the
	 * default store with not midification.
	 * 
	 * @param type
	 * @return
	 * @throws JenaStoreException
	 */
	public JenaStore createStore(TripleStoreType type)
			throws JenaStoreException {
		switch (type) {
		default:
		case JenaTDB:
			return createTDBStore(null);
		case Virtuoso:
			return createVirtuosoStore(null, null, null, null);
		}
	}

	/**
	 * Given the type and the storeName, return the associated {@link JenaStore}
	 * .
	 * 
	 * @param type
	 * @param storeName
	 * @return
	 */
	public JenaStore getStore(TripleStoreType type, String storeName) {
		switch (type) {
		default:
		case JenaTDB:
			return getTDBStore(storeName);
		case Virtuoso:
			return getVirtuosoStore(storeName);
		}
	}

	/**
	 * Given the storeName, return the associated {@link TDBStore}.
	 * 
	 * @param storeName
	 * @return
	 */
	public TDBStore getTDBStore(String storeName) {
		return tdbStores.get(storeName);
	}

	/**
	 * Given the storeName, return the associated {@link VirtuosoStore};
	 * 
	 * @param storeName
	 * @return
	 */
	public VirtuosoStore getVirtuosoStore(String storeName) {
		return virtuosoStores.get(storeName);
	}

	/**
	 * Given the name, it creates a new {@link TDBStore}. If the
	 * {@link TDBStore} with the given storeName already exists, then it is
	 * returned with no modification. If storeName is empty or null, it creates
	 * the default TDBStore.
	 * 
	 * @param storeName
	 * @return
	 * @throws JenaStoreException
	 */
	public TDBStore createTDBStore(String storeName) throws JenaStoreException {
		if (storeName == null || storeName.trim().equals("")) {
			storeName = DefaultTDBStoreName;
		}
		if (tdbStores.containsKey(storeName)) {
			logger.info(
					"Cannot create. TDBStore already exists: {}. Existing TDBStore is returned",
					storeName);
			return tdbStores.get(storeName);
		}

		String storeDirectory = TDB_STORES_DIR + storeName;
		TDBStore tdbStore = new TDBStore(storeDirectory);
		tdbStores.put(storeName, tdbStore);
		logger.info(
				"TDBStore at {} has been created and its dataset has been initialized.",
				tdbStore.getStoreDirectory());
		return tdbStore;
	}

	public VirtuosoStore createVirtuosoStore(String storeName,
			String serverURL, String username, String password)
			throws JenaStoreException {

		if (storeName == null || storeName.trim().equals("")) {
			storeName = DafaultVirtuosoStoreName;
		}
		if (virtuosoStores.containsKey(storeName)) {
			logger.info(
					"Cannot create. VirtuosoStore already exists: {}. Existing VirtuosoStore is returned",
					storeName);
			return virtuosoStores.get(storeName);
		}

		serverURL = serverURL == null ? DefaultVirtuosoServerURL : serverURL;
		username = username == null ? DefaultVirtuosoDBUsername : username;
		password = password == null ? DefaultVirtuosoDBPasswd : password;

		String storeFile = VIRTUOSO_STORES_DIR + storeName;

		VirtuosoStore virtuosoStore = new VirtuosoStore(storeFile, serverURL,
				username, password);
		virtuosoStores.put(storeName, virtuosoStore);
		return virtuosoStore;
	}

	public void removeStore(String storeName) throws JenaStoreException {
		TDBStore tdbStore = tdbStores.get(storeName);
		if (tdbStore != null) {
			tdbStore.remove();
		}
	}

	public enum TripleStoreType {
		/**
		 * Uses a JenaTDB which is in local file system as a backend for the
		 * repository
		 */
		JenaTDB,
		/**
		 * Uses a remote Virtuoso Universal Server as an RDF backend for the
		 * repository
		 */
		Virtuoso
	}

}
