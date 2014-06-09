/**
 *
 * Process Editor
 *
 * (C) 2009 inubit AG
 * (C) 2014 the authors
 *
 */
package com.inubit.research.validation.bpmn.soundness;

import com.inubit.research.validation.bpmn.adaptors.EdgeAdaptor;
import com.inubit.research.validation.bpmn.adaptors.GatewayAdaptor;
import com.inubit.research.validation.bpmn.adaptors.ModelAdaptor;
import com.inubit.research.validation.bpmn.adaptors.NodeAdaptor;
import com.inubit.research.validation.bpmn.adaptors.ProcessObjectAdaptor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author tmi
 */
public class ProcessState {

    private Map<EdgeAdaptor, Integer> tokensOnEdges;
    private Map<GatewayAdaptor, Integer> tokensOnEventBasedGateways;
    private boolean isFinal;

    public ProcessState(ModelAdaptor model) {
        this(model, false);
    }

    public ProcessState(ModelAdaptor model, boolean isFinal) {
        tokensOnEdges = new HashMap<EdgeAdaptor, Integer>();
        this.isFinal = isFinal;
        for (EdgeAdaptor edge : model.getEdges()) {
            tokensOnEdges.put(edge, 0);
        }

        tokensOnEventBasedGateways = new HashMap<GatewayAdaptor, Integer>();
        for (NodeAdaptor node : model.getNodes()) {
            if (node.isEventBasedGateway()) {
                tokensOnEventBasedGateways.put((GatewayAdaptor) node, 0);
            }
        }
    }

    ProcessState(Map<EdgeAdaptor, Integer> tokensOnEdges,
            Map<GatewayAdaptor, Integer> tokensOnEventBasedGateways) {
        if (tokensOnEdges == null) {
            this.tokensOnEdges = new HashMap<EdgeAdaptor, Integer>();
        } else {
            this.tokensOnEdges = tokensOnEdges;
        }

        if (tokensOnEventBasedGateways == null) {
            tokensOnEventBasedGateways = new HashMap<GatewayAdaptor, Integer>();
        } else {
            this.tokensOnEventBasedGateways = tokensOnEventBasedGateways;
        }
    }

    public int getTokensOnEdge(EdgeAdaptor edge) {
        if (!tokensOnEdges.containsKey(edge)) {
            tokensOnEdges.put(edge, 0);
        }
        return tokensOnEdges.get(edge);
    }

    public int getTokensOnEventBasedGateway(GatewayAdaptor gateway) {
        if (!tokensOnEventBasedGateways.containsKey(gateway)) {
            tokensOnEventBasedGateways.put(gateway, 0);
        }
        return tokensOnEventBasedGateways.get(gateway);
    }

    public int getTokensOn(ProcessObjectAdaptor object) {
        if (object.isEdge()) {
            return getTokensOnEdge((EdgeAdaptor)object);
        } else if (((NodeAdaptor)object).isEventBasedGateway()) {
            return getTokensOnEventBasedGateway((GatewayAdaptor)object);
        } else {
            return 0;
        }
    }

    public boolean hasToken(ProcessObjectAdaptor object) {
        return getTokensOn(object) > 0;
    }

    public Set<EdgeAdaptor> getEdges() {
        return tokensOnEdges.keySet();
    }

    public Set<GatewayAdaptor> getEventBasedGateways() {
        return tokensOnEventBasedGateways.keySet();
    }

    public Set<ProcessObjectAdaptor> getObjects() {
        HashSet<ProcessObjectAdaptor> objects = new HashSet<ProcessObjectAdaptor>();
        objects.addAll(getEdges());
        objects.addAll(getEventBasedGateways());
        return objects;
    }

    public Set<ProcessObjectAdaptor> objectsWithTokens() {
        Set<ProcessObjectAdaptor> objects = getObjects();
        for (Iterator<ProcessObjectAdaptor> iter = objects.iterator(); iter.hasNext(); ){
            if (!hasToken(iter.next())) {
                iter.remove();
            }
        }
        return objects;
    }

    public int tokenSum() {
        int sum = 0;
        for (int tokens : tokensOnEdges.values()) {
            sum += tokens;
        }
        for (int tokens : tokensOnEventBasedGateways.values()) {
            sum += tokens;
        }
        return sum;
    }

    public void addTokenToEdge(EdgeAdaptor edge) {
        isFinal = false;
        if (!tokensOnEdges.containsKey(edge)) {
            tokensOnEdges.put(edge, 0);
        }
        tokensOnEdges.put(edge, tokensOnEdges.get(edge) + 1);
    }

    public void addTokenToEventBasedGateway(GatewayAdaptor gateway) {
        isFinal = false;
        if (!tokensOnEventBasedGateways.containsKey(gateway)) {
            tokensOnEventBasedGateways.put(gateway, 0);
        }
        tokensOnEventBasedGateways.
                put(gateway, tokensOnEventBasedGateways.get(gateway) + 1);
    }

    /**
     * if the target of this edge is an EventBasedGateway, the token will be
     * added to this EventBasedGateway, else it will be added to the edge.
     */
    public void sendTokenToEdge(EdgeAdaptor edge) {
        if (edge.getTarget().isEventBasedGateway()) {
            addTokenToEventBasedGateway((GatewayAdaptor)edge.getTarget());
        } else {
            addTokenToEdge(edge);
        }
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ProcessState)) {
            return false;
        }
        ProcessState otherState = (ProcessState) other;
        return tokensOnEdges.equals(otherState.tokensOnEdges) &&
                tokensOnEventBasedGateways.equals(otherState.tokensOnEventBasedGateways) &&
                isFinal == otherState.isFinal();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + (this.tokensOnEdges != null ? this.tokensOnEdges.hashCode() : 0);
        hash = 53 * hash + (this.tokensOnEventBasedGateways != null ? this.tokensOnEventBasedGateways.hashCode() : 0);
        hash = 53 * hash + (this.isFinal ? 1 : 0);
        return hash;
    }

    public void sendTokenToAll(List<EdgeAdaptor> edges) {
        for (EdgeAdaptor edge : edges) {
            sendTokenToEdge(edge);
        }
    }

    public void removeTokenFrom(EdgeAdaptor edge) {
        isFinal = false;
        if (!tokensOnEdges.containsKey(edge)) {
            tokensOnEdges.put(edge, 0);
        }
        if (tokensOnEdges.get(edge) == 0) {
            return;
        }
        tokensOnEdges.put(edge, tokensOnEdges.get(edge) - 1);
    }

    public void removeTokenFrom(GatewayAdaptor eventBasedGateway) {
        isFinal = false;
        if (!tokensOnEventBasedGateways.containsKey(eventBasedGateway)) {
            tokensOnEventBasedGateways.put(eventBasedGateway, 0);
        }
        if (tokensOnEventBasedGateways.get(eventBasedGateway) == 0) {
            return;
        }
        tokensOnEventBasedGateways.put(eventBasedGateway,
                tokensOnEventBasedGateways.get(eventBasedGateway) - 1);
    }

    public void removeTokenFrom(ProcessObjectAdaptor object) {
        if (object.isEdge()) {
            removeTokenFrom((EdgeAdaptor)object);
        } else if (((NodeAdaptor)object).isEventBasedGateway()) {
            removeTokenFrom((GatewayAdaptor)object);
        }
    }

    public void removeTokensFromAll(List<ProcessObjectAdaptor> objects) {
        for (ProcessObjectAdaptor object : objects) {
            removeTokenFrom(object);
        }
    }

    @Override
    public ProcessState clone() {
        return new ProcessState(new HashMap<EdgeAdaptor, Integer>(tokensOnEdges),
                new HashMap<GatewayAdaptor, Integer>(tokensOnEventBasedGateways));
    }

    public ProcessState cloneAndSendTokensToAll(List<EdgeAdaptor> edges) {
        ProcessState clone = clone();
        clone.sendTokenToAll(edges);
        return clone;
    }

    public ProcessState cloneAndRemoveTokenFrom(ProcessObjectAdaptor object) {
        ProcessState clone = clone();
        clone.removeTokenFrom(object);
        return clone;
    }

    public ProcessState cloneAndRemoveTokensFromAll(
            List<ProcessObjectAdaptor> objects) {
        ProcessState clone = clone();
        clone.removeTokensFromAll(objects);
        return clone;
    }

    public boolean hasTokenAccumulation() {
        for (int count : tokensOnEdges.values()) {
            if (count >= 5) {
                return true;
            }
        }
        for (int count : tokensOnEventBasedGateways.values()) {
            if (count >= 5) {
                return true;
            }
        }
        return false;
    }

    public boolean hasTokensOnSameObjectsAs(ProcessState otherState) {
        Set<ProcessObjectAdaptor> objects = new HashSet<ProcessObjectAdaptor>();
        objects.addAll(tokensOnEdges.keySet());
        objects.addAll(tokensOnEventBasedGateways.keySet());
        objects.addAll(otherState.getEdges());
        objects.addAll(otherState.getEventBasedGateways());
        for (ProcessObjectAdaptor object : objects) {
            if ((getTokensOn(object) == 0) ^
                    (otherState.getTokensOn(object) == 0)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("<state hash=\"");
        builder.append(hashCode());
        builder.append("\" final=\"");
        builder.append(isFinal?"true":"false");
        builder.append("\">\n");
        for (Map.Entry<EdgeAdaptor, Integer> entry : tokensOnEdges.entrySet()) {
            builder.append(entry.getKey().toString());
            builder.append(" --> ");
            builder.append(entry.getValue());
            builder.append('\n');
        }
        for (Map.Entry<GatewayAdaptor, Integer> entry :
                tokensOnEventBasedGateways.entrySet()) {
            builder.append(entry.getKey().toString());
            builder.append(" --> ");
            builder.append(entry.getValue());
            builder.append('\n');
        }
        builder.append("</state>\n");
        return builder.toString();
    }

    public String toShortString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        for (ProcessObjectAdaptor object : getObjects()) {
            if (hasToken(object)) {
                if (getTokensOn(object) > 1) {
                    builder.append(getTokensOn(object));
                    builder.append(" x ");
                }
                builder.append(object.toString());
                builder.append("; ");
            }
        }
        builder.append("}");
        return builder.toString();
    }

    public boolean isFinal() {
        return isFinal;
    }

    public void setFinal() {
        isFinal = true;
    }
}
