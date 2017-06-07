import java.util.HashMap;
import java.util.List;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Offer;
import negotiator.boaframework.SortedOutcomeSpace;
import negotiator.issue.Value;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.parties.NegotiationInfo;
import negotiator.protocol.MultilateralProtocol;
import negotiator.protocol.StackedAlternatingOffersProtocol;

/*
 * @author Paul Meuser
 * Multilateral negotiating agent for 2017 ANAC competition
 * Version 1.0
 */

public class meuserAgent2 extends AbstractNegotiationParty{
	private SortedOutcomeSpace bidPool;
	private Action mostRecentAction = null;
	private Bid mostRecentBid = null;
	private Bid nextBid = null;
	private double maxUtil;
	private int numIssues;
	
	
	
	public meuserAgent2() {
		super();
	}
	
	@Override
	public  void  init(NegotiationInfo info) {
		
		super.init(info);
		
		//determine available bidding space, set next bid with max utility and record max utility
		bidPool = new SortedOutcomeSpace(utilitySpace);
		nextBid = bidPool.getMaxBidPossible().getBid();
		maxUtil = getUtility(nextBid);
		//record number of issues in bidding domain
		numIssues = nextBid.getIssues().size();
		
		mostRecentAction = null;
		
	}
	
	@Override
	public Action chooseAction(List <Class <? extends Action >> possibleActions ) {
		Action output;
		//for first bid, present bid with maximum possible utility.
		if (mostRecentAction == null) {
			output = new Offer(getPartyId(),nextBid);
		}
		else if (mostRecentAction instanceof Offer) {
			updateNextBid();
			//accept bids which exceed expected utility of next bid.
			if (getUtility(mostRecentBid) >= getUtility(nextBid)) {
				output = new Accept(getPartyId(),mostRecentBid);
			}
			//otherwise offer next bid
			else {
				output = new Offer(getPartyId(),nextBid);
			}
		}
		else if (mostRecentAction instanceof Accept) {
			updateNextBid();
			//accept bids which exceed half expected utility of next bid.
			if (getUtility(mostRecentBid) >= getUtility(nextBid)*2/3) {
				output = new Accept(getPartyId(),mostRecentBid);
			}
			//otherwise offer next bid
			else {
				output = new Offer(getPartyId(),nextBid);
			}
		}
		else {
			updateNextBid();
			output =  new Offer(getPartyId(),nextBid);
		}
		
		return output;
	}
	
	//set next bid by changing one value to match most recent bid from opponent such that
			//personal utility is maximized.
	private void updateNextBid() {
		double concessionUtility = 0.0;
		int changedValue = -1;
		for (int i=1; i<=numIssues; i++) {
			Bid testBid = new Bid(nextBid);
			Value nextValue = mostRecentBid.getValue(i);
			double valueUtility = getUtility(testBid.putValue(i, nextValue));
			if (valueUtility>concessionUtility) {
				concessionUtility = valueUtility;
				changedValue = i;
			}
		}
		
		nextBid.putValue(changedValue, mostRecentBid.getValue(changedValue));
	}
	
	
	@Override
	public void receiveMessage(AgentID sender , Action action ) {
		super.receiveMessage(sender, action);
		//collect most recent action, and bid
		mostRecentAction = action;
		if (mostRecentAction instanceof Offer) {
			mostRecentBid = ((Offer) mostRecentAction).getBid();
		}
		if (mostRecentAction instanceof Accept) {
			mostRecentBid = ((Accept) mostRecentAction).getBid();			
		}
	}
	
	public String getDescription () {
		return "Paul Meuser ANAC Agent";
	}
	
	public Class <? extends MultilateralProtocol > getProtocol () {
		return (new StackedAlternatingOffersProtocol()).getClass();
	}
	
	public HashMap <String , String > negotiationEnded(Bid acceptedBid ) {
		return null;
	}

	
	
}
