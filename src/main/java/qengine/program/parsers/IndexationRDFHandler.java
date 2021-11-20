package qengine.program.parsers;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;
import qengine.program.Dictionary;
import qengine.program.Indexation;

public final class IndexationRDFHandler extends AbstractRDFHandler {

    private final Dictionary dictionary;
    private final Indexation indexation;

    public IndexationRDFHandler() {
        dictionary = Dictionary.getInstance();
        indexation = Indexation.getInstance();
    }

    @Override
    public void handleStatement(Statement st) {
        // For debug purpose only
        //System.out.println("[i] Statement: " + st.getSubject() + "\t " + st.getPredicate() + "\t " + st.getObject());

        int subjectKey = dictionary.getWordByValue(st.getSubject().stringValue());
        int predicateKey = dictionary.getWordByValue(st.getPredicate().stringValue());
        int objectKey = dictionary.getWordByValue(st.getObject().stringValue());

        indexation.addToAllIndex(subjectKey, predicateKey, objectKey);
    };
}