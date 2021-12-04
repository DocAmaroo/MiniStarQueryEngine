package qengine.program.parsers;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;
import qengine.program.Dictionary;

public final class DictionaryRDFHandler extends AbstractRDFHandler {
	private Dictionary dico = new Dictionary();
	private int counter = 0;

	@Override
	public void handleStatement(Statement st) {
		// For debug purpose only
		// System.out.println("[i] Statement: " + st.getSubject() + "\t " + st.getPredicate() + "\t " + st.getObject());

		dico.addWord(st.getSubject().stringValue());
		dico.addWord(st.getPredicate().stringValue());
		dico.addWord(st.getObject().stringValue());
	};

	public Dictionary getDico() {
		return dico;
	}
}