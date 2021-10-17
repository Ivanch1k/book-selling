package ua.nure.hmyria;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import java.util.*;

public class BookSellerAgent extends Agent {
    private Hashtable<String, Integer> catalogue;
    private BookSellerGui myGui;
    private OfferRequestsServer offerBehaviour;
    private PurchaseOrdersServer purchaseBehaviour;

    protected void setup() {
        catalogue = new Hashtable<String, Integer>();

        myGui = new BookSellerGui(this);
        myGui.showGui();

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("book-selling");
        sd.setName("JADE-book-trading");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
        offerBehaviour = new OfferRequestsServer(catalogue);
        purchaseBehaviour = new PurchaseOrdersServer(catalogue);

        addBehaviour(offerBehaviour);
        addBehaviour(purchaseBehaviour);
    }

    protected void takeDown() {
        try {
            DFService.deregister(this);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
        myGui.dispose();
        System.out.println("Seller-agent "+getAID().getName()+" terminating.");
    }

    public void updateCatalogue(final String title, final int price) {
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                catalogue.put(title, Integer.valueOf(price));
                offerBehaviour.updateCatalogue(title, Integer.valueOf(price));
                purchaseBehaviour.updateCatalogue(title, Integer.valueOf(price));
                System.out.println(title+" inserted into catalogue. Price = "+price);
            }
        } );
    }
}
