package se.simonevertsson.gpu;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.List;

/**
 * Created by simon.evertsson on 2015-07-14.
 */
public class SpanningTree {

    private List<Relationship> relationships;

    private List<Node> visitOrder;

    public SpanningTree(List<Relationship> spanningTreeRelationships, List<Node> visitOrder) {
        this.relationships = spanningTreeRelationships;
        this.visitOrder = visitOrder;
    }

    public List<Relationship> getRelationships() {
        return relationships;
    }

    public List<Node> getVisitOrder() {
        return visitOrder;
    }
}