package eu.salusproject.common.triplestore;

import java.util.List;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * @author anil
 * 
 */
public interface JenaStore {

	/**
	 * Creates and returns an empty named OWL_DL {@link OntModel} within this
	 * {@link JenaStore}. The name of the model is the given URI. Apart from its
	 * URI, any {@link String} can be given as the name of the created model. To
	 * update a model, you need to first remove that model.
	 * 
	 * @param ontologyURI
	 * @return
	 * @throws JenaStoreException
	 */
	public OntModel createOntModel(String ontologyURI)
			throws JenaStoreException;

	/**
	 * Creates and returns a named OWL_DL {@link OntModel} which is initialized
	 * with the contents of the given ontologyFilePath. The ontology must be in
	 * RDF/XML serialization.
	 * 
	 * @param ontologyURI
	 * @param baseURI
	 *            can be null
	 * @param ontologyFilePath
	 * @return
	 * @throws JenaStoreException
	 */
	public OntModel createOntModel(String ontologyURI, String baseURI,
			String ontologyFilePath) throws JenaStoreException;

	/**
	 * Creates and returns a named OWL_DL {@link OntModel} which is initialized
	 * with the contents of the given ontologyFilePath. The serialization format
	 * of the ontology is fiven with format (i.e. "RDF/XML", "TTL", "N3"). The
	 * triples of this newly created model are added to the search index.
	 * 
	 * @param ontologyURI
	 * @param baseURI
	 *            can be null
	 * @param ontologyFilePath
	 * @param format
	 * @return
	 */
	public OntModel createOntModel(String ontologyURI, String baseURI,
			String ontologyFilePath, String format) throws JenaStoreException;

	/**
	 * Adds the given {@link Model} to this {@link JenaStore} and returns the
	 * added Model. After calling this method, the returned model should be used
	 * if persistence is required after WRITE operations on the model.
	 * 
	 * @param ontologyURI
	 * @param model
	 * @return the persisted model
	 */
	public Model addModel(String ontologyURI, Model model);

	/**
	 * Given the name, returns the {@link OntModel}. Returns <code>null</code>
	 * if the there is no {@link OntModel} with the given ontologyURI. The
	 * triples of the added model are added to the search index.
	 * 
	 * @param ontologyURI
	 * @return
	 */
	public OntModel getOntModel(String ontologyURI);

	/**
	 * Given the name, returns the {@link Model}. Returns <code>null</code> if
	 * the there is no {@link Model} with the given ontologyURI.
	 * 
	 * @param ontologyURI
	 * @return
	 */
	public Model getModel(String ontologyURI);

	/**
	 * Checks whether an {@link OntModel} with the given name (ontologyURI)
	 * exists or not.
	 * 
	 * @param ontologyURI
	 * @return
	 */
	public boolean hasModel(String ontologyURI);

	/**
	 * Lists names of all model within this {@link JenaStore}.
	 * 
	 * @return
	 */
	public List<String> listModels();

	/**
	 * Removes the {@link OntModel} with the given name (ontologyURI) from this
	 * {@link JenaStore}. If the {@link OntModel} does not exists, this method
	 * does nothing.
	 * 
	 * @param ontologyURI
	 */
	public void removeModel(String ontologyURI);

	/**
	 * Begin a READ or WRITE transaction on this {@link JenaStore}.
	 * 
	 * @param readWrite
	 */
	public void begin(ReadWrite readWrite);

	/**
	 * End a transaction on this {@link JenaStore}.
	 */
	public void end();

	/**
	 * Commit a transaction on this {@link JenaStore}.
	 */
	public void commit();

	/**
	 * Synchronize the memory with the persistence store.
	 */
	public void sync();

	/**
	 * Used to control whether TDB.sync should be called automatically or by
	 * user
	 * 
	 * @param autoSync
	 *            if given <code>true</code>, TDB.sync will be called
	 *            automatically after each modification on this JenaStore. If
	 *            given <code>false</code>, no sync will be done, it is the
	 *            users responsibility to call sync to keep JenaStore
	 *            consistent.
	 */
	public void setAutoSync(boolean autoSync);

	/**
	 * Close any communication with this {@link JenaStore}.
	 */
	public void close();

	/**
	 * Force this {@link JenaStore} to update its index with all Named Models it
	 * has.
	 */
	public void updateIndex();

	/**
	 * Force this {@link JenaStore} to update the index with the model named
	 * with ontologyURI.
	 * 
	 * @param ontologyURI
	 * @param addRemove
	 */
	public void updateIndex(String ontologyURI);

	/**
	 * Closes and totally removes this {@link JenaStore}.
	 * 
	 * @throws JenaStoreException
	 */
	public void remove() throws JenaStoreException;

	/**
	 * This method is only required by VirtuosoStore related query operations
	 * through the index of Virtuoso.
	 * 
	 * @return
	 */
	public Graph getGraph();

}
