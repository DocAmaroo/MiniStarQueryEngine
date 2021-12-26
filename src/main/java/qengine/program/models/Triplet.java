package qengine.program.models;

public class Triplet {
    private String subject;
    private String predicate;
    private String object;

    public Triplet(String subject, String predicate, String object) {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }

    public String getSubject() {
        return subject;
    }

    public String getPredicate() {
        return predicate;
    }

    public String getObject() {
        return object;
    }

    @Override
    public boolean equals(Object obj){
        if (!(obj instanceof Triplet)) {
            return false;
        }

        Triplet triplet = ((Triplet) obj);
        return object.equals(triplet.object) && subject.equals(triplet.subject) && predicate.equals(triplet.predicate);
    }

    @Override
    public String toString() {
        return "\t?" + getSubject() + " " + getPredicate() + " " + getObject();
    }
}
