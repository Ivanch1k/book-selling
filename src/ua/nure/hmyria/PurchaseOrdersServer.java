package ua.nure.hmyria;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Hashtable;

public class PurchaseOrdersServer extends CyclicBehaviour {
    private Hashtable<String, Integer> catalogue;

    public PurchaseOrdersServer(Hashtable<String, Integer> catalogue){
        this.catalogue = catalogue;
    }

    public void updateCatalogue(String title, Integer price){
        catalogue.put(title, price);
    }

    public void action() {
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
        ACLMessage msg = myAgent.receive(mt);
        if (msg != null) {
            String title = msg.getContent();
            ACLMessage reply = msg.createReply();

            Integer price = (Integer) catalogue.remove(title);
            if (price != null) {
                reply.setPerformative(ACLMessage.INFORM);
                System.out.println(title+" sold to agent "+msg.getSender().getName());
            }
            else {
                reply.setPerformative(ACLMessage.FAILURE);
                reply.setContent("not-available");
            }
            myAgent.send(reply);
        }
        else {
            block();
        }
    }
}
