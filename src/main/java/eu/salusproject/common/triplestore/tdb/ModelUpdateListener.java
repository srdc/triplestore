package eu.salusproject.common.triplestore.tdb;

import java.util.List;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelChangedListener;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.tdb.TDB;

public class ModelUpdateListener implements ModelChangedListener {

	private Dataset dataset;

	public ModelUpdateListener(Dataset dataset) {
		this.dataset = dataset;
	}

	@Override
	public void addedStatement(Statement s) {
		changeHandler();
	}

	@Override
	public void addedStatements(Statement[] statements) {
		changeHandler();
	}

	@Override
	public void addedStatements(List<Statement> statements) {
		changeHandler();
	}

	@Override
	public void addedStatements(StmtIterator statements) {
		changeHandler();
	}

	@Override
	public void addedStatements(Model m) {
		changeHandler();
	}

	@Override
	public void removedStatement(Statement s) {
		changeHandler();
	}

	@Override
	public void removedStatements(Statement[] statements) {
		changeHandler();
	}

	@Override
	public void removedStatements(List<Statement> statements) {
		changeHandler();
	}

	@Override
	public void removedStatements(StmtIterator statements) {
		changeHandler();
	}

	@Override
	public void removedStatements(Model m) {
		changeHandler();
	}

	@Override
	public void notifyEvent(Model m, Object event) {
		changeHandler();
	}

	private void changeHandler() {
		TDB.sync(dataset);
	}

}
