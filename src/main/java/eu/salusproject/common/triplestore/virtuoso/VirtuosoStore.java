package eu.salusproject.common.triplestore.virtuoso;

import static com.hp.hpl.jena.ontology.OntModelSpec.OWL_DL_MEM;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import virtuoso.jena.driver.VirtDataSource;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;

import eu.salusproject.common.triplestore.JenaStore;
import eu.salusproject.common.triplestore.JenaStoreException;

public class VirtuosoStore implements JenaStore {

	private static final Logger logger = LoggerFactory
			.getLogger(VirtuosoStore.class);

	private VirtDataSource dataSource;
	private File storeFile;

	public static final String SEPARATOR = "#";

	private final String serverURL;
	private final String username;
	private final String password;

	public VirtuosoStore(String storeFilePath, String serverURL,
			String username, String password) throws JenaStoreException {
		this.storeFile = new File(storeFilePath);
		StringBuilder sb = new StringBuilder();
		String virtData = sb.append(storeFile.getName()).append(SEPARATOR)
				.append(serverURL).append(SEPARATOR).append(username)
				.append(SEPARATOR).append(password).toString();
		try {
			FileUtils.writeStringToFile(storeFile, virtData, false);
		} catch (IOException e) {
			String msg = String.format(
					"Cannot create the storeFile: %s for VirtuosoStore.",
					storeFile.getAbsolutePath());
			throw new JenaStoreException(msg, e);
		}

		this.serverURL = serverURL;
		this.username = username;
		this.password = password;

		this.dataSource = new VirtDataSource(this.serverURL, this.username,
				this.password);
		logger.info("VirtDataSource has been successfully connected to {}",
				serverURL);
	}

	@Override
	public OntModel createOntModel(String ontologyURI)
			throws JenaStoreException {
		if (hasModel(ontologyURI)) {
			logger.info("The model: {} already exists.", ontologyURI);
			return getOntModel(ontologyURI);
		}

		OntModel ontModel = ModelFactory.createOntologyModel(OWL_DL_MEM);
		Model model = addModel(ontologyURI, ontModel);
		ontModel = ModelFactory.createOntologyModel(OWL_DL_MEM, model);
		logger.info("Empty OntModel has been created: {}", ontologyURI);
		return ontModel;
	}

	@Override
	public OntModel createOntModel(String ontologyURI, String baseURI,
			String ontologyFilePath) throws JenaStoreException {
		return createOntModel(ontologyURI, baseURI, ontologyFilePath, "RDF/XML");
	}

	@Override
	public OntModel createOntModel(String ontologyURI, String baseURI,
			String ontologyFilePath, String format) throws JenaStoreException {
		if (hasModel(ontologyURI)) {
			logger.info("The model: {} already exists.", ontologyURI);
			return getOntModel(ontologyURI);
		}
		InputStream inputStream = FileManager.get().open(ontologyFilePath);
		if (inputStream == null) {
			throw new IllegalArgumentException(String.format(
					"File: %s not found", ontologyFilePath));
		}
		OntModel ontModel = ModelFactory.createOntologyModel(OWL_DL_MEM);
		try {
			ontModel.read(inputStream, baseURI, format);
			inputStream.close();
		} catch (IOException e) {
			logger.error("Cannot close the inputstream", e);
			throw new JenaStoreException("Cannot close the inputstream", e);
		}
		Model model = addModel(ontologyURI, ontModel);
		ontModel = ModelFactory.createOntologyModel(OWL_DL_MEM, model);
		logger.info(
				"Newly created OntModel has been populated with the ontology at {}",
				ontologyURI, ontologyFilePath);
		return ontModel;
	}

	@Override
	public Model addModel(String ontologyURI, Model model) {
		dataSource.addNamedModel(ontologyURI, model);
		Model retModel = dataSource.getNamedModel(ontologyURI);
		retModel.setNsPrefixes(model.getNsPrefixMap());
		return retModel;
	}

	@Override
	public OntModel getOntModel(String ontologyURI) {
		Model m = getModel(ontologyURI);
		OntModel ontModel = ModelFactory.createOntologyModel(OWL_DL_MEM, m);
		return ontModel;
	}

	@Override
	public Model getModel(String ontologyURI) {
		return dataSource.getNamedModel(ontologyURI);
	}

	@Override
	public boolean hasModel(String ontologyURI) {
		return dataSource.containsNamedModel(ontologyURI);
	}

	@Override
	public List<String> listModels() {
		List<String> models = new ArrayList<String>();

		Iterator<String> modelIt = dataSource.listNames();
		while (modelIt.hasNext()) {
			models.add(modelIt.next());
		}
		return models;
	}

	@Override
	public void removeModel(String ontologyURI) {
		dataSource.removeNamedModel(ontologyURI);
	}

	@Override
	public void begin(ReadWrite readWrite) {
		// dataSource.begin(readWrite);
	}

	@Override
	public void end() {
		// dataSource.end();
	}

	@Override
	public void sync() {
		// Do nothing
	}
	
	@Override
	public void commit() {
		// dataSource.commit();
	}

	@Override
	public void close() {
		dataSource.close();
	}

	@Override
	public void updateIndex() {
		logger.info("Do nothing, Virtuoso seamlessly handles the indexing.");
	}

	@Override
	public void updateIndex(String ontologyURI) {
		logger.info("Do nothing, Virtuoso seamlessly handles the indexing.");
	}

	@Override
	public void remove() {
		// Remove all named models
		for (String modelName : listModels()) {
			dataSource.removeNamedModel(modelName);
		}
	}

	@Override
	public Graph getGraph() {
		return dataSource;
	}

	@Override
	public void setAutoSync(boolean autoSync) {
		// TODO Virtuoso store does not need sync or transactions at all
		
	}

}
