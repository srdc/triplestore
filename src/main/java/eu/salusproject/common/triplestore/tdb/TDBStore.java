package eu.salusproject.common.triplestore.tdb;

import static com.hp.hpl.jena.ontology.OntModelSpec.OWL_DL_MEM;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.jena.larq.IndexBuilderString;
import org.apache.jena.larq.LARQ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.util.FileManager;

import eu.salusproject.common.triplestore.JenaStore;
import eu.salusproject.common.triplestore.JenaStoreException;

/**
 * @author anil
 * 
 */
public class TDBStore implements JenaStore {

	private static final Logger logger = LoggerFactory
			.getLogger(TDBStore.class);

	private Dataset dataset;
	private String storeDirectory;

	private IndexBuilderString index;

	private Map<String, Model> namedModels;

	private ModelUpdateListener modelUpdateListener;

	public TDBStore(String storeDirectory) {
		this.storeDirectory = storeDirectory;
		long start = System.currentTimeMillis();
		this.dataset = TDBFactory.createDataset(storeDirectory);
		this.modelUpdateListener = new ModelUpdateListener(this.dataset);
		long end = System.currentTimeMillis();
		logger.info("Dataset restored in " + (end - start) + " miliseconds");

		index = new IndexBuilderString();
		namedModels = new HashMap<String, Model>();
		try {
			begin(ReadWrite.READ);
			Iterator<String> modelIt = dataset.listNames();
			while (modelIt.hasNext()) {
				String uri = modelIt.next();
				Model model = dataset.getNamedModel(uri);
				namedModels.put(uri, model);
				indexModel(uri, model);
			}
		} finally {
			end();
		}
		logger.info("All existing models are indexed and the Map cache of TDBStore has been initialized.");
	}

	public String getStoreDirectory() {
		return this.storeDirectory;
	}

	@Override
	public Model addModel(String ontologyURI, Model model) {
		Model retModel = null;
		try {
			begin(ReadWrite.WRITE);
			dataset.addNamedModel(ontologyURI, model);
			// sets same PrefixMap in TDB
			retModel = dataset.getNamedModel(ontologyURI);
			retModel.setNsPrefixes(model.getNsPrefixMap());
			commit();
		} finally {
			end();
		}
		indexModel(ontologyURI, retModel);
		namedModels.put(ontologyURI, retModel);
		return retModel;
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
		namedModels.put(ontologyURI, ontModel);
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
		namedModels.put(ontologyURI, ontModel);
		return ontModel;
	}

	@Override
	public OntModel getOntModel(String ontologyURI) {
		Model m = getModel(ontologyURI);
		OntModel ontModel = ModelFactory.createOntologyModel(OWL_DL_MEM, m);
		return ontModel;
	}

	@Override
	public Model getModel(String ontologyURI) {
		Model model = namedModels.get(ontologyURI);
		if (model != null) {
			return model;
		}
		try {
			begin(ReadWrite.READ);
			model = dataset.getNamedModel(ontologyURI);
		} finally {
			end();
		}
		if (model != null) {
			throw new IllegalStateException(
					"Map of NamedModels is not syncronized with the named models of the underlying dataset.");
		}
		return model;
	}

	@Override
	public boolean hasModel(String ontologyURI) {
		return namedModels.containsKey(ontologyURI);
	}

	@Override
	public List<String> listModels() {
		List<String> models = new ArrayList<String>();
		models.addAll(namedModels.keySet());
		return models;
	}

	@Override
	public void removeModel(String ontologyURI) {
		Model model = getModel(ontologyURI);
		model.unregister(index);
		unindex(model);
		namedModels.remove(ontologyURI);
		try {
			begin(ReadWrite.WRITE);
			dataset.removeNamedModel(ontologyURI);
			commit();
		} finally {
			end();
		}
	}

	@Override
	public void begin(ReadWrite readWrite) {
		// dataset.begin(readWrite);
	}

	@Override
	public void end() {
		// dataset.end();
	}

	@Override
	public void commit() {
		// dataset.commit();
	}

	@Override
	public void sync() {
		TDB.sync(dataset);
		logger.info("TDB dataset has been synchronized");
	}

	@Override
	public void close() {
		sync();
		dataset.close();
	}

	@Override
	public void updateIndex() {
		for (String uri : listModels()) {
			updateIndex(uri);
		}
	}

	@Override
	public void updateIndex(String ontologyURI) {
		Model model = getModel(ontologyURI);
		indexModel(ontologyURI, model);
	}

	private void indexModel(String ontologyURI, Model model) {
		index.indexStatements(model.listStatements());
		model.register(index);
		logger.info(
				"OntModel: {} has been indexed and the index has been registered for changes.",
				ontologyURI);
		LARQ.setDefaultIndex(index.getIndex());
	}

	private void unindex(Model model) {
		for (StmtIterator it = model.listStatements(); it.hasNext();) {
			index.unindexStatement(it.next());
		}
	}

	@Override
	public void remove() throws JenaStoreException {
		close();
		// Delete the corresponding folder from the file system.
		// try {
		// FileUtils.deleteDirectory(new File(this.storeDirectory));
		// } catch (IOException e) {
		// String msg = "Cannot remove the dataset directory";
		// logger.error(msg, e);
		// throw new JenaStoreException(msg, e);
		// }
	}

	@Override
	public Graph getGraph() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setAutoSync(boolean autoSync) {
		List<String> modelNameList = this.listModels();
		List<Model> modelList = new ArrayList<Model>();
		for (String model : modelNameList) {
			modelList.add(this.getModel(model));
		}
		if (autoSync) {
			logger.info("Auto sync activated");
			for (Model m : modelList) {
				m.register(modelUpdateListener);
			}
		} else {
			logger.info("Auto sync deactivated");
			for (Model m : modelList) {
				m.unregister(modelUpdateListener);
			}
		}

	}

}
