package ua.nure.hmyria;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class RequestPerformer extends Behaviour {
    private AID bestSeller;
    private int bestPrice;
    private int repliesCnt = 0;
    private MessageTemplate mt;
    private int step = 0;
    private AID[] sellerAgents;
    private String targetBookTitle;

    public RequestPerformer(AID[] sellerAgents, String targetBookTitle){
        super();
        this.sellerAgents = sellerAgents;
        this.targetBookTitle = targetBookTitle;
    }

    public void action() {
        switch (step) {
            case 0:
                ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                for (AID sellerAgent : sellerAgents) {
                    cfp.addReceiver(sellerAgent);
                }
                cfp.setContent(targetBookTitle);
                cfp.setConversationId("book-trade");
                cfp.setReplyWith("cfp"+System.currentTimeMillis());
                myAgent.send(cfp);

                mt = MessageTemplate.and(MessageTemplate.MatchConversationId("book-trade"),
                        MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                step = 1;
                break;
            case 1:
                ACLMessage reply = myAgent.receive(mt);
                if (reply != null) {
                    if (reply.getPerformative() == ACLMessage.PROPOSE) {
                        int price = Integer.parseInt(reply.getContent());
                        if (bestSeller == null || price < bestPrice) {
                            bestPrice = price;
                            bestSeller = reply.getSender();
                        }
                    }
                    repliesCnt++;
                    if (repliesCnt >= sellerAgents.length) {
                        step = 2;
                    }
                }
                else {
                    block();
                }
                break;
            case 2:
                ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                order.addReceiver(bestSeller);
                order.setContent(targetBookTitle);
                order.setConversationId("book-trade");
                order.setReplyWith("order"+System.currentTimeMillis());
                myAgent.send(order);
                mt = MessageTemplate.and(MessageTemplate.MatchConversationId("book-trade"),
                        MessageTemplate.MatchInReplyTo(order.getReplyWith()));
                step = 3;
                break;
            case 3:
                reply = myAgent.receive(mt);
                if (reply != null) {
                    if (reply.getPerformative() == ACLMessage.INFORM) {
                        System.out.println(targetBookTitle+" successfully purchased from agent "+reply.getSender().getName());
                        System.out.println("Price = "+bestPrice);
                        myAgent.doDelete();
                    }
                    else {
                        System.out.println("Attempt failed: requested book already sold.");
                    }
                    step = 4;
                }
                else {
                    block();
                }
                break;
        }
    }

    public boolean done() {
        if (step == 2 && bestSeller == null) {
            System.out.println("Attempt failed: "+targetBookTitle+" not available for sale");
        }
        return ((step == 2 && bestSeller == null) || step == 4);
    }
}