package qengine.program.parsers;

import org.apache.jena.tdb.index.Index;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;
import qengine.program.Dictionary;
import qengine.program.Indexation;

public final class IndexationRDFHandler extends AbstractRDFHandler {

    private Dictionary dico;
    private Indexation index;

    public IndexationRDFHandler(Dictionary dico) {
        this.dico = dico;
        this.index = new Indexation();
    }

    @Override
    public void handleStatement(Statement st) {
        // For debug purpose only
        //System.out.println("[i] Statement: " + st.getSubject() + "\t " + st.getPredicate() + "\t " + st.getObject());

        int subjectKey = dico.getWordReverseByKey(st.getSubject().stringValue());
        int predicateKey = dico.getWordReverseByKey(st.getPredicate().stringValue());
        int objectKey = dico.getWordReverseByKey(st.getObject().stringValue());

        index.addToAllIndex(subjectKey, predicateKey, objectKey);
    };

    public Indexation getIndex() {
        return index;
    }
}